package midend.ir.value;

import backend.MIPSBuilder;
import midend.ir.type.Type;
import midend.ir.Use;

import java.util.ArrayList;
import java.util.Iterator;

public class Value {
    protected final String name;
    protected final Type type;
    protected final ArrayList<Use> uses;
    protected final MIPSBuilder mipsBuilder;

    public Value(String name, Type type) {
        this.name = name;
        this.type = type;
        this.mipsBuilder = MIPSBuilder.getInstance();
        this.uses = new ArrayList<>();
    }

    public void addUse(User user, int pos) {
        uses.add(new Use(this, user, pos));
    }

    public void removeUse(User user, int pos) { // 这个只是把对应位置置为null，因此遍历的时候要小心
        for (int i = 0; i < uses.size(); i++) {
            Use use = uses.get(i);
            if (use != null && use.getUser().equals(user) && i == pos) {
                uses.set(i, null);
                break;
            }
        }
    }

    public void replaceUseOfThisWith(Value newValue) {
        for (Use use : uses) {
            if (use != null)
                use.getUser().replaceOperand(use.getPos(), newValue);
        }
    }

    public ArrayList<Use> getUses() {
        return uses;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void buildMIPS() {
        mipsBuilder.buildComment(this.toString());
    }

    public void buildFIFOMIPS() {
        mipsBuilder.buildComment(this.toString());
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String asArg() {
        return type + " " + name;
    }

    public boolean isUseful() {
        for (Use use : uses) {
            if (use != null)
                return true;
        }
        return false;
    }
}
