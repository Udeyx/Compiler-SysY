package backend.instr.i;

import backend.Register;
import backend.instr.Instr;
import util.OpCode;

public class IInstr extends Instr {
    protected final Register rs;
    protected final Register rt;

    protected final String offset;

    public IInstr(OpCode opCode, Register rs, Register rt, String offset) {
        super(opCode);
        this.rs = rs;
        this.rt = rt;
        this.offset = offset;
    }
}
