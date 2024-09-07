package org.jsignal.prop;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Supplier;

public class PropGenerator {
  private final static String GEN_CLASS_SUFFIX = "PropGen";
  private final static String BUILDER_CLASS_NAME = "Builder";

  private final ProcessingEnvironment procEnv;

  public PropGenerator(ProcessingEnvironment procEnv) {
    this.procEnv = procEnv;
  }

  public void generate(Element element) {
    String genClassName = element.getSimpleName() + GEN_CLASS_SUFFIX;

    TypeSpec.Builder genClassBuilder = TypeSpec.interfaceBuilder(genClassName)
      .addModifiers(Modifier.PUBLIC)
      .addType(generateBuilder(element));

    saveGeneratedClass(genClassBuilder.build(), element);
  }

  public TypeSpec generateBuilder(Element element) {
    var propFields = element.getEnclosedElements()
      .stream()
      .filter(enclosed -> enclosed.getKind() == ElementKind.FIELD)
      .filter(enclosed -> findAnnotation(enclosed, Prop.class).isPresent())
      .toList();

    TypeSpec.Builder genBuilderClassBuilder = TypeSpec.classBuilder(BUILDER_CLASS_NAME)
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

    for (var propField : propFields) {
      // TODO: build
      genBuilderClassBuilder.addMethod(MethodSpec.methodBuilder(propField.getSimpleName().toString())
        .addModifiers(Modifier.PUBLIC)
        .build());
    }

    return genBuilderClassBuilder.build();
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
