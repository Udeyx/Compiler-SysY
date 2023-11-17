package backend.instr.i;

import backend.Register;
import util.OpCode;

public class SWInstr extends IInstr {
    public SWInstr(Register base, Register rt, int offset) {
        super(OpCode.SW, base, rt, String.valueOf(offset));
    }

    @Override
    public String toString() {
        return "sw " + rt + ", " + offset + "(" + rs + ")";
    }
}
