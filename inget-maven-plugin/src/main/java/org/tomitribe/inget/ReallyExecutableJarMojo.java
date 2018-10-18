package org.tomitribe.inget;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Make an artifact generated by the build really executable. The resulting artifact
 * can be run directly from the command line (Java must be installed and in the
 * shell path).
 *
 */
@Mojo(name = "executable",
        requiresProject = true,
        threadSafe = true,
        defaultPhase = LifecyclePhase.PACKAGE)
public class ReallyExecutableJarMojo extends AbstractMojo {
    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Java command line arguments to embed. Only used with the default stanza.
     */
    @Parameter(defaultValue = "", property = "really-executable-jar.flags")
    private String flags = "";

    /**
     * Name of the generated binary.
     */
    @Parameter(property = "really-executable-jar.cmdFileName")
    private String cmdFileName = null;

    /**
     * Specifies the classifier of the artifact that will be made executable.
     */
    @Parameter(property = "really-executable-jar.classifier")
    private String classifier;

    /**
     * Allow other packaging types than "jar".
     */
    @Parameter(property = "really-executable-jar.allowOtherTypes")
    private String allowOtherTypes;

    /**
     * Attach the binary as an artifact to the deploy.
     */
    @Parameter(defaultValue = "false", property = "really-executable-jar.attachProgramFile")
    private boolean attachProgramFile = false;

    /**
     * File ending of the program artifact.
     */
    @Parameter(defaultValue = "sh", property = "really-executable-jar.programFileType")
    private String programFileType = "sh";

    /**
     * Shell script to add to the jar instead of the default stanza.
     */
    @Parameter(property = "really-executable-jar.scriptFile")
    private String scriptFile = null;

    @Parameter(property = "really-executable-jar.shadedJar")
    private String shadedJar = null;

    @Override
    public void execute() throws MojoExecutionException {
        try {

            List<File> files = new ArrayList<File>();
            if (!StringUtils.isEmpty(shadedJar)) {
                File f = new File(project.getBasedir() + "/target", shadedJar);
                if (f.exists()) {
                    files.add(f);
                }
            } else {
                if (shouldProcess(project.getArtifact())) {
                    files.add(project.getArtifact().getFile());
                }

                for (Artifact item : project.getAttachedArtifacts()) {
                    if (shouldProcess(item)) {
                        files.add(item.getFile());
                    }
                }
            }


            if (files.isEmpty()) {
                throw new MojoExecutionException("Could not find any jars to make executable");
            }

            if (cmdFileName != null && !cmdFileName.matches("\\s+")) {
                for (File file : files) {
                    File dir = file.getParentFile();
                    File exec = new File(dir, cmdFileName);
                    FileUtils.copyFile(file, exec);
                    makeExecutable(exec);
                    if (attachProgramFile) {
                        projectHelper.attachArtifact(project, programFileType, exec);
                    }
                }
            } else {
                for (File file : files) {
                    makeExecutable(file);
                }
            }

        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    private boolean shouldProcess(Artifact artifact) {
        getLog().debug("Considering " + artifact);
        if (artifact == null) {
            return false;
        }

        if (!Boolean.valueOf(allowOtherTypes) && !artifact.getType().equals("jar")) {
            return false;
        }

        return classifier == null || classifier.equals(artifact.getClassifier());
    }

    private void makeExecutable(File file)
            throws IOException, MojoExecutionException {
        getLog().debug("Making " + file.getAbsolutePath() + " executable");

        Path original = Paths.get(file.getAbsolutePath() + ".rx-orig");
        Files.move(file.toPath(), original);
        try (final FileOutputStream out = new FileOutputStream(file);
             final InputStream in = Files.newInputStream(original)) {

            if (scriptFile == null) {
                out.write(("#!/bin/sh\n\nexec java " + flags + " -jar \"$0\" \"$@\"\n\n").getBytes("ASCII"));
            } else if (Files.exists(Paths.get(scriptFile))) {
                getLog().debug(String.format("Loading file[%s] from filesystem", scriptFile));

                byte[] script = Files.readAllBytes(Paths.get(scriptFile));
                out.write(script);
                out.write(new byte[]{'\n', '\n'});
            } else {
                getLog().debug(String.format("Loading file[%s] from jar[%s]", scriptFile, original));

                try (final URLClassLoader loader = new URLClassLoader(new URL[]{original.toUri().toURL()}, null);
                     final InputStream scriptIn = loader.getResourceAsStream(scriptFile)) {

                    out.write(IOUtil.toString(scriptIn).getBytes("ASCII"));
                    out.write("\n\n".getBytes("ASCII"));
                }
            }
            IOUtil.copy(in, out);
        } finally {
            Files.deleteIfExists(original);
        }

        file.setExecutable(true, false);

        getLog().info(String.format("Successfully made JAR [%s] executable", file.getAbsolutePath()));
    }
}