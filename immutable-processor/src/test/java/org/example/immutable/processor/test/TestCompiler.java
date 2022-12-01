package org.example.immutable.processor.test;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import dagger.BindsInstance;
import dagger.Component;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.tools.JavaFileObject;
import org.example.immutable.Immutable;
import org.example.immutable.processor.ImmutableProcessor;
import org.example.immutable.processor.base.ImmutableBaseProcessor;
import org.example.immutable.processor.base.LiteProcessor;
import org.example.immutable.processor.base.ProcessorModule;
import org.example.immutable.processor.base.ProcessorScope;

/**
 * Compiles sources with either {@link ImmutableProcessor} or a custom annotation processor.
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

    /** Creates a compiler with {@link ImmutableBaseProcessor} and a custom {@link LiteProcessor}. */
    public static TestCompiler create(Class<? extends LiteProcessor> liteProcessorClass) {
        Processor processor = new TestImmutableProcessor(liteProcessorClass);
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

    /**
     * Processes interfaces annotated with {@link Immutable}.
     *
     * <p>It uses a custom implementation of {@link LiteProcessor}.</p>
     */
    private static final class TestImmutableProcessor extends ImmutableBaseProcessor {

        private final Class<? extends LiteProcessor> liteProcessorClass;

        // Normally, a Processor must provide a public no-arg constructor, but TestCompiler instantiates this directly.
        TestImmutableProcessor(Class<? extends LiteProcessor> liteProcessorClass) {
            this.liteProcessorClass = liteProcessorClass;
        }

        @Override
        protected LiteProcessor initLiteProcessor(ProcessingEnvironment processingEnv) {
            TestProcessorComponent processorComponent = TestProcessorComponent.builder()
                    .processingEnv(processingEnv)
                    .liteProcessorClass(liteProcessorClass)
                    .build();
            return processorComponent.liteProcessor();
        }
    }

    @Component(modules = {ProcessorModule.class, TestProcessorModule.class})
    @ProcessorScope
    interface TestProcessorComponent {

        static Builder builder() {
            return DaggerTestCompiler_TestProcessorComponent.builder();
        }

        LiteProcessor liteProcessor();

        @Component.Builder
        interface Builder {

            Builder processingEnv(@BindsInstance ProcessingEnvironment processingEnv);

            Builder liteProcessorClass(@BindsInstance Class<? extends LiteProcessor> liteProcessorClass);

            TestProcessorComponent build();
        }
    }
}
