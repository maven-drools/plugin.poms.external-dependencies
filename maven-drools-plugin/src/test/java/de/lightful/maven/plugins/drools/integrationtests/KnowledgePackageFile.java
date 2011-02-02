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
package de.lightful.maven.plugins.drools.integrationtests;

import org.apache.maven.it.Verifier;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.definition.KnowledgePackage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static org.fest.assertions.Assertions.assertThat;

public class KnowledgePackageFile {

  private File delegate;

  public KnowledgePackageFile(File delegate) {
    this.delegate = delegate;
  }

  public KnowledgePackageFile(String fileName) {
    this.delegate = new File(fileName);
  }

  public KnowledgePackageFile(Verifier verifier, String relativeFileName) {
    this.delegate = new File(verifier.getBasedir() + File.separator + relativeFileName);
  }

  public File getFile() {
    return delegate;
  }

  public Iterable<KnowledgePackage> getKnowledgePackages() throws IOException, ClassNotFoundException {
    return loadKnowledgePackagesFromFile();
  }

  private Collection<KnowledgePackage> loadKnowledgePackagesFromFile() throws IOException, ClassNotFoundException {
    Object streamedInObject = DroolsStreamUtils.streamIn(new FileInputStream(delegate));
    assertThat(streamedInObject).as("object read from stream").isNotNull().isInstanceOf(Collection.class);

    Collection loadedObjects = Collection.class.cast(streamedInObject);
    ensureLoadedObjectsAreKnowledgePackages(loadedObjects);
    Collection<KnowledgePackage> knowledgePackages = convertCollectionItemsToKnowledgePackages(loadedObjects);
    return knowledgePackages;
  }

  private void ensureLoadedObjectsAreKnowledgePackages(Collection loadedObjects) {
    int i = 1;
    for (Object loadedObject : loadedObjects) {
      assertThat(loadedObject).as("object #" + i + " from read collection").isInstanceOf(KnowledgePackage.class);
      i++;
    }
  }

  @SuppressWarnings("unchecked")
  private Collection<KnowledgePackage> convertCollectionItemsToKnowledgePackages(Collection loadedObjects) {
    Collection<KnowledgePackage> knowledgePackages = new ArrayList<KnowledgePackage>();
    knowledgePackages.addAll(loadedObjects);
    return knowledgePackages;
  }
}
