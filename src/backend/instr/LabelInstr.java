package backend.instr;

import util.OpCode;

public class LabelInstr extends Instr {

    private final String name;

    public LabelInstr(String name) {
        super(OpCode.LABEL);
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ":";
    }
}
