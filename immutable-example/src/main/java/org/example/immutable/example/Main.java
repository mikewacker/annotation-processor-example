package org.example.immutable.example;

public final class Main {

    public static void main(String[] args) {
        Empty empty = createImmutableObject();
        displayImmutableObject(empty);
    }

    private static Empty createImmutableObject() {
        return Empty.of();
    }

    private static void displayImmutableObject(Empty empty) {
        System.out.println(empty.getClass().getCanonicalName());
    }
}
