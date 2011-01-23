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
package de.lightful.maven.plugins.drools.integrationtests;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.definition.KnowledgePackage;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

@Test
public class CanCompileMinimumDrlFileTest {

  private static final String DROOLS_KNOWLEDGE_PACKAGE_EXTENSION = ".dkp";
  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + DROOLS_KNOWLEDGE_PACKAGE_EXTENSION;

  public void testCanCallCleanGoal() throws Exception {
    File testDirectory = ResourceExtractor.simpleExtractResources(getClass(), "compile");
    Verifier verifier = new Verifier(testDirectory.getAbsolutePath());
    final String logFileName = verifier.getLogFileName();

    verifier.setDebug(true);
    verifier.setMavenDebug(true);
    verifier.executeGoal("clean");
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testDoesCreateOutputFile() throws Exception {
    File testDirectory = ResourceExtractor.simpleExtractResources(getClass(), "compile");
    Verifier verifier = new Verifier(testDirectory.getAbsolutePath());
    final String logFileName = verifier.getLogFileName();
    verifier.executeGoal("clean");

//    verifier.setDebug(true);
//    verifier.setMavenDebug(true);
    verifier.executeGoal("compile");
    verifier.verifyErrorFreeLog();

    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);
  }

  @Test
  public void testOutputFileContainsDroolsKnowledgePackages() throws Exception {
    File testDirectory = ResourceExtractor.simpleExtractResources(getClass(), "compile");
    Verifier verifier = new Verifier(testDirectory.getAbsolutePath());
    final String logFileName = verifier.getLogFileName();
    verifier.executeGoal("clean");
    verifier.executeGoal("compile");
    verifier.verifyErrorFreeLog();

    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);
    File knowledgeFile = new File(verifier.getBasedir() + File.separator + EXPECTED_OUTPUT_FILE);
    assertThat(knowledgeFile.exists()).as("Knowledge File exists").isTrue();

    Object streamedInObject = DroolsStreamUtils.streamIn(new FileInputStream(knowledgeFile));
    assertThat(streamedInObject).as("object read from stream").isNotNull().isInstanceOf(Collection.class);
    Collection<?> loadedObjects = Collection.class.cast(streamedInObject);
    int i = 1;
    for (Object loadedObject : loadedObjects) {
      assertThat(loadedObject).as("object #" + i + " from read collection").isInstanceOf(KnowledgePackage.class);
      i++;
    }

    Collection<KnowledgePackage> knowledgePackages = (Collection<KnowledgePackage>) loadedObjects;
    assertThat(knowledgePackages).as("collection of knowledge packages").hasSize(1);
  }
}
