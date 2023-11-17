package backend.instr.r;

import backend.Register;
import util.OpCode;

public class SLLInstr extends RInstr {
    private final int shAmt;

    public SLLInstr(Register rt, Register rd, int shAmt) {
        super(OpCode.SLL, Register.ZERO, rt, rd);
        this.shAmt = shAmt;
    }

    @Override
    public String toString() {
        return "sll " + rd + ", " + rt + ", " + shAmt;
    }
}
