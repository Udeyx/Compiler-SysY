package backend.instr.r;

import backend.Register;
import util.OpCode;

public class ADDUInstr extends RInstr {
    public ADDUInstr(Register rs, Register rt, Register rd) {
        super(OpCode.ADDU, rs, rt, rd);
    }

    @Override
    public String toString() {
        return "addu " + rd + ", " + rs + ", " + rt;
    }
}
