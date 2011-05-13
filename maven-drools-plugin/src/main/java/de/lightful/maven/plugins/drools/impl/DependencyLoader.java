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

import de.lightful.maven.plugins.drools.impl.predicates.ArtifactPredicate;
import de.lightful.maven.plugins.drools.knowledgeio.KnowledgePackageFile;
import de.lightful.maven.plugins.drools.knowledgeio.KnowledgePackageFormatter;
import de.lightful.maven.plugins.drools.knowledgeio.LogStream;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.definition.KnowledgePackage;
import org.fest.util.Arrays;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

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

  private LogStream<?> debug;
  private LogStream<?> info;
  private LogStream<?> warn;
  private LogStream<?> error;

  public KnowledgeBuilder createKnowledgeBuilderForRuleCompilation(MavenProject project, Set<Artifact> dependencyArtifacts, RepositorySystemSession repositorySession, RepositorySystem repositorySystem, List<RemoteRepository> remoteProjectRepositories) throws MojoFailureException {
    try {
      URLClassLoader classLoader = createCompileClassLoader(dependencyArtifacts);
      info.write("\n\nUsing class loader with these URLs:").nl();
      int i = 1;
      for (URL url : classLoader.getURLs()) {
        info.write("URL in use (#" + i + "): ").write(url.toString()).nl();
      }
      KnowledgeBuilderConfiguration configuration = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(new Properties(), classLoader);
      KnowledgeBase existingKnowledge = createKnowledgeBaseFromDependencies(classLoader, dependencyArtifacts, project, repositorySystem,
                                                                            repositorySession, remoteProjectRepositories);
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

  private KnowledgeBase createKnowledgeBaseFromDependencies(URLClassLoader classLoader, Set<Artifact> dependencyArtifacts, MavenProject project, RepositorySystem repositorySystem, RepositorySystemSession repositorySession, List<RemoteRepository> remoteProjectRepositories) throws MojoFailureException {
    List<Artifact> compileArtifacts = filterArtifacts(ArtifactPredicate.DROOLS_KNOWLEDGE_MODULE_FOR_COMPILATION, dependencyArtifacts);
    final KnowledgeBaseConfiguration configuration = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null, classLoader);
    final KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(configuration);

    // Example code to experiment with Artifact Resolution:
    ArtifactRequest request = new ArtifactRequest();
    final DefaultArtifact artifactToResolve = new DefaultArtifact("org.apache.maven:maven-model:3.0");
    request.setArtifact(artifactToResolve);
    request.setRepositories(remoteProjectRepositories);
    info.write("Resolving artifact " + artifactToResolve + " from " + remoteProjectRepositories);
    ArtifactResult result;
    try {
      result = repositorySystem.resolveArtifact(repositorySession, request);
      info.write("Resolved artifact " + artifactToResolve + " to " + result.getArtifact().getFile() + " from " + result.getRepository());
    }
    catch (ArtifactResolutionException e) {
      warn.write(e.getMessage()).nl();
    }

    for (Artifact dependencyArtifact : dependencyArtifacts) {
      // Example code to experiment with Dependency Resolution:
      CollectRequest collectRequest = new CollectRequest();
      Dependency dependency = convertToAetherDependency(dependencyArtifact);
      collectRequest.setRoot(dependency);
      collectRequest.setRepositories(remoteProjectRepositories);
      info.write("Resolving artifact " + artifactToResolve + " from " + remoteProjectRepositories);
      CollectResult collectResult;
      try {
        collectResult = repositorySystem.collectDependencies(repositorySession, collectRequest);
        dumpDependencyTree(collectResult);
      }
      catch (DependencyCollectionException e) {
        error.write(e.getMessage()).nl();
      }
    }

    for (Artifact droolCompileArtifact : compileArtifacts) {
      addDroolsArtifact(knowledgeBase, droolCompileArtifact);
    }
    return knowledgeBase;
  }

  private Dependency convertToAetherDependency(Artifact mavenArtifact) {
    org.sonatype.aether.artifact.Artifact artifact = new DefaultArtifact(
        mavenArtifact.getGroupId(), mavenArtifact.getArtifactId(), mavenArtifact.getClassifier(),
        mavenArtifact.getArtifactHandler().getExtension(), mavenArtifact.getVersion()
    );
    return new Dependency(artifact, mavenArtifact.getScope());
  }

  private void dumpDependencyTree(CollectResult collectResult) {
    info.write("Resolved dependencies of " + collectResult.getRoot().toString() + ".").nl();
  }

  private void addDroolsArtifact(KnowledgeBase knowledgeBase, Artifact compileArtifact) throws MojoFailureException {
    final Collection<KnowledgePackage> knowledgePackages = loadKnowledgePackages(compileArtifact);
    knowledgeBase.addKnowledgePackages(knowledgePackages);
    info.write("Loaded drools dependency " + coordinatesOf(compileArtifact)).nl();
    info.write("  Contains packages:").nl();
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
    info.write("Passing URLs to new URLClassLoader instance: ").write(Arrays.format(urls)).nl();
    URLClassLoader classLoader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader()) {
      @Override
      public Class<?> loadClass(String name) throws ClassNotFoundException {
        info.write("Loading class '" + name + "'").nl();
        final Class<?> loadedClass = super.loadClass(name);
        info.write("Loading class '" + name + "': loaded class [" + loadedClass + "]").nl();
        return loadedClass;
      }

      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        info.write("Finding class '" + name + "'").nl();
        final Class<?> foundClass = super.findClass(name);
        info.write("Finding class '" + name + "': found class [" + foundClass + "]").nl();
        return foundClass;
      }
    };

    info.write("Adding classpath URLs to classloader:").nl();
    for (URL classpathUrl : classpathUrls) {
      info.write("   | " + classpathUrl).nl();
    }
    info.write("   #").nl();
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
    debug.write("Dependency Artifact #" + i + ": Id=" + artifact.getId()).nl();
    debug.write("Dependency Artifact #" + i + ": GroupId=" + artifact.getGroupId()).nl();
    debug.write("Dependency Artifact #" + i + ": ArtifactId=" + artifact.getArtifactId()).nl();
    debug.write("Dependency Artifact #" + i + ": Type=" + artifact.getType()).nl();
    debug.write("Dependency Artifact #" + i + ": Classifier=" + artifact.getClassifier()).nl();
    debug.write("Dependency Artifact #" + i + ": Scope=" + artifact.getScope()).nl();

    debug.write("Dependency Artifact #" + i + ": BaseVersion=" + artifact.getBaseVersion()).nl();
    debug.write("Dependency Artifact #" + i + ": Version=" + artifact.getVersion()).nl();
    debug.write("Dependency Artifact #" + i + ": AvailableVersions=" + artifact.getAvailableVersions()).nl();
    final File artifactFile = artifact.getFile();
    if (artifactFile != null) {
      debug.write("Dependency Artifact #" + i + ": File=" + artifactFile.getAbsolutePath()).nl();
    }
    else {
      debug.write("Dependency Artifact #" + i + ": File={null}").nl();
    }
  }
}