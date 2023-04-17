package org.example.processor.source;

import java.io.PrintWriter;
import java.io.StringWriter;

/** Generates the source code for an entity. */
@FunctionalInterface
public interface SourceGenerator<E> {

    void generateSource(PrintWriter writer, E entity);

    default String toSource(E entity) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        generateSource(writer, entity);
        return stringWriter.toString();
    }
}
