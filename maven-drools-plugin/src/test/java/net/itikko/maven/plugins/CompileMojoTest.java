/*
 * Copyright (c) 2009 Ansgar Konermann <konermann@itikko.net>
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
package net.itikko.maven.plugins;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

public class CompileMojoTest extends AbstractMojoTestCase {

  public void setUp() throws Exception {
    super.setUp();
  }

  public void testExecute() throws Exception {
    File testPom = new File(getBasedir(), "target/test-classes/unit-testing/discovery.pom.xml");
    CompileMojo mojo = (CompileMojo) lookupMojo(CompileMojo.GOAL, testPom);

    assertThat(mojo).as("CompileMojo instance").isNotNull();
  }
}
