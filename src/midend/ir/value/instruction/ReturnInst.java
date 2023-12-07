package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.VoidType;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;

public class ReturnInst extends Instruction {

    // operands: src
    public ReturnInst(Value src) {
        super("", src.getType());
        src.addUse(this, 0);
        this.operands.add(src);
    }

    public ReturnInst() {
        super("", VoidType.VOID);
    }

    @Override
    public String toString() {
        if (type.equals(VoidType.VOID)) {
            return "ret void\n";
        } else {
            return "ret " + type + " " + operands.get(0).getName();
        }
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        if (!type.equals(VoidType.VOID)) {
            Value src = operands.get(0);
            if (src instanceof ConstantInt constantInt) {
                mipsBuilder.buildLi(Register.V0, constantInt);
            } else {
                int srcPos = mipsBuilder.getSymbolPos(src);
                mipsBuilder.buildLw(Register.V0, srcPos, Register.SP);
            }

        }
        mipsBuilder.buildJr(Register.RA);
    }

    @Override
    public boolean canBeDel() {
        return false;
    }

    @Override
    public void buildFIFOMIPS() {
        super.buildFIFOMIPS();
        if (!type.equals(VoidType.VOID)) {
            Value src = operands.get(0);
            if (src instanceof ConstantInt constantInt) {
                mipsBuilder.buildLi(Register.V0, constantInt);
            } else {
                Register srcReg = mipsBuilder.getSymbolReg(src);
                if (srcReg != null) {
                    mipsBuilder.buildAddu(Register.V0, Register.ZERO, srcReg);
                } else {
                    int srcPos = mipsBuilder.getSymbolPos(src);
                    mipsBuilder.buildLw(Register.V0, srcPos, Register.SP);
                }
            }

        }
        mipsBuilder.buildJr(Register.RA);
    }
}
