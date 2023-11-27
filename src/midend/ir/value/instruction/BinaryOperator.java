package midend.ir.value.instruction;

import midend.ir.type.Type;
import midend.ir.value.Value;
import util.BOType;

public class BinaryOperator extends Instruction {

    private final BOType boType;
    protected final Value operand1;
    protected final Value operand2;
    protected final Value tar;

    public BinaryOperator(Type type, Value operand1, Value operand2, Value tar, BOType boType) {
        super(tar.getName(), type);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.tar = tar;
        this.boType = boType;
    }

    @Override
    public String toString() {
        return tar.getName() + " = " + boType.toString() + " " + type + " " + operand1.getName() + ", " + operand2.getName();
    }
}
