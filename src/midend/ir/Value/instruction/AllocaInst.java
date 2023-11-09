package midend.ir.Value.instruction;

import midend.ir.Type.PointerType;
import midend.ir.Type.Type;

public class AllocaInst extends Instruction {

    public AllocaInst(String name, PointerType type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return name + " = alloca " + ((PointerType) type).getEleType();
    }
}
