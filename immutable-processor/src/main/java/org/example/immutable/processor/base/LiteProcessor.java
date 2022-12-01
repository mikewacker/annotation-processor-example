package org.example.immutable.processor.base;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Lightweight annotation processor.
 *
 * <p>A full {@link Processor} will implement all the boilerplate logic,
 * and then {@link Processor#process(Set, RoundEnvironment)}
 * will hand off control to {@link #process(Set, RoundEnvironment)}.</p>
 */
public interface LiteProcessor {

    /**
     * Corresponds to {@link Processor#process(Set, RoundEnvironment)} with a few simplifications:
     *
     * <ol>
     *     <li>The set of annotations is never empty.</li>
     *     <li>It does not return a value.</li>
     * </ol>
     */
    void process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);
}
