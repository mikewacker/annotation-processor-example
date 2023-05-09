package org.example.immutable.processor.generator;

import java.io.PrintWriter;
import javax.inject.Inject;
import javax.inject.Named;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.ImmutableType;
import org.example.processor.imports.ImportManager;
import org.example.processor.source.SourceGenerator;

/** Generates source code from {@link ImmutableImpl}'s. */
final class ImmutableImplGenerator {

    /** Generates a source file from the {@link ImmutableImpl}. */
    @SourceScope
    static final class Source implements SourceGenerator<ImmutableImpl> {

        private final SourceGenerator<ImportManager> packageAndImportsGenerator;
        private final SourceGenerator<ImmutableType> typeDeclarationGenerator;
        private final SourceGenerator<ImmutableMember> fieldGenerator;
        private final SourceGenerator<ImmutableImpl> constructorGenerator;
        private final SourceGenerator<ImmutableMember> methodGenerator;

        @Inject
        Source(
                SourceGenerator<ImportManager> packageAndImportsGenerator,
                SourceGenerator<ImmutableType> typeDeclarationGenerator,
                @Named("field") SourceGenerator<ImmutableMember> fieldGenerator,
                @Named("constructor") SourceGenerator<ImmutableImpl> constructorGenerator,
                @Named("method") SourceGenerator<ImmutableMember> methodGenerator) {
            this.packageAndImportsGenerator = packageAndImportsGenerator;
            this.typeDeclarationGenerator = typeDeclarationGenerator;
            this.fieldGenerator = fieldGenerator;
            this.constructorGenerator = constructorGenerator;
            this.methodGenerator = methodGenerator;
        }

        @Override
        public void generateSource(PrintWriter writer, ImmutableImpl impl) {
            packageAndImportsGenerator.generateSource(writer, impl.importManager());
            typeDeclarationGenerator.generateSource(writer, impl.type());
            if (impl.members().isEmpty()) {
                writer.println(" {}");
                return;
            }

            writer.println(" {");
            writer.println();
            for (ImmutableMember member : impl.members()) {
                fieldGenerator.generateSource(writer, member);
            }
            writer.println();
            constructorGenerator.generateSource(writer, impl);
            for (ImmutableMember member : impl.members()) {
                writer.println();
                methodGenerator.generateSource(writer, member);
            }
            writer.println("}");
        }
    }

    /** Generates source code for a constructor from the {@link ImmutableImpl}. */
    @SourceScope
    static final class Constructor implements SourceGenerator<ImmutableImpl> {

        private final SourceGenerator<ImmutableMember> constructorArgGenerator;
        private final SourceGenerator<ImmutableMember> fieldInitializerGenerator;

        @Inject
        Constructor(
                @Named("constructorArg") SourceGenerator<ImmutableMember> constructorArgGenerator,
                @Named("fieldInitializer") SourceGenerator<ImmutableMember> fieldInitializerGenerator) {
            this.constructorArgGenerator = constructorArgGenerator;
            this.fieldInitializerGenerator = fieldInitializerGenerator;
        }

        @Override
        public void generateSource(PrintWriter writer, ImmutableImpl impl) {
            writer.format("    %s(", impl.type().simpleName());
            if (impl.members().isEmpty()) {
                writer.println(") {}");
                return;
            }

            constructorArgGenerator.generateSource(writer, impl.members().get(0));
            for (ImmutableMember member :
                    impl.members().subList(1, impl.members().size())) {
                writer.print(", ");
                constructorArgGenerator.generateSource(writer, member);
            }
            writer.println(") {");
            for (ImmutableMember member : impl.members()) {
                fieldInitializerGenerator.generateSource(writer, member);
            }
            writer.println("    }");
        }
    }

    private ImmutableImplGenerator() {}
}
