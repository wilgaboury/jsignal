package org.jsignal.prop;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("org.jsignal.props.GenerateComponentBuilder")
public class PropAnnotationProcessor extends AbstractProcessor {
  private PropGenerator generator;

  @Override
  public synchronized void init(ProcessingEnvironment procEnv) {
    super.init(procEnv);

    generator = new PropGenerator(procEnv);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    assert annotations.size() == 1;
    var annotation = annotations.iterator().next();
    var elements = roundEnv.getElementsAnnotatedWith(annotation);
    for (var element : elements) {
      if (element.getKind() == ElementKind.CLASS) {
        generator.generate((TypeElement) element);
      }
    }
    return true;
  }
}
