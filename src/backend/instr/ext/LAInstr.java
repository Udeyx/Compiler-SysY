package backend.instr.ext;

import backend.Register;
import backend.instr.Instr;
import util.OpCode;

public class LAInstr extends Instr {
    private final Register tar;
    private final String label;

    public LAInstr(Register tar, String label) {
        super(OpCode.LA);
        this.tar = tar;
        this.label = label;
    }

    @Override
    public String toString() {
        return "la " + tar + ", " + label;
    }
}
