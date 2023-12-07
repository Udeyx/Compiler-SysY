package midend.ir.value.instruction;

import midend.ir.type.Type;
import midend.ir.value.Value;
import util.BOType;

public class AddInst extends BinaryOperator {

    public AddInst(Type type, Value operand1, Value operand2, Value tar) {
        super(type, operand1, operand2, tar, BOType.ADD);
    }
}
