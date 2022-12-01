package test;

import java.awt.Color;
import java.util.Optional;
import javax.annotation.processing.Generated;

@Generated("org.example.immutable.processor.ImmutableProcessor")
class ImmutableColoredRectangle implements ColoredRectangle {

    private final Rectangle rectangle;
    private final Color fillColor;
    private final Optional<Color> edgeColor;

    ImmutableColoredRectangle(Rectangle rectangle, Color fillColor, Optional<Color> edgeColor) {
        this.rectangle = rectangle;
        this.fillColor = fillColor;
        this.edgeColor = edgeColor;
    }

    @Override
    public Rectangle rectangle() {
        return rectangle;
    }

    @Override
    public Color fillColor() {
        return fillColor;
    }

    @Override
    public Optional<Color> edgeColor() {
        return edgeColor;
    }
}
