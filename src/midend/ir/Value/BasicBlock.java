package midend.ir.Value;

import midend.ir.Type.LabelType;
import midend.ir.Type.Type;
import midend.ir.Value.instruction.Instruction;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private final ArrayList<Instruction> instructions;

    public BasicBlock(String name) {
        super(name, LabelType.LABEL);
        this.instructions = new ArrayList<>();
    }

    public void addInst(Instruction inst) {
        instructions.add(inst);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (Instruction inst : instructions) {
            sb.append("    ");
            sb.append(inst);
            sb.append("\n");
        }
        return sb.toString();
    }
}
