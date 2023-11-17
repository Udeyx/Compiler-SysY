package backend.instr.r;

import backend.Register;
import util.OpCode;

public class SUBUInstr extends RInstr {
    public SUBUInstr(Register rs, Register rt, Register rd) {
        super(OpCode.SUBU, rs, rt, rd);
    }

    @Override
    public String toString() {
        return "subu " + rd + ", " + rs + ", " + rt;
    }
}
