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
package de.lightful.maven.plugins.drools.impl.logging;

import de.lightful.maven.plugins.drools.impl.config.Pass;
import de.lightful.maven.plugins.drools.knowledgeio.LogStream;
import org.apache.maven.plugin.MojoFailureException;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.fest.util.Arrays;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class PluginLogger {

  private LogStream<?> infoStream;
  private LogStream<?> debugStream;
  private LogStream<?> errorStream;
  private LogStream<?> warnStream;

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private PluginLogger product;

    private Builder() {
      this.product = new PluginLogger();
    }

    public Builder errorStream(LogStream<?> errorStream) {
      product.errorStream = errorStream;
      return this;
    }

    public Builder warnStream(LogStream<?> warnStream) {
      product.warnStream = warnStream;
      return this;
    }

    public Builder infoStream(LogStream<?> infoStream) {
      product.infoStream = infoStream;
      return this;
    }

    public Builder debugStream(LogStream<?> debugStream) {
      product.debugStream = debugStream;
      return this;
    }

    public PluginLogger create() {
      validateProduct();
      return product;
    }

    private void validateProduct() {
      assertThat(product.errorStream).as("ERROR log stream").isNotNull();
      assertThat(product.warnStream).as("WARN log stream").isNotNull();
      assertThat(product.infoStream).as("INFO log stream").isNotNull();
      assertThat(product.debugStream).as("DEBUG log stream").isNotNull();
    }
  }

  private PluginLogger() {
  }

  public LogStream<?> getErrorStream() {
    return errorStream;
  }

  public LogStream<?> getWarnStream() {
    return warnStream;
  }

  public LogStream<?> getInfoStream() {
    return infoStream;
  }

  public LogStream<?> getDebugStream() {
    return debugStream;
  }

  public void dumpPassesConfiguration(Pass[] passes) {
    for (Pass pass : passes) {
      infoStream.log("Pass #" + pass.getSequenceNumber() + ":").nl();
      infoStream.log("    Name:             '" + pass.getName() + "'").nl();
      infoStream.log("    Rule Source Root: " + pass.getRuleSourceRoot()).nl();
      infoStream.log("    Includes:         " + Arrays.format(pass.getIncludes())).nl();
      infoStream.log("    Excludes:         " + Arrays.format(pass.getExcludes())).nl();
    }
  }

  public void dumpOverallPluginConfiguration(Pass[] passes, String name) {
    infoStream.log("This is the compiler plugin").nl();
    infoStream.log("Passes: " + Arrays.format(passes)).nl();
    infoStream.log("Project: " + name).nl();
  }

  public void reportCompilationErrors(KnowledgeBuilderErrors errors, File fileToCompile) throws MojoFailureException {
    if (errors.isEmpty()) {
      debugStream.log("Compilation of " + fileToCompile.getAbsolutePath() + " completed successfully.").nl();
      return;
    }
    debugStream.log("Error(s) occurred while compiling " + fileToCompile + ":");
    formatCompilerErrors(debugStream, errors);
    throw new MojoFailureException("Compilation errors occurred.");
  }

  private void formatCompilerErrors(LogStream<?> logStream, KnowledgeBuilderErrors errors) {
    int i = 0;
    for (KnowledgeBuilderError error : errors) {
      i++;
      logStream.log("Error #" + i);
      final int[] errorLines = error.getErrorLines();
      if (errorLines.length > 0) {
        logStream.log(" [occurred in line(s) ");
        for (int errorLineIndex = 0; errorLineIndex < errorLines.length; errorLineIndex++) {
          logStream.log("" + errorLines[errorLineIndex]);
          if (errorLineIndex + 1 < errorLines.length) {
            logStream.log(", ");
          }
        }
        logStream.log("]");
      }
      logStream.log(": ");
      logStream.log(error.getMessage());
      logStream.nl();
    }
  }

  public void logCompileProgress(File fileToCompile) {
    infoStream.log("  Compiling rule file '" + fileToCompile.getAbsolutePath() + "' ...").nl();
  }
}
