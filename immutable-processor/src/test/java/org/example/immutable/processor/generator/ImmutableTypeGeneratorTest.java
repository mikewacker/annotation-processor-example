package org.example.immutable.processor.generator;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class ImmutableTypeGeneratorTest {

    private static final SourceGenerator<ImportableType> QUALIFIED_TYPE_NAMER =
            (writer, type) -> writer.print(type.qualifiedName());
    private static final SourceGenerator<MemberType> TYPE_GENERATOR = new MemberTypeGenerator(QUALIFIED_TYPE_NAMER);

    @Test
    public void toTypeDeclarationSource() {
        SourceGenerator<ImmutableType> generator = createTypeDeclarationGenerator();
        ImmutableType type = ImmutableType.of(
                MemberType.declaredType(
                        ImportableType.of("test.ImmutableTest"),
                        MemberType.typeParameter("T", MemberType.declaredType(ImportableType.ofClass(Runnable.class)))),
                MemberType.declaredType(ImportableType.of("test.Test"), MemberType.typeVariable("T")));
        assertThat(generator.toSource(type))
                .isEqualTo(String.join(
                        "\n",
                        "@javax.annotation.processing.Generated(\"org.example.immutable.processor.ImmutableProcessor\")",
                        "class ImmutableTest<T extends java.lang.Runnable> implements test.Test<T>"));
    }

    private SourceGenerator<ImmutableType> createTypeDeclarationGenerator() {
        return new ImmutableTypeGenerator(TYPE_GENERATOR, QUALIFIED_TYPE_NAMER);
    }
}
