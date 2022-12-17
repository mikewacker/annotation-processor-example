package org.example.immutable.processor.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.awt.Color;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.example.immutable.processor.test.TestImmutableImpls;
import org.example.immutable.processor.test.TestResources;
import org.junit.jupiter.api.Test;

public final class ImmutableImplTest {

    @Test
    public void sourceName() {
        ImmutableImpl impl = TestImmutableImpls.rectangle();
        assertThat(impl.sourceName()).isEqualTo("test.ImmutableRectangle");
    }

    @Test
    public void typeQualifier() {
        TypeQualifier typeQualifier = TestImmutableImpls.coloredRectangle().typeQualifier();
        TypeQualifier expectedTypeQualifier = TypeQualifier.of(
                "test",
                Set.of("Rectangle", "ColoredRectangle", "Empty"),
                Set.of(),
                Set.of(
                        TopLevelType.ofClass(Generated.class),
                        TopLevelType.ofClass(Override.class),
                        TopLevelType.of("test", "ImmutableColoredRectangle"),
                        TopLevelType.of("test", "ColoredRectangle"),
                        TopLevelType.of("test", "Rectangle"),
                        TopLevelType.ofClass(Color.class),
                        TopLevelType.ofClass(Optional.class)));
        assertThat(typeQualifier).isEqualTo(expectedTypeQualifier);
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        ImmutableImpl impl = TestImmutableImpls.rectangle();
        TestResources.serializeAndDeserialize(impl, new TypeReference<>() {});
    }
}
