package backend;

public enum Register {
    // special registers
    ZERO("0"),
    AT("at"),
    V0("v0"), V1("v1"),
    A0("a0"), A1("a1"), A2("a2"), A3("a3"),
    GP("gp"), SP("sp"), FP("fp"), RA("ra"),

    // temp registers
    K0("k0"), K1("k1"),

    // global registers
    T0("t0"), T1("t1"), T2("t2"), T3("t3"), T4("t4"), T5("t5"), T6("t6"), T7("t7"),
    S0("s0"), S1("s1"), S2("s2"), S3("s3"), S4("s4"), S5("s5"), S6("s6"), S7("s7"),
    T8("t8"), T9("t9");

    private final String val;

    Register(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "$" + val;
    }
}
