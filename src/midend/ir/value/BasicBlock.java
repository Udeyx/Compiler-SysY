package midend.ir.value;

import midend.ir.type.LabelType;
import midend.ir.value.instruction.Instruction;

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

    @Override
    public void buildMIPS() {
        mipsBuilder.buildLabel(name);
        instructions.forEach(Instruction::buildMIPS);
    }
}