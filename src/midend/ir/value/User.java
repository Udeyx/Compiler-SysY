package midend.ir.value;

import midend.ir.type.Type;

import java.util.ArrayList;

public class User extends Value {
    protected final ArrayList<Value> operands;

    public User(String name, Type type) {
        super(name, type);
        this.operands = new ArrayList<>();
    }

    public void replaceOperand(int pos, Value newOperand) {
        operands.set(pos, newOperand);
        newOperand.addUse(this, pos);
    }

    public void delUsesFromOperands() {
        for (int i = 0; i < operands.size(); i++) {
            operands.get(i).removeUse(this, i);
        }
    }
}
