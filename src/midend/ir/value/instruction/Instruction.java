package midend.ir.value.instruction;

import midend.ir.type.Type;
import midend.ir.value.User;

public class Instruction extends User {
    public Instruction(String name, Type type) {
        super(name, type);
    }
}
