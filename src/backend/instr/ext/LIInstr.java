package backend.instr.ext;

import backend.Register;
import backend.instr.Instr;
import util.OpCode;

public class LIInstr extends Instr {
    private final int val;
    private final Register tar;

    public LIInstr(Register tar, int val) {
        super(OpCode.LI);
        this.tar = tar;
        this.val = val;
    }

    @Override
    public String toString() {
        return "li " + tar + ", " + val;
    }
}
