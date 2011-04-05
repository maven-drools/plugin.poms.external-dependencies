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
package de.lightful.maven.plugins.drools.integrationtests;

import de.lightful.maven.plugins.testing.ExecuteGoals;
import de.lightful.maven.plugins.testing.MavenVerifierTest;
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.Verifier;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;

import static de.lightful.maven.plugins.drools.impl.WellKnownNames.FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;
import static de.lightful.maven.plugins.drools.impl.WellKnownNames.GOAL_CLEAN;
import static de.lightful.maven.plugins.drools.impl.WellKnownNames.GOAL_COMPILE;
import static de.lightful.maven.plugins.drools.impl.WellKnownNames.GOAL_DEPLOY;
import static de.lightful.maven.plugins.drools.impl.WellKnownNames.GOAL_TEST_COMPILE;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;

@Test
@VerifyUsingProject("can_deploy_artifact_to_given_repository")
@DefaultSettingsFile
public class CanDeployArtifactToGivenRepository extends MavenVerifierTest {

  public static final String EXPECTED_ARTIFACT_NAME = "de/lightful/maven/plugins/plugintest/drools/plugintest.artifact/1.0.0/plugintest.artifact-1.0.0" + "." + FILE_EXTENSION_DROOLS_KNOWLEDGE_MODULE;

  @Inject
  private Verifier verifier;

  @Test
  @Parameters("repository.deploymenttests")
  @ExecuteGoals(GOAL_CLEAN)
  public void testFileGetsDeployedToExpectedLocation(String deploymentRepositoryUrl) throws Exception {
    verifier.executeGoal(GOAL_COMPILE);
    verifier.executeGoal(GOAL_TEST_COMPILE);
    verifier.executeGoal("package");
    verifier.executeGoal("integration-test");
    verifier.executeGoal("install");
    verifier.executeGoal(GOAL_DEPLOY);
    verifier.verifyErrorFreeLog();

    URL repositoryUrl = new URL(deploymentRepositoryUrl);
    File repositoryDirectory = new File(repositoryUrl.getFile());

    assertThat(repositoryDirectory).exists().isDirectory();
    File expectedDeployedArtifact = new File(repositoryDirectory, EXPECTED_ARTIFACT_NAME);
    assertThat(expectedDeployedArtifact).exists().isFile();
    fail("This test must fail. If it does not by itself, something is wrong! Please fix me!");
  }
}