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
package de.lightful.maven.plugins.drools.knowledgeio;

import org.drools.definition.KnowledgePackage;
import org.drools.definitions.impl.KnowledgePackageImp;
import org.drools.factmodel.FieldDefinition;
import org.drools.rule.*;

import java.util.Collection;
import java.util.Map;

public class KnowledgePackageFormatter {

  public static String dumpKnowledgePackages(Collection<KnowledgePackage> knowledgePackages) {
    StringBuilder builder = new StringBuilder(4000);
    for (KnowledgePackage knowledgePackage : knowledgePackages) {
      builder
          .append("    ")
          .append(knowledgePackage.getName());
      org.drools.rule.Package internalPackage = ((KnowledgePackageImp) knowledgePackage).pkg;
      for (Map.Entry<String, TypeDeclaration> typeDeclaration : internalPackage.getTypeDeclarations().entrySet()) {
        builder
            .append("      type '")
            .append(typeDeclaration.getKey()).append("': ");
        for (FieldDefinition fieldDefinition : typeDeclaration.getValue().getTypeClassDef().getFieldsDefinitions()) {
          builder
              .append("        field '")
              .append(fieldDefinition.getName())
              .append("' is of type ")
              .append(fieldDefinition.getTypeName());
        }
      }
    }
    return builder.toString();
  }
}