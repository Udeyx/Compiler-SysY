package backend.instr.i;

import backend.Register;
import util.OpCode;

public class ADDIInstr extends IInstr {
    public ADDIInstr(Register rs, Register rt, int immediate) {
        super(OpCode.ADDI, rs, rt, String.valueOf(immediate));
    }

    @Override
    public String toString() {
        return "addi " + rt + ", " + rs + ", " + offset;
    }
}
