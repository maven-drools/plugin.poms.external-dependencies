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

/** Various constant values used both in production and test code of the plugin. */
public interface WellKnownNames {

  /**
   * Maven packaging identifier to be used in pom.xml if drools packaging is desired. <p/> Example:<br/>
   * <pre>
   * &lt;project ...&gt;
   *   ...
   *   &lt;packaging&gt;drools&lt;/packaging&gt;
   *   ...
   * &lt;/project&gt;
   * </pre>
   */
  String DROOLS_PACKAGING_IDENTIFIER = "dkp";

  /**
   * File extension used by plugin to create final target file name. The extension <b>.dkp</b> is short for "Drools Knowledge
   * Package(s).
   */
  String DROOLS_KNOWLEDGE_PACKAGE_EXTENSION = ".dkp";
  String GOAL_COMPILE = "compile";
  String SCOPE_COMPILE = GOAL_COMPILE;

  String ARTIFACT_TYPE_JAR = "jar";
  String ARTIFACT_TYPE_DROOLS_KNOWLEDGE_PACKAGE = "dkp";
}
