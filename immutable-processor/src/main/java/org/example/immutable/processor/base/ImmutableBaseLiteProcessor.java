package org.example.immutable.processor.base;

import com.google.common.collect.Iterables;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.example.immutable.Immutable;

/**
 * Finds all types annotated with {@link Immutable} and processes them.
 *
 * <p>Each type is processed by the abstract {@link #process(TypeElement)} method.</p>
 */
public abstract class ImmutableBaseLiteProcessor implements LiteProcessor {

    @Override
    public final void process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        getImmutableTypes(annotations, roundEnv).forEach(this::process);
    }

    /** Processes a type annotated with {@link Immutable}. */
    protected abstract void process(TypeElement typeElement);

    /** Gets all types annotated with {@link Immutable}. */
    private static Stream<TypeElement> getImmutableTypes(
            Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        TypeElement annotation = Iterables.getOnlyElement(annotations);
        Set<? extends Element> element = roundEnv.getElementsAnnotatedWith(annotation);
        return element.stream().map(TypeElement.class::cast);
    }
}
