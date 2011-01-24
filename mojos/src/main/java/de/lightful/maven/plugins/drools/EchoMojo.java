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
