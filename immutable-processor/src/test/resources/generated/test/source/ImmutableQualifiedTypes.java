package test.source;

@javax.annotation.processing.Generated("org.example.immutable.processor.ImmutableProcessor")
class ImmutableQualifiedTypes implements QualifiedTypes {

    private final String member1;
    private final java.lang.String member2;
    private final Generated member3;

    ImmutableQualifiedTypes(String member1, java.lang.String member2, Generated member3) {
        this.member1 = member1;
        this.member2 = member2;
        this.member3 = member3;
    }

    @java.lang.Override
    public String member1() {
        return member1;
    }

    @java.lang.Override
    public java.lang.String member2() {
        return member2;
    }

    @java.lang.Override
    public Generated member3() {
        return member3;
    }
}
