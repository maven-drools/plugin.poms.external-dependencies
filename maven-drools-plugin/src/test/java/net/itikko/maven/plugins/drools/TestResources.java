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

public abstract class TestResources {

  private static final String BASEDIR = "target/test-classes/unit-testing/";

  public static final String DISCOVERY_POM = BASEDIR + "discovery.pom.xml";
  public static final String COMPILE_EMPTY_FILE_POM = BASEDIR + "compile-empty.pom.xml";
}