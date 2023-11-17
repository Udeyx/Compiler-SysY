package backend.instr.r;

import backend.Register;
import util.OpCode;

public class DIVInstr extends RInstr {
    public DIVInstr(Register rs, Register rt) {
        super(OpCode.DIV, rs, rt, Register.ZERO);
    }

    @Override
    public String toString() {
        return "div " + rs + ", " + rt;
    }
}
