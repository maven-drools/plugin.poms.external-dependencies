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
package de.lightful.maven.plugins.drools;

import java.io.File;
import java.util.Arrays;

/** Since the Drools packaging APIs do not support forward references, we allow specification of any number of compiler passes. */
public class Pass {

  private String name;

  private File ruleSourceRoot;

  private String[] includes;

  private String[] excludes;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public File getRuleSourceRoot() {
    return ruleSourceRoot;
  }

  public void setRuleSourceRoot(File ruleSourceRoot) {
    this.ruleSourceRoot = ruleSourceRoot;
  }

  public String[] getIncludes() {
    return includes;
  }

  public void setIncludes(String[] includes) {
    this.includes = includes;
  }

  public String[] getExcludes() {
    return excludes;
  }

  public void setExcludes(String[] excludes) {
    this.excludes = excludes;
  }

  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Pass");
    sb.append("{name='").append(name).append('\'');
    sb.append(", ruleSourceRoot=").append(ruleSourceRoot);
    sb.append(", includes=").append(includes == null ? "null" : Arrays.asList(includes).toString());
    sb.append(", excludes=").append(excludes == null ? "null" : Arrays.asList(excludes).toString());
    sb.append('}');
    return sb.toString();
  }
}
