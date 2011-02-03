package de.lightful.maven.plugins.drools.integrationtests;

import de.lightful.maven.plugins.testing.ExecuteGoals;
import de.lightful.maven.plugins.testing.MavenVerifierTest;
import de.lightful.maven.plugins.testing.VerifyUsingProject;
import org.apache.maven.it.Verifier;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static de.lightful.maven.plugins.drools.WellKnownNames.DROOLS_KNOWLEDGE_PACKAGE_EXTENSION;

@Test
@VerifyUsingProject("single_java_dependency")
@ExecuteGoals("clean")
public class CanUseExistingJavaModel extends MavenVerifierTest {

  private static final String EXPECTED_OUTPUT_FILE = "target/plugintest.artifact-1.0.0" + DROOLS_KNOWLEDGE_PACKAGE_EXTENSION;

  @Inject
  private Verifier verifier;

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
}
