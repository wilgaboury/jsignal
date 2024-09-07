package org.jsignal.prop;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@AutoService(Processor.class)
public class PropAnnotationProcessor extends AbstractProcessor {
  private PropGenerator generator;

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(GeneratePropBuilder.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_21;
  }

  @Override
  public synchronized void init(ProcessingEnvironment procEnv) {
    super.init(procEnv);

    generator = new PropGenerator(procEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    System.out.println("SIZE: " + annotations.size());
    if (annotations.size() == 1) {
      var annotation = annotations.iterator().next();
      var elements = roundEnv.getElementsAnnotatedWith(annotation);
      for (var element : elements) {
        if (element.getKind() == ElementKind.CLASS) {
          generator.generate((TypeElement) element);
        }
      }
    }
    return true;
  }
}
