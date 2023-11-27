package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.IntegerType;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;
import util.ICmpType;
import util.OpCode;

public class ICmpInst extends Instruction {
    private final Value operand1;
    private final Value operand2;
    private final ICmpType iCmpType;
    private final Value tar;

    public ICmpInst(ICmpType iCmpType, Value operand1, Value operand2, Value tar) {
        super(tar.getName(), IntegerType.I1);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.iCmpType = iCmpType;
        this.tar = tar;
    }

    @Override
    public String toString() {
        return tar.getName() + " = icmp " + iCmpType + " " + operand1.getType() + " "
                + operand1.getName() + ", " + operand2.getName();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        if (operand1 instanceof ConstantInt) {
            mipsBuilder.buildLi(Register.T1, Integer.parseInt(operand1.getName()));
        } else {
            int op1Pos = mipsBuilder.getSymbolPos(operand1.getName());
            mipsBuilder.buildLw(Register.T1, op1Pos, Register.SP);
        }

        if (operand2 instanceof ConstantInt) {
            mipsBuilder.buildLi(Register.T2, Integer.parseInt(operand2.getName()));
        } else {
            int op2Pos = mipsBuilder.getSymbolPos(operand2.getName());
            mipsBuilder.buildLw(Register.T2, op2Pos, Register.SP);
        }

        OpCode opCode = switch (iCmpType) {
            case EQ -> OpCode.SEQ;
            case NE -> OpCode.SNE;
            case SGT -> OpCode.SGT;
            case SGE -> OpCode.SGE;
            case SLT -> OpCode.SLT;
            default -> OpCode.SLE;
        };

        mipsBuilder.buildCmp(Register.T0, Register.T1, Register.T2, opCode);
        int tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildSw(Register.T0, tarPos, Register.SP);
    }
}
