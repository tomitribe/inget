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
import org.tomitribe.common.Configuration;
import org.tomitribe.model.ModelGenerator;
import org.tomitribe.resource.ResourcesGenerator;

@Mojo(name = "generate")
public class MainGenerator extends AbstractMojo {

    @Parameter(property = "generate.generated_sources")
    private String generatedSources;

    @Parameter(property = "generate.sources")
    private String sources;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        getLog().info("SOURCES: " + sources);
        getLog().info("GENERATED_SOURCES: " + generatedSources);

        Configuration.SOURCES = sources;
        Configuration.GENERATED_SOURCES = generatedSources;

        try {
            getLog().info("Started model code generation.");
            ModelGenerator.execute();
            getLog().info("Finished model code generation at.");
            getLog().info("Started resource code generation.");
            ResourcesGenerator.execute();
            getLog().info("Finished resource code generation.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
