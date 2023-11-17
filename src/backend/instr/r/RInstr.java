package backend.instr.r;

import backend.Register;
import backend.instr.Instr;
import util.OpCode;

public class RInstr extends Instr {
    protected final Register rs;
    protected final Register rt;
    protected final Register rd;

    public RInstr(OpCode opCode, Register rs, Register rt, Register rd) {
        super(opCode);
        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
    }
}
