package org.example.immutable.processor.generator;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.stream.Collectors;
import javax.annotation.processing.Generated;
import org.example.immutable.processor.ImmutableProcessor;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.imports.ImportGenerator;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;

/** Writes the source code for an {@link ImmutableImpl}, using an open {@link Writer}. */
final class SourceWriter {

    private static final MemberType GENERATED_TYPE = MemberType.declaredType(ImportableType.ofClass(Generated.class));
    private static final MemberType OVERRIDE_TYPE = MemberType.declaredType(ImportableType.ofClass(Override.class));
    private static final String PROCESSOR_QUALIFIED_NAME = ImmutableProcessor.class.getCanonicalName();

    private final PrintWriter writer;
    private final ImmutableImpl impl;
    private final SourceGenerator<MemberType> typeNamer;

    /** Writes the source code for the provided {@link ImmutableImpl}. */
    public static void writeSource(Writer writer, ImmutableImpl impl) {
        try (PrintWriter printWriter = new PrintWriter(writer)) {
            SourceWriter sourceWriter = new SourceWriter(printWriter, impl);
            sourceWriter.writeSource();
        }
    }

    private SourceWriter(PrintWriter writer, ImmutableImpl impl) {
        this.writer = writer;
        this.impl = impl;
        typeNamer = MemberType.Namer.of(impl.importManager());
    }

    /** Writes the source code. */
    private void writeSource() {
        ImportGenerator.instance().generateSource(writer, impl.importManager());
        writeClassHeader();
        if (impl.members().isEmpty()) {
            writeConstructor();
            writeClassFooter();
            return;
        }
        writeFields();
        writeConstructor();
        writeMethods();
        writeClassFooter();
    }

    /** Writes the class declaration and the opening curly brace. */
    private void writeClassHeader() {
        ImmutableType type = impl.type();

        writer.format("@%s(\"%s\")", name(GENERATED_TYPE), PROCESSOR_QUALIFIED_NAME)
                .println();
        writer.format(
                        "class %s implements %s {",
                        name(type.implType().topLevelDeclaration()), name(type.interfaceType()))
                .println();
    }

    /** Writes the fields that back the methods. */
    private void writeFields() {
        writer.println();
        for (ImmutableMember member : impl.members()) {
            writer.format("    private final %s %s;", name(member.type()), member.name())
                    .println();
        }
    }

    /** Writes the sole constructor to initialize all the fields. */
    private void writeConstructor() {
        String simpleName = impl.type().implType().rawType().simpleName();
        String params = impl.members().stream()
                .map(member -> String.format("%s %s", name(member.type()), member.name()))
                .collect(Collectors.joining(", "));

        writer.println();
        writer.format("    %s(%s) {", simpleName, params).println();
        for (ImmutableMember member : impl.members()) {
            writer.format("        this.%1$s = %1$s;", member.name()).println();
        }
        writer.println("    }");
    }

    /** Writes the methods that implement the interface. */
    private void writeMethods() {
        for (ImmutableMember member : impl.members()) {
            writer.println();
            writer.format("    @%s", name(OVERRIDE_TYPE)).println();
            writer.format("    public %s %s() {", name(member.type()), member.name())
                    .println();
            writer.format("        return %s;", member.name()).println();
            writer.println("    }");
        }
    }

    /** Writes the closing curly brace for the class. */
    private void writeClassFooter() {
        writer.println("}");
    }

    /** Gets the type's name, qualifying types as appropriate. */
    private String name(MemberType type) {
        return typeNamer.toSource(type);
    }
}
