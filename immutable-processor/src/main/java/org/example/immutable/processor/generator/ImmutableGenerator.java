package org.example.immutable.processor.generator;

import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.processor.base.ProcessorScope;
import org.example.processor.source.IsolatingSourceFileGenerator;
import org.example.processor.source.SourceGenerator;

/** Generates source files from {@link ImmutableImpl}'s. */
@ProcessorScope
public final class ImmutableGenerator extends IsolatingSourceFileGenerator<ImmutableImpl, TypeElement> {

    @Inject
    ImmutableGenerator(Filer filer) {
        super(filer);
    }

    @Override
    protected String getSourceName(ImmutableImpl model) {
        return model.sourceName();
    }

    @Override
    protected SourceGenerator<ImmutableImpl> createSourceGenerator(ImmutableImpl model) {
        return SourceWriter::writeSource;
    }
}
