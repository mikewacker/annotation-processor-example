package org.example.immutable.processor;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.Immutable;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.generator.ImmutableGenerator;
import org.example.immutable.processor.modeler.ImmutableImpls;

/** Processes interfaces annotated with {@link Immutable}. */
@ProcessorScope
final class ImmutableLiteProcessor extends ImmutableBaseLiteProcessor {

    private final ImmutableImpls implFactory;
    private final ImmutableGenerator generator;

    @Inject
    ImmutableLiteProcessor(ImmutableImpls implFactory, ImmutableGenerator generator) {
        this.implFactory = implFactory;
        this.generator = generator;
    }

    @Override
    protected void process(TypeElement typeElement) {
        implFactory.create(typeElement).ifPresent(generator::generateSource);
    }
}
