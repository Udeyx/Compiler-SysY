package backend.instr;

import util.OpCode;

public abstract class Instr {
    protected final OpCode opCode;

    public Instr(OpCode opCode) {
        this.opCode = opCode;
    }
}
