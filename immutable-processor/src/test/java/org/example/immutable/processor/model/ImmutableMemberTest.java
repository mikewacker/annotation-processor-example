package org.example.immutable.processor.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class ImmutableMemberTest {

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        ImmutableMember member = TestImmutableImpls.rectangle().members().get(0);
        TestResources.serializeAndDeserialize(member, new TypeReference<>() {});
    }
}
