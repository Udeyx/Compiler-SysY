package midend.ir.Value.instruction;

import midend.ir.Type.Type;
import midend.ir.Value.Value;
import util.BOType;

public class AddInst extends BinaryOperator {

    public AddInst(Type type, Value operand1, Value operand2, Value tar) {
        super(type, operand1, operand2, tar, BOType.ADD);
    }
}
