package org.jsignal.prop;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class TestCompile {
  @Test
  public void testSuccessfulCodeGenerator() {
    Compilation compilation = javac()
      .withProcessors(new PropAnnotationProcessor())
      .compile(JavaFileObjects.forResource("TestComponent.java"));

    assertThat(compilation).succeeded();
  }
}
