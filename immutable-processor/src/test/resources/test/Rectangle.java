package test;

import org.example.immutable.Immutable;

@Immutable
public interface Rectangle {

    static Rectangle of(double width, double height) {
        return null; // Not implemented for testing purposes.
    }

    double width();

    double height();

    default double area() {
        return width() * height();
    }
}
