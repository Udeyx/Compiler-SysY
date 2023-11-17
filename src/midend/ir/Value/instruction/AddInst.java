package midend.ir.Value.instruction;

import backend.Register;
import midend.ir.Type.Type;
import midend.ir.Value.ConstantInt;
import midend.ir.Value.Value;
import util.BOType;

public class AddInst extends BinaryOperator {

    public AddInst(Type type, Value operand1, Value operand2, Value tar) {
        super(type, operand1, operand2, tar, BOType.ADD);
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        /*
         首先看两个操作数是不是Constant, 是的话要用li分配出寄存器
         由于operand只能是i32类型，因此不可能是指针，故不可能是全局变量，
         所以用不到la
         */
        if (operand1 instanceof ConstantInt) {
            mipsBuilder.buildLi(Register.T1, Integer.parseInt(operand1.getName()));
        } else {
            int offset = mipsBuilder.getSymbolPos(operand1.getName());
            mipsBuilder.buildLw(Register.T1, offset, Register.SP);
        }

        if (operand2 instanceof ConstantInt) {
            mipsBuilder.buildLi(Register.T2, Integer.parseInt(operand2.getName()));
        } else {
            int offset = mipsBuilder.getSymbolPos(operand2.getName());
            mipsBuilder.buildLw(Register.T2, offset, Register.SP);
        }

        mipsBuilder.buildAddu(Register.T0, Register.T1, Register.T2);
        int tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildSw(Register.T0, tarPos, Register.SP);
    }
}
