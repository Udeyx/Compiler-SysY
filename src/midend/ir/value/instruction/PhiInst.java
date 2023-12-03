package midend.ir.value.instruction;

import midend.ir.type.Type;
import midend.ir.value.BasicBlock;
import midend.ir.value.Value;

import java.util.List;

public class PhiInst extends Instruction {
    private final Value tar;
    // operands: src1, option1, src2, option2, ......

    public PhiInst(Type type, Value option1, BasicBlock src1, Value option2,
                   BasicBlock src2, Value tar, String name) {
        super(name, type);
        this.tar = tar;
        // maintain def use should be done when insert content to phi
//        src1.addUse(this);
//        src2.addUse(this);
//        option1.addUse(this);
//        option2.addUse(this);
//        this.operands.addAll(List.of(src1, src2, option1, option2));
    }

    public void addAnOption(Value src, Value option) {
        src.addUse(this, operands.size());
        operands.add(src);
        option.addUse(this, operands.size());
        operands.add(option);
    }

    public Value getTar() {
        return tar;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = phi ").append(type).append(" ");
        for (int i = 0; i < operands.size(); i += 2) {
            sb.append("[ ");
            sb.append(operands.get(i).getName()).append(", %").append(operands.get(i + 1).getName());
            if (i + 2 < operands.size())
                sb.append(" ], ");
            else
                sb.append(" ]");
        }
        return sb.toString();
    }
}