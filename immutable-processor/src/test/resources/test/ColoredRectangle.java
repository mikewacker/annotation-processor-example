package test;

import org.example.immutable.Immutable;
import java.awt.Color;
import java.util.Optional;

@Immutable
public interface ColoredRectangle {

    public static ColoredRectangle of(Rectangle rectangle, Color fillColor, Optional<Color> edgeColor) {
        return null; // Not implemented for testing purposes.
    }

    Rectangle rectangle();

    Color fillColor();

    Optional<Color> edgeColor();
}
