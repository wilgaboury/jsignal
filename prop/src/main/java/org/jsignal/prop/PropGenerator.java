package org.jsignal.prop;

import com.palantir.javapoet.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PropGenerator {
  private final static String GEN_COMPONENT_CLASS_SUFFIX = "PropComponent";
  private final static String GEN_HELPER_CLASS_SUFFIX = "PropHelper";
  private final static String BUILDER_CLASS_NAME = "Builder";
  private final static String BUILDER_REQUIRED_NAME_SUFFIX = "RequiredStep";
  private final static String BUILDER_ONEOF_NAME_SUFFIX = "OneofStep";
  private final static String BUILDER_FIELD_NAME = "result";
  private final static String BUILDER_TRANSITIVE_FIELD_NAME = "transitive";
  private final static String TO_BUILDER_VAR_NAME = "result";

  private final ProcessingEnvironment procEnv;

  public PropGenerator(ProcessingEnvironment procEnv) {
    this.procEnv = procEnv;
  }

  public boolean isComponentClass(TypeElement element) {
    return element.getAnnotation(GeneratePropComponent.class) != null;
  }

  public String genClassSimpleName(TypeElement element) {
    return element.getSimpleName() + (isComponentClass(element) ? GEN_COMPONENT_CLASS_SUFFIX : GEN_HELPER_CLASS_SUFFIX);
  }

  public ClassName genClassName(TypeElement element) {
    return ClassName.get(packageName(element), genClassSimpleName(element));
  }

  public static String requiredStepInterfaceName(Element field) {
    String fieldName = field.getSimpleName().toString();
    String cap = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    return BUILDER_CLASS_NAME + cap + BUILDER_REQUIRED_NAME_SUFFIX;
  }

  public static String oneofStepInterfaceName(String oneofKey) {
    String cap = oneofKey.substring(0, 1).toUpperCase() + oneofKey.substring(1);
    return BUILDER_CLASS_NAME + cap + BUILDER_ONEOF_NAME_SUFFIX;
  }

  public ClassName genClassInnerName(TypeElement element, String name) {
    return ClassName.get(packageName(element), genClassSimpleName(element), name);
  }

  public ClassName classInnerName(TypeElement element, String name) {
    return ClassName.get(packageName(element), element.getSimpleName().toString(), name);
  }

  public ClassName builderClassName(TypeElement element) {
    return genClassInnerName(element, BUILDER_CLASS_NAME);
  }

  public void generate(TypeElement element) {
    TypeElement transitiveElement = transitivePropsElement(element);

    TypeSpec.Builder genClassBuilder = TypeSpec.classBuilder(genClassSimpleName(element))
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT, Modifier.SEALED)
      .addPermittedSubclass(TypeName.get(element.asType()));

    MethodSpec.Builder onBuildMethod = MethodSpec.methodBuilder("onBuild");

    if (transitiveElement != null) {
      onBuildMethod.addParameter(ParameterSpec.builder(
            TypeName.get(transitiveElement.asType()),
            "transitive"
          )
          .build()
      );
    }
    genClassBuilder.addMethod(onBuildMethod.build());

    if (isComponentClass(element)) {
      genClassBuilder.addModifiers().superclass(ClassName.get("org.jsignal.ui", "Component"));
    }

    TypeSpec builderReturnType = generateBuilders(element, transitiveElement, genClassBuilder);

    genClassBuilder.addMethod(MethodSpec.methodBuilder("builder")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .returns(genClassInnerName(element, builderReturnType.name()))
      .addCode(
        """
          return new $T();
          """,
        builderClassName(element)
      )
      .build()
    );

    saveGeneratedClass(genClassBuilder.build(), element);
  }

  public static TypeElement transitivePropsElement(TypeElement element) {
    return (TypeElement) element.getEnclosedElements()
      .stream()
      .filter(enclosed -> enclosed.getKind() == ElementKind.CLASS)
      .filter(enclosed -> findAnnotation(enclosed, TransitiveProps.class).isPresent())
      .findFirst()
      .orElse(null);
  }

  public static List<? extends Element> propFields(TypeElement element) {
    return element.getEnclosedElements()
      .stream()
      .filter(enclosed -> enclosed.getKind() == ElementKind.FIELD)
      .filter(enclosed -> findAnnotation(enclosed, Prop.class).isPresent())
      .toList();
  }

  public TypeSpec generateBuilders(
    TypeElement element,
    TypeElement transitiveElement,
    TypeSpec.Builder genClassBuilder
  ) {
    List<TypeSpec> builderInterfaces = new ArrayList<>();

    Set<? extends Element> transitivePropFields = transitiveElement != null
      ? new LinkedHashSet<>(propFields(transitiveElement))
      : Collections.emptySet();

    var propFields = Stream.concat(propFields(element).stream(), transitivePropFields.stream()).toList();

    var requiredPropFields = new ArrayList<Element>();
    var oneofPropFieldsMap = new LinkedHashMap<String, ArrayList<Element>>();
    var optionalPropFields = new ArrayList<Element>();

    for (var propField : propFields) {
      Prop propAnnotation = propField.getAnnotation(Prop.class);

      if (propAnnotation.required()) {
        requiredPropFields.add(propField);
      } else if (!propAnnotation.oneofKey().isEmpty()) {
        oneofPropFieldsMap.computeIfAbsent(propAnnotation.oneofKey(), k -> new ArrayList<>()).add(propField);
      } else {
        optionalPropFields.add(propField);
      }
    }

    TypeSpec.Builder builderClassBuilder = TypeSpec.classBuilder(BUILDER_CLASS_NAME)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addField(FieldSpec.builder(TypeName.get(element.asType()), BUILDER_FIELD_NAME)
        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        .build()
      );

    MethodSpec.Builder builderClassConstructor = MethodSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .addCode(
        """
          this.$L = new $T();
          """,
        BUILDER_FIELD_NAME,
        ClassName.get(element)
      );

    if (transitiveElement != null) {
      builderClassBuilder.addField(FieldSpec.builder(
            TypeName.get(transitiveElement.asType()),
            BUILDER_TRANSITIVE_FIELD_NAME
          )
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
          .build()
      );
      builderClassConstructor.addCode(
        """
          this.$L = new $T();
          """,
        BUILDER_TRANSITIVE_FIELD_NAME,
        ClassName.get(transitiveElement)
      );
    }

    builderClassBuilder.addMethod(builderClassConstructor.build());

    MethodSpec.Builder toBuilderMethod = MethodSpec.methodBuilder("toBuilder")
      .addModifiers(Modifier.PUBLIC)
      .returns(genClassInnerName(element, BUILDER_CLASS_NAME))
      .addCode(
        """
          $T $L = new $T();
          $T that = ($T)this;
          """,
        builderClassName(element),
        TO_BUILDER_VAR_NAME,
        builderClassName(element),
        TypeName.get(element.asType()),
        TypeName.get(element.asType())
      );

    ClassName previousBuilderType = builderClassName(element);

    for (var oneofPropEntry : oneofPropFieldsMap.sequencedEntrySet().reversed()) {
      String interfaceName = oneofStepInterfaceName(oneofPropEntry.getKey());
      TypeSpec.Builder oneofStep = TypeSpec.interfaceBuilder(interfaceName)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

      for (var oneofPropField : oneofPropEntry.getValue()) {
        boolean isTransitive = transitivePropFields.contains(oneofPropField);
        addSetterMethods(false, isTransitive, oneofStep, oneofPropField, previousBuilderType, null);
        addSetterMethods(
          true,
          isTransitive,
          builderClassBuilder,
          oneofPropField,
          builderClassName(element),
          toBuilderMethod
        );
      }

      builderInterfaces.add(oneofStep.build());
      previousBuilderType = genClassInnerName(element, interfaceName);
    }

    for (var requiredPropField : requiredPropFields.reversed()) {
      String interfaceName = requiredStepInterfaceName(requiredPropField);
      TypeSpec.Builder requiredStep = TypeSpec.interfaceBuilder(interfaceName)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

      boolean isTransitive = transitivePropFields.contains(requiredPropField);
      addSetterMethods(false, isTransitive, requiredStep, requiredPropField, previousBuilderType, null);
      addSetterMethods(
        true,
        isTransitive,
        builderClassBuilder,
        requiredPropField,
        builderClassName(element),
        toBuilderMethod
      );

      builderInterfaces.add(requiredStep.build());
      previousBuilderType = genClassInnerName(element, interfaceName);
    }

    for (var optionalPropField : optionalPropFields) {
      boolean isTransitive = transitivePropFields.contains(optionalPropField);
      addSetterMethods(
        true,
        isTransitive,
        builderClassBuilder,
        optionalPropField,
        builderClassName(element),
        toBuilderMethod
      );
    }

    MethodSpec.Builder buildMethod = MethodSpec.methodBuilder("build")
      .addModifiers(Modifier.PUBLIC)
      .returns(TypeName.get(element.asType()));

    if (transitiveElement == null) {
      buildMethod.addCode(
        """
          (($T)$L).onBuild();
          return $L;
          """,
        genClassName(element),
        BUILDER_FIELD_NAME,
        BUILDER_FIELD_NAME
      );
    } else {
      buildMethod.addCode(
        """
          (($T)$L).onBuild($L);
          return $L;
          """,
        genClassName(element),
        BUILDER_FIELD_NAME,
        BUILDER_TRANSITIVE_FIELD_NAME,
        BUILDER_FIELD_NAME
      );
    }

    builderClassBuilder.addMethod(buildMethod.build());

    builderInterfaces = builderInterfaces.reversed();

    TypeSpec builderClass = builderClassBuilder.addSuperinterfaces(builderInterfaces.stream()
      .map(type -> genClassInnerName(element, type.name()))
      .toList()
    ).build();

    toBuilderMethod.addCode(
      """
        return $L;
        """,
      TO_BUILDER_VAR_NAME
    );

    genClassBuilder.addMethod(toBuilderMethod.build());

    genClassBuilder.addTypes(builderInterfaces);
    genClassBuilder.addType(builderClass);

    return builderInterfaces.isEmpty() ? builderClass : builderInterfaces.getFirst();
  }

  public void addSetterMethods(
    boolean isBuilder,
    boolean isTransitive,
    TypeSpec.Builder typeBuilder,
    Element field,
    TypeName returnType,
    MethodSpec.Builder toBuilderMethod
  ) {
    Prop annotation = field.getAnnotation(Prop.class);

    var fieldName = field.getSimpleName().toString();

    var builderFieldName = isTransitive ? BUILDER_TRANSITIVE_FIELD_NAME : BUILDER_FIELD_NAME;

    TypeMirror supplierTypeArgument = getSupplierTypeArgument(field.asType()).orElse(null);

    List<Modifier> modifiers = isBuilder ? List.of(Modifier.PUBLIC) : List.of(Modifier.PUBLIC, Modifier.ABSTRACT);

    var directSetterMethodBuilder = MethodSpec.methodBuilder(field.getSimpleName().toString())
      .addModifiers(modifiers)
      .addParameter(ParameterSpec.builder(TypeName.get(field.asType()), fieldName).build())
      .returns(returnType);

    if (!annotation.noRx() && supplierTypeArgument != null) {
      if (isBuilder) {
        directSetterMethodBuilder.addCode(
          """
            this.$L.$L = $T.createMemo($L);
            return this;
            """,
          builderFieldName,
          fieldName,
          ClassName.get("org.jsignal.rx", "RxUtil"),
          fieldName
        );
      }

      String methodName = fieldName;
      if (procEnv.getTypeUtils().isAssignable(supplierTypeArgument, field.asType())) {
        methodName = methodName + field.getAnnotation(Prop.class).suffix();
      }

      if (!annotation.noConst()) {
        MethodSpec.Builder constSetterMethodBuilder = MethodSpec.methodBuilder(methodName)
          .addModifiers(modifiers)
          .addParameter(ParameterSpec.builder(TypeName.get(supplierTypeArgument), fieldName).build())
          .returns(returnType);

        if (isBuilder) {
          constSetterMethodBuilder.addCode(
            """
              this.$L.$L = $T.of($L);
              return this;
              """,
            builderFieldName,
            fieldName,
            ClassName.get("org.jsignal.rx", "Constant"),
            fieldName
          );
        }

        typeBuilder.addMethod(constSetterMethodBuilder.build());
      }
    } else if (isBuilder) {
      directSetterMethodBuilder.addCode(
        """
          this.$L.$L = $L;
          return this;
          """,
        builderFieldName,
        fieldName,
        fieldName
      );
    }

    if (!isTransitive && toBuilderMethod != null) {
      toBuilderMethod.addCode(
        """
          $L.$L.$L = that.$L;
          """,
        TO_BUILDER_VAR_NAME,
        builderFieldName,
        fieldName,
        fieldName
      );
    }

    typeBuilder.addMethod(directSetterMethodBuilder.build());
  }

  public Optional<TypeMirror> getSupplierTypeArgument(TypeMirror type) {
    if (type.getKind() == TypeKind.DECLARED) {
      DeclaredType declaredType = (DeclaredType) type;

      if (declaredType.getTypeArguments().size() == 1) {
        TypeElement supplierType = procEnv.getElementUtils().getTypeElement(Supplier.class.getCanonicalName());
        TypeMirror typeArgument = declaredType.getTypeArguments().getFirst();
        DeclaredType supplierTypeWithTypeArgument = procEnv.getTypeUtils().getDeclaredType(supplierType, typeArgument);
        if (procEnv.getTypeUtils().isAssignable(type, supplierTypeWithTypeArgument)) {
          return Optional.of(typeArgument);
        }
      }
    }

    return Optional.empty();
  }

  public String packageName(Element element) {
    PackageElement packageElement = procEnv.getElementUtils().getPackageOf(element);
    return packageElement.getQualifiedName().toString();
  }

  private void saveGeneratedClass(TypeSpec spec, Element element) {
    PackageElement packageElement = procEnv.getElementUtils().getPackageOf(element);
    String packageName = packageElement.getQualifiedName().toString();
    JavaFile javaFile = JavaFile.builder(packageName, spec).build();

    try {
      javaFile.writeTo(procEnv.getFiler());
    } catch (IOException e) {
      procEnv.getMessager().printError("failed to write java file for generated class: " + spec.name());
      throw new RuntimeException();
    }
  }

  public static Optional<AnnotationMirror> findAnnotation(
    Element element,
    Class<? extends Annotation> annotationClass
  ) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (annotationMirror.getAnnotationType().toString().equals(annotationClass.getCanonicalName())) {
        return Optional.of(annotationMirror);
      }
    }
    return Optional.empty();
  }
}
