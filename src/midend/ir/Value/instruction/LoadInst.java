package midend.ir.Value.instruction;

import backend.Register;
import midend.ir.Type.PointerType;
import midend.ir.Value.GlobalVar;
import midend.ir.Value.Value;

public class LoadInst extends Instruction {

    private final Value src;
    private final Value tar;

    public LoadInst(Value src, Value tar) {
        super(tar.getName(), ((PointerType) src.getType()).getEleType());
        this.src = src;
        this.tar = tar;
    }

    @Override
    public String toString() {
        return tar.getName() + " = load " + type + ", " + src.getType() + " " + src.getName();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        // load是从一个i32*里面读东西，因此要考虑src是全局变量
        if (src instanceof GlobalVar) {
            mipsBuilder.buildLa(Register.T1, src.getName());
            mipsBuilder.buildLw(Register.T0, 0, Register.T1);
        } else {
            int srcPos = mipsBuilder.getSymbolPos(src.getName());
            mipsBuilder.buildLw(Register.T0, srcPos, Register.SP);
        }
        int tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildSw(Register.T0, tarPos, Register.SP);
    }
}