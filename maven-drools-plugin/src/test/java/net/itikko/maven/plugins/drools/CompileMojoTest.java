/*
 * Copyright (c) 2009-2010 Ansgar Konermann <konermann@itikko.net>
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
package net.itikko.maven.plugins.drools;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;

public class CompileMojoTest extends AbstractMojoTestCase {

  public void setUp() throws Exception {
    super.setUp();
  }

  public void testMojoLookupSucceeds() throws Exception {
    File testPom = new File(getBasedir(), TestResources.DISCOVERY_POM);
    CompileMojo mojo = (CompileMojo) lookupMojo(CompileMojo.GOAL, testPom);

    assertThat(mojo).as("CompileMojo instance").isNotNull();
  }

//  public void testMojoCompilesEmptyDroolsFile throws Exception {
//    File testPom = new File(getBasedir(), )
//  }
}
