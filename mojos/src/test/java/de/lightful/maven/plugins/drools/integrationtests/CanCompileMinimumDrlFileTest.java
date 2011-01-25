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

import de.lightful.maven.plugins.testing.ExecuteGoals;
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.definition.KnowledgePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

@Test
@VerifyUsingProject("compile_single_file")
@ExecuteGoals("clean")
public class CanCompileMinimumDrlFileTest {

  private Logger log = LoggerFactory.getLogger(CanCompileMinimumDrlFileTest.class);

  private static final String DROOLS_KNOWLEDGE_PACKAGE_EXTENSION = ".dkp";
  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + DROOLS_KNOWLEDGE_PACKAGE_EXTENSION;

  private Verifier verifier;

  @BeforeMethod
  private void setUpVerifier(Method testMethod) {
    final String testDirectoryName = obtainTestDirectoryName(testMethod);
    File testDirectory;
    try {
      testDirectory = ResourceExtractor.simpleExtractResources(getClass(), testDirectoryName);
      verifier = new Verifier(testDirectory.getAbsolutePath());
      String[] goals = obtainGoalsToExecute(testMethod);
      assertThat(goals.length).as("Number of goals to execute").isGreaterThan(0);
      for (String goal : goals) {
        verifier.executeGoal(goal);
      }
    }
    catch (IOException e) {
      fail("Unable to extract integration test resources from directory '" + testDirectoryName + "'.", e);
    }
    catch (VerificationException e) {
      fail("Unable to construct Maven Verifier from project in directory " + testDirectoryName + ".", e);
    }
  }

  private String[] obtainGoalsToExecute(Method testMethod) {
    List<String> goals = new ArrayList<String>();
    final Class<?> declaringClass = testMethod.getDeclaringClass();
    final ExecuteGoals annotationOnClass = declaringClass.getAnnotation(ExecuteGoals.class);
    if (annotationOnClass != null) {
      goals.addAll(Arrays.asList(annotationOnClass.value()));
    }
    final ExecuteGoals annotationOnMethod = testMethod.getAnnotation(ExecuteGoals.class);
    if (annotationOnMethod != null) {
      goals.addAll(Arrays.asList(annotationOnMethod.value()));
    }
    return goals.toArray(new String[goals.size()]);
  }

  private String obtainTestDirectoryName(Method testMethod) {
    final VerifyUsingProject annotationOnMethod = testMethod.getAnnotation(VerifyUsingProject.class);
    if (annotationOnMethod != null) {
      return annotationOnMethod.value();
    }

    final Class<?> declaringClass = testMethod.getDeclaringClass();
    final VerifyUsingProject annotationOnClass = declaringClass.getAnnotation(VerifyUsingProject.class);
    if (annotationOnClass == null) {
      throw new IllegalArgumentException("No @" + VerifyUsingProject.class.getSimpleName() + " annotation found on " +
                                         "test method or test class. Don't know where to take project definition from.");
    }
    return annotationOnClass.value();
  }

  @Test
  public void testCanCallCleanGoal() throws Exception {
    verifier.verifyErrorFreeLog();
  }

  @Test
  @ExecuteGoals("compile")
  public void testDoesCreateOutputFile() throws Exception {
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);
  }

  @Test
  @ExecuteGoals("compile")
  public void testOutputFileContainsDroolsKnowledgePackages() throws Exception {
    verifier.verifyErrorFreeLog();
    verifier.assertFilePresent(EXPECTED_OUTPUT_FILE);
    File knowledgeFile = new File(verifier.getBasedir() + File.separator + EXPECTED_OUTPUT_FILE);
    assertThat(knowledgeFile.exists()).as("Knowledge File exists").isTrue();

    Object streamedInObject = DroolsStreamUtils.streamIn(new FileInputStream(knowledgeFile));
    assertThat(streamedInObject).as("object read from stream").isNotNull().isInstanceOf(Collection.class);

    Collection loadedObjects = Collection.class.cast(streamedInObject);
    ensureLoadedObjectsAreKnowledgePackages(loadedObjects);
    Collection<KnowledgePackage> knowledgePackages = convertCollectionItemsToKnowledgePackages(loadedObjects);

    assertThat(knowledgePackages).as("collection of knowledge packages").hasSize(1);
  }

  private void ensureLoadedObjectsAreKnowledgePackages(Collection loadedObjects) {
    int i = 1;
    for (Object loadedObject : loadedObjects) {
      assertThat(loadedObject).as("object #" + i + " from read collection").isInstanceOf(KnowledgePackage.class);
      i++;
    }
  }

  @SuppressWarnings("unchecked")
  private Collection<KnowledgePackage> convertCollectionItemsToKnowledgePackages(Collection loadedObjects) {
    Collection<KnowledgePackage> knowledgePackages = new ArrayList<KnowledgePackage>();
    knowledgePackages.addAll(loadedObjects);
    return knowledgePackages;
  }
}
