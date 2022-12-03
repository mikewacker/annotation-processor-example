package test.error;

import org.example.immutable.Immutable;

@Immutable
public abstract class MultipleErrors {

    public abstract void member1();

    public abstract <T> T member2();
}
