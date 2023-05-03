package test.method;

import org.example.immutable.Immutable;

@Immutable
public interface TypeDeclaredInner_GenericGeneric<O, I> {

    GenericGenericOuter<O>.Inner<I> member();
}

class GenericGenericOuter<O> {

    class Inner<I> {}
}
