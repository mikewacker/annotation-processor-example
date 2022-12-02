package test.type.error;

import org.example.immutable.Immutable;

public interface InterfaceNestedWithImpl {

    @Immutable
    interface Inner {}
}

@Immutable
interface InterfaceNestedWithImpl_Inner {}
