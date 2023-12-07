package backend;

import midend.ir.value.Value;

public class VRPair {
    private final Value value;
    private final Register register;

    public VRPair(Value value, Register register) {
        this.value = value;
        this.register = register;
    }

    public Value getVal() {
        return value;
    }

    public Register getReg() {
        return register;
    }
}
