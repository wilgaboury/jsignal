package org.jsignal.prop;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class TestCompile {
  @Test
  public void testSuccessfulCodeGenerator() {
    // TODO: only works when using intellij testing

//    Compilation compilation = javac()
//      .withProcessors(new PropAnnotationProcessor())
//      .compile(JavaFileObjects.forResource("test/TestComponent.java"));
//
//    assertThat(compilation).succeeded();
//
//    assertThat(compilation)
//      .generatedSourceFile("test.TestComponentComponent")
//      .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/test/TestComponentComponent.java"));
  }
}
