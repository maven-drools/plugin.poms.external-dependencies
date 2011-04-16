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
package de.lightful.maven.plugins.drools.mojos;

import de.lightful.maven.plugins.drools.impl.DependencyLoader;
import de.lightful.maven.plugins.drools.impl.WellKnownNames;
import de.lightful.maven.plugins.drools.impl.config.Pass;
import de.lightful.maven.plugins.drools.impl.logging.MavenDebugLogStream;
import de.lightful.maven.plugins.drools.impl.logging.MavenInfoLogStream;
import de.lightful.maven.plugins.drools.impl.logging.PluginLogger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.ResourceType;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.jfrog.maven.annomojo.annotations.MojoComponent;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

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

  private Build build;

  private KnowledgeBuilder knowledgeBuilder;

  private MavenInfoLogStream info;
  private MavenDebugLogStream debug;

  @MojoComponent
  private RepositorySystem repoSystem;

  @MojoParameter(defaultValue = "${repositorySystemSession}", readonly = true)
  private RepositorySystemSession repoSession;

  @MojoParameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
  private List<RemoteRepository> projectRepos;

  private PluginLogger pluginLogger;
  private DependencyLoader dependencyLoader;

  public CompileMojo() {
  }

  public void execute() throws MojoFailureException {
    initialize();

    pluginLogger.dumpPluginConfiguration(passes, project.getName());

    fixupPassesInformation();
    pluginLogger.dumpPassesConfiguration(passes);

    runAllPasses();
    writeOutputFile();
  }

  private void initialize() {
    final Log log = getLog();
    info = new MavenInfoLogStream(log);
    debug = new MavenDebugLogStream(log);
    pluginLogger = new PluginLogger(info, debug);
    dependencyLoader = new DependencyLoader(pluginLogger);
  }

  private void fixupPassesInformation() {
    int passNumber = PluginLogger.FIRST_PASS_NUMBER;
    for (Pass pass : passes) {
      if (pass.getName() == null || "".equals(pass.getName())) {
        pass.setName("Pass #" + passNumber);
      }
      if (pass.getIncludes() == null || pass.getIncludes().length == 0) {
        pass.setIncludes(new String[] {"**/*.drl"});
      }

      passNumber++;
    }
  }

  private void runAllPasses() throws MojoFailureException {
    knowledgeBuilder = dependencyLoader.createKnowledgeBuilderForRuleCompilation(project.getDependencyArtifacts());
    for (Pass pass : passes) {
      executePass(pass);
    }
  }

  private void writeOutputFile() throws MojoFailureException {
    final String packaging = project.getPackaging();
    final Log log = getLog();
    if (!WellKnownNames.DROOLS_KNOWLEDGE_MODULE_PACKAGING_IDENTIFIER.equals(packaging)) {
      log.error("Internal error: packaging of project must be equal to '" + WellKnownNames.DROOLS_KNOWLEDGE_MODULE_PACKAGING_IDENTIFIER + "' when using this plugin!");
    }
    build = project.getBuild();
    writeFinalOutputFile(build.getFinalName() + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE);
  }

  private void writeFinalOutputFile(String outputFileName) throws MojoFailureException {
    final String buildDirectoryName = project.getBuild().getDirectory();
    File buildDirectory = new File(buildDirectoryName);
    File outputFile = new File(buildDirectoryName, outputFileName);

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
        throw new MojoFailureException("Unable to delete " + absoluteOutputFileName + "!");
      }
      else {
        try {
          outputFile.createNewFile();
        }
        catch (IOException e) {
          throw new MojoFailureException("Unable to create output file " + absoluteOutputFileName + "!", e);
        }
      }
    }

    try {
      DroolsStreamUtils.streamOut(new FileOutputStream(outputFile), knowledgePackages, false);
      log.info("Setting project artifact to " + outputFile.getAbsolutePath());
      final Artifact artifact = project.getArtifact();
      artifact.setFile(outputFile);
    }
    catch (IOException e) {
      throw new MojoFailureException("Unable to write compiled knowledge into output file!", e);
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
}
