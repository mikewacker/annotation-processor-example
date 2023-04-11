package org.example.immutable.processor;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.Immutable;
import org.example.immutable.processor.base.ImmutableBaseLiteProcessor;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.error.Errors;
import org.example.immutable.processor.generator.ImmutableGenerator;
import org.example.immutable.processor.modeler.ImmutableImpls;

/** Processes interfaces annotated with {@link Immutable}. */
@ProcessorScope
final class ImmutableLiteProcessor extends ImmutableBaseLiteProcessor {

    private final ImmutableImpls implFactory;
    private final ImmutableGenerator generator;
    private final Errors errorReporter;

    @Inject
    ImmutableLiteProcessor(ImmutableImpls implFactory, ImmutableGenerator generator, Errors errorReporter) {
        this.implFactory = implFactory;
        this.generator = generator;
        this.errorReporter = errorReporter;
    }

    @Override
    protected void process(TypeElement typeElement) {
        try {
            implFactory.create(typeElement).ifPresent(impl -> generator.generateSource(impl, typeElement));
        } catch (RuntimeException e) {
            String message = createErrorMessage(e, typeElement);
            errorReporter.error(message);
        }
    }

    /** Create the error message for an unexpected exception while processing the type. */
    private String createErrorMessage(RuntimeException e, TypeElement typeElement) {
        StringWriter messageWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(messageWriter);
        printWriter
                .format("unexpected exception processing %s", typeElement.getQualifiedName())
                .println();
        e.printStackTrace(printWriter);
        return messageWriter.toString();
    }
}
