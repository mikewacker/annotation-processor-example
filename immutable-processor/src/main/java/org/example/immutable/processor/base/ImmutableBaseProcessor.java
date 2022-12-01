package org.example.immutable.processor.base;

import com.google.auto.service.AutoService;
import dagger.Component;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.example.immutable.Immutable;

/**
 * Implements boilerplate processing logic for interfaces annotated with {@link Immutable}.
 *
 * <p>The core processing logic lives in a {@link LiteProcessor},
 * which is provided by the abstract {@link #initLiteProcessor(ProcessingEnvironment)} method.
 * (The {@link Component} for dependency injection will also be created in this method.)</p>
 *
 * <p>The full implementation will also need to be annotated with an {@link AutoService} annotation:
 * {@code @AutoService(Processor.class)}.</p>
 */
public abstract class ImmutableBaseProcessor implements Processor {

    private LiteProcessor liteProcessor;

    @Override
    public final Set<String> getSupportedOptions() {
        return Set.of();
    }

    @Override
    public final Set<String> getSupportedAnnotationTypes() {
        return Set.of(Immutable.class.getCanonicalName());
    }

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public final void init(ProcessingEnvironment processingEnv) {
        liteProcessor = initLiteProcessor(processingEnv);
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }
        liteProcessor.process(annotations, roundEnv);
        return false;
    }

    @Override
    public final Iterable<? extends Completion> getCompletions(
            Element element, AnnotationMirror annotationMirror, ExecutableElement executableElement, String s) {
        return Set.of();
    }

    protected abstract LiteProcessor initLiteProcessor(ProcessingEnvironment processingEnv);
}
