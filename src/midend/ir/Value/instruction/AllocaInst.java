package midend.ir.Value.instruction;

import midend.ir.Type.PointerType;

public class AllocaInst extends Instruction {

    public AllocaInst(String name, PointerType type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return name + " = alloca " + ((PointerType) type).getEleType();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        mipsBuilder.allocStackSpace(name);
    }
}
