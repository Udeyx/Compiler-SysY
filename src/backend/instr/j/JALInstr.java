package backend.instr.j;

import backend.instr.Instr;
import util.OpCode;

public class JALInstr extends Instr {
    private final String label;

    public JALInstr(String label) {
        super(OpCode.JAL);
        this.label = label;
    }

    @Override
    public String toString() {
        return "jal " + label;
    }
}
