package org.example.immutable.processor.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class TopLevelTypeTest {

    @Test
    public void ofQualifiedName() {
        TopLevelType type = TopLevelType.ofQualifiedName(String.class.getCanonicalName());
        assertThat(type.packageName()).isEqualTo("java.lang");
        assertThat(type.simpleName()).isEqualTo("String");
    }

    @Test
    public void ofQualifiedName_NoPackage() {
        TopLevelType type = TopLevelType.ofQualifiedName("TypeWithoutPackage");
        assertThat(type.packageName()).isEqualTo("");
        assertThat(type.simpleName()).isEqualTo("TypeWithoutPackage");
    }

    @Test
    public void ofClass() {
        TopLevelType type = TopLevelType.ofClass(String.class);
        assertThat(type.packageName()).isEqualTo("java.lang");
        assertThat(type.simpleName()).isEqualTo("String");
    }

    @Test
    public void qualifiedName() {
        TopLevelType type = TestImmutableImpls.rectangle().type().rawImplType();
        assertThat(type.qualifiedName()).isEqualTo("test.ImmutableRectangle");
    }

    @Test
    public void qualifiedName_NoPackage() {
        TopLevelType type = TopLevelType.of("", "TypeWithoutPackage");
        assertThat(type.qualifiedName()).isEqualTo("TypeWithoutPackage");
    }

    @Test
    public void toImportableType() {
        TopLevelType type = TopLevelType.ofClass(String.class);
        assertThat(type.toImportableType()).isEqualTo(ImportableType.ofClass(String.class));
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        TopLevelType type = TestImmutableImpls.rectangle().type().rawImplType();
        TestResources.serializeAndDeserialize(type, new TypeReference<>() {});
    }
}
