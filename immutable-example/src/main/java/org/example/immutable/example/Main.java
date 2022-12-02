package org.example.immutable.example;

public final class Main {

    public static void main(String[] args) {
        Rectangle rectangle = createImmutableObject();
        displayImmutableObject(rectangle);
    }

    private static Rectangle createImmutableObject() {
        return Rectangle.of(3, 4);
    }

    private static void displayImmutableObject(Rectangle rectangle) {
        System.out.format("width: %s", rectangle.width()).println();
        System.out.format("height: %s", rectangle.height()).println();
        System.out.format("area: %s", rectangle.area()).println();
    }
}
