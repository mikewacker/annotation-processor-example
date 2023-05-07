package org.example.immutable.processor.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import org.example.immutable.processor.test.TestResources;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class MemberTypeTest {

    @Test
    public void primitiveType() {
        MemberType type = MemberType.primitiveType("int");
        assertThat(type.nameFormat()).isEqualTo("int");
        assertThat(type.args()).isEmpty();
    }

    @Test
    public void arrayType() {
        MemberType type = MemberType.arrayType(MemberType.primitiveType("int"));
        assertThat(type.nameFormat()).isEqualTo("int[]");
        assertThat(type.args()).isEmpty();
    }

    @Test
    public void declaredType_NonGeneric() {
        MemberType type = MemberType.declaredType(ImportableType.ofClass(String.class));
        assertThat(type.nameFormat()).isEqualTo("%s");
        assertThat(type.args()).containsExactly(ImportableType.ofClass(String.class));
        assertThat(type.rawType()).isEqualTo(ImportableType.ofClass(String.class));
    }

    @Test
    public void declaredType_Generic() {
        MemberType type = MemberType.declaredType(
                ImportableType.ofClass(Map.class),
                MemberType.declaredType(ImportableType.ofClass(String.class)),
                MemberType.declaredType(ImportableType.ofClass(Integer.class)));
        assertThat(type.nameFormat()).isEqualTo("%s<%s, %s>");
        assertThat(type.args())
                .containsExactly(
                        ImportableType.ofClass(Map.class),
                        ImportableType.ofClass(String.class),
                        ImportableType.ofClass(Integer.class));
        assertThat(type.rawType()).isEqualTo(ImportableType.ofClass(Map.class));
    }

    @Test
    public void declaredType_AddTypeArgumentsToOuterTypes() {
        ImportableType rawInner2Type = ImportableType.of("test.Outer$Inner$Inner2");
        ImportableType rawInnerType = ImportableType.of("test.Outer$Inner");
        ImportableType rawOuterType = ImportableType.of("test.Outer");
        MemberType typeArg = MemberType.declaredType(ImportableType.ofClass(Integer.class));

        MemberType type = MemberType.declaredType(rawInner2Type, typeArg);
        type = type.addTypeArgumentsToOuterType(rawInnerType, typeArg);
        type = type.addTypeArgumentsToOuterType(rawOuterType, typeArg);
        assertThat(type.nameFormat()).isEqualTo("%s<%s>.Inner<%s>.Inner2<%s>");
        assertThat(type.args())
                .containsExactly(
                        rawOuterType,
                        ImportableType.ofClass(Integer.class),
                        ImportableType.ofClass(Integer.class),
                        ImportableType.ofClass(Integer.class));
    }

    @Test
    public void typeParameter_NoBounds() {
        MemberType type = MemberType.typeParameter("T");
        assertThat(type.nameFormat()).isEqualTo("T");
        assertThat(type.args()).isEmpty();
    }

    @Test
    public void typeParameter_Bounds() {
        MemberType type = MemberType.typeParameter(
                "T",
                MemberType.declaredType(ImportableType.ofClass(Runnable.class)),
                MemberType.declaredType(ImportableType.ofClass(Comparable.class), MemberType.typeVariable("T")));
        assertThat(type.nameFormat()).isEqualTo("T extends %s & %s<T>");
        assertThat(type.args())
                .containsExactly(ImportableType.ofClass(Runnable.class), ImportableType.ofClass(Comparable.class));
    }

    @Test
    public void typeVariable() {
        MemberType type = MemberType.typeVariable("T");
        assertThat(type.nameFormat()).isEqualTo("T");
        assertThat(type.args()).isEmpty();
    }

    @Test
    public void wildcardType() {
        MemberType type = MemberType.wildcardType();
        assertThat(type.nameFormat()).isEqualTo("?");
        assertThat(type.args()).isEmpty();
    }

    @Test
    public void wildcardExtendsType() {
        MemberType type =
                MemberType.wildcardExtendsType(MemberType.declaredType(ImportableType.ofClass(Runnable.class)));
        assertThat(type.nameFormat()).isEqualTo("? extends %s");
        assertThat(type.args()).containsExactly(ImportableType.ofClass(Runnable.class));
    }

    @Test
    public void wildcardSuperType() {
        MemberType type = MemberType.wildcardSuperType(MemberType.declaredType(ImportableType.ofClass(Runnable.class)));
        assertThat(type.nameFormat()).isEqualTo("? super %s");
        assertThat(type.args()).containsExactly(ImportableType.ofClass(Runnable.class));
    }

    @Test
    public void topLevelDeclaration() {
        MemberType type = MemberType.declaredType(
                ImportableType.ofClass(Map.class),
                MemberType.declaredType(ImportableType.ofClass(String.class)),
                MemberType.declaredType(ImportableType.ofClass(Integer.class)));
        MemberType declarationType = type.topLevelDeclaration();
        assertThat(declarationType.nameFormat()).isEqualTo("Map<%s, %s>");
        assertThat(declarationType.args())
                .containsExactly(ImportableType.ofClass(String.class), ImportableType.ofClass(Integer.class));
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        MemberType type = MemberType.declaredType(ImportableType.ofClass(String.class));
        TestResources.serializeAndDeserialize(type, new TypeReference<>() {});
    }
}
