package midend.ir.Value.instruction;

import midend.ir.Type.Type;
import midend.ir.Value.Value;

public class ZExtInst extends Instruction {
    private final Value src;
    private final Value tar;

    public ZExtInst(Value src, Value tar) {
        super(tar.getName(), tar.getType());
        this.src = src;
        this.tar = tar;
    }

    @Override
    public String toString() {
        return tar.getName() + " = zext " + src.getType() + " " + src.getName() + " to " + tar.getType();
    }
}
