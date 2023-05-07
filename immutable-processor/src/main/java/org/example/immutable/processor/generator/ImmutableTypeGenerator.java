package org.example.immutable.processor.generator;

import java.io.PrintWriter;
import javax.annotation.processing.Generated;
import javax.inject.Inject;
import org.example.immutable.processor.ImmutableProcessor;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;

/** Generates source code for a type declaration from the {@link ImmutableType}. */
@SourceScope
final class ImmutableTypeGenerator implements SourceGenerator<ImmutableType> {

    private static final ImportableType GENERATED = ImportableType.ofClass(Generated.class);
    private static final String PROCESSOR_CANONICAL_NAME = ImmutableProcessor.class.getCanonicalName();

    private final SourceGenerator<MemberType> typeGenerator;
    private final SourceGenerator<ImportableType> typeNamer;

    @Inject
    ImmutableTypeGenerator(SourceGenerator<MemberType> typeGenerator, SourceGenerator<ImportableType> typeNamer) {
        this.typeGenerator = typeGenerator;
        this.typeNamer = typeNamer;
    }

    @Override
    public void generateSource(PrintWriter writer, ImmutableType immutableType) {
        writer.print("@");
        typeNamer.generateSource(writer, GENERATED);
        writer.format("(\"%s\")", PROCESSOR_CANONICAL_NAME).println();
        writer.print("class ");
        typeGenerator.generateSource(writer, immutableType.implType().topLevelDeclaration());
        writer.print(" implements ");
        typeGenerator.generateSource(writer, immutableType.interfaceType());
    }
}
