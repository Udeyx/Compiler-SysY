package midend.ir.Value.instruction;

import backend.Register;
import midend.ir.Type.Type;
import midend.ir.Type.VoidType;
import midend.ir.Value.BasicBlock;
import midend.ir.Value.ConstantInt;
import midend.ir.Value.Value;

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
