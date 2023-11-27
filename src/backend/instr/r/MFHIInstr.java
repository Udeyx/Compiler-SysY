package backend.instr.r;

import backend.Register;
import util.OpCode;

public class MFHIInstr extends RInstr {
    public MFHIInstr(Register rd) {
        super(OpCode.MFHI, Register.ZERO, Register.ZERO, rd);
    }

    @Override
    public String toString() {
        return "mfhi " + rd;
    }
}
