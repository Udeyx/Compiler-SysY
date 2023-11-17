package backend.instr.i;

import backend.Register;
import util.OpCode;

public class LWInstr extends IInstr {
    public LWInstr(Register base, Register rt, int offset) {
        super(OpCode.LW, base, rt, String.valueOf(offset));
    }

    @Override
    public String toString() {
        return "lw " + rt + ", " + offset + "(" + rs + ")";
    }
}
