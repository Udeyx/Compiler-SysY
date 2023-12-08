package midend.ir.value.instruction;

import midend.ir.type.Type;
import midend.ir.value.GlobalVar;
import midend.ir.value.User;
import midend.ir.value.Value;

import java.util.Objects;

public class Instruction extends User {

    public Instruction(String name, Type type) {
        super(name, type);
    }

    public boolean canBeDel() {
        return uses.stream().noneMatch(Objects::nonNull);
    }

    public String calGVNHash() {
        return null;
    }

    public boolean useGlobalVar() {
        for (Value operand : operands) {
            if (operand instanceof GlobalVar)
                return true;
        }
        return false;
    }
}
