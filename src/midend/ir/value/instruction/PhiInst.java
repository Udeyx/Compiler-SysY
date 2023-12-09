package midend.ir.value.instruction;

import midend.ir.type.Type;
import midend.ir.value.BasicBlock;
import midend.ir.value.Value;

import java.util.HashMap;
import java.util.HashSet;

public class PhiInst extends Instruction {
    private final Value tar;
    // operands: src1, option1, src2, option2, ......

    public PhiInst(Type type, Value tar, String name) {
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

    public HashMap<BasicBlock, HashSet<Value>> getOptSrcMap() {
        HashMap<BasicBlock, HashSet<Value>> optSrcMap = new HashMap<>();
        for (int i = 0; i < operands.size(); i += 2) {
            BasicBlock block = (BasicBlock) operands.get(i + 1);
            Value src = operands.get(i);
            if (!optSrcMap.containsKey(block)) {
                optSrcMap.put(block, new HashSet<>());
            }
            optSrcMap.get(block).add(src);
        }
        return optSrcMap;
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
