package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.ArrayType;
import midend.ir.type.PointerType;

public class AllocaInst extends Instruction {

    public AllocaInst(String name, PointerType type) {
        super(name, type);
    }

    @Override
    public String toString() {
        return name + " = alloca " + ((PointerType) type).getEleType();
    }

    /**
     * 这里要明确，用alloca会申请出一块空间用来存变量
     * 以及一个4byte的空间存指针，这两个不可混淆
     * 所以要先为变量声明空间，再为指针声明空间
     */
    @Override
    public void buildMIPS() {
        super.buildMIPS();
        int size;
        if (((PointerType) type).getEleType() instanceof ArrayType) {
            int eleNum = ((ArrayType) ((PointerType) type).getEleType()).getEleNum();
            size = eleNum * 4;
        } else
            size = 4;
        int startPos = mipsBuilder.allocAnonymousStackSpace(size);
        mipsBuilder.buildLi(Register.K0, startPos);
        mipsBuilder.buildAddu(Register.K0, Register.K0, Register.SP);
        int pointerPos = mipsBuilder.allocStackSpace(name);
        mipsBuilder.buildSw(Register.K0, pointerPos, Register.SP);
    }

    @Override
    public boolean canBeDel() {
        return false;
    }
}
