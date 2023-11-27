package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.VoidType;
import midend.ir.value.BasicBlock;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;

public class BranchInst extends Instruction {
    private final Value cond;
    private final BasicBlock trueBlock;
    private final BasicBlock falseBlock;

    public BranchInst(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        super("", VoidType.VOID);
        this.cond = cond;
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
    }

    @Override
    public String toString() {
        return "br i1 " + cond.getName() + ", label " + "%" + trueBlock.getName()
                + ", label " + "%" + falseBlock.getName();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        if (cond instanceof ConstantInt) {
            mipsBuilder.buildLi(Register.T0, Integer.parseInt(cond.getName()));
        } else {
            int condPos = mipsBuilder.getSymbolPos(cond.getName());
            mipsBuilder.buildLw(Register.T0, condPos, Register.SP);
        }
        mipsBuilder.buildBne(Register.T0, Register.ZERO, trueBlock.getName());
        mipsBuilder.buildJ(falseBlock.getName());
    }
}
