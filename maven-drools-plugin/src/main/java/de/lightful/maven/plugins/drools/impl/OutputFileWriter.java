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
package de.lightful.maven.plugins.drools.impl;

import de.lightful.maven.plugins.drools.impl.logging.PluginLogger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.definition.KnowledgePackage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

public class OutputFileWriter {

  public OutputFileWriter() {
  }

  public void writeOutputFile(Collection<KnowledgePackage> knowledgePackages, PluginLogger logger, MavenProject mavenProject) throws MojoFailureException {
    if (!WellKnownNames.DROOLS_KNOWLEDGE_MODULE_PACKAGING_IDENTIFIER.equals(mavenProject.getPackaging())) {
      logger.getErrorStream().log("Internal error: packaging of project must be equal to '" + WellKnownNames.DROOLS_KNOWLEDGE_MODULE_PACKAGING_IDENTIFIER + "' when using this plugin!").nl();
    }
    Build build = mavenProject.getBuild();
    File buildDirectory = new File(build.getDirectory());
    File outputFile = new File(buildDirectory, build.getFinalName() + "." + WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE);

    final String absoluteOutputFileName = outputFile.getAbsolutePath();
    logger.getInfoStream().log("Writing " + knowledgePackages.size() + " knowledge packages into output file " + absoluteOutputFileName).nl();

    if (!buildDirectory.exists()) {
      logger.getDebugStream().log(("Output directory " + buildDirectory.getAbsolutePath() + " does not exist, creating.")).nl();
      buildDirectory.mkdirs();
    }

    if (outputFile.exists()) {
      logger.getWarnStream().log("Output file " + absoluteOutputFileName + " exists, overwriting.").nl();
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
      logger.getInfoStream().log(("Setting project artifact to " + outputFile.getAbsolutePath())).nl();
      final Artifact artifact = mavenProject.getArtifact();
      artifact.setFile(outputFile);
    }
    catch (IOException e) {
      throw new MojoFailureException("Unable to write compiled knowledge into output file!", e);
    }
  }
}