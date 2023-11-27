package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.VoidType;
import midend.ir.value.ConstantInt;
import midend.ir.value.GlobalVar;
import midend.ir.value.Value;

public class StoreInst extends Instruction {
    private final Value src;
    private final Value tar;

    public StoreInst(Value src, Value tar) {
        super("", VoidType.VOID);
        this.src = src;
        this.tar = tar;
    }

    @Override
    public String toString() {
        return "store " + src.getType() + " " + src.getName() + ", "
                + tar.getType() + " " + tar.getName();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        // store是把一个i32存到i32*里面，因此要考虑tar是全局变量
        if (src instanceof ConstantInt) {
            mipsBuilder.buildLi(Register.T0, Integer.parseInt(src.getName()));
        } else {
            int srcPos = mipsBuilder.getSymbolPos(src.getName());
            mipsBuilder.buildLw(Register.T0, srcPos, Register.SP);
        }
        if (tar instanceof GlobalVar) {
            mipsBuilder.buildLa(Register.T1, tar.getName());
            mipsBuilder.buildSw(Register.T0, 0, Register.T1);
        } else {
            int pointerPos = mipsBuilder.getSymbolPos(tar.getName());
            mipsBuilder.buildLw(Register.T1, pointerPos, Register.SP);
            mipsBuilder.buildSw(Register.T0, 0, Register.T1);
        }
    }
}
