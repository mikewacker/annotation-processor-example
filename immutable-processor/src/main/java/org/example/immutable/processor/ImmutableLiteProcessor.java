package org.example.immutable.processor;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.Immutable;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;

/** Processes interfaces annotated with {@link Immutable}. */
@ProcessorScope
final class ImmutableLiteProcessor extends ImmutableBaseLiteProcessor {

    @Inject
    ImmutableLiteProcessor() {}

    @Override
    protected void process(TypeElement typeElement) {
        // TODO: Implement me.
    }
}
