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
import de.lightful.maven.plugins.drools.impl.config.ConfigurationValidator;
import de.lightful.maven.plugins.drools.impl.config.Pass;
import de.lightful.maven.plugins.drools.impl.logging.MavenDebugLogStream;
import de.lightful.maven.plugins.drools.impl.logging.MavenErrorLogStream;
import de.lightful.maven.plugins.drools.impl.logging.MavenInfoLogStream;
import de.lightful.maven.plugins.drools.impl.logging.MavenWarnLogStream;
import de.lightful.maven.plugins.drools.impl.logging.PluginLogger;
import de.lightful.maven.plugins.drools.knowledgeio.LogStream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.drools.builder.KnowledgeBuilder;
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

@MojoGoal(WellKnownNames.GOAL_COMPILE)
@MojoRequiresDependencyResolution("runtime")
public class CompileMojo extends AbstractMojo {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  @MojoParameter(
      description = "Define what the compiler should compile in each of its passes. " +
                    "You need at least one pass to create useful output.",
      readonly = false,
      required = true
  )
  private Pass[] passes;

  @MojoParameter(defaultValue = "${project}")
  private MavenProject project;

  private KnowledgeBuilder knowledgeBuilder;

  @MojoComponent
  private RepositorySystem repositorySystem;

  @MojoParameter(defaultValue = "${repositorySystemSession}", readonly = true)
  private RepositorySystemSession repositorySession;

  @MojoParameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
  private List<RemoteRepository> projectRepositories;

  private PluginLogger pluginLogger;
  private DependencyLoader dependencyLoader;
  private LogStream<?> infoLogStream;
  private LogStream<?> debugLogStream;
  private LogStream<?> warnLogStream;
  private LogStream<?> errorLogStream;

  public void execute() throws MojoFailureException {
    initializeLogging();
    pluginLogger.dumpOverallPluginConfiguration(passes, project.getName());

    validatePassesConfiguration();
    pluginLogger.dumpPassesConfiguration(passes);

    initializeDependencyLoader();

    runAllPasses();
    writeOutputFile();
  }

  private void initializeLogging() {
    final Log log = getLog();
    infoLogStream = new MavenInfoLogStream(log);
    debugLogStream = new MavenDebugLogStream(log);
    warnLogStream = new MavenWarnLogStream(log);
    errorLogStream = new MavenErrorLogStream(log);
    pluginLogger = PluginLogger.builder()
        .errorStream(errorLogStream)
        .warnStream(warnLogStream)
        .infoStream(infoLogStream)
        .debugStream(debugLogStream)
        .create();
  }

  private void validatePassesConfiguration() {
    ConfigurationValidator configurationValidator = new ConfigurationValidator();
    configurationValidator.validateConfiguration(passes);
  }

  private void initializeDependencyLoader() {
    dependencyLoader = new DependencyLoader(pluginLogger);
  }

  private void runAllPasses() throws MojoFailureException {
    knowledgeBuilder = dependencyLoader.createKnowledgeBuilderForRuleCompilation(project.getDependencyArtifacts());
    for (Pass pass : passes) {
      executePass(pass);
    }
  }

  private void writeOutputFile() throws MojoFailureException {
    final String packaging = project.getPackaging();
    if (!WellKnownNames.DROOLS_KNOWLEDGE_MODULE_PACKAGING_IDENTIFIER.equals(packaging)) {
      errorLogStream.log("Internal error: packaging of project must be equal to '" + WellKnownNames.DROOLS_KNOWLEDGE_MODULE_PACKAGING_IDENTIFIER + "' when using this plugin!").nl();
    }
    Build build = project.getBuild();
    writeFinalOutputFile(build.getFinalName() + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE);
  }

  private void writeFinalOutputFile(String outputFileName) throws MojoFailureException {
    final String buildDirectoryName = project.getBuild().getDirectory();
    File buildDirectory = new File(buildDirectoryName);
    File outputFile = new File(buildDirectoryName, outputFileName);

    final Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();

    final String absoluteOutputFileName = outputFile.getAbsolutePath();
    infoLogStream.log("Writing " + knowledgePackages.size() + " knowledge packages into output file " + absoluteOutputFileName).nl();

    if (!buildDirectory.exists()) {
      debugLogStream.log(("Output directory " + buildDirectory.getAbsolutePath() + " does not exist, creating.")).nl();
      buildDirectory.mkdirs();
    }

    if (outputFile.exists()) {
      warnLogStream.log("Output file " + absoluteOutputFileName + " exists, overwriting.").nl();
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
      infoLogStream.log(("Setting project artifact to " + outputFile.getAbsolutePath())).nl();
      final Artifact artifact = project.getArtifact();
      artifact.setFile(outputFile);
    }
    catch (IOException e) {
      throw new MojoFailureException("Unable to write compiled knowledge into output file!", e);
    }
  }

  private void executePass(Pass pass) throws MojoFailureException {
    infoLogStream.log("Executing compiler pass '" + pass.getName() + "'...").nl();
    final String[] filesToCompile = determineFilesToCompile(pass);
    for (String currentFile : filesToCompile) {
      compileRuleFile(pass.getRuleSourceRoot(), currentFile);
    }
    infoLogStream.log("Done with compiler pass '" + pass.getName() + "'.").nl();
  }

  private String[] determineFilesToCompile(Pass pass) {
    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setBasedir(pass.getRuleSourceRoot());
    scanner.setIncludes(pass.getIncludes());
    scanner.setExcludes(pass.getExcludes());
    scanner.setCaseSensitive(true);
    scanner.scan();

    return scanner.getIncludedFiles();
  }

  private void compileRuleFile(File ruleSourceRoot, String nameOfFileToCompile) throws MojoFailureException {
    File fileToCompile = new File(ruleSourceRoot, nameOfFileToCompile);
    pluginLogger.logCompileProgress(fileToCompile);
    knowledgeBuilder.add(ResourceFactory.newFileResource(fileToCompile), detectTypeOf(fileToCompile));
    pluginLogger.reportCompilationErrors(knowledgeBuilder.getErrors(), fileToCompile);
  }

  private ResourceType detectTypeOf(File fileToCompile) {
    return ResourceType.DRL;
  }
}
