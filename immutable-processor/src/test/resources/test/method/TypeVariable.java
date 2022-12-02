package test.method;

import org.example.immutable.Immutable;

@Immutable
public interface TypeVariable<T> {

    T member();
}
