package midend.ir.Value.instruction;

import midend.ir.Type.Type;
import midend.ir.Type.VoidType;
import midend.ir.Value.BasicBlock;
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
}
