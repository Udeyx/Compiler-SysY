package midend.ir.Value.instruction;

import midend.ir.Type.IntegerType;
import midend.ir.Type.Type;
import midend.ir.Value.Value;
import util.ICmpType;

public class ICmpInst extends Instruction {
    private final Value operand1;
    private final Value operand2;
    private final ICmpType iCmpType;
    private final Value tar;

    public ICmpInst(ICmpType iCmpType, Value operand1, Value operand2, Value tar) {
        super(tar.getName(), IntegerType.I1);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.iCmpType = iCmpType;
        this.tar = tar;
    }

    @Override
    public String toString() {
        return tar.getName() + " = icmp " + iCmpType + " " + operand1.getType() + " "
                + operand1.getName() + ", " + operand2.getName();
    }
}
