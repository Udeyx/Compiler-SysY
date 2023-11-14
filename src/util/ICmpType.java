package util;

public enum ICmpType {
    EQ("eq"), NE("ne"), // == !=
    SGT("sgt"), SGE("sge"), // > >=
    SLT("slt"), SLE("sle"); // < <=
    private final String val;

    ICmpType(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
