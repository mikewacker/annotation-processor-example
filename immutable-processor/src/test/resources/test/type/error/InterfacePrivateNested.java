package test.type.error;

import org.example.immutable.Immutable;

public class InterfacePrivateNested {

    private interface Private {

        @Immutable
        interface Nested {}
    }
}
