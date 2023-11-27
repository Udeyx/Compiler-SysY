package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.PointerType;
import midend.ir.value.GlobalVar;
import midend.ir.value.Value;

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

    /**
     * 对于普通的i32*，要lw两次，第一次取出指针指的地址
     * 第二次从对应地址中取出值
     */
    @Override
    public void buildMIPS() {
        super.buildMIPS();
        // load是从一个i32*里面读东西，因此要考虑src是全局变量
        if (src instanceof GlobalVar) {
            mipsBuilder.buildLa(Register.T1, src.getName());
            mipsBuilder.buildLw(Register.T0, 0, Register.T1);
        } else {
            int srcPos = mipsBuilder.getSymbolPos(src.getName());
            mipsBuilder.buildLw(Register.T1, srcPos, Register.SP);
            mipsBuilder.buildLw(Register.T0, 0, Register.T1);
        }
        int tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildSw(Register.T0, tarPos, Register.SP);
    }
}
