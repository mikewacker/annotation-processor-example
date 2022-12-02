package test.type;

import org.example.immutable.Immutable;

import java.util.concurrent.Callable;

@Immutable
public interface InterfaceGenericBounds<T extends Runnable & Callable<Void>> {}
