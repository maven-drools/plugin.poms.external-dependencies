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
package net.itikko.maven.plugins.drools;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.fest.util.Arrays;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import java.util.List;

@MojoGoal(CompileMojo.GOAL)
public class CompileMojo extends AbstractMojo {

  public static final String GOAL = "compile";

  @MojoParameter(
      description = "Rule Source Roots",
      required = true
  )
  private List<String> ruleSourceRoots;

  @MojoParameter(defaultValue = "${project}")
  private MavenProject project;

  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("This is the compiler plugin");
    getLog().info("RuleSourceRoots: " + Arrays.format(ruleSourceRoots));
//    project.getBuild().
  }
}
