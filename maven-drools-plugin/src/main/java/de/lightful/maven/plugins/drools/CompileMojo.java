/*
 * Copyright (c) 2009-2011 Ansgar Konermann <konermann@itikko.net>
 *
 * This file is part of the Maven 2 Drools Plugin.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lightful.maven.plugins.drools;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.fest.util.Arrays;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@MojoGoal(CompileMojo.GOAL)
public class CompileMojo extends AbstractMojo {

  public static final String GOAL = "compile";

  @MojoParameter(
      description = "Define what the compiler should compile in each of its passes. " +
                    "You need at least one pass to create useful output.",
      readonly = false,
      required = true
  )
  private Pass[] passes;

  @MojoParameter(defaultValue = "${project}")
  private MavenProject project;
  private static final int FIRST_PASS_NUMBER = 1;
  private Build build;
  private static final String DROOLS_PACKAGING_IDENTIFIER = "drools";
  private static final String DROOLS_KNOWLEDGE_PACKAGE_EXTENSION = ".dkp"; // stands for Drools Knowledge Package(s)

  public void execute() throws MojoExecutionException, MojoFailureException {
    final Log log = getLog();
    log.info("This is the compiler plugin");
    log.info("Passes: " + Arrays.format(passes));
    log.info("Project: " + project.getName());

    fixupPassesInformation();
    dumpPassesConfiguration();

    runAllPasses();

//    project.getBuild().
  }

  private void fixupPassesInformation() {
    int passNumber = FIRST_PASS_NUMBER;
    for (Pass pass : passes) {
      if (pass.getName() == null || "".equals(pass.getName())) {
        pass.setName("Pass #" + passNumber);
      }
      if (pass.getIncludes() == null || pass.getIncludes().length == 0) {
        pass.setIncludes(new String[] {"*.drl"});
      }

      passNumber++;
    }
  }

  private void dumpPassesConfiguration() {
    final Log log = getLog();
    int passNumber = FIRST_PASS_NUMBER;
    for (Pass pass : passes) {
      log.info("Pass #" + passNumber + ":");
      log.info("    Name:             '" + pass.getName() + "'");
      log.info("    Rule Source Root: " + pass.getRuleSourceRoot());
      log.info("    Includes:         " + pass.getIncludes());
      log.info("    Excludes:         " + pass.getExcludes());
      passNumber++;
    }
  }

  private void runAllPasses() {
    for (Pass pass : passes) {
      executePass(pass);
    }
    writeOutputFile();
  }

  private void writeOutputFile() {
    final String packaging = project.getPackaging();
    final Log log = getLog();
    if (!DROOLS_PACKAGING_IDENTIFIER.equals(packaging)) {
      log.error("Internal error: packaging of project must be equal to '" + DROOLS_PACKAGING_IDENTIFIER + "' when using this plugin!");
    }
    build = project.getBuild();
    final String currentFinalName = build.getFinalName();
    if (!currentFinalName.endsWith(DROOLS_KNOWLEDGE_PACKAGE_EXTENSION)) {
      build.setFinalName(currentFinalName + DROOLS_KNOWLEDGE_PACKAGE_EXTENSION);
    }
    String outputFileName = build.getFinalName();
    writeFinalOutputFile(outputFileName);
  }

  private void writeFinalOutputFile(String outputFileName) {
    final String buildDirectoryName = project.getBuild().getDirectory();
    File buildDirectory = new File(buildDirectoryName);
    File outputFile = new File(buildDirectoryName + File.separator + outputFileName);

    final Log log = getLog();
    final String absoluteOutputFileName = outputFile.getAbsolutePath();
    log.info("Writing XXXX bytes into output file " + absoluteOutputFileName);

    if (!buildDirectory.exists()) {
      log.debug("Output directory " + buildDirectory.getAbsolutePath() + " does not exist, creating.");
      buildDirectory.mkdirs();
    }

    if (outputFile.exists()) {
      log.warn("Output file " + absoluteOutputFileName + " exists, overwriting.");
      if (!outputFile.delete()) {
        log.error("Unable to delete " + absoluteOutputFileName + "!");
      }
      else {
        try {
          outputFile.createNewFile();
        }
        catch (IOException e) {
          log.error("Unable to create output file " + absoluteOutputFileName + "!", e);
        }
      }
    }

    try {
      PrintWriter printWriter = new PrintWriter(outputFile);
      printWriter.append("THIS IS THE NEW KNOWLEDGE :-)");
      printWriter.close();
    }
    catch (IOException e) {
      log.error("Unable to write compiled knowledge into output file!", e);
    }
  }

  private void executePass(Pass pass) {
    final Log log = getLog();
    log.info("Executing compiler pass '" + pass.getName() + "'...");
    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setBasedir(pass.getRuleSourceRoot());
    scanner.setIncludes(pass.getIncludes());
    scanner.setExcludes(pass.getExcludes());
    scanner.setCaseSensitive(true);
    scanner.scan();

    final String[] filesToCompile = scanner.getIncludedFiles();
    for (String fileToCompile : filesToCompile) {
      log.info("  Compiling rule file '" + fileToCompile + "' ...");
    }
    log.info("Done with compiler pass '" + pass.getName() + "'.");
  }
}
