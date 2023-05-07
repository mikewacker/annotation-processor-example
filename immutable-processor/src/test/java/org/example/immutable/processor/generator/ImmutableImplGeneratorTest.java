package org.example.immutable.processor.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.imports.ImportManager;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class ImmutableImplGeneratorTest {

    private static final SourceGenerator<ImportManager> PACKAGE_AND_IMPORTS_GENERATOR =
            (writer, type) -> writer.println("[packageAndImports]\n");
    private static final SourceGenerator<ImmutableType> TYPE_GENERATOR =
            (writer, type) -> writer.print("[typeDeclaration]");
    private static final SourceGenerator<ImmutableMember> FIELD_GENERATOR =
            (writer, member) -> writer.println("    [field]");
    private static final SourceGenerator<ImmutableImpl> CONSTRUCTOR_GENERATOR =
            (writer, impl) -> writer.println("    [constructor]");
    private static final SourceGenerator<ImmutableMember> METHOD_GENERATOR =
            (writer, member) -> writer.println("    [method]");

    private static final SourceGenerator<ImmutableMember> CONSTRUCTOR_ARG_GENERATOR =
            (writer, member) -> writer.format("double %s", member.name());
    private static final SourceGenerator<ImmutableMember> FIELD_INITIALIZER_GENERATOR = (writer, member) ->
            writer.format("        this.%1$s = %1$s;", member.name()).println();

    @Test
    public void toSource() {
        SourceGenerator<ImmutableImpl> generator = createSourceGenerator();
        ImmutableImpl impl = createImpl();
        assertThat(generator.toSource(impl))
                .isEqualTo(String.join(
                        "\n",
                        "[packageAndImports]",
                        "",
                        "[typeDeclaration] {",
                        "",
                        "    [field]",
                        "    [field]",
                        "",
                        "    [constructor]",
                        "",
                        "    [method]",
                        "",
                        "    [method]",
                        "}",
                        ""));
    }

    @Test
    public void toSource_NoMembers() {
        SourceGenerator<ImmutableImpl> generator = createSourceGenerator();
        ImmutableImpl impl = createImpl_NoMembers();
        assertThat(generator.toSource(impl))
                .isEqualTo(String.join("\n", "[packageAndImports]", "", "[typeDeclaration] {}", ""));
    }

    @Test
    public void toConstructorSource() {
        SourceGenerator<ImmutableImpl> generator = createConstructorGenerator();
        ImmutableImpl impl = createImpl();
        assertThat(generator.toSource(impl))
                .isEqualTo(String.join(
                        "\n",
                        "    ImmutableTest(double member1, double member2) {",
                        "        this.member1 = member1;",
                        "        this.member2 = member2;",
                        "    }",
                        ""));
    }

    @Test
    public void toConstructorSource_NoMembers() {
        SourceGenerator<ImmutableImpl> generator = createConstructorGenerator();
        ImmutableImpl impl = createImpl_NoMembers();
        assertThat(generator.toSource(impl)).isEqualTo(String.join("\n", "    ImmutableTest() {}", ""));
    }

    private static SourceGenerator<ImmutableImpl> createSourceGenerator() {
        return new ImmutableImplGenerator.Source(
                PACKAGE_AND_IMPORTS_GENERATOR,
                TYPE_GENERATOR,
                FIELD_GENERATOR,
                CONSTRUCTOR_GENERATOR,
                METHOD_GENERATOR);
    }

    private static SourceGenerator<ImmutableImpl> createConstructorGenerator() {
        return new ImmutableImplGenerator.Constructor(CONSTRUCTOR_ARG_GENERATOR, FIELD_INITIALIZER_GENERATOR);
    }

    private static ImmutableImpl createImpl() {
        return ImmutableImpl.of(
                createType(),
                List.of(
                        ImmutableMember.of("member1", MemberType.primitiveType("double")),
                        ImmutableMember.of("member2", MemberType.primitiveType("double"))));
    }

    private static ImmutableImpl createImpl_NoMembers() {
        return ImmutableImpl.of(createType(), List.of());
    }

    private static ImmutableType createType() {
        return ImmutableType.of(
                MemberType.declaredType(ImportableType.of("test.ImmutableTest")),
                MemberType.declaredType(ImportableType.of("test.Test")));
    }
}
