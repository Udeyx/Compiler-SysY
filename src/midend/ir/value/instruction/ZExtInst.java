package midend.ir.value.instruction;

import backend.Register;
import midend.ir.value.Value;

public class ZExtInst extends Instruction {
    private final Value tar;

    public ZExtInst(Value src, Value tar) {
        super(tar.getName(), tar.getType());
        this.tar = tar;
        src.addUse(this, 0);
        this.operands.add(src);
    }

    @Override
    public String toString() {
        Value src = operands.get(0);
        return tar.getName() + " = zext " + src.getType() + " " + src.getName() + " to " + tar.getType();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        // src只可能是i1的变量，不可能是ConstantInt，因为ConstantInt都是i32的
        Value src = operands.get(0);
        int srcPos = mipsBuilder.getSymbolPos(src.getName());
        int tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildLw(Register.K0, srcPos, Register.SP);
        mipsBuilder.buildSw(Register.K0, tarPos, Register.SP);
    }
}
