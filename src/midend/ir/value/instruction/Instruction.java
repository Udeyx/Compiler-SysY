package midend.ir.value.instruction;

import midend.ir.type.Type;
import midend.ir.value.User;

import java.util.Objects;

public class Instruction extends User {
    public Instruction(String name, Type type) {
        super(name, type);
    }

    public boolean canBeDel() {
        return uses.stream().noneMatch(Objects::nonNull);
    }
}
