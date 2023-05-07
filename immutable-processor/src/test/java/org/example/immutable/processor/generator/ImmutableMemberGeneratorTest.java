package org.example.immutable.processor.generator;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class ImmutableMemberGeneratorTest {

    private static final SourceGenerator<MemberType> TYPE_GENERATOR = (writer, type) -> writer.print("double");
    private static final SourceGenerator<ImportableType> QUALIFIED_TYPE_NAMER =
            (writer, type) -> writer.print(type.qualifiedName());

    @Test
    public void toFieldSource() {
        SourceGenerator<ImmutableMember> generator = crateFieldGenerator();
        ImmutableMember member = createMember();
        assertThat(generator.toSource(member)).isEqualTo("    private final double member;\n");
    }

    @Test
    public void toConstructorArgSource() {
        SourceGenerator<ImmutableMember> generator = createConstructorArgGenerator();
        ImmutableMember member = createMember();
        assertThat(generator.toSource(member)).isEqualTo("double member");
    }

    @Test
    public void toFieldInitializerSource() {
        SourceGenerator<ImmutableMember> generator = createdFieldInitializerGenerator();
        ImmutableMember member = createMember();
        assertThat(generator.toSource(member)).isEqualTo("        this.member = member;\n");
    }

    @Test
    public void toMethodSource() {
        SourceGenerator<ImmutableMember> generator = createMethodGenerator();
        ImmutableMember member = createMember();
        assertThat(generator.toSource(member))
                .isEqualTo(String.join(
                        "\n",
                        "    @java.lang.Override",
                        "    public double member() {",
                        "        return member;",
                        "    }",
                        ""));
    }

    private static SourceGenerator<ImmutableMember> crateFieldGenerator() {
        return new ImmutableMemberGenerator.Field(TYPE_GENERATOR);
    }

    private static SourceGenerator<ImmutableMember> createConstructorArgGenerator() {
        return new ImmutableMemberGenerator.ConstructorArg(TYPE_GENERATOR);
    }

    private static SourceGenerator<ImmutableMember> createdFieldInitializerGenerator() {
        return new ImmutableMemberGenerator.FieldInitializer();
    }

    private static SourceGenerator<ImmutableMember> createMethodGenerator() {
        return new ImmutableMemberGenerator.Method(TYPE_GENERATOR, QUALIFIED_TYPE_NAMER);
    }

    private static ImmutableMember createMember() {
        return ImmutableMember.of("member", MemberType.primitiveType("double"));
    }
}
