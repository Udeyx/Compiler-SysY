package util;

public enum BOType {
    ADD("add"), SUB("sub"), MUL("mul"), SDIV("sdiv"), SREM("srem");
    private final String val;

    BOType(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
