/*
 * Copyright (c) 2009-2011 Ansgar Konermann <konermann@itikko.net>
 *
 * This file is part of the Maven 2 Drools Plugin.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lightful.maven.plugins.drools.integrationtests;

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;

@Test
public class CanCompileMinimumDrlFile {

  public void testCanCompileMinimumDrlFile() throws Exception {

    File testDirectory = ResourceExtractor.simpleExtractResources(getClass(), "compile");

    Verifier verifier = new Verifier(testDirectory.getAbsolutePath());

//    File resourceDir = ResourceExtractor.simpleExtractResources(TestResources.class, TestResources.COMPILE_MINIMUM_DRL_POM);
//    File settingsFile = ResourceExtractor.simpleExtractResources(TestResources.class, TestResources.SETTINGS_FILE_FOR_INTEGRATION_TESTS);
//    File baseDir = ResourceExtractor.simpleExtractResources(getClass(), "/compile");
//
//    Verifier verifier = new Verifier(baseDir.getAbsolutePath(), settingsFile.getAbsolutePath());
//    final String logFileName = verifier.getLogFileName();
//    verifier.setDebug(true);
//    verifier.setMavenDebug(true);
//    verifier.executeGoal("clean");
//    verifier.verifyErrorFreeLog();
  }

  @DataProvider(name = "xyzDataPro")
  private Object[][] getTestData() {
    return new Object[][] {};
  }
}
