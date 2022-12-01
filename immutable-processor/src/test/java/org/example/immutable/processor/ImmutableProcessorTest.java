package org.example.immutable.processor;

import org.example.immutable.processor.test.TestCompiler;
import org.junit.jupiter.api.Test;

public final class ImmutableProcessorTest {

    @Test
    public void compileWithoutVerifyingSource_Rectangle() {
        compileWithoutVerifyingSource("test/Rectangle.java");
    }

    @Test
    public void compileWithoutVerifyingSource_ColoredRectangle() {
        compileWithoutVerifyingSource("test/ColoredRectangle.java");
    }

    @Test
    public void compileWithoutVerifyingSource_Empty() {
        compileWithoutVerifyingSource("test/Empty.java");
    }

    private void compileWithoutVerifyingSource(String sourcePath) {
        TestCompiler.create().compile(sourcePath);
    }
}
