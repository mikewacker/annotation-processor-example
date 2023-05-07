package org.example.processor.source;

import java.io.PrintWriter;
import java.io.StringWriter;

/** Generates source code from an object model. */
@FunctionalInterface
public interface SourceGenerator<M> {

    void generateSource(PrintWriter writer, M model);

    default String toSource(M model) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        generateSource(writer, model);
        return stringWriter.toString();
    }
}
