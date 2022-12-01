package test;

import org.example.immutable.Immutable;

@Immutable
public interface Empty {

    static Empty of() {
        return null; // Not implemented for testing purposes.
    }
}
