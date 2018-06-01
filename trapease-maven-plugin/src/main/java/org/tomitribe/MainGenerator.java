/*
 * Tomitribe Confidential
 *
 * Copyright Tomitribe Corporation. 2018
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package org.tomitribe;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.tomitribe.client.ClientGenerator;
import org.tomitribe.common.Configuration;
import org.tomitribe.common.Utils;
import org.tomitribe.model.ModelGenerator;
import org.tomitribe.resource.ResourcesGenerator;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

@Mojo(name = "generate")
public class MainGenerator extends AbstractMojo {

    @Parameter(property = "generate.model_package")
    private String modelPackage;

    @Parameter(property = "generate.resource_package")
    private String resourcePackage;

    @Parameter(property = "generate.generate_client", defaultValue = "false")
    private Boolean generateClient;

    @Parameter(property = "generate.resource_suffix", defaultValue = "Resource")
    private String resourceSuffix;

    @Parameter(property = "generate.model_suffix", defaultValue = "Model")
    private String modelSuffix;


    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

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
        Configuration.RESOURCE_SUFFIX = resourceSuffix;
        Configuration.MODEL_SUFFIX = modelSuffix;
        Configuration.TEMP_SOURCE = project.getBuild().getDirectory() + File.separator + "temp-source";

        try {
            if (modelPackage != null) {
                boolean existsInCurrentProject = new File(Configuration.getModelPath()).exists();
                if (existsInCurrentProject) {
                    getLog().info("Started Model Code Generation.");
                    ModelGenerator.execute();
                    getLog().info("Finished Model Code Generation.");
                } else {
                    List<Artifact> modelDependencies = artifacts.stream()
                            .filter(a -> hasModel(a.getFile())).collect(Collectors.toList());

                    if(modelDependencies.size() == 0){
                        throw new MojoExecutionException(
                                "Model was not found. Add the correct 'modelPackage' for " +
                                "this project or add a jar with the .java files for the model.");
                    }

                    modelDependencies.stream().forEach(m -> extractJavaFiles(m.getFile()));
                    Configuration.MODEL_SOURCES = Configuration.TEMP_SOURCE;
                }
            }

            if (resourcePackage != null) {
                boolean resourcesExistsInCurrentProject = new File(Configuration.getResourcePath()).exists();
                if (resourcesExistsInCurrentProject) {
                    getLog().info("Started Resource Code Generation.");
                    ResourcesGenerator.execute();
                    getLog().info("Finished Resource Code Generation.");
                } else {
                    if(generateClient){
                        List<Artifact> resourceDependencies = artifacts.stream()
                                .filter(a -> hasResources(a.getFile())).collect(Collectors.toList());

                        if(resourceDependencies.size() == 0){
                            throw new MojoExecutionException(
                                    "Resources were not found. Add the correct 'resourcePackage' for " +
                                            "this project or add a jar with the .java files for the resources.");
                        }

                        resourceDependencies.stream().forEach(m -> extractJavaFiles(m.getFile()));
                        Configuration.RESOURCE_SOURCES = Configuration.TEMP_SOURCE;

                        getLog().info("Started Client Code Generation.");
                        ClientGenerator.execute();
                        getLog().info("Finished Client Code Generation.");
                    }
                }

            } else {
                getLog().info("Skipping Resources Code Generation, " +
                        "resource package not found. Add a valid 'resourcePackage'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void extractJavaFiles(File jarFile) {
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
}
