package org.example.immutable.processor.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class TopLevelTypeTest {

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
    public void serializeAndDeserialize() throws JsonProcessingException {
        TopLevelType type = TestImmutableImpls.rectangle().type().rawImplType();
        TestResources.serializeAndDeserialize(type, new TypeReference<>() {});
    }
}
