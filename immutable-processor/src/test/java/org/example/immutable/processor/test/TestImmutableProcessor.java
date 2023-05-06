package org.example.immutable.processor.test;

import dagger.BindsInstance;
import dagger.Component;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.inject.Named;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.example.immutable.Immutable;
import org.example.processor.base.AdapterProcessor;
import org.example.processor.base.LiteProcessor;
import org.example.processor.base.ProcessorModule;
import org.example.processor.base.ProcessorScope;

/** Processes interfaces annotated with {@link Immutable}, using the provided {@link LiteProcessor}. */
final class TestImmutableProcessor extends AdapterProcessor {

    private static final String DIAGNOSTIC_TAG = String.format("@%s", Immutable.class.getSimpleName());

    private final Class<? extends LiteProcessor> liteProcessorClass;

    public static Processor of(Class<? extends LiteProcessor> liteProcessorClass) {
        return new TestImmutableProcessor(liteProcessorClass);
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Immutable.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Iterable<? extends Completion> getCompletions(
            Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return Set.of();
    }

    @Override
    protected LiteProcessor createLiteProcessor(ProcessingEnvironment processingEnv) {
        ProcessorComponent processorComponent = ProcessorComponent.of(processingEnv, liteProcessorClass);
        return processorComponent.liteProcessor();
    }

    private TestImmutableProcessor(Class<? extends LiteProcessor> liteProcessorClass) {
        this.liteProcessorClass = liteProcessorClass;
    }

    @Component(modules = {ProcessorModule.class, TestProcessorModule.class})
    @ProcessorScope
    interface ProcessorComponent {

        static ProcessorComponent of(
                ProcessingEnvironment processingEnv, Class<? extends LiteProcessor> liteProcessorClass) {
            return DaggerTestImmutableProcessor_ProcessorComponent.factory()
                    .create(processingEnv, DIAGNOSTIC_TAG, liteProcessorClass);
        }

        LiteProcessor liteProcessor();

        @Component.Factory
        interface Factory {

            ProcessorComponent create(
                    @BindsInstance ProcessingEnvironment processingEnv,
                    @BindsInstance @Named("diagnosticTag") String diagnosticTag,
                    @BindsInstance Class<? extends LiteProcessor> liteProcessorClass);
        }
    }
}
