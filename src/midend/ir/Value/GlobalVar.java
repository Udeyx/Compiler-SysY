package midend.ir.Value;

import midend.ir.Type.ArrayType;
import midend.ir.Type.PointerType;
import midend.ir.Type.Type;

import java.util.ArrayList;

public class GlobalVar extends User {
    private final boolean isConst;
    private final ArrayList<Integer> initVal;

    public GlobalVar(String name, PointerType type, boolean isConst, ArrayList<Integer> initVal) {
        super(name, type);
        this.isConst = isConst;
        this.initVal = initVal;
    }

    @Override
    public String toString() {
        if (initVal.size() == 1) {
            String prefix = getName() + " = dso_local global " + ((PointerType) type).getEleType() + " ";
            return initVal.isEmpty() ? prefix + "0" : prefix + initVal.get(0);
        } else { // 1d array
            StringBuilder sb = new StringBuilder();
            Type eleType = (((PointerType) getType()).getEleType());
            sb.append(getName()).append(" = dso_local global ");
            sb.append(eleType);
            if (initVal.isEmpty()) {
                sb.append(" zeroinitializer");
            } else {
                sb.append(" [");
                for (int i = 0; i < initVal.size(); i++) {
                    sb.append(((ArrayType) eleType).getEleType()).append(" ").append(initVal.get(i));
                    if (i < initVal.size() - 1)
                        sb.append(", ");
                }
                sb.append("]");
            }
            return sb.toString();
        }
    }
}
