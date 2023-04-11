package org.example.immutable.processor.generator;

import java.io.IOException;
import java.io.Writer;
import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.example.immutable.processor.base.ProcessorScope;
import org.example.immutable.processor.model.ImmutableImpl;

/** Generates source code for {@link ImmutableImpl}'s. */
@ProcessorScope
public final class ImmutableGenerator {

    private final Filer filer;

    @Inject
    ImmutableGenerator(Filer filer) {
        this.filer = filer;
    }

    /** Generates the source code for the provided {@link ImmutableImpl}. */
    public void generateSource(ImmutableImpl impl, TypeElement typeElement) {
        try {
            JavaFileObject sourceFile = filer.createSourceFile(impl.sourceName(), typeElement);
            try (Writer writer = sourceFile.openWriter()) {
                SourceWriter.writeSource(writer, impl);
            }
        } catch (IOException e) {
            throw new RuntimeException("unexpected IO exception writing source", e);
        }
    }
}
