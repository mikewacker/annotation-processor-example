package org.example.processor.type;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

public final class ImportableTypeTest {

    @Test
    public void ofPackageAndClass() {
        ImportableType type = ImportableType.ofPackageAndClass("test", "TopLevel.Nested");
        assertThat(type.binaryName()).isEqualTo("test.TopLevel$Nested");
    }

    @Test
    public void ofClass() {
        ImportableType type = ImportableType.ofClass(Map.Entry.class);
        assertThat(type.binaryName()).isEqualTo("java.util.Map$Entry");
    }

    @Test
    public void qualifiedAndSimpleName_TopLevelType() {
        ImportableType type = ImportableType.ofClass(Map.class);
        assertThat(type.qualifiedName()).isEqualTo("java.util.Map");
        assertThat(type.simpleName()).isEqualTo("Map");
    }

    @Test
    public void qualifiedAndSimpleName_NestedType() {
        ImportableType type = ImportableType.ofClass(Map.Entry.class);
        assertThat(type.qualifiedName()).isEqualTo("java.util.Map.Entry");
        assertThat(type.simpleName()).isEqualTo("Entry");
    }

    @Test
    public void qualifiedAndSimpleName_TypeWithoutPackage() {
        ImportableType type = ImportableType.of("TypeWithoutPackage$Nested");
        assertThat(type.qualifiedName()).isEqualTo("TypeWithoutPackage.Nested");
        assertThat(type.simpleName()).isEqualTo("Nested");
    }

    @Test
    public void packageAndClassName_TopLevelType() {
        ImportableType type = ImportableType.ofClass(Map.class);
        assertThat(type.packageName()).isEqualTo("java.util");
        assertThat(type.className()).isEqualTo("Map");
    }

    @Test
    public void packageAndClassName_NestedType() {
        ImportableType type = ImportableType.ofClass(Map.Entry.class);
        assertThat(type.packageName()).isEqualTo("java.util");
        assertThat(type.className()).isEqualTo("Map.Entry");
    }

    @Test
    public void packageAndClassName_TypeWithoutPackage() {
        ImportableType type = ImportableType.of("TypeWithoutPackage$Nested");
        assertThat(type.packageName()).isEmpty();
        assertThat(type.className()).isEqualTo("TypeWithoutPackage.Nested");
    }

    @Test
    public void topLevelAndEnclosingType() {
        ImportableType type = ImportableType.ofClass(Map.Entry.class);
        assertThat(type.isTopLevelType()).isFalse();
        assertThat(type.topLevelType()).isEqualTo(ImportableType.ofClass(Map.class));
        assertThat(type.enclosingType()).isPresent();

        ImportableType enclosingType = type.enclosingType().get();
        assertThat(enclosingType).isEqualTo(ImportableType.ofClass(Map.class));
        assertThat(enclosingType.isTopLevelType()).isTrue();
        assertThat(enclosingType.topLevelType()).isSameAs(enclosingType);
        assertThat(enclosingType.enclosingType()).isEmpty();
    }

    @Test
    public void qualifiedSuffix() {
        ImportableType type = ImportableType.ofClass(Map.Entry.class);
        ImportableType outerType = ImportableType.ofClass(Map.class);
        assertThat(type.qualifiedSuffix(outerType)).isEqualTo(".Entry");
    }

    @Test
    public void compareTo() {
        ImportableType type1 = ImportableType.ofClass(String.class);
        ImportableType type2 = ImportableType.ofClass(Map.class);
        assertThat(type1.compareTo(type2)).isLessThan(0);
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ImportableType type = ImportableType.ofClass(Map.Entry.class);
        String json = mapper.writeValueAsString(type);
        ImportableType deserializedType = mapper.readValue(json, new TypeReference<>() {});
        assertThat(deserializedType).isEqualTo(type);
    }

    @Test
    public void error_qualifiedSuffix_NotOuterType() {
        ImportableType type = ImportableType.ofClass(Map.class);
        ImportableType outerType = ImportableType.ofClass(Object.class);
        error_qualifiedSuffix(type, outerType);
    }

    @Test
    public void error_qualifiedSuffix_SameType() {
        ImportableType type = ImportableType.ofClass(Map.class);
        error_qualifiedSuffix(type, type);
    }

    private void error_qualifiedSuffix(ImportableType type, ImportableType outerType) {
        assertThatThrownBy(() -> type.qualifiedSuffix(outerType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("%s is not an outer type of %s", outerType.binaryName(), type.binaryName());
    }
}
