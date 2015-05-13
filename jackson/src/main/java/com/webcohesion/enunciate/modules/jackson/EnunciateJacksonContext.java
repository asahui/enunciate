package com.webcohesion.enunciate.modules.jackson;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnum;
import com.webcohesion.enunciate.module.EnunciateModuleContext;
import com.webcohesion.enunciate.modules.jackson.model.*;
import com.webcohesion.enunciate.modules.jackson.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jackson.model.types.KnownJsonType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson.model.util.JacksonUtil;
import com.webcohesion.enunciate.modules.jackson.model.util.MapType;

import javax.activation.DataHandler;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJacksonContext extends EnunciateModuleContext {

  private int prefixIndex = 0;
  private final Map<String, JsonType> knownTypes;
  private final Map<String, TypeDefinition> typeDefinitions;
  private final Map<String, ElementDeclaration> elementDeclarations;
  private final Map<String, String> namespacePrefixes;
  private final Map<String, SchemaInfo> schemas;
  private final Map<String, Map<String, XmlSchemaType>> packageSpecifiedTypes;

  public EnunciateJacksonContext(EnunciateContext context) {
    super(context);
    this.knownTypes = loadKnownTypes();
    this.typeDefinitions = new HashMap<String, TypeDefinition>();
    this.elementDeclarations = new HashMap<String, ElementDeclaration>();
    this.namespacePrefixes = new HashMap<String, String>();
    this.schemas = new HashMap<String, SchemaInfo>();
    this.packageSpecifiedTypes = new HashMap<String, Map<String, XmlSchemaType>>();
  }

  public EnunciateContext getContext() {
    return context;
  }

  public JsonType getKnownType(Element declaration) {
    if (declaration instanceof TypeElement) {
      return this.knownTypes.get(((TypeElement) declaration).getQualifiedName().toString());
    }
    return null;
  }

  public TypeDefinition findTypeDefinition(Element declaration) {
    if (declaration instanceof TypeElement) {
      return this.typeDefinitions.get(((TypeElement) declaration).getQualifiedName().toString());
    }
    return null;
  }

  public ElementDeclaration findElementDeclaration(Element declaredElement) {
    if (declaredElement instanceof TypeElement) {
      return this.elementDeclarations.get(((TypeElement) declaredElement).getQualifiedName().toString());
    }
    else if (declaredElement instanceof ExecutableElement) {
      return this.elementDeclarations.get(declaredElement.toString());
    }
    return null;
  }

  public Map<String, XmlSchemaType> getPackageSpecifiedTypes(String packageName) {
    return this.packageSpecifiedTypes.get(packageName);
  }

  public void setPackageSpecifiedTypes(String packageName, Map<String, XmlSchemaType> explicitTypes) {
    this.packageSpecifiedTypes.put(packageName, explicitTypes);
  }

  public Map<String, String> getNamespacePrefixes() {
    return namespacePrefixes;
  }

  public void addNamespacePrefix(String namespace, String prefix) {
    this.namespacePrefixes.put(namespace, prefix);
  }

  public Map<String, SchemaInfo> getSchemas() {
    return schemas;
  }

  protected Map<String, JsonType> loadKnownTypes() {
    HashMap<String, JsonType> knownTypes = new HashMap<String, JsonType>();

    knownTypes.put(Boolean.class.getName(), KnownJsonType.BOOLEAN);
    knownTypes.put(Byte.class.getName(), KnownJsonType.BYTE);
    knownTypes.put(Character.class.getName(), KnownJsonType.UNSIGNED_SHORT);
    knownTypes.put(Double.class.getName(), KnownJsonType.DOUBLE);
    knownTypes.put(Float.class.getName(), KnownJsonType.FLOAT);
    knownTypes.put(Integer.class.getName(), KnownJsonType.INT);
    knownTypes.put(Long.class.getName(), KnownJsonType.LONG);
    knownTypes.put(Short.class.getName(), KnownJsonType.SHORT);
    knownTypes.put(Boolean.TYPE.getName(), KnownJsonType.BOOLEAN);
    knownTypes.put(Byte.TYPE.getName(), KnownJsonType.BYTE);
    knownTypes.put(Double.TYPE.getName(), KnownJsonType.DOUBLE);
    knownTypes.put(Float.TYPE.getName(), KnownJsonType.FLOAT);
    knownTypes.put(Integer.TYPE.getName(), KnownJsonType.INT);
    knownTypes.put(Long.TYPE.getName(), KnownJsonType.LONG);
    knownTypes.put(Short.TYPE.getName(), KnownJsonType.SHORT);
    knownTypes.put(Character.TYPE.getName(), KnownJsonType.UNSIGNED_SHORT);
    knownTypes.put(String.class.getName(), KnownJsonType.STRING);
    knownTypes.put(java.math.BigInteger.class.getName(), KnownJsonType.INTEGER);
    knownTypes.put(java.math.BigDecimal.class.getName(), KnownJsonType.DECIMAL);
    knownTypes.put(java.util.Calendar.class.getName(), KnownJsonType.DATE_TIME);
    knownTypes.put(java.util.Date.class.getName(), KnownJsonType.DATE_TIME);
    knownTypes.put(Timestamp.class.getName(), KnownJsonType.DATE_TIME);
    knownTypes.put(javax.xml.namespace.QName.class.getName(), KnownJsonType.QNAME);
    knownTypes.put(java.net.URI.class.getName(), KnownJsonType.STRING);
    knownTypes.put(javax.xml.datatype.Duration.class.getName(), KnownJsonType.DURATION);
    knownTypes.put(java.lang.Object.class.getName(), KnownJsonType.ANY_TYPE);
    knownTypes.put(byte[].class.getName(), KnownJsonType.BASE64_BINARY);
    knownTypes.put(java.awt.Image.class.getName(), KnownJsonType.BASE64_BINARY);
    knownTypes.put(DataHandler.class.getName(), KnownJsonType.BASE64_BINARY);
    knownTypes.put(javax.xml.transform.Source.class.getName(), KnownJsonType.BASE64_BINARY);
    knownTypes.put(java.util.UUID.class.getName(), KnownJsonType.STRING);
    knownTypes.put(XMLGregorianCalendar.class.getName(), KnownJsonType.DATE_TIME); //JAXB spec says it maps to anySimpleType, but we can just assume dateTime...
    knownTypes.put(GregorianCalendar.class.getName(), KnownJsonType.DATE_TIME);

    return knownTypes;
  }

  /**
   * Find the type definition for a class given the class's declaration.
   *
   * @param declaration The declaration.
   * @return The type definition.
   */
  protected TypeDefinition createTypeDefinition(TypeElement declaration) {
    if (declaration.getKind() == ElementKind.INTERFACE) {
      if (declaration.getAnnotation(javax.xml.bind.annotation.XmlType.class) != null) {
        throw new EnunciateException(declaration.getQualifiedName() + ": an interface must not be annotated with @XmlType.");
      }
    }

    declaration = narrowToAdaptingType(declaration);

    if (isEnumType(declaration)) {
      if (declaration.getAnnotation(XmlQNameEnum.class) != null) {
        return new QNameEnumTypeDefinition(declaration, this);
      }
      else {
        return new EnumTypeDefinition(declaration, this);
      }
    }
    else {
      ComplexTypeDefinition typeDef = new ComplexTypeDefinition(declaration, this);
      if ((typeDef.getValue() != null) && (hasNeitherAttributesNorElements(typeDef))) {
        return new SimpleTypeDefinition(typeDef);
      }
      else {
        return typeDef;
      }
    }
  }

  /**
   * Narrows the existing declaration down to its adapting declaration, if it's being adapted. Otherwise, the original declaration will be returned.
   *
   * @param declaration The declaration to narrow.
   * @return The narrowed declaration.
   */
  protected TypeElement narrowToAdaptingType(TypeElement declaration) {
    AdapterType adapterType = JacksonUtil.findAdapterType(declaration, this);
    if (adapterType != null) {
      TypeMirror adaptingType = adapterType.getAdaptingType();
      if (adaptingType.getKind() != TypeKind.DECLARED) {
        return declaration;
      }
      else {
        TypeElement adaptingDeclaration = (TypeElement) ((DeclaredType) adaptingType).asElement();
        if (adaptingDeclaration == null) {
          throw new EnunciateException(String.format("Class %s is being adapted by a type (%s) that doesn't seem to be on the classpath.", declaration.getQualifiedName(), adaptingType));
        }
        return adaptingDeclaration;
      }
    }
    return declaration;
  }

  /**
   * A quick check to see if a declaration defines a enum schema type.
   *
   * @param declaration The declaration to check.
   * @return the value of the check.
   */
  protected boolean isEnumType(TypeElement declaration) {
    return declaration.getKind() == ElementKind.ENUM;
  }

  /**
   * Whether the specified type definition has neither attributes nor elements.
   *
   * @param typeDef The type def.
   * @return Whether the specified type definition has neither attributes nor elements.
   */
  protected boolean hasNeitherAttributesNorElements(TypeDefinition typeDef) {
    boolean none = (typeDef.getAttributes().isEmpty()) && (typeDef.getElements().isEmpty());
    TypeElement superDeclaration = (TypeElement) ((DeclaredType)typeDef.getSuperclass()).asElement();
    if (!Object.class.getName().equals(superDeclaration.getQualifiedName().toString())) {
      none &= hasNeitherAttributesNorElements(new ComplexTypeDefinition(superDeclaration, this));
    }
    return none;
  }

  /**
   * Add a namespace.
   *
   * @param namespace The namespace to add.
   * @return The prefix for the namespace.
   */
  public String addNamespace(String namespace) {
    String prefix = this.namespacePrefixes.get(namespace);
    if (prefix == null) {
      prefix = generatePrefix(namespace);
      this.namespacePrefixes.put(namespace, prefix);
    }
    return prefix;
  }

  /**
   * Generate a prefix for the given namespace.
   *
   * @param namespace The namespace for which to generate a prefix.
   * @return The prefix that was generated.
   */
  protected String generatePrefix(String namespace) {
    String prefix = "ns" + (prefixIndex++);
    while (this.namespacePrefixes.values().contains(prefix)) {
      prefix = "ns" + (prefixIndex++);
    }
    return prefix;
  }

  /**
   * Add a type definition to the model.
   *
   * @param typeDef The type definition to add to the model.
   */
  public void add(TypeDefinition typeDef) {
    add(typeDef, new LinkedList<Element>());
  }

  /**
   * Adds a schema declaration to the model.
   *
   * @param schema The schema declaration to add to the model.
   */
  public void add(Schema schema) {
    add(schema, new LinkedList<Element>());
  }

  /**
   * Add a root element to the model.
   *
   * @param rootElement The root element to add.
   */
  public void add(RootElementDeclaration rootElement) {
    if (findElementDeclaration(rootElement) == null) {
      this.elementDeclarations.put(rootElement.getQualifiedName().toString(), rootElement);
      debug("Added %s as a root XML element.", rootElement.getQualifiedName());
      add(rootElement.getSchema());

      String namespace = rootElement.getNamespace();
      String prefix = addNamespace(namespace);

      SchemaInfo schemaInfo = schemas.get(namespace);
      if (schemaInfo == null) {
        schemaInfo = new SchemaInfo(this);
        schemaInfo.setId(prefix);
        schemaInfo.setNamespace(namespace);
        schemas.put(namespace, schemaInfo);
      }
      schemaInfo.getRootElements().add(rootElement);

      addReferencedTypeDefinitions(rootElement);
    }
  }

  /**
   * Add an XML registry.
   *
   * @param registry The registry to add.
   */
  public void add(Registry registry) {
    add(registry, new LinkedList<Element>());
  }

  protected void add(Registry registry, LinkedList<Element> stack) {
    add(registry.getSchema());

    String namespace = registry.getSchema().getNamespace();
    String prefix = addNamespace(namespace);

    SchemaInfo schemaInfo = schemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo(this);
      schemaInfo.setId(prefix);
      schemaInfo.setNamespace(namespace);
      schemas.put(namespace, schemaInfo);
    }
    schemaInfo.getRegistries().add(registry);
    debug("Added %s as an XML registry.", registry.getQualifiedName());

    stack.push(registry);
    try {
      addReferencedTypeDefinitions(registry, stack);
      for (LocalElementDeclaration led : registry.getLocalElementDeclarations()) {
        add(led, stack);
      }
    }
    finally {
      stack.pop();
    }
  }

  /**
   * Add the referenced type definitions for a registry..
   *
   * @param registry The registry.
   */
  protected void addReferencedTypeDefinitions(Registry registry, LinkedList<Element> stack) {
    addSeeAlsoTypeDefinitions(registry, stack);
    for (ExecutableElement methodDeclaration : registry.getInstanceFactoryMethods()) {
      stack.push(methodDeclaration);
      try {
        addReferencedTypeDefinitions(methodDeclaration.getReturnType(), stack);
      }
      finally {
        stack.pop();
      }
    }
  }

  protected void add(LocalElementDeclaration led, LinkedList<Element> stack) {
    String namespace = led.getNamespace();
    String prefix = addNamespace(namespace);

    SchemaInfo schemaInfo = schemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo(this);
      schemaInfo.setId(prefix);
      schemaInfo.setNamespace(namespace);
      schemas.put(namespace, schemaInfo);
    }
    schemaInfo.getLocalElementDeclarations().add(led);
    debug("Added %s as a local element declaration.", led.getSimpleName());
    addReferencedTypeDefinitions(led, stack);
  }

  /**
   * Adds the referenced type definitions for the specified local element declaration.
   *
   * @param led The local element declaration.
   */
  protected void addReferencedTypeDefinitions(LocalElementDeclaration led, LinkedList<Element> stack) {
    addSeeAlsoTypeDefinitions(led, stack);
    DecoratedTypeElement scope = led.getElementScope();
    if (!isKnownTypeDefinition(scope) && scope.getKind() == ElementKind.CLASS) {
      add(createTypeDefinition(scope), stack);
    }
    TypeElement typeDeclaration = led.getElementType();
    if (!isKnownTypeDefinition(typeDeclaration) && scope.getKind() == ElementKind.CLASS) {
      add(createTypeDefinition(typeDeclaration), stack);
    }
  }

  public boolean isKnownTypeDefinition(TypeElement el) {
    return findTypeDefinition(el) != null || isKnownType(el);
  }

  /**
   * Add any statically-referenced type definitions to the model.
   *
   * @param rootEl The root element.
   */
  protected void addReferencedTypeDefinitions(RootElementDeclaration rootEl) {
    TypeDefinition typeDefinition = rootEl.getTypeDefinition();
    if (typeDefinition != null) {
      add(typeDefinition);
    }
    else {
      //some root elements don't have a reference to their type definitions.
      add(createTypeDefinition(rootEl.getDelegate()));
    }
  }

  protected void add(Schema schema, LinkedList<Element> stack) {
    stack.add(schema);
    try {
      String namespace = schema.getNamespace();
      String prefix = addNamespace(namespace);
      this.namespacePrefixes.putAll(schema.getSpecifiedNamespacePrefixes());
      SchemaInfo schemaInfo = schemas.get(namespace);
      if (schemaInfo == null) {
        schemaInfo = new SchemaInfo(this);
        schemaInfo.setId(prefix);
        schemaInfo.setNamespace(namespace);
        schemas.put(namespace, schemaInfo);
      }

      if (schema.getElementFormDefault() != XmlNsForm.UNSET) {
        for (Schema pckg : schemaInfo.getPackages()) {
          if ((pckg.getElementFormDefault() != null) && (schema.getElementFormDefault() != pckg.getElementFormDefault())) {
            throw new EnunciateException(schema.getQualifiedName() + ": inconsistent elementFormDefault declarations: " + pckg.getQualifiedName());
          }
        }
      }

      if (schema.getAttributeFormDefault() != XmlNsForm.UNSET) {
        for (Schema pckg : schemaInfo.getPackages()) {
          if ((pckg.getAttributeFormDefault() != null) && (schema.getAttributeFormDefault() != pckg.getAttributeFormDefault())) {
            throw new EnunciateException(schema.getQualifiedName() + ": inconsistent attributeFormDefault declarations: " + pckg.getQualifiedName());
          }
        }
      }

      schemaInfo.getPackages().add(schema);
    }
    finally {
      stack.pop();
    }
  }

  protected void add(TypeDefinition typeDef, LinkedList<Element> stack) {
    //todo: do a 'known type' check before even creating the instance of 'TypeDefinition'
    if (findTypeDefinition(typeDef) == null && !isKnownType(typeDef)) {
      this.typeDefinitions.put(typeDef.getQualifiedName().toString(), typeDef);
      debug("Added %s as a type definition.", typeDef.getQualifiedName());

      if (typeDef.getAnnotation(XmlRootElement.class) != null && findElementDeclaration(typeDef) == null) {
        //if the type definition is a root element, we want to make sure it's added to the model.
        add(new RootElementDeclaration(typeDef.getDelegate(), typeDef, this));
      }

      typeDef.getReferencedFrom().addAll(stack);
      try {
        stack.push(typeDef);
        add(typeDef.getSchema(), stack);

        String namespace = typeDef.getNamespace();
        String prefix = addNamespace(namespace);

        SchemaInfo schemaInfo = this.schemas.get(namespace);
        if (schemaInfo == null) {
          schemaInfo = new SchemaInfo(this);
          schemaInfo.setId(prefix);
          schemaInfo.setNamespace(namespace);
          this.schemas.put(namespace, schemaInfo);
        }
        schemaInfo.getTypeDefinitions().add(typeDef);

        addSeeAlsoTypeDefinitions(typeDef, stack);

        for (com.webcohesion.enunciate.modules.jackson.model.Element element : typeDef.getElements()) {
          addReferencedTypeDefinitions(element, stack);

          ImplicitSchemaElement implicitElement = getImplicitElement(element);
          if (implicitElement != null) {
            String implicitNamespace = element.isWrapped() ? element.getWrapperNamespace() : element.getNamespace();
            SchemaInfo referencedSchemaInfo = schemas.get(implicitNamespace);
            if (referencedSchemaInfo == null) {
              referencedSchemaInfo = new SchemaInfo(this);
              referencedSchemaInfo.setId(addNamespace(implicitNamespace));
              referencedSchemaInfo.setNamespace(implicitNamespace);
              schemas.put(implicitNamespace, referencedSchemaInfo);
            }
            referencedSchemaInfo.getImplicitSchemaElements().add(implicitElement);
          }
        }

        for (Attribute attribute : typeDef.getAttributes()) {
          addReferencedTypeDefinitions(attribute, stack);
          ImplicitSchemaAttribute implicitAttribute = getImplicitAttribute(attribute);
          if (implicitAttribute != null) {
            String implicitAttributeNamespace = attribute.getNamespace();
            SchemaInfo referencedSchemaInfo = schemas.get(implicitAttributeNamespace);
            if (referencedSchemaInfo == null) {
              referencedSchemaInfo = new SchemaInfo(this);
              referencedSchemaInfo.setId(addNamespace(implicitAttributeNamespace));
              referencedSchemaInfo.setNamespace(implicitAttributeNamespace);
              schemas.put(implicitAttributeNamespace, referencedSchemaInfo);
            }
            referencedSchemaInfo.getImplicitSchemaAttributes().add(implicitAttribute);
          }
        }

        if (typeDef.getAnyAttributeQNameEnumRef() != null) {
          addReferencedTypeDefinitions(typeDef.getAnyAttributeQNameEnumRef(), stack);
        }

        Value value = typeDef.getValue();
        if (value != null) {
          addReferencedTypeDefinitions(value, stack);
        }

        TypeMirror superclass = typeDef.getSuperclass();
        if (!typeDef.isEnum() && superclass != null) {
          addReferencedTypeDefinitions(superclass, stack);
        }
      }
      finally {
        stack.pop();
      }
    }
  }

  protected void addReferencedTypeDefinitions(Accessor accessor, LinkedList<Element> stack) {
    addSeeAlsoTypeDefinitions(accessor, stack);
    TypeMirror enumRef = accessor.getQNameEnumRef();
    if (enumRef != null) {
      addReferencedTypeDefinitions(enumRef, stack);
    }
  }

  /**
   * Add the type definition(s) referenced by the given attribute.
   *
   * @param attribute The attribute.
   * @param stack The context stack.
   */
  protected void addReferencedTypeDefinitions(Attribute attribute, LinkedList<Element> stack) {
    addReferencedTypeDefinitions((Accessor) attribute, stack);
    addReferencedTypeDefinitions(attribute.isAdapted() ? attribute.getAdapterType() : attribute.getAccessorType(), stack);
  }

  /**
   * Add the type definition(s) referenced by the given value.
   *
   * @param value The value.
   * @param stack The context stack.
   */
  protected void addReferencedTypeDefinitions(Value value, LinkedList<Element> stack) {
    addReferencedTypeDefinitions((Accessor) value, stack);
    addReferencedTypeDefinitions(value.isAdapted() ? value.getAdapterType() : value.getAccessorType(), stack);
  }

  /**
   * Add the referenced type definitions for the specified element.
   *
   * @param element The element.
   * @param stack The context stack.
   */
  protected void addReferencedTypeDefinitions(com.webcohesion.enunciate.modules.jackson.model.Element element, LinkedList<Element> stack) {
    addReferencedTypeDefinitions((Accessor) element, stack);
    if (element instanceof ElementRef && element.isCollectionType()) {
      //special case for collections of element refs because the collection is lazy-loaded.
      addReferencedTypeDefinitions(element.getAccessorType(), stack);
    }
    else {
      for (com.webcohesion.enunciate.modules.jackson.model.Element choice : element.getChoices()) {
        addReferencedTypeDefinitions(choice.isAdapted() ? choice.getAdapterType() : choice.getAccessorType(), stack);
      }
    }
  }

  /**
   * Adds any referenced type definitions for the specified type mirror.
   *
   * @param type The type mirror.
   */
  protected void addReferencedTypeDefinitions(TypeMirror type, LinkedList<Element> stack) {
    type.accept(new ReferencedTypeDefinitionVisitor(), stack);
  }

  /**
   * Gets the implicit element for the specified element, or null if there is no implicit element.
   *
   * @param element The element.
   * @return The implicit element, or null if none.
   */
  protected ImplicitSchemaElement getImplicitElement(com.webcohesion.enunciate.modules.jackson.model.Element element) {
    if (!(element instanceof ElementRef)) {
      boolean qualified = element.getForm() == XmlNsForm.QUALIFIED;
      String typeNamespace = element.getTypeDefinition().getNamespace();
      typeNamespace = typeNamespace == null ? "" : typeNamespace;
      String elementNamespace = element.isWrapped() ? element.getWrapperNamespace() : element.getNamespace();
      elementNamespace = elementNamespace == null ? "" : elementNamespace;

      if ((!elementNamespace.equals(typeNamespace)) && (qualified || !"".equals(elementNamespace))) {
        return element.isWrapped() ? new ImplicitWrappedElementRef(element) : new ImplicitElementRef(element);
      }
    }

    return null;
  }

  /**
   * Gets the implicit attribute for the specified attribute, or null if there is no implicit attribute.
   *
   * @param attribute The attribute.
   * @return The implicit attribute, or null if none.
   */
  protected ImplicitSchemaAttribute getImplicitAttribute(Attribute attribute) {
    boolean qualified = attribute.getForm() == XmlNsForm.QUALIFIED;
    String typeNamespace = attribute.getTypeDefinition().getNamespace();
    typeNamespace = typeNamespace == null ? "" : typeNamespace;
    String attributeNamespace = attribute.getNamespace();
    attributeNamespace = attributeNamespace == null ? "" : attributeNamespace;

    if ((!attributeNamespace.equals(typeNamespace)) && (qualified || !"".equals(attributeNamespace))) {
      return new ImplicitAttributeRef(attribute);
    }
    else {
      return null;
    }
  }

  /**
   * Add any type definitions that are referenced using {@link javax.xml.bind.annotation.XmlSeeAlso}.
   *
   * @param declaration The declaration.
   */
  protected void addSeeAlsoTypeDefinitions(Element declaration, LinkedList<Element> stack) {
    XmlSeeAlso seeAlso = declaration.getAnnotation(XmlSeeAlso.class);
    if (seeAlso != null) {
      Elements elementUtils = getContext().getProcessingEnvironment().getElementUtils();
      Types typeUtils = getContext().getProcessingEnvironment().getTypeUtils();
      stack.push(elementUtils.getTypeElement(XmlSeeAlso.class.getName()));
      try {
        Class[] classes = seeAlso.value();
        for (Class clazz : classes) {
          addSeeAlsoReference(elementUtils.getTypeElement(clazz.getName()));
        }
      }
      catch (MirroredTypesException e) {
        List<? extends TypeMirror> mirrors = e.getTypeMirrors();
        for (TypeMirror mirror : mirrors) {
          Element element = typeUtils.asElement(mirror);
          if (element instanceof TypeElement) {
            addSeeAlsoReference((TypeElement) element);
          }
        }
      }
      finally {
        stack.pop();
      }
    }
  }

  /**
   * Add a "see also" reference.
   *
   * @param typeDeclaration The reference.
   */
  protected void addSeeAlsoReference(TypeElement typeDeclaration) {
    if (!isKnownTypeDefinition(typeDeclaration) && typeDeclaration.getAnnotation(XmlRegistry.class) == null) {
      add(createTypeDefinition(typeDeclaration));
    }
  }

  /**
   * Whether the specified type is a known type.
   *
   * @param typeDef The type def.
   * @return Whether the specified type is a known type.
   */
  protected boolean isKnownType(TypeElement typeDef) {
    return knownTypes.containsKey(typeDef.getQualifiedName().toString()) || ((DecoratedTypeMirror)typeDef.asType()).isInstanceOf(JAXBElement.class);
  }

  /**
   * Visitor for XML-referenced type definitions.
   */
  private class ReferencedTypeDefinitionVisitor extends SimpleTypeVisitor6<Void, LinkedList<Element>> {

    @Override
    public Void visitArray(ArrayType t, LinkedList<Element> stack) {
      return t.getComponentType().accept(this, stack);
    }

    @Override
    public Void visitDeclared(DeclaredType declaredType, LinkedList<Element> stack) {
      TypeElement declaration = (TypeElement) declaredType.asElement();
      if (declaration.getKind() == ElementKind.ENUM) {
        if (!isKnownTypeDefinition(declaration)) {
          add(createTypeDefinition(declaration));
        }
      }
      else if (declaredType instanceof AdapterType) {
        ((AdapterType) declaredType).getAdaptingType().accept(this, stack);
      }
      else if (MapType.findMapType(declaredType, EnunciateJacksonContext.this) == null) {
        String qualifiedName = declaration.getQualifiedName().toString();
        if (Object.class.getName().equals(qualifiedName)) {
          //skip base object; not a type definition.
          return null;
        }

        if (stack.contains(declaration)) {
          //we're already visiting this class...
          return null;
        }

        stack.push(declaration);
        try {
          if (!isKnownTypeDefinition(declaration) && !((DecoratedDeclaredType)declaredType).isCollection() && !((DecoratedDeclaredType)declaredType).isInstanceOf(JAXBElement.class)) {
            add(createTypeDefinition(declaration));
          }

          List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
          if (typeArgs != null) {
            for (TypeMirror typeArg : typeArgs) {
              typeArg.accept(this, stack);
            }
          }
        }
        finally {
          stack.pop();
        }
      }

      return null;
    }

    @Override
    public Void visitTypeVariable(TypeVariable t, LinkedList<Element> stack) {
      return t.getUpperBound().accept(this, stack);
    }

    @Override
    public Void visitWildcard(WildcardType t, LinkedList<Element> stack) {
      TypeMirror extendsBound = t.getExtendsBound();
      if (extendsBound != null) {
        extendsBound.accept(this, stack);
      }

      TypeMirror superBound = t.getSuperBound();
      if (superBound != null) {
        superBound.accept(this, stack);
      }

      return null;
    }

    @Override
    public Void visitUnknown(TypeMirror t, LinkedList<Element> stack) {
      return defaultAction(t, stack);
    }

  }
}