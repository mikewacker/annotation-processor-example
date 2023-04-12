package test.source;

import org.example.immutable.Immutable;

@Immutable
public interface QualifiedTypes {

    String member1();

    java.lang.String member2();

    Generated member3();

    Override member4();
}

interface String {}

interface Generated {}

interface Override {}
