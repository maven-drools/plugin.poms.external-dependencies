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
package de.lightful.maven.plugins.drools.impl;

import org.apache.maven.artifact.Artifact;

import static de.lightful.maven.plugins.drools.impl.WellKnownNames.ARTIFACT_TYPE_DROOLS_KNOWLEDGE_PACKAGE;
import static de.lightful.maven.plugins.drools.impl.WellKnownNames.SCOPE_COMPILE;

public class IsDroolsKnowledgePackageForCompilation extends ArtifactPredicate {

  public static final ArtifactPredicate INSTANCE = new IsDroolsKnowledgePackageForCompilation();

  private IsDroolsKnowledgePackageForCompilation() {
  }

  @Override
  public boolean isTrueFor(Artifact item) {
    return SCOPE_COMPILE.equals(item.getScope()) && ARTIFACT_TYPE_DROOLS_KNOWLEDGE_PACKAGE.equals(item.getType());
  }
}
