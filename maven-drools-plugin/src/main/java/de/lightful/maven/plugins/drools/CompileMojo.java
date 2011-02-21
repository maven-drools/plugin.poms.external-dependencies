/*
 * Copyright (c) 2009-2011 Ansgar Konermann
 *
 * This file is part of the Maven 3 Drools Plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lightful.maven.plugins.drools;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.drools.builder.*;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.fest.util.Arrays;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

@MojoGoal(CompileMojo.GOAL)
@MojoRequiresDependencyResolution("runtime")
public class CompileMojo extends AbstractMojo {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

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

  private KnowledgeBuilder knowledgeBuilder;

  public void execute() throws MojoFailureException {
    final Log log = getLog();
    log.info("This is the compiler plugin");
    log.info("Passes: " + Arrays.format(passes));
    log.info("Project: " + project.getName());

    fixupPassesInformation();
    dumpPassesConfiguration();

    runAllPasses();
    writeOutputFile();
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

  private void runAllPasses() throws MojoFailureException {
    for (Pass pass : passes) {
      executePass(pass);
    }
  }

  private void writeOutputFile() {
    final String packaging = project.getPackaging();
    final Log log = getLog();
    if (!WellKnownNames.DROOLS_PACKAGING_IDENTIFIER.equals(packaging)) {
      log.error("Internal error: packaging of project must be equal to '" + WellKnownNames.DROOLS_PACKAGING_IDENTIFIER + "' when using this plugin!");
    }
    build = project.getBuild();
    final String currentFinalName = build.getFinalName();
    if (!currentFinalName.endsWith(WellKnownNames.DROOLS_KNOWLEDGE_PACKAGE_EXTENSION)) {
      build.setFinalName(currentFinalName + WellKnownNames.DROOLS_KNOWLEDGE_PACKAGE_EXTENSION);
    }
    String outputFileName = build.getFinalName();
    writeFinalOutputFile(outputFileName);
  }

  private void writeFinalOutputFile(String outputFileName) {
    final String buildDirectoryName = project.getBuild().getDirectory();
    File buildDirectory = new File(buildDirectoryName);
    File outputFile = new File(buildDirectoryName + File.separator + outputFileName);

    final Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();

    final Log log = getLog();
    final String absoluteOutputFileName = outputFile.getAbsolutePath();
    log.info("Writing " + knowledgePackages.size() + " knowledge packages into output file " + absoluteOutputFileName);

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
      DroolsStreamUtils.streamOut(new FileOutputStream(outputFile), knowledgePackages, false);
    }
    catch (IOException e) {
      log.error("Unable to write compiled knowledge into output file!", e);
    }
  }

  private void executePass(Pass pass) throws MojoFailureException {
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
      compileRuleFile(pass.getRuleSourceRoot(), fileToCompile);
    }
    log.info("Done with compiler pass '" + pass.getName() + "'.");
  }

  private void compileRuleFile(File ruleSourceRoot, String nameOfFileToCompile) throws MojoFailureException {
    final Log log = getLog();
    File fileToCompile = new File(ruleSourceRoot, nameOfFileToCompile);
    log.info("  Compiling rule file '" + fileToCompile.getAbsolutePath() + "' ...");
    KnowledgeBuilder knowledgeBuilder = getKnowledgeBuilder();
    knowledgeBuilder.add(ResourceFactory.newFileResource(fileToCompile), detectTypeOf(fileToCompile));
    handleErrors(knowledgeBuilder, fileToCompile);
  }

  private void handleErrors(KnowledgeBuilder knowledgeBuilder, File fileToCompile) throws MojoFailureException {
    final Log log = getLog();
    final KnowledgeBuilderErrors errors = knowledgeBuilder.getErrors();
    if (errors.isEmpty()) {
      log.debug("Compilation of " + fileToCompile.getAbsolutePath() + " completed successfully.");
      return;
    }
    log.error("Error(s) occurred while compiling " + fileToCompile + ":");
    log.error(formatErrors(errors));
    throw new MojoFailureException("Compilation errors occurred.");
  }

  private String formatErrors(KnowledgeBuilderErrors errors) {
    StringBuilder builder = new StringBuilder();
    int i = 0;
    for (KnowledgeBuilderError error : errors) {
      i++;
      builder.append("Error #" + i);
      final int[] errorLines = error.getErrorLines();
      if (errorLines.length > 0) {
        builder.append(" [occurred in line(s) ");
        for (int errorLineIndex = 0; errorLineIndex < errorLines.length; errorLineIndex++) {
          builder.append(errorLines[errorLineIndex]);
          if (errorLineIndex + 1 < errorLines.length) {
            builder.append(", ");
          }
        }
        builder.append("]");
      }
      builder.append(": ");
      builder.append(error.getMessage());
      builder.append(LINE_SEPARATOR);
    }
    return builder.toString();
  }

  private ResourceType detectTypeOf(File fileToCompile) {
    return ResourceType.DRL;
  }

  private KnowledgeBuilder getKnowledgeBuilder() throws MojoFailureException {
    if (knowledgeBuilder == null) {
      knowledgeBuilder = createNewKnowledgeBuilder();
    }
    return knowledgeBuilder;
  }

  private KnowledgeBuilder createNewKnowledgeBuilder() throws MojoFailureException {

    /*

     For java dependencies:
     * project.getCompileClasspathElements()
     * alle Elemente daraus in URL umwandeln
     * alle URLs an einen neuen URL ClassLoader übergeben
     * Delegation an Standard-Classloader (welchen?)
     *
     * Diesen neuen URL Class Loader an Knowledge Builder übergeben
     * Packaging laufen lassen
     *
     * Für "drools"-Dependencies muss man die Artefakte von Hand in die Knowledge Base laden,
     * bevor man weiter packagen kann (später!)
     *
     */

    final Log log = getLog();
    try {
      URLClassLoader classLoader = createCompileClassloader();
      Properties properties = new Properties();
      KnowledgeBuilderConfiguration configuration = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(properties, classLoader);
      return KnowledgeBuilderFactory.newKnowledgeBuilder(configuration);
    }
    catch (DependencyResolutionRequiredException e) {
      throw new MojoFailureException("Internal error: declared resolution of compile-scoped dependencies, but got exception!", e);
    }
    catch (MalformedURLException e) {
      throw new MojoFailureException("Got malformed URL for compile classpath element.", e);
    }
  }

  private URLClassLoader createCompileClassloader() throws DependencyResolutionRequiredException, MalformedURLException {
    Log log = getLog();

    final Set<Artifact> artifacts = project.getDependencyArtifacts();
    int i = 1;
    List<Artifact> compileArtifacts = new ArrayList<Artifact>();
    for (Artifact artifact : artifacts) {
      if (isRelevantForCompile(artifact)) {
        compileArtifacts.add(artifact);
      }
      log.debug("Dependency Artifact #" + i + ": Id=" + artifact.getId());
      log.debug("Dependency Artifact #" + i + ": GroupId=" + artifact.getGroupId());
      log.debug("Dependency Artifact #" + i + ": ArtifactId=" + artifact.getArtifactId());
      log.debug("Dependency Artifact #" + i + ": Type=" + artifact.getType());
      log.debug("Dependency Artifact #" + i + ": Classifier=" + artifact.getClassifier());
      log.debug("Dependency Artifact #" + i + ": Scope=" + artifact.getScope());

      log.debug("Dependency Artifact #" + i + ": BaseVersion=" + artifact.getBaseVersion());
      log.debug("Dependency Artifact #" + i + ": Version=" + artifact.getVersion());
      log.debug("Dependency Artifact #" + i + ": AvailableVersions=" + artifact.getAvailableVersions());
      log.debug("Dependency Artifact #" + i + ": File=" + artifact.getFile().getAbsolutePath());
      i++;
    }

    ArrayList<URL> classpathUrls = new ArrayList<URL>();
    for (Artifact compileArtifact : compileArtifacts) {
      URL classpathElementUrl = new URL("file://" + compileArtifact.getFile());
      classpathUrls.add(classpathElementUrl);
    }
    URLClassLoader classLoader = new URLClassLoader(classpathUrls.toArray(new URL[classpathUrls.size()]));
    log.info("Adding classpath URLs to classloader:");
    for (URL classpathUrl : classpathUrls) {
      log.info("   | " + classpathUrl);
    }
    log.info("   #");
    return classLoader;
  }

  private boolean isRelevantForCompile(Artifact artifact) {
    return ("jar".equals(artifact.getType()) && "compile".equals(artifact.getScope()));
  }
}
