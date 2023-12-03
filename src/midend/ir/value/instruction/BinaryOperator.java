package midend.ir.value.instruction;

import midend.ir.type.Type;
import midend.ir.value.Value;
import util.BOType;

import java.util.List;

public class BinaryOperator extends Instruction {

    private final BOType boType;
    protected final Value tar;
    // operands: operand1, operand2

    public BinaryOperator(Type type, Value operand1, Value operand2, Value tar, BOType boType) {
        super(tar.getName(), type);
        this.tar = tar;
        this.boType = boType;
        // maintain def use
        operand1.addUse(this, 0);
        operand2.addUse(this, 1);
        this.operands.addAll(List.of(operand1, operand2));
    }

    @Override
    public String toString() {
        return tar.getName() + " = " + boType.toString() + " " + type + " "
                + operands.get(0).getName() + ", " + operands.get(1).getName();
    }
}
