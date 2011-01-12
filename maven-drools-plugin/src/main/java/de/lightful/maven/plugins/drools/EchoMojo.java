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

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jfrog.maven.annomojo.annotations.MojoGoal;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;

import java.util.List;

@MojoGoal(EchoMojo.GOAL)
@MojoRequiresDependencyResolution("compile")
public class EchoMojo extends AbstractMojo {

  @MojoParameter(expression = "${greeting}", defaultValue = "Hello World!")
  private String message;

  @MojoParameter(defaultValue = "${project.dependencies}", readonly = true, required = true)
  private List<Dependency> dependencies;

  @MojoParameter(defaultValue = "${project}")
  private MavenProject project;

  public static final String GOAL = "echo";

  public void execute() throws MojoExecutionException {
    getLog().info(message);
    for (Dependency dependency : dependencies) {
      StringBuilder sb = new StringBuilder();
      sb.append(dependency.getGroupId());
      sb.append(":");
      sb.append(dependency.getGroupId());
      sb.append(":");
      sb.append(dependency.getVersion());
      sb.append(": ");
      sb.append(dependency.getClassifier());
      sb.append(":");
      sb.append(dependency.getSystemPath());
      getLog().info(sb.toString());
    }
  }
}
