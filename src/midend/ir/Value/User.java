package midend.ir.Value;

import midend.ir.Type.Type;

import java.util.ArrayList;

public class User extends Value {
    protected final ArrayList<Value> operands;

    public User(String name, Type type) {
        super(name, type);
        this.operands = new ArrayList<>();
    }
}
