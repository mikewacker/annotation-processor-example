package org.example.immutable.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.example.immutable.processor.test.TestCompiler;
import org.junit.jupiter.api.Test;

public final class ImmutableProcessorTest {

    @Test
    public void compile_Empty() {
        compile("test/Empty.java", "test.ImmutableEmpty", "generated/test/ImmutableEmpty.java");
    }

    private void compile(String sourcePath, String generatedQualifiedName, String expectedGeneratedSourcePath) {
        Compilation compilation = TestCompiler.create().compile(sourcePath);
        assertThat(compilation)
                .generatedSourceFile(generatedQualifiedName)
                .hasSourceEquivalentTo(JavaFileObjects.forResource(expectedGeneratedSourcePath));
    }

    @Test
    public void compileWithoutVerifyingSource_Rectangle() {
        compileWithoutVerifyingSource("test/Rectangle.java");
    }

    @Test
    public void compileWithoutVerifyingSource_ColoredRectangle() {
        compileWithoutVerifyingSource("test/ColoredRectangle.java");
    }

    private void compileWithoutVerifyingSource(String sourcePath) {
        TestCompiler.create().compile(sourcePath);
    }
}
