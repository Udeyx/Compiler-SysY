package midend.ir.Value.instruction;

import midend.ir.Type.PointerType;
import midend.ir.Value.Value;

public class LoadInst extends Instruction {

    private final Value src;
    private final Value tar;

    public LoadInst(Value src, Value tar) {
        super(tar.getName(), ((PointerType) src.getType()).getEleType());
        this.src = src;
        this.tar = tar;
    }

    @Override
    public String toString() {
        return name + " = load " + type + ", " + src.getType() + " " + src.getName();
    }
}
