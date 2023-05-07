package org.example.immutable.processor.generator;

import java.io.PrintWriter;
import javax.inject.Inject;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;

/** Generates source code for a type from the {@link MemberType}. */
@SourceScope
final class MemberTypeGenerator implements SourceGenerator<MemberType> {

    private final SourceGenerator<ImportableType> typeNamer;

    @Inject
    MemberTypeGenerator(SourceGenerator<ImportableType> typeNamer) {
        this.typeNamer = typeNamer;
    }

    @Override
    public void generateSource(PrintWriter writer, MemberType type) {
        Object[] args = type.args().stream().map(typeNamer::toSource).toArray();
        String name = String.format(type.nameFormat(), args);
        writer.print(name);
    }
}
