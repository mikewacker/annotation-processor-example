package org.example.immutable.processor.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class ImmutableTypeTest {

    @Test
    public void qualifiedAndSimpleName() {
        ImmutableType type = ImmutableType.of(
                MemberType.declaredType(ImportableType.of("test.ImmutableTest")),
                MemberType.declaredType(ImportableType.of("test.Test")));
        assertThat(type.qualifiedName()).isEqualTo("test.ImmutableTest");
        assertThat(type.simpleName()).isEqualTo("ImmutableTest");
    }

    @Test
    public void typeVars_NonGeneric() {
        ImmutableType type = ImmutableType.of(
                MemberType.declaredType(ImportableType.of("test.ImmutableTest")),
                MemberType.declaredType(ImportableType.of("test.Test")));
        assertThat(type.typeVars()).isEmpty();
    }

    @Test
    public void typeVars_Generic() {
        ImmutableType type = ImmutableType.of(
                MemberType.declaredType(
                        ImportableType.of("test.ImmutableTest"),
                        MemberType.typeParameter(
                                "T",
                                MemberType.declaredType(
                                        ImportableType.ofClass(Comparable.class), MemberType.typeVariable("T"))),
                        MemberType.typeParameter("U")),
                MemberType.declaredType(
                        ImportableType.of("test.Test"), MemberType.typeVariable("T"), MemberType.typeVariable("U")));
        assertThat(type.typeVars()).containsExactly("T", "U");
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        ImmutableType type = TestImmutableImpls.rectangle().type();
        TestResources.serializeAndDeserialize(type, new TypeReference<>() {});
    }
}
