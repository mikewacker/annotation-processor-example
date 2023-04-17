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
import org.example.processor.imports.ImportManager;
import org.example.processor.imports.SimpleImportManager;
import org.example.processor.type.ImportableType;
import org.junit.jupiter.api.Test;

public final class ImmutableImplTest {

    @Test
    public void sourceName() {
        ImmutableImpl impl = TestImmutableImpls.rectangle();
        assertThat(impl.sourceName()).isEqualTo("test.ImmutableRectangle");
    }

    @Test
    public void importManager() {
        ImportManager importManager = TestImmutableImpls.coloredRectangle().importManager();
        String packageName =
                TestImmutableImpls.coloredRectangle().type().rawImplType().packageName();
        ImportManager expectedImportManager = SimpleImportManager.of(
                Set.of(
                        ImportableType.ofClass(Generated.class),
                        ImportableType.ofClass(Override.class),
                        ImportableType.of("test.ImmutableColoredRectangle"),
                        ImportableType.of("test.ColoredRectangle"),
                        ImportableType.of("test.Rectangle"),
                        ImportableType.ofClass(Color.class),
                        ImportableType.ofClass(Optional.class)),
                packageName);
        assertThat(importManager).isEqualTo(expectedImportManager);
    }

    @Test
    public void serializeAndDeserialize() throws JsonProcessingException {
        ImmutableImpl impl = TestImmutableImpls.rectangle();
        TestResources.serializeAndDeserialize(impl, new TypeReference<>() {});
    }
}
