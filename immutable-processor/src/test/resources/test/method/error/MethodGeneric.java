package test.method.error;

import org.example.immutable.Immutable;

@Immutable
public interface MethodGeneric {

    <T> T member();
}
