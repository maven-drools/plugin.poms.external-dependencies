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
import org.drools.rule.TypeDeclaration;

import java.util.Collection;
import java.util.Map;

public class KnowledgePackageFormatter {

  public static void dumpKnowledgePackages(LogStream logStream, Collection<KnowledgePackage> knowledgePackages) {
    for (KnowledgePackage knowledgePackage : knowledgePackages) {
      logStream.write("    ").write(knowledgePackage.getName()).nl();
      org.drools.rule.Package internalPackage = ((KnowledgePackageImp) knowledgePackage).pkg;
      for (Map.Entry<String, TypeDeclaration> typeDeclaration : internalPackage.getTypeDeclarations().entrySet()) {
        logStream.write("      type '").write(typeDeclaration.getKey()).write("': ").nl();
        for (FieldDefinition fieldDefinition : typeDeclaration.getValue().getTypeClassDef().getFieldsDefinitions()) {
          logStream
              .write("        field '").write(fieldDefinition.getName()).write("' is of type ").write(fieldDefinition.getTypeName()).nl();
        }
      }
    }
  }
}