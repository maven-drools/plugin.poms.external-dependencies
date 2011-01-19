/*
 * Copyright (c) 2009-2011 Ansgar Konermann <konermann@itikko.net>
 *
 * This file is part of the Maven 3 Drools Plugin.
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
package net.itikko.maven.learningtests;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.type.FactType;
import org.drools.io.impl.ReaderResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;

@Test
public class DroolsPackagingTest {

  private static final String RULE_BASE_PATH = "src/test/rules";
  private byte[] dataTypesKnowledgePackages;
  private FactType personType;

  @BeforeMethod
  public void packageDataTypes() throws Exception {
    Collection<KnowledgePackage> dataTypesKnowledgeBase = packageRules(newKnowledgeBuilder(), "datatypes/person.drl");
    dataTypesKnowledgePackages = DroolsStreamUtils.streamOut(dataTypesKnowledgeBase, true);
  }

  private static KnowledgeBuilder newKnowledgeBuilder() {
    final KnowledgeBuilderConfiguration configuration = configureDumpDirectory();
    return KnowledgeBuilderFactory.newKnowledgeBuilder(configuration);
  }

  private static KnowledgeBuilderConfiguration configureDumpDirectory() {
    Properties properties = new Properties();
    properties.setProperty("drools.dump.dir", "src/generated");
    return KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(properties, null);
  }

  @Test
  public void test_can_reuse_existing_binary_package() throws Exception {
    @SuppressWarnings("unchecked")
    Collection<KnowledgePackage> dataTypesKnowledgePackages =
        (Collection<KnowledgePackage>) DroolsStreamUtils.streamIn(this.dataTypesKnowledgePackages, true);

    final KnowledgeBase existingKnowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
    existingKnowledgeBase.addKnowledgePackages(dataTypesKnowledgePackages);

    final KnowledgeBuilder builderWithDataTypesPackage =
        KnowledgeBuilderFactory.newKnowledgeBuilder(existingKnowledgeBase, configureDumpDirectory());
    final Collection<KnowledgePackage> assessmentRulesPackage = packageRules(builderWithDataTypesPackage, "risk-assessment/check-age.drl");
    existingKnowledgeBase.addKnowledgePackages(assessmentRulesPackage);

    final StatefulKnowledgeSession session = existingKnowledgeBase.newStatefulKnowledgeSession();
    session.insert(createPersonWithAge(existingKnowledgeBase, 16));
    session.fireAllRules();
    final Collection<FactHandle> factHandles = session.getFactHandles();
    assertThat(factHandles.contains("VIOLATION: age < 18"));
  }

  private Object createPersonWithAge(KnowledgeBase kbase, int age) throws IllegalAccessException, InstantiationException {
    personType = kbase.getFactType("rules.datatypes.person", "Person");
    final Object person = personType.newInstance();
    personType.set(person, "age", age);
    return person;
  }

  private Collection<KnowledgePackage> packageRules(KnowledgeBuilder knowledgeBuilder, String... ruleFiles) throws Exception {
    for (String ruleFileName : ruleFiles) {
      File ruleFile = new File(RULE_BASE_PATH + File.separatorChar + ruleFileName);
      knowledgeBuilder.add(new ReaderResource(new FileReader(ruleFile)), ResourceType.DRL);
      if (knowledgeBuilder.hasErrors()) {
        handlerBuilderErrors(knowledgeBuilder, ruleFileName);
      }
    }
    return knowledgeBuilder.getKnowledgePackages();
  }

  private void handlerBuilderErrors(KnowledgeBuilder builder, String ruleFileName) {
    throw new RuntimeException("Error compiling file " + ruleFileName + ": " + builder.getErrors().toString());
  }
}
