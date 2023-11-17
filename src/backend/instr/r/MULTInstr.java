package backend.instr.r;

import backend.Register;
import util.OpCode;

public class MULTInstr extends RInstr {
    public MULTInstr(Register rs, Register rt) {
        super(OpCode.MULT, rs, rt, Register.ZERO);
    }

    @Override
    public String toString() {
        return "mult " + rs + ", " + rt;
    }
}
