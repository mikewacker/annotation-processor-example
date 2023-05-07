package org.example.immutable.processor;

import java.io.IOException;
import java.util.Optional;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.Immutable;
import org.example.immutable.processor.generator.ImmutableGenerator;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.modeler.ImmutableImpls;
import org.example.processor.base.IsolatingLiteProcessor;
import org.example.processor.base.ProcessorScope;

/** Processes interfaces annotated with {@link Immutable}. */
@ProcessorScope
final class ImmutableLiteProcessor extends IsolatingLiteProcessor<TypeElement> {

    private final ImmutableImpls implFactory;
    private final ImmutableGenerator generator;

    @Inject
    ImmutableLiteProcessor(ImmutableImpls implFactory, ImmutableGenerator generator) {
        super(Immutable.class);
        this.implFactory = implFactory;
        this.generator = generator;
    }

    @Override
    protected void process(TypeElement typeElement) throws IOException {
        Optional<ImmutableImpl> maybeImpl = implFactory.create(typeElement);
        if (maybeImpl.isEmpty()) {
            return;
        }
        ImmutableImpl impl = maybeImpl.get();
        generator.generateSourceFile(impl, typeElement);
    }
}
