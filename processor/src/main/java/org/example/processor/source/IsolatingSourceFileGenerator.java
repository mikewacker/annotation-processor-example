package org.example.processor.source;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;

/**
 * Generates each source file from a single object model, which was generated from a single originating element.
 *
 * <p>The object model will need to provide the name of the source via {@link #getSourceName(Object)}.
 * It may also be used to help create the {@link SourceGenerator} via {@link #createSourceGenerator(Object)}.</p>
 */
public abstract class IsolatingSourceFileGenerator<M, E extends Element> {

    private final Filer filer;

    protected IsolatingSourceFileGenerator(Filer filer) {
        this.filer = filer;
    }

    /** Generates a source file from the object model, which was generated from the originating element. */
    public final void generateSourceFile(M model, E originatingElement) throws IOException {
        FileObject sourceFileObject = createSourceFileObject(model, originatingElement);
        try (Writer underlyingWriter = sourceFileObject.openWriter();
                PrintWriter writer = new PrintWriter(underlyingWriter)) {
            generateSourceFile(writer, model);
        }
    }

    /** Gets the source name from the object model. */
    protected abstract String getSourceName(M model);

    /** Creates the source generator from the object model. */
    protected abstract SourceGenerator<M> createSourceGenerator(M model);

    /** Creates the source file. */
    private FileObject createSourceFileObject(M model, E originatingElement) throws IOException {
        String sourceName = getSourceName(model);
        return filer.createSourceFile(sourceName, originatingElement);
    }

    /** Generates the source file using its writer. */
    private void generateSourceFile(PrintWriter writer, M model) {
        SourceGenerator<M> sourceGenerator = createSourceGenerator(model);
        sourceGenerator.generateSource(writer, model);
    }
}
