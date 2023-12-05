package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.IntegerType;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;
import util.ICmpType;
import util.OpCode;

import java.util.List;

public class ICmpInst extends Instruction {
    private final ICmpType iCmpType;
    private final Value tar;
    // operands: operand1, operand2

    public ICmpInst(ICmpType iCmpType, Value operand1, Value operand2, Value tar) {
        super(tar.getName(), IntegerType.I1);
        this.iCmpType = iCmpType;
        this.tar = tar;
        // maintain def use
        operand1.addUse(this, 0);
        operand2.addUse(this, 1);
        this.operands.addAll(List.of(operand1, operand2));
    }

    @Override
    public String toString() {
        Value operand1 = operands.get(0);
        Value operand2 = operands.get(1);
        return tar.getName() + " = icmp " + iCmpType + " " + operand1.getType() + " "
                + operand1.getName() + ", " + operand2.getName();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        Value operand1 = operands.get(0);
        Value operand2 = operands.get(1);
        if (operand1 instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K0, constantInt);
        } else {
            int op1Pos = mipsBuilder.getSymbolPos(operand1.getName());
            mipsBuilder.buildLw(Register.K0, op1Pos, Register.SP);
        }

        if (operand2 instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K1, constantInt);
        } else {
            int op2Pos = mipsBuilder.getSymbolPos(operand2.getName());
            mipsBuilder.buildLw(Register.K1, op2Pos, Register.SP);
        }

        OpCode opCode = switch (iCmpType) {
            case EQ -> OpCode.SEQ;
            case NE -> OpCode.SNE;
            case SGT -> OpCode.SGT;
            case SGE -> OpCode.SGE;
            case SLT -> OpCode.SLT;
            default -> OpCode.SLE;
        };

        mipsBuilder.buildCmp(Register.K0, Register.K0, Register.K1, opCode);
        int tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildSw(Register.K0, tarPos, Register.SP);
    }
}
