package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.VoidType;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;

public class Move extends Instruction {
    private final Value src;
    private final Value tar;

    public Move(Value tar, Value src) {
        super("", VoidType.VOID);
        this.tar = tar;
        this.src = src;
    }

    @Override
    public String toString() {
        return "move " + tar.getName() + ", " + src.getName();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        if (src instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K0, constantInt);
        } else {
            int srcPos = mipsBuilder.getSymbolPos(src.getName());
            mipsBuilder.buildLw(Register.K0, srcPos, Register.SP);
        }
        int tarPos;
        if (mipsBuilder.hasSymbol(tar.getName()))
            tarPos = mipsBuilder.getSymbolPos(tar.getName());
        else
            tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildSw(Register.K0, tarPos, Register.SP);
    }
}
