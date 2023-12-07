package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.PointerType;
import midend.ir.value.GlobalVar;
import midend.ir.value.Value;

public class LoadInst extends Instruction {

    private final Value tar;
    // operands: src

    public LoadInst(Value src, Value tar) {
        super(tar.getName(), ((PointerType) src.getType()).getEleType());
        this.tar = tar;
        // maintain use def
        src.addUse(this, 0);
        this.operands.add(src);
    }

    public Value getSrc() {
        return operands.get(0);
    }

    @Override
    public String toString() {
        return tar.getName() + " = load " + type + ", " + operands.get(0).getType()
                + " " + operands.get(0).getName();
    }

    /**
     * 对于普通的i32*，要lw两次，第一次取出指针指的地址
     * 第二次从对应地址中取出值
     */
    @Override
    public void buildMIPS() {
        super.buildMIPS();
        // load是从一个i32*里面读东西，因此要考虑src是全局变量
        Value src = operands.get(0);
        if (src instanceof GlobalVar) {
            mipsBuilder.buildLa(Register.K1, src.getName());
            mipsBuilder.buildLw(Register.K0, 0, Register.K1);
        } else {
            int srcPos = mipsBuilder.getSymbolPos(src);
            mipsBuilder.buildLw(Register.K1, srcPos, Register.SP);
            mipsBuilder.buildLw(Register.K0, 0, Register.K1);
        }
        int tarPos = mipsBuilder.allocStackSpace(this);
        mipsBuilder.buildSw(Register.K0, tarPos, Register.SP);
    }

    @Override
    public void buildFIFOMIPS() {
        super.buildFIFOMIPS();
        Value src = operands.get(0);
        Register srcReg;
        if (src instanceof GlobalVar) {
            mipsBuilder.buildLa(Register.K1, src.getName());
            srcReg = Register.K1;
        } else {
            srcReg = mipsBuilder.getSymbolReg(src);
            if (srcReg == null) {
                int srcPos = mipsBuilder.getSymbolPos(src);
                mipsBuilder.buildLw(Register.K1, srcPos, Register.SP);
                srcReg = Register.K1;
            }
        }
        int tarPos = mipsBuilder.allocStackSpace(this);
        Register tarReg = mipsBuilder.allocReg(this);
        mipsBuilder.buildLw(tarReg, 0, srcReg);
    }
}
