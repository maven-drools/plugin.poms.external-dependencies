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
import org.fest.util.Arrays;

public class PluginLogger {

  public static final int FIRST_PASS_NUMBER = 1;

  private LogStream<?> infoStream;
  private LogStream<?> debugStream;

  public PluginLogger(LogStream<?> infoStream, LogStream<?> debugStream) {
    this.infoStream = infoStream;
    this.debugStream = debugStream;
  }

  public void dumpPassesConfiguration(Pass[] passes) {
    int passNumber = FIRST_PASS_NUMBER;
    for (Pass pass : passes) {
      infoStream.log("Pass #" + passNumber + ":").nl();
      infoStream.log("    Name:             '" + pass.getName() + "'").nl();
      infoStream.log("    Rule Source Root: " + pass.getRuleSourceRoot()).nl();
      infoStream.log("    Includes:         " + Arrays.format(pass.getIncludes())).nl();
      infoStream.log("    Excludes:         " + Arrays.format(pass.getExcludes())).nl();
      passNumber++;
    }
  }

  public void dumpPluginConfiguration(Pass[] passes, String name) {
    infoStream.log("This is the compiler plugin").nl();
    infoStream.log("Passes: " + Arrays.format(passes)).nl();
    infoStream.log("Project: " + name).nl();
  }

  public LogStream<?> getInfoStream() {
    return infoStream;
  }

  public LogStream<?> getDebugStream() {
    return debugStream;
  }
}
