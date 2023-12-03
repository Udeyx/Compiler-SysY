package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.VoidType;
import midend.ir.value.BasicBlock;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;

import java.util.List;

public class BranchInst extends Instruction {
    // operands: cond, trueBlock, falseBlock
    public BranchInst(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        super("", VoidType.VOID);
        // maintain defuse
        cond.addUse(this, 0);
        trueBlock.addUse(this, 1);
        falseBlock.addUse(this, 2);
        this.operands.addAll(List.of(cond, trueBlock, falseBlock));
    }

    @Override
    public String toString() {
        Value cond = operands.get(0);
        Value trueBlock = operands.get(1);
        Value falseBlock = operands.get(2);
        return trueBlock.equals(falseBlock) ? "br label %" + trueBlock.getName() :
                "br i1 " + cond.getName() +
                        ", label " + "%" + trueBlock.getName()
                        + ", label " + "%" + falseBlock.getName();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        Value cond = operands.get(0);
        Value trueBlock = operands.get(1);
        Value falseBlock = operands.get(2);
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
