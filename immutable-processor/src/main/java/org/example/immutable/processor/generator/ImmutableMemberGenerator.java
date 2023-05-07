package org.example.immutable.processor.generator;

import java.io.PrintWriter;
import javax.inject.Inject;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;

/** Generates source code from {@link ImmutableMember}'s. */
final class ImmutableMemberGenerator {

    /** Generates source code for the source file from the {@link ImmutableMember}. */
    @SourceScope
    static final class Field implements SourceGenerator<ImmutableMember> {

        private final SourceGenerator<ImmutableMember> typedNameGenerator;

        @Inject
        Field(SourceGenerator<MemberType> typeGenerator) {
            this.typedNameGenerator = new TypedName(typeGenerator);
        }

        @Override
        public void generateSource(PrintWriter writer, ImmutableMember member) {
            writer.print("    private final ");
            typedNameGenerator.generateSource(writer, member);
            writer.println(";");
        }
    }

    /** Generates source code for a constructor argument from the {@link ImmutableMember}. */
    @SourceScope
    static final class ConstructorArg implements SourceGenerator<ImmutableMember> {

        private final SourceGenerator<ImmutableMember> typedNameGenerator;

        @Inject
        ConstructorArg(SourceGenerator<MemberType> typeGenerator) {
            typedNameGenerator = new TypedName(typeGenerator);
        }

        @Override
        public void generateSource(PrintWriter writer, ImmutableMember member) {
            typedNameGenerator.generateSource(writer, member);
        }
    }

    /** Generates source code for a field initializer from the {@link ImmutableMember}. */
    @SourceScope
    static final class FieldInitializer implements SourceGenerator<ImmutableMember> {

        @Inject
        FieldInitializer() {}

        @Override
        public void generateSource(PrintWriter writer, ImmutableMember member) {
            writer.format("        this.%1$s = %1$s;", member.name()).println();
        }
    }

    /** Generates source code for a method from the {@link ImmutableMember}. */
    @SourceScope
    static final class Method implements SourceGenerator<ImmutableMember> {

        private static final ImportableType OVERRIDE = ImportableType.ofClass(Override.class);

        private final SourceGenerator<ImmutableMember> typedNameGenerator;
        private final SourceGenerator<ImportableType> typeNamer;

        @Inject
        Method(SourceGenerator<MemberType> typeGenerator, SourceGenerator<ImportableType> typeNamer) {
            typedNameGenerator = new TypedName(typeGenerator);
            this.typeNamer = typeNamer;
        }

        @Override
        public void generateSource(PrintWriter writer, ImmutableMember member) {
            writer.print("    @");
            typeNamer.generateSource(writer, OVERRIDE);
            writer.println();
            writer.print("    public ");
            typedNameGenerator.generateSource(writer, member);
            writer.println("() {");
            writer.format("        return %s;", member.name()).println();
            writer.println("    }");
        }
    }

    /** Generates source the code for the name preceded by its type. */
    private static class TypedName implements SourceGenerator<ImmutableMember> {

        private final SourceGenerator<MemberType> typeGenerator;

        TypedName(SourceGenerator<MemberType> typeGenerator) {
            this.typeGenerator = typeGenerator;
        }

        @Override
        public void generateSource(PrintWriter writer, ImmutableMember member) {
            typeGenerator.generateSource(writer, member.type());
            writer.format(" %s", member.name());
        }
    }

    private ImmutableMemberGenerator() {}
}
