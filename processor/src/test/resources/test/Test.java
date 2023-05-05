package test;

public final class Test {

    @Override
    public boolean equals(Object o) {
        return o instanceof Test;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "Test";
    }
}
