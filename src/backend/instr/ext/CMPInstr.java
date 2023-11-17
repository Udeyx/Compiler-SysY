package backend.instr.ext;

import backend.Register;
import backend.instr.Instr;
import util.OpCode;

public class CMPInstr extends Instr {
    private final Register rd;
    private final Register rs;
    private final Register rt;

    public CMPInstr(Register rd, Register rs, Register rt, OpCode opCode) {
        super(opCode);
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        return opCode + " " + rd + ", " + rs + ", " + rt;
    }
}
