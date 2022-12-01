package org.example.immutable.processor.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class NamedTypeTest {

    @Test
    public void of_TopLevelType() {
        NamedType type = NamedType.of(TopLevelType.of(String.class));
        assertThat(type.nameFormat()).isEqualTo("%s");
        assertThat(type.args()).containsExactly(TopLevelType.of(String.class));
    }

    @Test
    public void concat() {
        NamedType type1 = NamedType.of("Map");
        NamedType type2 = NamedType.of("<%s, %s>", TopLevelType.of(String.class), TopLevelType.of(Integer.class));
        NamedType type = NamedType.concat(type1, type2);
        assertThat(type.nameFormat()).isEqualTo("Map<%s, %s>");
        assertThat(type.args()).containsExactly(TopLevelType.of(String.class), TopLevelType.of(Integer.class));
    }

    @Test
    public void join() {
        List<NamedType> types =
                List.of(NamedType.of(TopLevelType.of(String.class)), NamedType.of(TopLevelType.of(Integer.class)));
        NamedType type = NamedType.join(types, ", ", "<", ">");
        assertThat(type.nameFormat()).isEqualTo("<%s, %s>");
        assertThat(type.args()).containsExactly(TopLevelType.of(String.class), TopLevelType.of(Integer.class));
    }

    @Test
    public void name_WithoutQualifiedTypes() {
        NamedType type = NamedType.of(
                "%s.Entry<%s, %s>",
                TopLevelType.of(Map.class), TopLevelType.of(String.class), TopLevelType.of(String.class));
        assertThat(type.name()).isEqualTo("Map.Entry<String, String>");
    }

    @Test
    public void name_WithQualifiedTypes() {
        NamedType type = NamedType.of(
                "%s.Entry<%s, %s>",
                TopLevelType.of(Map.class), TopLevelType.of(String.class), TopLevelType.of(String.class));
        Set<TopLevelType> qualifiedTypes = Set.of(TopLevelType.of(String.class));
        String name = type.name(qualifiedTypes);
        assertThat(name).isEqualTo("Map.Entry<java.lang.String, java.lang.String>");
    }

    @Test
    public void declarationName() {
        TopLevelType rawType = TopLevelType.of("test", "T");
        NamedType type = NamedType.of("%s<T extends %s>", rawType, rawType);
        Set<TopLevelType> qualifiedTypes = Set.of(rawType);
        String declarationName = type.declarationName(qualifiedTypes);
        assertThat(declarationName).isEqualTo("T<T extends test.T>");
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        NamedType type = TestImmutableImpls.rectangle().type().implType();
        TestResources.serializeAndDeserialize(type, new TypeReference<>() {});
    }
}
