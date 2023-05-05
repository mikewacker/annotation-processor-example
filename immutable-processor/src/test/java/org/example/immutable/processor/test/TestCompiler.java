package org.example.immutable.processor.test;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import org.example.immutable.processor.ImmutableProcessor;
import org.example.processor.base.LiteProcessor;

/**
 * Compiles sources with either {@link ImmutableProcessor} or a custom annotation processor.
 *
 * <p>By default, it checks that the sources compile both without and with the annotation processor.
 * However, it can be configured to expect a compilation failure for either.</p>
 */
public final class TestCompiler {

    private Processor processor;
    private boolean compiles = true;
    private boolean compilesWithoutProcessor = true;

    /** Creates a compiler with {@link ImmutableProcessor}. */
    public static TestCompiler create() {
        Processor processor = new ImmutableProcessor();
        return new TestCompiler(processor);
    }

    /**
     * Creates a compiler with {@link TestImmutableProcessor} and the provided {@link LiteProcessor}.
     *
     * <p>To be used, the {@link LiteProcessor} class must also be added to {@link TestProcessorModule}.</p>
     */
    public static TestCompiler create(Class<? extends LiteProcessor> liteProcessorClass) {
        Processor processor = TestImmutableProcessor.of(liteProcessorClass);
        return new TestCompiler(processor);
    }

    private TestCompiler(Processor processor) {
        this.processor = processor;
    }

    /** Configures the compiler to expect a compilation failure with the annotation processor. */
    public TestCompiler expectingCompilationFailure() {
        compiles = false;
        return this;
    }

    /** Configures the compiler to expect a compilation failure without the annotation processor. */
    public TestCompiler expectingCompilationFailureWithoutProcessor() {
        compilesWithoutProcessor = false;
        return this;
    }

    /** Compiles the sources, verifying the status of the compilation. */
    public Compilation compile(String... sourcePaths) {
        return compile(List.of(sourcePaths));
    }

    /** Compiles the sources, verifying the status of the compilation. */
    public Compilation compile(Iterable<String> sourcePaths) {
        List<JavaFileObject> sourceFiles = StreamSupport.stream(sourcePaths.spliterator(), false)
                .map(JavaFileObjects::forResource)
                .toList();
        compileWithoutProcessor(sourceFiles);
        return compileWithProcessor(sourceFiles);
    }

    /** Compiles the sources with the annotation processor, verifying the status of the compilation. */
    private Compilation compileWithProcessor(Iterable<JavaFileObject> sourceFiles) {
        Compilation compilation = Compiler.javac()
                .withProcessors(processor)
                // Suppress this warning: "Implicitly compiled files were not subject to annotation processing."
                .withOptions("-implicit:none")
                .compile(sourceFiles);
        if (compiles) {
            assertThat(compilation).succeededWithoutWarnings();
        } else {
            assertThat(compilation).failed();
        }
        return compilation;
    }

    /** Compiles the sources without the annotation processor, verifying the status of the compilation. */
    private void compileWithoutProcessor(Iterable<JavaFileObject> sourceFiles) {
        Compilation compilation = Compiler.javac().compile(sourceFiles);
        if (compilesWithoutProcessor) {
            assertThat(compilation).succeededWithoutWarnings();
        } else {
            assertThat(compilation).failed();
        }
    }
}
