package test.method;

import org.example.immutable.Immutable;

@Immutable
public interface TypeDeclaredInner_NonGenericGeneric<I> {

    NonGenericGenericOuter.Inner<I> member();
}

class NonGenericGenericOuter {

    class Inner<I> {}
}
