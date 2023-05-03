package org.example.immutable.processor.test;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.MemberType;
import org.example.processor.type.ImportableType;

/** Creates test {@link ImmutableImpl}'s. */
public final class TestImmutableImpls {

    private static final ImmutableImpl RECTANGLE = createRectangle();
    private static final ImmutableImpl COLORED_RECTANGLE = createColoredRectangle();
    private static final ImmutableImpl EMPTY = createEmpty();

    /** Gets the expected {@link ImmutableImpl} for {@code test/Rectangle.java}. */
    public static ImmutableImpl rectangle() {
        return RECTANGLE;
    }

    /** Gets the expected {@link ImmutableImpl} for {@code test/ColoredRectangle.java}. */
    public static ImmutableImpl coloredRectangle() {
        return COLORED_RECTANGLE;
    }

    /** Gets the expected {@link ImmutableImpl} for {@code test/Empty.java}. */
    public static ImmutableImpl empty() {
        return EMPTY;
    }

    private static ImmutableImpl createRectangle() {
        ImmutableType type = ImmutableType.of(
                MemberType.declaredType(ImportableType.of("test.ImmutableRectangle")),
                MemberType.declaredType(ImportableType.of("test.Rectangle")));

        ImmutableMember width = ImmutableMember.of("width", MemberType.primitiveType("double"));
        ImmutableMember height = ImmutableMember.of("height", MemberType.primitiveType("double"));
        return ImmutableImpl.of(type, List.of(width, height));
    }

    private static ImmutableImpl createColoredRectangle() {
        ImmutableType type = ImmutableType.of(
                MemberType.declaredType(ImportableType.of("test.ImmutableColoredRectangle")),
                MemberType.declaredType(ImportableType.of("test.ColoredRectangle")));

        ImmutableMember rectangle =
                ImmutableMember.of("rectangle", MemberType.declaredType(ImportableType.of("test.Rectangle")));
        ImmutableMember fillColor =
                ImmutableMember.of("fillColor", MemberType.declaredType(ImportableType.ofClass(Color.class)));
        ImmutableMember edgeColor = ImmutableMember.of(
                "edgeColor",
                MemberType.declaredType(
                        ImportableType.ofClass(Optional.class),
                        MemberType.declaredType(ImportableType.ofClass(Color.class))));
        return ImmutableImpl.of(type, List.of(rectangle, fillColor, edgeColor));
    }

    private static ImmutableImpl createEmpty() {
        ImmutableType type = ImmutableType.of(
                MemberType.declaredType(ImportableType.of("test.ImmutableEmpty")),
                MemberType.declaredType(ImportableType.of("test.Empty")));
        return ImmutableImpl.of(type, List.of());
    }

    // static class
    private TestImmutableImpls() {}
}
