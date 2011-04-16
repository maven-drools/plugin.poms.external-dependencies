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

import de.lightful.maven.plugins.drools.impl.logging.PluginLogger;
import de.lightful.maven.plugins.drools.impl.predicates.ArtifactPredicate;
import de.lightful.maven.plugins.drools.knowledgeio.KnowledgePackageFile;
import de.lightful.maven.plugins.drools.knowledgeio.KnowledgePackageFormatter;
import de.lightful.maven.plugins.drools.knowledgeio.LogStream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoFailureException;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.definition.KnowledgePackage;
import org.fest.util.Arrays;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class DependencyLoader {

  private PluginLogger logger;
  private LogStream<?> info;
  private LogStream<?> debug;

  public DependencyLoader(PluginLogger logger) {
    this.logger = logger;
    info = logger.getInfoStream();
    debug = logger.getDebugStream();
  }

  public KnowledgeBuilder createKnowledgeBuilderForRuleCompilation(Set<Artifact> dependencyArtifacts) throws MojoFailureException {
    try {
      URLClassLoader classLoader = createCompileClassLoader(dependencyArtifacts);
      info.log("\n\nUsing class loader with these URLs:").nl();
      int i = 1;
      for (URL url : classLoader.getURLs()) {
        info.log("URL in use (#" + i + "): ").log(url.toString()).nl();
      }
      KnowledgeBuilderConfiguration configuration = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(new Properties(), classLoader);
      KnowledgeBase existingKnowledge = createKnowledgeBaseFromDependencies(classLoader, dependencyArtifacts);
      return KnowledgeBuilderFactory.newKnowledgeBuilder(existingKnowledge, configuration);
    }
    catch (DependencyResolutionRequiredException e) {
      throw new MojoFailureException("Internal error: declared resolution of compile-scoped dependencies, but got exception!", e);
    }
    catch (MalformedURLException e) {
      throw new MojoFailureException("Got malformed URL for compile classpath element.", e);
    }
    catch (IOException e) {
      throw new MojoFailureException("Error while creating drools KnowledgeBuilder.", e);
    }
  }

  private KnowledgeBase createKnowledgeBaseFromDependencies(URLClassLoader classLoader, Set<Artifact> dependencyArtifacts) throws MojoFailureException {
    List<Artifact> compileArtifacts = filterArtifacts(ArtifactPredicate.DROOLS_KNOWLEDGE_MODULE_FOR_COMPILATION, dependencyArtifacts);
    final KnowledgeBaseConfiguration configuration = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null, classLoader);
    final KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(configuration);
    for (Artifact droolCompileArtifact : compileArtifacts) {
      addDroolsArtifact(knowledgeBase, droolCompileArtifact);
    }
    return knowledgeBase;
  }

  private void addDroolsArtifact(KnowledgeBase knowledgeBase, Artifact compileArtifact) throws MojoFailureException {
    final Collection<KnowledgePackage> knowledgePackages = loadKnowledgePackages(compileArtifact);
    knowledgeBase.addKnowledgePackages(knowledgePackages);
    info.log("Loaded drools dependency " + coordinatesOf(compileArtifact)).nl();
    info.log("  Contains packages:").nl();
    KnowledgePackageFormatter.dumpKnowledgePackages(info, knowledgePackages);
  }

  private Collection<KnowledgePackage> loadKnowledgePackages(Artifact compileArtifact) throws MojoFailureException {
    KnowledgePackageFile knowledgePackageFile = new KnowledgePackageFile(compileArtifact.getFile());
    final Collection<KnowledgePackage> knowledgePackages;
    try {
      knowledgePackages = knowledgePackageFile.getKnowledgePackages();
    }
    catch (IOException e) {
      throw new MojoFailureException("Unable to load compile-scoped dependency " + coordinatesOf(compileArtifact), e);
    }
    catch (ClassNotFoundException e) {
      throw new MojoFailureException("Unable to load compile-scoped dependency " + coordinatesOf(compileArtifact), e);
    }
    return knowledgePackages;
  }

  private String coordinatesOf(Artifact artifact) {
    return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getType() + ":" + artifact.getVersion();
  }

  private URLClassLoader createCompileClassLoader(Set<Artifact> dependencyArtifacts) throws DependencyResolutionRequiredException, IOException {
    List<Artifact> compileArtifacts = filterArtifacts(ArtifactPredicate.JAR_FOR_COMPILATION, dependencyArtifacts);

    ArrayList<URL> classpathUrls = new ArrayList<URL>();
    for (Artifact compileArtifact : compileArtifacts) {
      URL classpathElementUrl = compileArtifact.getFile().toURI().toURL();
      classpathUrls.add(classpathElementUrl);
    }
    final URL[] urls = classpathUrls.toArray(new URL[classpathUrls.size()]);
    info.log("Passing URLs to new URLClassLoader instance: ").log(Arrays.format(urls)).nl();
    URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader()) {
      @Override
      public Class<?> loadClass(String name) throws ClassNotFoundException {
        info.log("Loading class '" + name + "'").nl();
        final Class<?> loadedClass = super.loadClass(name);
        info.log("Loading class '" + name + "': loaded class [" + loadedClass + "]").nl();
        return loadedClass;
      }

      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        info.log("Finding class '" + name + "'").nl();
        final Class<?> foundClass = super.findClass(name);
        info.log("Finding class '" + name + "': found class [" + foundClass + "]").nl();
        return foundClass;
      }
    };

    info.log("Adding classpath URLs to classloader:").nl();
    for (URL classpathUrl : classpathUrls) {
      info.log("   | " + classpathUrl).nl();
    }
    info.log("   #").nl();
    return classLoader;
  }

  private List<Artifact> filterArtifacts(ArtifactPredicate keepPredicate, Set<Artifact> unfilteredArtifacts) {

    int i = 1;
    List<Artifact> filteredArtifacts = new ArrayList<Artifact>();
    for (Artifact artifact : unfilteredArtifacts) {
      if (keepPredicate.isTrueFor(artifact)) {
        filteredArtifacts.add(artifact);
      }
      dumpArtifactInfo(i, artifact);
      i++;
    }
    return filteredArtifacts;
  }

  private void dumpArtifactInfo(int i, Artifact artifact) {
    debug.log("Dependency Artifact #" + i + ": Id=" + artifact.getId()).nl();
    debug.log("Dependency Artifact #" + i + ": GroupId=" + artifact.getGroupId()).nl();
    debug.log("Dependency Artifact #" + i + ": ArtifactId=" + artifact.getArtifactId()).nl();
    debug.log("Dependency Artifact #" + i + ": Type=" + artifact.getType()).nl();
    debug.log("Dependency Artifact #" + i + ": Classifier=" + artifact.getClassifier()).nl();
    debug.log("Dependency Artifact #" + i + ": Scope=" + artifact.getScope()).nl();

    debug.log("Dependency Artifact #" + i + ": BaseVersion=" + artifact.getBaseVersion()).nl();
    debug.log("Dependency Artifact #" + i + ": Version=" + artifact.getVersion()).nl();
    debug.log("Dependency Artifact #" + i + ": AvailableVersions=" + artifact.getAvailableVersions()).nl();
    final File artifactFile = artifact.getFile();
    if (artifactFile != null) {
      debug.log("Dependency Artifact #" + i + ": File=" + artifactFile.getAbsolutePath()).nl();
    }
    else {
      debug.log("Dependency Artifact #" + i + ": File={null}").nl();
    }
  }
}