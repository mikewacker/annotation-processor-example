package test.method;

import org.example.immutable.Immutable;

@Immutable
public interface TypeDeclaredNestedGenericInstance {

    Outer<String>.Inner<String> member();
}

class Outer<O> {

    class Inner<I> {}
}
