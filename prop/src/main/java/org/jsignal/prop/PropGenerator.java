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

public class PropGenerator {
  private final static String GEN_CLASS_SUFFIX = "PropComponent";
  private final static String BUILDER_CLASS_NAME = "Builder";
  private final static String BUILDER_FIELD_NAME = "component";

  private final ProcessingEnvironment procEnv;

  public PropGenerator(ProcessingEnvironment procEnv) {
    this.procEnv = procEnv;
  }

  public static String genClassName(TypeElement element) {
    return element.getSimpleName() + GEN_CLASS_SUFFIX;
  }

  public static ClassName genClassInnerName(TypeElement element, String name) {
    return ClassName.get(element.getQualifiedName().toString() + GEN_CLASS_SUFFIX, name);
  }

  public static ClassName builderClassName(TypeElement element) {
    return genClassInnerName(element, BUILDER_CLASS_NAME);
  }

  public void generate(TypeElement element) {
    List<TypeSpec> builders = generateBuilder(element);

    TypeSpec.Builder genClassBuilder = TypeSpec.classBuilder(genClassName(element))
      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
      .superclass(ClassName.get(Component.class))
      .addTypes(builders)
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

    // TODO: implement
//    for (var oneofPropEntry : oneofPropFieldsMap.sequencedEntrySet().reversed()) {}

    for (var requiredPropField : requiredPropFields.reversed()) {

    }

    for (var optionalPropField : optionalPropFields) {
      var fieldName = optionalPropField.getSimpleName().toString();

      TypeMirror supplierTypeArgument = getSupplierTypeArgument(optionalPropField.asType()).orElse(null);

      var simpleSetterMethodBuilder = MethodSpec.methodBuilder(optionalPropField.getSimpleName().toString())
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(TypeName.get(optionalPropField.asType()), fieldName).build())
        .returns(builderClassName(element));

      if (supplierTypeArgument != null) {
        simpleSetterMethodBuilder.addCode("""
            this.$L.$L = $T.createMemo($L);
            return this;
            """,
          BUILDER_FIELD_NAME,
          fieldName,
          RxUtil.class,
          fieldName
        );


        String methodName = fieldName;
        if (procEnv.getTypeUtils().isAssignable(supplierTypeArgument, optionalPropField.asType())) {
          methodName = methodName + optionalPropField.getAnnotation(Prop.class).suffix();
        }

        builderClassBuilder.addMethod(MethodSpec.methodBuilder(methodName)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(ParameterSpec.builder(TypeName.get(supplierTypeArgument), fieldName).build())
          .returns(builderClassName(element))
          .addCode("""
              this.$L.$L = $T.of($L);
              return this;
              """,
            BUILDER_FIELD_NAME,
            fieldName,
            Constant.class,
            fieldName
          )
          .build());
      } else {
        simpleSetterMethodBuilder.addCode("""
            this.$L.$L = $L;
            return this;
            """,
          BUILDER_FIELD_NAME,
          fieldName,
          fieldName
        );
      }

      builderClassBuilder.addMethod(simpleSetterMethodBuilder.build());
    }

    builderClassBuilder.addMethod(MethodSpec.methodBuilder("build")
      .addModifiers(Modifier.PUBLIC)
      .returns(TypeName.get(element.asType()))
      .addCode("""
          return $L;
          """,
        BUILDER_FIELD_NAME)
      .build()
    );

    result.add(builderClassBuilder.build());

    return result;
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
