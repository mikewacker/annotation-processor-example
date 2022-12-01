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

/**
 * Compiles sources with {@link ImmutableProcessor}.
 *
 * <p>It checks that the sources compile both without and with the annotation processor.</p>
 */
public class TestCompiler {

    private Processor processor;

    /** Creates a compiler with {@link ImmutableProcessor}. */
    public static TestCompiler create() {
        Processor processor = new ImmutableProcessor();
        return new TestCompiler(processor);
    }

    private TestCompiler(Processor processor) {
        this.processor = processor;
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
        assertThat(compilation).succeededWithoutWarnings();
        return compilation;
    }

    /** Compiles the sources without the annotation processor, verifying the status of the compilation. */
    private void compileWithoutProcessor(Iterable<JavaFileObject> sourceFiles) {
        Compilation compilation = Compiler.javac().compile(sourceFiles);
        assertThat(compilation).succeededWithoutWarnings();
    }
}
