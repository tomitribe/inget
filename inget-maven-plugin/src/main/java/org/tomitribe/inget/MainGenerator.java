/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2018
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package org.tomitribe.inget;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.tomitribe.inget.client.ClientGenerator;
import org.tomitribe.inget.cmd.CmdGenerator;
import org.tomitribe.inget.common.Authentication;
import org.tomitribe.inget.common.Configuration;
import org.tomitribe.inget.common.CustomTypeSolver;
import org.tomitribe.inget.common.Utils;
import org.tomitribe.inget.model.ModelGenerator;
import org.tomitribe.inget.resource.ResourcesGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * @goal generate-sources
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MainGenerator extends AbstractMojo {

    @Parameter(property = "generate.model_package")
    private String modelPackage;

    @Parameter(property = "generate.resource_package")
    private String resourcePackage;

    @Parameter(property = "generate.generate_model", defaultValue = "false")
    private Boolean generateModel;

    @Parameter(property = "generate.generate_resources", defaultValue = "false")
    private Boolean generateResources;

    @Parameter(property = "generate.generate_client", defaultValue = "false")
    private Boolean generateClient;

    @Parameter(property = "generate.generate_cmd", defaultValue = "false")
    private Boolean generateCmd;

    @Parameter(property = "generate.client_name", defaultValue = "ResourceClient")
    private String clientName;

    @Parameter(property = "generate.resource_suffix")
    private String resourceSuffix;

    @Parameter(property = "generate.model_suffix", defaultValue = "Model")
    private String modelSuffix;

    @Parameter(property = "generate.cmdline_name")
    private String cmdLineName;

    @Parameter(property = "generate.authentication")
    private String authentication;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        project.setArtifactFilter(new AndArtifactFilter());
        Set<Artifact> artifacts = project.getArtifacts();
        final String generatedSources = project.getBuild().getDirectory() + File.separator + "generated-sources";
        project.addCompileSourceRoot(generatedSources);
        Configuration.MODEL_SOURCES = project.getBuild().getSourceDirectory();
        Configuration.RESOURCE_SOURCES = project.getBuild().getSourceDirectory();
        Configuration.GENERATED_SOURCES = generatedSources;
        Configuration.MODEL_PACKAGE = modelPackage;
        Configuration.RESOURCE_PACKAGE = resourcePackage;
        Configuration.CLIENT_NAME = clientName;
        Configuration.RESOURCE_SUFFIX = resourceSuffix;
        Configuration.MODEL_SUFFIX = modelSuffix;
        Configuration.TEMP_SOURCE = project.getBuild().getDirectory() + File.separator + "temp-source";

        if (cmdLineName != null) {
            Configuration.CMD_LINE_NAME = cmdLineName;
        } else {
            Configuration.CMD_LINE_NAME = project.getArtifactId();
        }

        if (authentication != null) {
            if (authentication.equalsIgnoreCase(Authentication.BASIC.name())) {
                Configuration.AUTHENTICATION = Authentication.BASIC;
            }

            if (authentication.equalsIgnoreCase(Authentication.SIGNATURE.name())) {
                Configuration.AUTHENTICATION = Authentication.SIGNATURE;
            }
        }

        try {
            generateModel(artifacts);
            generateResources(artifacts);

            FileUtils.mkdir(generatedSources);
            // Only after resolving the model and resource paths
            CustomTypeSolver.init();

            if (generateClient) {
                requireResourcePackage();
                getLog().info("Started Client Code Generation.");
                ClientGenerator.execute();
                getLog().info("Finished Client Code Generation.");
                Configuration.CLIENT_SOURCES = Configuration.GENERATED_SOURCES;
            }

            if (generateCmd) {
                boolean clientExistsInCurrentProject = new File(Configuration.getClientPath()).exists();
                if (clientExistsInCurrentProject) {
                    Configuration.CLIENT_SOURCES = Configuration.getClientPath();
                } else {
                    List<Artifact> clientDependencies = artifacts.stream()
                            .filter(a -> hasClient(a.getFile()))
                            .collect(Collectors.toList());

                    if (clientDependencies.size() == 0) {
                        throw new MojoExecutionException(
                                "Clients were not found. Generate the client adding the 'resourcePackage' and 'generateClient' as true.");
                    }

                    clientDependencies.forEach(m -> extractJavaFiles(m.getFile()));
                    Configuration.RESOURCE_SOURCES = Configuration.TEMP_SOURCE;
                    Configuration.CLIENT_SOURCES = Configuration.TEMP_SOURCE;
                }
                Configuration.CMD_PACKAGE = Configuration.RESOURCE_PACKAGE + ".cmd";
                getLog().info("Started Command Code Generation.");
                CmdGenerator.execute();
                getLog().info("Finished Command Code Generation.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateResources(Set<Artifact> artifacts) throws IOException, MojoExecutionException {
        if (generateResources) {
            requireModelPackage();

            if (resourcePackage == null) {
                Configuration.RESOURCE_PACKAGE = "org.tomitribe.resources";
            }

            File resourceFolder = new File(Configuration.getResourcePath());
            boolean resourcesExistsInCurrentProject = resourceFolder.exists();
            if (!resourcesExistsInCurrentProject) {
                resourceFolder.mkdirs();
            }

            getLog().info("Started Resource Code Generation.");
            ResourcesGenerator.execute();
            getLog().info("Finished Resource Code Generation.");
            Configuration.RESOURCE_SOURCES = Configuration.GENERATED_SOURCES;
        } else {
            if (resourcePackage != null) {
                List<String> compileSourceRoots = project.getCompileSourceRoots();
                if (compileSourceRoots != null) {
                    for (String source : compileSourceRoots) {
                        File folder = new File(source, Utils.transformPackageToPath(Configuration.RESOURCE_PACKAGE));
                        if (folder.exists()) {
                            Configuration.RESOURCE_SOURCES = source;
                            return;
                        }
                    }
                }

                List<Artifact> resourceDependencies = artifacts.stream()
                        .filter(a -> hasResources(a.getFile())).collect(Collectors.toList());

                if (resourceDependencies.size() == 0) {
                    throw new MojoExecutionException(
                            "Resources were not found. Add the correct 'resourcePackage' for " +
                                    "this project or add a jar with the .java files for the resources.");
                } else {
                    resourceDependencies.stream().forEach(m -> extractJavaFiles(m.getFile()));
                    Configuration.RESOURCE_SOURCES = Configuration.TEMP_SOURCE;
                }
            }
        }
        Configuration.CLIENT_SOURCES = Configuration.RESOURCE_SOURCES;
    }

    private void generateModel(Set<Artifact> artifacts) throws IOException, MojoExecutionException {
        if (generateModel) {
            requireModelPackage();

            File modelFolder = new File(Configuration.getModelPath());
            boolean existsInCurrentProject = modelFolder.exists();
            if (!existsInCurrentProject) {
                modelFolder.mkdirs();
            }
            getLog().info("Started Model Code Generation.");
            ModelGenerator.execute();
            getLog().info("Finished Model Code Generation.");
        } else {
            if (modelPackage != null) {
                List<String> compileSourceRoots = project.getCompileSourceRoots();
                if (compileSourceRoots != null) {
                    for (String source : compileSourceRoots) {
                        File folder = new File(source, Utils.transformPackageToPath(Configuration.MODEL_PACKAGE));
                        if (folder.exists()) {
                            Configuration.MODEL_SOURCES = source;
                            return;
                        }
                    }
                }

                List<Artifact> modelDependencies = artifacts.stream()
                        .filter(a -> hasModel(a.getFile())).collect(Collectors.toList());

                if (modelDependencies.size() == 0) {
                    throw new MojoExecutionException(
                            "Model was not found. Add the correct 'modelPackage' for " +
                                    "this project or add a jar with the .java files for the model.");
                }

                modelDependencies.stream().forEach(m -> extractJavaFiles(m.getFile()));
                Configuration.MODEL_SOURCES = Configuration.TEMP_SOURCE;
            }
        }
    }

    private boolean hasModel(File jarFile) {
        try {
            JarFile jar = new JarFile(jarFile);
            Enumeration<? extends JarEntry> enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                if (zipEntry.getName().equals(Utils.transformPackageToPath(Configuration.MODEL_PACKAGE) + File.separator)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean hasResources(File jarFile) {
        try {
            JarFile jar = new JarFile(jarFile);
            Enumeration<? extends JarEntry> enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                if (zipEntry.getName().equals(Utils.transformPackageToPath(Configuration.RESOURCE_PACKAGE) + File.separator)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean hasClient(File jarFile) {
        try {
            JarFile jar = new JarFile(jarFile);
            Enumeration<? extends JarEntry> enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                String pkg = Utils.transformPackageToPath(Configuration.RESOURCE_PACKAGE) + File.separator + "client";
                if (zipEntry.getName().equals(pkg) || zipEntry.getName().equals(pkg + File.separator)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void extractJavaFiles(File jarFile) {
        try {
            JarFile jar = new JarFile(jarFile);
            Enumeration<? extends JarEntry> enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                ZipEntry zipEntry = enumeration.nextElement();
                if (zipEntry.getName().endsWith(".java")) {
                    InputStream is = jar.getInputStream(zipEntry);
                    File generatedSources = new File(Configuration.TEMP_SOURCE);
                    java.io.File output = new java.io.File(generatedSources, java.io.File.separator + zipEntry.getName());
                    if (!output.getParentFile().exists()) {
                        output.getParentFile().mkdirs();
                    }
                    output.createNewFile();
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(output);
                    while (is.available() > 0) {  // write contents of 'is' to 'fos'
                        fos.write(is.read());
                    }
                    fos.close();
                    is.close();
                }
            }
            jar.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requireResourcePackage() throws MojoExecutionException {
        if (resourcePackage == null) {
            throw new MojoExecutionException(
                    "The 'resourcePackage' configuration was not found.");
        }
    }

    private void requireModelPackage() throws MojoExecutionException {
        if (modelPackage == null) {
            throw new MojoExecutionException(
                    "The 'modelPackage' configuration was not found.");
        }
    }
}
