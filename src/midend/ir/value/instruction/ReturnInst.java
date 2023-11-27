package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.VoidType;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;

public class ReturnInst extends Instruction {
    private final Value src;

    public ReturnInst(Value src) {
        super(src.getName(), src.getType());
        this.src = src;
    }

    public ReturnInst() {
        super("", VoidType.VOID);
        this.src = null;
    }

    @Override
    public String toString() {
        if (type.equals(VoidType.VOID)) {
            return "ret void\n";
        } else {
            return "ret " + type + " " + name;
        }
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        if (!type.equals(VoidType.VOID)) {
            if (src instanceof ConstantInt) {
                mipsBuilder.buildLi(Register.V0, Integer.parseInt(src.getName()));
            } else {
                int srcPos = mipsBuilder.getSymbolPos(src.getName());
                mipsBuilder.buildLw(Register.V0, srcPos, Register.SP);
            }

        }
        mipsBuilder.buildJr(Register.RA);
    }
}
