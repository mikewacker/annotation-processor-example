package org.example.immutable.example;

import java.awt.Color;
import java.util.Optional;

public final class Main {

    public static void main(String[] args) {
        ColoredRectangle coloredRectangle = createImmutableObject();
        displayImmutableObject(coloredRectangle);
    }

    private static ColoredRectangle createImmutableObject() {
        Rectangle rectangle = Rectangle.of(3, 4);
        return ColoredRectangle.of(rectangle, Color.RED, Optional.of(Color.BLACK));
    }

    private static void displayImmutableObject(ColoredRectangle coloredRectangle) {
        System.out.format("width: %s", coloredRectangle.rectangle().width()).println();
        System.out.format("height: %s", coloredRectangle.rectangle().height()).println();
        System.out.format("area: %s", coloredRectangle.rectangle().area()).println();
        System.out.format("fill color: %s", coloredRectangle.fillColor()).println();
        coloredRectangle
                .edgeColor()
                .ifPresent(color -> System.out.format("edge color: %s", color).println());
    }
}
