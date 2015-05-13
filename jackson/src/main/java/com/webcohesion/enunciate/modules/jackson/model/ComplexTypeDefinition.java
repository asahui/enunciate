/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webcohesion.enunciate.modules.jackson.model;

import com.webcohesion.enunciate.models.xml.ComplexContentType;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonTypeFactory;

import javax.lang.model.element.TypeElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A complex type definition.
 *
 * @author Ryan Heaton
 */
public class ComplexTypeDefinition extends SimpleTypeDefinition {

  public ComplexTypeDefinition(TypeElement delegate, EnunciateJacksonContext context) {
    super(delegate, context);
  }

  @Override
  public JsonType getBaseType() {
    JsonType baseType = super.getBaseType();

    if (baseType == null) {
      baseType = JsonTypeFactory.getJsonType(getSuperclass(), this.context);
    }

    return baseType;
  }

  /**
   * The compositor for this type definition.
   *
   * @return The compositor for this type definition.
   */
  public String getCompositorName() {
    //"all" isn't supported because the spec isn't clear on what to do when:
    // 1. A class with the "all" compositor is extended.
    // 2. an "element" content element has maxOccurs > 0
    //return getPropertyOrder() == null ? "all" : "sequence";
    return "sequence";
  }

  /**
   * The content type of this complex type definition.
   *
   * @return The content type of this complex type definition.
   */
  public ComplexContentType getContentType() {
    if (!getElements().isEmpty()) {
      if (isBaseObject()) {
        return ComplexContentType.IMPLIED;
      }
      else {
        return ComplexContentType.COMPLEX;
      }
    }
    else if (getBaseType().isObject()) {
      return ComplexContentType.SIMPLE;
    }
    else {
      return ComplexContentType.EMPTY;
    }
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean isComplex() {
    return getAnnotation(XmlJavaTypeAdapter.class) == null;
  }

  @Override
  public boolean isBaseObject() {
    TypeElement superDeclaration = (TypeElement) this.env.getTypeUtils().asElement(getSuperclass());
    return superDeclaration == null
      || Object.class.getName().equals(superDeclaration.getQualifiedName().toString())
      || isXmlTransient(superDeclaration);
  }

}