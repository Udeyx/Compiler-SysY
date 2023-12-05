package backend.instr.ext;

import backend.Register;
import backend.instr.Instr;
import midend.ir.value.ConstantInt;
import util.OpCode;

public class LIInstr extends Instr {
    private final int val;
    private final Register tar;

    public LIInstr(Register tar, int val) {
        super(OpCode.LI);
        this.tar = tar;
        this.val = val;
    }

    public LIInstr(Register tar, ConstantInt val) {
        super(OpCode.LI);
        this.tar = tar;
        this.val = Integer.parseInt(val.getName());
    }

    @Override
    public String toString() {
        return "li " + tar + ", " + val;
    }
}
