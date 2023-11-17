package backend.instr.j;

import backend.Register;
import backend.instr.Instr;
import util.OpCode;

public class JRInstr extends Instr {
    private final Register rs;

    public JRInstr(Register rs) {
        super(OpCode.JR);
        this.rs = rs;
    }

    @Override
    public String toString() {
        return "jr " + rs;
    }
}
