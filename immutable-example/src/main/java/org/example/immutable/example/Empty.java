package org.example.immutable.example;

import org.example.immutable.Immutable;

@Immutable
public interface Empty {

    static Empty of() {
        return new ImmutableEmpty();
    }
}
