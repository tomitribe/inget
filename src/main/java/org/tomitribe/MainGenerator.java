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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.tomitribe.common.Configuration;
import org.tomitribe.model.ModelGenerator;
import org.tomitribe.resource.ResourcesGenerator;

import java.io.File;

@Mojo(name = "generate")
public class MainGenerator extends AbstractMojo {

    @Parameter(property = "generate.model_package")
    private String modelPackage;

    @Parameter(property = "generate.resource_package")
    private String resourcePackage;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String generatedSources = project.getBuild().getOutputDirectory() + File.separator + "generated-sources";
        project.addCompileSourceRoot(generatedSources);
        Configuration.SOURCES = project.getBuild().getSourceDirectory();
        Configuration.GENERATED_SOURCES = generatedSources;
        Configuration.MODEL_PACKAGE = modelPackage;
        Configuration.RESOURCE_PACKAGE = resourcePackage;

        try {
            if (modelPackage != null) {
                getLog().info("Started Model Code Generation.");
                ModelGenerator.execute();
                getLog().info("Finished Model Code Generation.");
            } else {
                getLog().info("Skiping Model Code Generation");
            }

            if (resourcePackage != null) {
                getLog().info("Started Resource Code Generation.");
                ResourcesGenerator.execute();
                getLog().info("Finished Resource Code Generation.");
            } else {
                getLog().info("Skiping Resources Code Generation");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
