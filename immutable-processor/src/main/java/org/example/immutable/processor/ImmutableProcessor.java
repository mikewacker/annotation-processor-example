package org.example.immutable.processor;

import com.google.auto.service.AutoService;
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

/** Processes interfaces annotated with {@link Immutable}. */
@AutoService(Processor.class)
public final class ImmutableProcessor implements Processor {

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
    public void init(ProcessingEnvironment processingEnv) {
        // TODO: Implement me.
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // TODO: Implement me.
        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(
            Element element, AnnotationMirror annotationMirror, ExecutableElement executableElement, String s) {
        return Set.of();
    }
}
