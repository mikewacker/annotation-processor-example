package org.example.processor.base;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import javax.annotation.processing.Processor;

/** Compiles the test source with an annotation processor. */
final class TestCompiler {

    /** Compiles the test source with the provided processor. */
    public static Compilation compile(Processor processor) {
        return Compiler.javac()
                .withProcessors(processor)
                // Suppress this warning: "Implicitly compiled files were not subject to annotation processing."
                .withOptions("-implicit:none")
                .compile(JavaFileObjects.forResource("test/Test.java"));
    }

    // static class
    private TestCompiler() {}
}
