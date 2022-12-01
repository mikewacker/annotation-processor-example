package test;

import javax.annotation.processing.Generated;

@Generated("org.example.immutable.processor.ImmutableProcessor")
class ImmutableRectangle implements Rectangle {

    private final double width;
    private final double height;

    ImmutableRectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double width() {
        return width;
    }

    @Override
    public double height() {
        return height;
    }
}
