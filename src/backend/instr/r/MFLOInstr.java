package backend.instr.r;

import backend.Register;
import util.OpCode;

public class MFLOInstr extends RInstr {
    public MFLOInstr(Register rd) {
        super(OpCode.MFLO, Register.ZERO, Register.ZERO, rd);
    }

    @Override
    public String toString() {
        return "mflo " + rd;
    }
}
