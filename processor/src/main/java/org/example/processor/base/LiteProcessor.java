package org.example.processor.base;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Lightweight annotation processor.
 *
 * <p>{@link AdapterProcessor} can adapt a {@link LiteProcessor} to a {@link Processor}.</p>
 */
public interface LiteProcessor {

    /**
     * Corresponds to {@link Processor#process(Set, RoundEnvironment)} with a few simplifications:
     *
     * <ol>
     *     <li>The set of annotations is never empty.</li>
     *     <li>It does not return a value.</li>
     *     <li>It can throw an {@link Exception}.</li>
     * </ol>
     */
    void process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws Exception;
}
