package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.Type;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;
import util.BOType;

public class SremInst extends BinaryOperator {
    public SremInst(Type type, Value operand1, Value operand2, Value tar) {
        super(type, operand1, operand2, tar, BOType.SREM);
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        /*
         首先看两个操作数是不是Constant, 是的话要用li分配出寄存器
         由于operand只能是i32类型，因此不可能是指针，故不可能是全局变量，
         所以用不到la
         */
        Value operand1 = operands.get(0);
        Value operand2 = operands.get(1);
        if (operand1 instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K0, constantInt);
        } else {
            int offset = mipsBuilder.getSymbolPos(operand1.getName());
            mipsBuilder.buildLw(Register.K0, offset, Register.SP);
        }

        if (operand2 instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K1, constantInt);
        } else {
            int offset = mipsBuilder.getSymbolPos(operand2.getName());
            mipsBuilder.buildLw(Register.K1, offset, Register.SP);
        }

        mipsBuilder.buildDiv(Register.K0, Register.K1);
        mipsBuilder.buildMfhi(Register.K0);
        int tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildSw(Register.K0, tarPos, Register.SP);

    }
}
