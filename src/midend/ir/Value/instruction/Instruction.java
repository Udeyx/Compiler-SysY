package midend.ir.Value.instruction;

import midend.ir.Type.Type;
import midend.ir.Value.User;

public class Instruction extends User {
    public Instruction(String name, Type type) {
        super(name, type);
    }
}
