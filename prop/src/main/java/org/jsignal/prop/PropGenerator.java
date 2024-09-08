package org.jsignal.prop;

import com.squareup.javapoet.*;
import org.jsignal.rx.Constant;
import org.jsignal.rx.RxUtil;
import org.jsignal.ui.Component;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// TODO: have field in builder for each prop and add toBuilder method for converting back, this will then cover ~95% of builder use cases in JSignal

public class PropGenerator {
  private final static String GEN_CLASS_SUFFIX = "PropComponent";
  private final static String BUILDER_CLASS_NAME = "Builder";
  private final static String BUILDER_REQUIRED_NAME_SUFFIX = "RequiredStep";
  private final static String BUILDER_ONEOF_NAME_SUFFIX = "OneofStep";
  private final static String BUILDER_FIELD_NAME = "component";

  private final ProcessingEnvironment procEnv;

  public PropGenerator(ProcessingEnvironment procEnv) {
    this.procEnv = procEnv;
  }

  public String genClassSimpleName(TypeElement element) {
    return element.getSimpleName() + GEN_CLASS_SUFFIX;
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
    return ClassName.get(packageName(element), element.getSimpleName().toString() + GEN_CLASS_SUFFIX, name);
  }

  public ClassName builderClassName(TypeElement element) {
    return genClassInnerName(element, BUILDER_CLASS_NAME);
  }

  public void generate(TypeElement element) {
    List<TypeSpec> builders = generateBuilder(element);

    TypeSpec.Builder genClassBuilder = TypeSpec.classBuilder(genClassSimpleName(element))
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .superclass(ClassName.get(Component.class))
      .addTypes(builders)
      .addMethod(MethodSpec.methodBuilder("onBuild")
        .addModifiers(Modifier.PROTECTED)
        .build()
      )
      .addMethod(MethodSpec.methodBuilder("builder")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(genClassInnerName(element, builders.getFirst().name))
        .addCode("""
            return new $T();
            """,
          builderClassName(element)
        )
        .build()
      );

    saveGeneratedClass(genClassBuilder.build(), element);
  }

  public List<TypeSpec> generateBuilder(TypeElement element) {
    var result = new ArrayList<TypeSpec>();

    var propFields = element.getEnclosedElements()
      .stream()
      .filter(enclosed -> enclosed.getKind() == ElementKind.FIELD)
      .filter(enclosed -> findAnnotation(enclosed, Prop.class).isPresent())
      .toList();

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
      )
      .addMethod(MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addCode("""
            this.$L = new $T();
            """,
          BUILDER_FIELD_NAME,
          ClassName.get(element)
        )
        .build()
      );

    ClassName previousBuilderType = builderClassName(element);

    for (var oneofPropEntry : oneofPropFieldsMap.sequencedEntrySet().reversed()) {
      String interfaceName = oneofStepInterfaceName(oneofPropEntry.getKey());
      TypeSpec.Builder oneofStep = TypeSpec.interfaceBuilder(interfaceName)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

      for (var oneofPropField : oneofPropEntry.getValue()) {
        addSetterMethods(false, oneofStep, oneofPropField, previousBuilderType);
        addSetterMethods(true, builderClassBuilder, oneofPropField, builderClassName(element));
      }

      result.add(oneofStep.build());
      previousBuilderType = genClassInnerName(element, interfaceName);
    }

    for (var requiredPropField : requiredPropFields.reversed()) {
      String interfaceName = requiredStepInterfaceName(requiredPropField);
      TypeSpec.Builder requiredStep = TypeSpec.interfaceBuilder(interfaceName)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

      addSetterMethods(false, requiredStep, requiredPropField, previousBuilderType);
      addSetterMethods(true, builderClassBuilder, requiredPropField, builderClassName(element));

      result.add(requiredStep.build());
      previousBuilderType = genClassInnerName(element, interfaceName);
    }

    for (var optionalPropField : optionalPropFields) {
      addSetterMethods(true, builderClassBuilder, optionalPropField, builderClassName(element));
    }

    builderClassBuilder.addMethod(MethodSpec.methodBuilder("build")
      .addModifiers(Modifier.PUBLIC)
      .returns(TypeName.get(element.asType()))
      .addCode("""
          (($T)$L).onBuild();
          return $L;
          """,
        genClassName(element),
        BUILDER_FIELD_NAME,
        BUILDER_FIELD_NAME
      )
      .build()
    );

    builderClassBuilder.addSuperinterfaces(result.reversed().stream()
      .map(type -> genClassInnerName(element, type.name))
      .toList()
    );
    result.add(0, builderClassBuilder.build());

    return result.reversed();
  }

  public void addSetterMethods(boolean isBuilder, TypeSpec.Builder typeBuilder, Element field, TypeName returnType) {
    Prop annotation = field.getAnnotation(Prop.class);

    var fieldName = field.getSimpleName().toString();

    TypeMirror supplierTypeArgument = getSupplierTypeArgument(field.asType()).orElse(null);

    List<Modifier> modifiers = isBuilder ? List.of(Modifier.PUBLIC) : List.of(Modifier.PUBLIC, Modifier.ABSTRACT);

    var directSetterMethodBuilder = MethodSpec.methodBuilder(field.getSimpleName().toString())
      .addModifiers(modifiers)
      .addParameter(ParameterSpec.builder(TypeName.get(field.asType()), fieldName).build())
      .returns(returnType);

    if (supplierTypeArgument != null) {
      if (isBuilder) {
        directSetterMethodBuilder.addCode("""
            this.$L.$L = $T.createMemo($L);
            return this;
            """,
          BUILDER_FIELD_NAME,
          fieldName,
          RxUtil.class,
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
          constSetterMethodBuilder.addCode("""
              this.$L.$L = $T.of($L);
              return this;
              """,
            BUILDER_FIELD_NAME,
            fieldName,
            Constant.class,
            fieldName
          );
        }

        typeBuilder.addMethod(constSetterMethodBuilder.build());
      }
    } else if (isBuilder) {
      directSetterMethodBuilder.addCode("""
          this.$L.$L = $L;
          return this;
          """,
        BUILDER_FIELD_NAME,
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
      procEnv.getMessager().printError("failed to write java file for generated class: " + spec.name);
      throw new RuntimeException();
    }
  }

  public static Optional<AnnotationMirror> findAnnotation(Element element, Class<? extends Annotation> annotationClass) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (annotationMirror.getAnnotationType().toString().equals(annotationClass.getCanonicalName())) {
        return Optional.of(annotationMirror);
      }
    }
    return Optional.empty();
  }
}
