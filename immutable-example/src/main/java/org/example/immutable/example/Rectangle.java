package org.example.immutable.example;

import org.example.immutable.Immutable;

@Immutable
public interface Rectangle {

    static Rectangle of(double width, double height) {
        return new ImmutableRectangle(width, height);
    }

    double width();

    double height();

    default double area() {
        return width() * height();
    }
}
