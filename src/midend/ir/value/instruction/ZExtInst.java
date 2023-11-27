package midend.ir.value.instruction;

import backend.Register;
import midend.ir.value.Value;

public class ZExtInst extends Instruction {
    private final Value src;
    private final Value tar;

    public ZExtInst(Value src, Value tar) {
        super(tar.getName(), tar.getType());
        this.src = src;
        this.tar = tar;
    }

    @Override
    public String toString() {
        return tar.getName() + " = zext " + src.getType() + " " + src.getName() + " to " + tar.getType();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        // src只可能是i1的变量，不可能是ConstantInt，因为ConstantInt都是i32的
        int srcPos = mipsBuilder.getSymbolPos(src.getName());
        int tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildLw(Register.T0, srcPos, Register.SP);
        mipsBuilder.buildSw(Register.T0, tarPos, Register.SP);
    }
}
