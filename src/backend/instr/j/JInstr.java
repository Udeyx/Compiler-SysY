package backend.instr.j;

import backend.instr.Instr;
import util.OpCode;

public class JInstr extends Instr {
    private final String label;

    public JInstr(String label) {
        super(OpCode.J);
        this.label = label;
    }

    @Override
    public String toString() {
        return "j " + label;
    }
}
