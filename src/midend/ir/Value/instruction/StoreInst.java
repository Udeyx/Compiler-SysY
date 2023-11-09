package midend.ir.Value.instruction;

import midend.ir.Type.Type;
import midend.ir.Type.VoidType;
import midend.ir.Value.Value;

public class StoreInst extends Instruction {
    private final Value src;
    private final Value tar;

    public StoreInst(Value src, Value tar) {
        super("", VoidType.VOID);
        this.src = src;
        this.tar = tar;
    }

    @Override
    public String toString() {
        return "store " + src.getType() + " " + src.getName() + ", "
                + tar.getType() + " " + tar.getName();
    }
}
