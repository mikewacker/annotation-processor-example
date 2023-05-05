package org.example.processor.base;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Isolating {@link LiteProcessor} where each output is generated from a single input.
 *
 * <p>It contains a single abstract method, {@link #process(Element)}, which processes a single annotated element.</p>
 *
 * <p>It is assumed that the type parameter {@code E} corresponds to the the {@link Target} for the annotation.
 * E.g., if {@code E} is a {@link TypeElement}, the annotation is annotated with {@code @Target(ElementType.TYPE)}.</p>
 */
public abstract class IsolatingLiteProcessor<E extends Element> implements LiteProcessor {

    private final String targetAnnotationCanonicalName;

    /** Creates an {@link IsolatingLiteProcessor} for the provided annotation. */
    protected IsolatingLiteProcessor(Class<? extends Annotation> targetAnnotation) {
        targetAnnotationCanonicalName = targetAnnotation.getCanonicalName();
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws Exception {
        Optional<TypeElement> maybeAnnotationToProcess = findAnnotationToProcess(annotations);
        if (maybeAnnotationToProcess.isEmpty()) {
            return;
        }
        TypeElement annotationToProcess = maybeAnnotationToProcess.get();

        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotationToProcess);
        for (Element annotatedElement : annotatedElements) {
            process((E) annotatedElement);
        }
    }

    /** Processes a single annotated element. */
    protected abstract void process(E annotatedElement) throws Exception;

    /** Finds the annotation to process, or empty. */
    private Optional<TypeElement> findAnnotationToProcess(Set<? extends TypeElement> annotations) {
        return annotations.stream()
                .filter(this::isAnnotationToProcess)
                .map(TypeElement.class::cast)
                .findFirst();
    }

    /** Determines if this annotation is the annotation to process. */
    private boolean isAnnotationToProcess(TypeElement annotation) {
        String annotationQualifiedName = annotation.getQualifiedName().toString();
        return annotationQualifiedName.equals(targetAnnotationCanonicalName);
    }
}
