package com.webcohesion.enunciate.modules.jackson1.api.impl;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.modules.jackson1.model.*;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonArrayType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonClassType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonMapType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonType;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.LinkedList;

/**
 * @author Ryan Heaton
 */
public class ExampleImpl implements Example {

  private final ObjectTypeDefinition type;

  public ExampleImpl(ObjectTypeDefinition type) {
    this.type = type;
  }

  @Override
  public String getLang() {
    return "js";
  }

  @Override
  public String getBody() {
    ObjectNode node = JsonNodeFactory.instance.objectNode();

    build(node, this.type, new LinkedList<String>());

    ObjectMapper mapper = new ObjectMapper().enable(SerializationConfig.Feature.INDENT_OUTPUT);
    try {
      return mapper.writeValueAsString(node);
    }
    catch (JsonProcessingException e) {
      throw new EnunciateException(e);
    }
    catch (IOException e) {
      throw new EnunciateException(e);
    }
  }

  private void build(ObjectNode node, ObjectTypeDefinition type, LinkedList<String> contextStack) {
    for (Member member : type.getMembers()) {
      for (Member choice : member.getChoices()) {
        JsonType jsonType = choice.getJsonType();

        String example = null;
        DocumentationExample documentationExample = choice.getAnnotation(DocumentationExample.class);
        if (documentationExample != null) {
          if (documentationExample.exclude()) {
            continue;
          }
          else if (!"##default".equals(documentationExample.value())) {
            example = documentationExample.value();
          }
        }

        node.put(choice.getName(), exampleNode(jsonType, example, contextStack));
      }
    }

    JsonType supertype = type.getSupertype();
    if (supertype instanceof JsonClassType && ((JsonClassType)supertype).getTypeDefinition() instanceof ObjectTypeDefinition) {
      build(node, (ObjectTypeDefinition) ((JsonClassType) supertype).getTypeDefinition(), contextStack);
    }

    if (type.getWildcardMember() != null) {
      node.put("extension1", "...");
      node.put("extension2", "...");
    }

  }

  private JsonNode exampleNode(JsonType jsonType, String specifiedExample, LinkedList<String> contextStack) {
    if (jsonType instanceof JsonClassType) {
      TypeDefinition typeDefinition = ((JsonClassType) jsonType).getTypeDefinition();
      if (typeDefinition instanceof ObjectTypeDefinition) {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        if (!contextStack.contains(typeDefinition.getQualifiedName().toString())) {
          contextStack.push(typeDefinition.getQualifiedName().toString());
          try {
            build(objectNode, (ObjectTypeDefinition) typeDefinition, contextStack);
          }
          finally {
            contextStack.pop();
          }
        }
        return objectNode;
      }
      else if (typeDefinition instanceof EnumTypeDefinition) {
        String example = "???";

        if (specifiedExample != null) {
          example = specifiedExample;
        }
        else if (((EnumTypeDefinition) typeDefinition).getEnumValues().size() > 0) {
          example = ((EnumTypeDefinition) typeDefinition).getEnumValues().values().iterator().next();
        }

        return JsonNodeFactory.instance.textNode(example);
      }
      else {
        return exampleNode(((SimpleTypeDefinition) typeDefinition).getBaseType(), specifiedExample, contextStack);
      }
    }
    else if (jsonType instanceof JsonMapType) {
      ObjectNode mapNode = JsonNodeFactory.instance.objectNode();
      mapNode.put("property1", "...");
      mapNode.put("property2", "...");
      return mapNode;
    }
    else if (jsonType.isArray()) {
      ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
      JsonNode componentNode = exampleNode(((JsonArrayType) jsonType).getComponentType(), specifiedExample, contextStack);
      arrayNode.add(componentNode);
      arrayNode.add(componentNode);
      return arrayNode;
    }
    else if (jsonType.isNumber()) {
      int example = 12345;
      if (specifiedExample != null) {
        try {
          example = Integer.parseInt(specifiedExample);
        }
        catch (NumberFormatException e) {
          this.type.getContext().getContext().getLogger().warn("\"%s\" was provided as a documentation example, but it is not a valid JSON number, so it will be ignored.");
        }
      }
      return JsonNodeFactory.instance.numberNode(example);
    }
    else if (jsonType.isBoolean()) {
      boolean example = !"false".equals(specifiedExample);
      return JsonNodeFactory.instance.booleanNode(example);
    }
    else if (jsonType.isString()) {
      String example = specifiedExample;
      if (example == null) {
        example = "...";
      }
      return JsonNodeFactory.instance.textNode(example);
    }
    else {
      return JsonNodeFactory.instance.objectNode();
    }
  }
}