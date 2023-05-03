package test.method;

import org.example.immutable.Immutable;

@Immutable
public interface TypeDeclaredInner_NonGenericNonGeneric {

    NonGenericNonGenericOuter.Inner member();
}

class NonGenericNonGenericOuter {

    class Inner {}
}
