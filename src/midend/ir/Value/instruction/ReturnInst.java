package midend.ir.Value.instruction;

import midend.ir.Type.VoidType;
import midend.ir.Value.Value;

public class ReturnInst extends Instruction {

    public ReturnInst(Value src) {
        super(src.getName(), src.getType());
    }

    public ReturnInst() {
        super("", VoidType.VOID);
    }

    @Override
    public String toString() {
        if (type.equals(VoidType.VOID)) {
            return "ret void\n";
        } else {
            return "ret " + type + " " + name;
        }
    }
}
