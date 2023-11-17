package backend.instr.i;

import backend.Register;
import util.OpCode;

public class BNEInstr extends IInstr {
    public BNEInstr(Register rs, Register rt, String label) {
        super(OpCode.BNE, rs, rt, label);
    }

    @Override
    public String toString() {
        return "bne " + rs + ", " + rt + ", " + offset;
    }
}
