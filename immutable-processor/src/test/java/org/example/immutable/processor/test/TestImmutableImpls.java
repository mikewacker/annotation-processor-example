package org.example.immutable.processor.test;

import java.util.List;
import org.example.immutable.processor.model.ImmutableImpl;
import org.example.immutable.processor.model.ImmutableMember;
import org.example.immutable.processor.model.ImmutableType;
import org.example.immutable.processor.model.NamedType;
import org.example.immutable.processor.model.TopLevelType;

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
        // Create the type.
        TopLevelType rawImplType = TopLevelType.of("test", "ImmutableRectangle");
        TopLevelType rawInterfaceType = TopLevelType.of("test", "Rectangle");

        List<String> typeVars = List.of();
        NamedType implType = NamedType.ofTopLevelType(rawImplType);
        NamedType interfaceType = NamedType.ofTopLevelType(rawInterfaceType);

        ImmutableType type = ImmutableType.of(rawImplType, typeVars, implType, interfaceType);

        // Create the members.
        NamedType doubleType = NamedType.of("double");

        ImmutableMember width = ImmutableMember.of("width", doubleType);
        ImmutableMember height = ImmutableMember.of("height", doubleType);

        // Create the implementation.
        return ImmutableImpl.of(type, List.of(width, height));
    }

    private static ImmutableImpl createColoredRectangle() {
        // Create the type.
        TopLevelType rawImplType = TopLevelType.of("test", "ImmutableColoredRectangle");
        TopLevelType rawInterfaceType = TopLevelType.of("test", "ColoredRectangle");

        List<String> typeVars = List.of();
        NamedType implType = NamedType.ofTopLevelType(rawImplType);
        NamedType interfaceType = NamedType.ofTopLevelType(rawInterfaceType);

        ImmutableType type = ImmutableType.of(rawImplType, typeVars, implType, interfaceType);

        // Create the members.
        TopLevelType rectangleImport = TopLevelType.of("test", "Rectangle");
        TopLevelType colorImport = TopLevelType.of("java.awt", "Color");
        TopLevelType optionalImport = TopLevelType.of("java.util", "Optional");

        NamedType rectangleType = NamedType.ofTopLevelType(rectangleImport);
        NamedType colorType = NamedType.ofTopLevelType(colorImport);
        NamedType optionalColorType = NamedType.of("%s<%s>", optionalImport, colorImport);

        ImmutableMember rectangle = ImmutableMember.of("rectangle", rectangleType);
        ImmutableMember fillColor = ImmutableMember.of("fillColor", colorType);
        ImmutableMember edgeColor = ImmutableMember.of("edgeColor", optionalColorType);

        // Create the implementation.
        return ImmutableImpl.of(type, List.of(rectangle, fillColor, edgeColor));
    }

    private static ImmutableImpl createEmpty() {
        // Create the type.
        TopLevelType rawImplType = TopLevelType.of("test", "ImmutableEmpty");
        TopLevelType rawInterfaceType = TopLevelType.of("test", "Empty");

        List<String> typeVars = List.of();
        NamedType implType = NamedType.ofTopLevelType(rawImplType);
        NamedType interfaceType = NamedType.ofTopLevelType(rawInterfaceType);

        ImmutableType type = ImmutableType.of(rawImplType, typeVars, implType, interfaceType);

        // Create the implementation.
        return ImmutableImpl.of(type, List.of());
    }

    // static class
    private TestImmutableImpls() {}
}
