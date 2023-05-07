package org.example.immutable.processor.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.util.Map;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.source.SourceGenerator;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class MemberTypeGeneratorTest {

    @Test
    public void toTypeSource() {
        SourceGenerator<MemberType> typeGenerator = createTypeGenerator();
        MemberType type = MemberType.declaredType(
                ImportableType.ofClass(Map.class),
                MemberType.declaredType(ImportableType.ofClass(String.class)),
                MemberType.declaredType(ImportableType.ofClass(Integer.class)));
        assertThat(typeGenerator.toSource(type)).isEqualTo("Map<java.lang.String, Integer>");
    }

    private static SourceGenerator<MemberType> createTypeGenerator() {
        return new MemberTypeGenerator(MemberTypeGeneratorTest::qualifyStringOnly);
    }

    private static void qualifyStringOnly(PrintWriter writer, ImportableType type) {
        String name = !type.equals(ImportableType.ofClass(String.class)) ? type.simpleName() : type.qualifiedName();
        writer.print(name);
    }
}
