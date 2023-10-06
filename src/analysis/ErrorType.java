package analysis;

public enum ErrorType {
    A("a"),
    B("b"),
    C("c"),
    D("d"),
    E("e"),
    F("f"),
    G("g"),
    H("h"),
    I("i"),
    J("j"),
    K("k"),
    L("l"),
    M("m");
    private final String val;

    ErrorType(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
