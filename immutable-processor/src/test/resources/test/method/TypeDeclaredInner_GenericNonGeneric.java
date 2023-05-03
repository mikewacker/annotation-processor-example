package test.method;

import org.example.immutable.Immutable;

@Immutable
public interface TypeDeclaredInner_GenericNonGeneric<O> {

    GenericNonGenericOuter<O>.Inner member();
}

class GenericNonGenericOuter<O> {

    class Inner {}
}
