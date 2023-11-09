package midend.ir.Type;

import java.util.ArrayList;

public class FunctionType extends Type {
    private final ArrayList<Type> paramTypes;
    private final Type returnType;

    public FunctionType(ArrayList<Type> paramTypes, Type returnType) {
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    public ArrayList<Type> getParamTypes() {
        return paramTypes;
    }

    public Type getReturnType() {
        return returnType;
    }
}
