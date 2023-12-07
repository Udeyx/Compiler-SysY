package midend.ir.value;

import midend.ir.type.ArrayType;
import midend.ir.type.IntegerType;
import midend.ir.type.PointerType;
import midend.ir.type.Type;

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
        if (type.equals(new PointerType(IntegerType.I32))) {
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

    @Override
    public void buildMIPS() {
        Type eleType = ((PointerType) type).getEleType();
        if (eleType.equals(IntegerType.I32)) {
            if (initVal.isEmpty())
                mipsBuilder.buildSpace(name, 1);
            else
                mipsBuilder.buildWord(name, initVal);
        } else {
            if (initVal.isEmpty())
                mipsBuilder.buildSpace(name, ((ArrayType) eleType).getEleNum());
            else
                mipsBuilder.buildWord(name, initVal);
        }
    }

    @Override
    public void buildFIFOMIPS() {
        Type eleType = ((PointerType) type).getEleType();
        if (eleType.equals(IntegerType.I32)) {
            if (initVal.isEmpty())
                mipsBuilder.buildSpace(name, 1);
            else
                mipsBuilder.buildWord(name, initVal);
        } else {
            if (initVal.isEmpty())
                mipsBuilder.buildSpace(name, ((ArrayType) eleType).getEleNum());
            else
                mipsBuilder.buildWord(name, initVal);
        }
    }
}
