package org.example.immutable.processor.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class ImmutableTypeTest {

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        ImmutableType type = TestImmutableImpls.rectangle().type();
        TestResources.serializeAndDeserialize(type, new TypeReference<>() {});
    }
}
