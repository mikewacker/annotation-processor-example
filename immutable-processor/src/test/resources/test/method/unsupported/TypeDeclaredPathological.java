package test.method;

import org.example.immutable.Immutable;

@Immutable
public interface TypeDeclaredPathological {

    OuterDerived<String>.Inner<String> member();
}

class OuterBase<O> {

    class Inner<I> {}
}

class OuterDerived<O> extends OuterBase<O> {}
