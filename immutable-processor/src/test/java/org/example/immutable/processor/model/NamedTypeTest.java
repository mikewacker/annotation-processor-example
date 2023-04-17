package org.example.immutable.processor.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class NamedTypeTest {

    @Test
    public void ofTopLevelType() {
        NamedType type = NamedType.ofTopLevelType(TopLevelType.ofClass(String.class));
        assertThat(type.nameFormat()).isEqualTo("%s");
        assertThat(type.args()).containsExactly(TopLevelType.ofClass(String.class));
    }

    @Test
    public void ofBinaryName_TopLevelType() {
        NamedType type = NamedType.ofBinaryName(String.class.getName());
        assertThat(type.nameFormat()).isEqualTo("%s");
        assertThat(type.args()).containsExactly(TopLevelType.ofClass(String.class));
    }

    @Test
    public void ofBinaryName_NestedType() {
        NamedType type = NamedType.ofBinaryName(Map.Entry.class.getName());
        assertThat(type.nameFormat()).isEqualTo("%s.Entry");
        assertThat(type.args()).containsExactly(TopLevelType.ofClass(Map.class));
    }

    @Test
    public void concat_Types() {
        NamedType type1 = NamedType.of("Map");
        NamedType type2 =
                NamedType.of("<%s, %s>", TopLevelType.ofClass(String.class), TopLevelType.ofClass(Integer.class));
        NamedType type = NamedType.concat(type1, type2);
        assertThat(type.nameFormat()).isEqualTo("Map<%s, %s>");
        assertThat(type.args())
                .containsExactly(TopLevelType.ofClass(String.class), TopLevelType.ofClass(Integer.class));
    }

    @Test
    public void concat_TypeAndSuffix() {
        NamedType originalType = NamedType.ofTopLevelType(TopLevelType.ofClass(String.class));
        NamedType type = NamedType.concat(originalType, "[]");
        assertThat(type.nameFormat()).isEqualTo("%s[]");
        assertThat(type.args()).containsExactly(TopLevelType.ofClass(String.class));
    }

    @Test
    public void concat_TypeAndPrefix() {
        NamedType originalType = NamedType.ofTopLevelType(TopLevelType.ofClass(Runnable.class));
        NamedType type = NamedType.concat("? extends ", originalType);
        assertThat(type.nameFormat()).isEqualTo("? extends %s");
        assertThat(type.args()).containsExactly(TopLevelType.ofClass(Runnable.class));
    }

    @Test
    public void join() {
        List<NamedType> types = List.of(
                NamedType.ofTopLevelType(TopLevelType.ofClass(String.class)),
                NamedType.ofTopLevelType(TopLevelType.ofClass(Integer.class)));
        NamedType type = NamedType.join(types, ", ", "<", ">");
        assertThat(type.nameFormat()).isEqualTo("<%s, %s>");
        assertThat(type.args())
                .containsExactly(TopLevelType.ofClass(String.class), TopLevelType.ofClass(Integer.class));
    }

    @Test
    public void name_WithoutQualifiedTypes() {
        NamedType type = NamedType.of(
                "%s.Entry<%s, %s>",
                TopLevelType.ofClass(Map.class),
                TopLevelType.ofClass(String.class),
                TopLevelType.ofClass(String.class));
        assertThat(type.name()).isEqualTo("Map.Entry<String, String>");
    }

    @Test
    public void name_WithQualifiedTypes() {
        NamedType type = NamedType.of(
                "%s<%s, %s>",
                TopLevelType.ofClass(Map.class),
                TopLevelType.ofClass(String.class),
                TopLevelType.ofClass(String.class));
        String name = type.name(NamedTypeTest::generateSource);
        assertThat(name).isEqualTo("java.util.Map<String, String>");
    }

    @Test
    public void declarationName() {
        NamedType type = NamedType.of(
                "%s<%s, %s>",
                TopLevelType.ofClass(Map.class),
                TopLevelType.ofClass(String.class),
                TopLevelType.ofClass(String.class));
        String declarationName = type.declarationName(NamedTypeTest::generateSource);
        assertThat(declarationName).isEqualTo("Map<String, String>");
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        NamedType type = TestImmutableImpls.rectangle().type().implType();
        TestResources.serializeAndDeserialize(type, new TypeReference<>() {});
    }

    private static void generateSource(PrintWriter writer, ImportableType type) {
        String name = type.equals(ImportableType.ofClass(String.class)) ? type.simpleName() : type.qualifiedName();
        writer.write(name);
    }
}
