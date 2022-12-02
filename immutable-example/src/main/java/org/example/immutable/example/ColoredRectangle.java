package org.example.immutable.example;

import java.awt.Color;
import java.util.Optional;
import org.example.immutable.Immutable;

@Immutable
public interface ColoredRectangle {

    static ColoredRectangle of(Rectangle rectangle, Color fillColor, Optional<Color> edgeColor) {
        return new ImmutableColoredRectangle(rectangle, fillColor, edgeColor);
    }

    Rectangle rectangle();

    Color fillColor();

    Optional<Color> edgeColor();
}
