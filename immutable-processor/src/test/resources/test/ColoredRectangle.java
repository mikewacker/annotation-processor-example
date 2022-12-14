package test;

import java.awt.Color;
import java.util.Optional;
import org.example.immutable.Immutable;

@Immutable
public interface ColoredRectangle {

    static ColoredRectangle of(Rectangle rectangle, Color fillColor, Optional<Color> edgeColor) {
        return null; // Not implemented for testing purposes.
    }

    Rectangle rectangle();

    Color fillColor();

    Optional<Color> edgeColor();
}
