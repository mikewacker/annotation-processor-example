package org.example.immutable.processor;

import com.google.auto.service.AutoService;
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
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;
import org.example.immutable.Immutable;
import org.example.processor.base.AdapterProcessor;
import org.example.processor.base.LiteProcessor;
import org.example.processor.base.ProcessorModule;
import org.example.processor.base.ProcessorScope;

/** Processes interfaces annotated with {@link Immutable}. */
@AutoService(Processor.class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
public final class ImmutableProcessor extends AdapterProcessor {

    private static final String DIAGNOSTIC_TAG = String.format("@%s", Immutable.class.getSimpleName());

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
        ProcessorComponent processorComponent = ProcessorComponent.of(processingEnv);
        return processorComponent.liteProcessor();
    }

    @Component(modules = ProcessorModule.class)
    @ProcessorScope
    interface ProcessorComponent {

        static ProcessorComponent of(ProcessingEnvironment processingEnv) {
            return DaggerImmutableProcessor_ProcessorComponent.factory().create(processingEnv, DIAGNOSTIC_TAG);
        }

        ImmutableLiteProcessor liteProcessor();

        @Component.Factory
        interface Factory {

            ProcessorComponent create(
                    @BindsInstance ProcessingEnvironment processingEnv,
                    @BindsInstance @Named("diagnosticTag") String diagnosticTag);
        }
    }
}
