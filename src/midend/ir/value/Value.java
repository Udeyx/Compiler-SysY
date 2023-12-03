package midend.ir.value;

import backend.MIPSBuilder;
import midend.ir.type.Type;
import midend.ir.Use;

import java.util.ArrayList;

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

    @Override
    public String toString() {
        return super.toString();
    }

    public String asArg() {
        return type + " " + name;
    }
}
