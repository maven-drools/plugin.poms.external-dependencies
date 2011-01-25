/*
 * Copyright (c) 2009-2011 Ansgar Konermann
 *
 * This file is part of the Maven 3 Drools Plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lightful.maven.plugins.drools;

public abstract class TestResources {

  private static final String BASEDIR = "target/test-classes";
  private static final String UNIT_TEST_PACKAGE = BASEDIR + "/unit-tests";
  private static final String INTEGRATION_TEST_PACKAGE = BASEDIR + "integrationtests";

  public static final String DISCOVERY_POM = UNIT_TEST_PACKAGE + "/discovery.pom.xml";

  public static final String SETTINGS_FILE_FOR_INTEGRATION_TESTS = INTEGRATION_TEST_PACKAGE + "/integration-settings.xml";
  public static final String COMPILE_MINIMUM_DRL_POM = INTEGRATION_TEST_PACKAGE + "/pom.xml";
}