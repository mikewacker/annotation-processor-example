package org.example.processor.base;

import dagger.BindsInstance;
import dagger.Component;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/** Processes methods annotated with {@link Override}, using the provided {@link LiteProcessor}. */
final class TestProcessor extends AdapterProcessor {

    private final Class<? extends LiteProcessor> liteProcessorClass;

    public static Processor of(Class<? extends LiteProcessor> liteProcessorClass) {
        return new TestProcessor(liteProcessorClass);
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Override.class.getCanonicalName());
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

    private TestProcessor(Class<? extends LiteProcessor> liteProcessorClass) {
        this.liteProcessorClass = liteProcessorClass;
    }

    @Component(modules = {ProcessorModule.class, TestProcessorModule.class})
    @ProcessorScope
    interface ProcessorComponent {

        static ProcessorComponent of(
                ProcessingEnvironment processingEnv, Class<? extends LiteProcessor> liteProcessorClass) {
            return DaggerTestProcessor_ProcessorComponent.factory().create(processingEnv, liteProcessorClass);
        }

        LiteProcessor liteProcessor();

        @Component.Factory
        interface Factory {

            ProcessorComponent create(
                    @BindsInstance ProcessingEnvironment processingEnv,
                    @BindsInstance Class<? extends LiteProcessor> liteProcessorClass);
        }
    }
}
