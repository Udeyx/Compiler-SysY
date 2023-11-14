package midend.ir.Value.instruction;

import midend.ir.Type.PointerType;
import midend.ir.Value.Value;

import java.util.ArrayList;

public class GEPInst extends Instruction {
    private final Value pointer;
    private final ArrayList<Value> indexes;
    private final Value tar;

    public GEPInst(Value pointer, Value tar) {
        super(tar.getName(), tar.getType());
        this.pointer = pointer;
        this.indexes = new ArrayList<>();
        this.tar = tar;
    }

    public void addIndex(Value index) {
        indexes.add(index);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" = getelementptr ");
        sb.append(((PointerType) pointer.getType()).getEleType());
        sb.append(", ");
        sb.append(pointer.getType()).append(" ");
        sb.append(pointer.getName()).append(", ");
        for (int i = 0; i < indexes.size(); i++) {
            sb.append(indexes.get(i).getType()).append(" ");
            sb.append(indexes.get(i).getName());
            if (i < indexes.size() - 1)
                sb.append(", ");
        }
        return sb.toString();
    }
}
