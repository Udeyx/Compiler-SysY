package frontend.node.exp;

import midend.ir.Value.Value;
import util.NodeType;
import frontend.node.Node;

public class Exp extends Node implements ValueHolder {
    public Exp() {
        super(NodeType.EXP);
    }

    @Override
    public int evaluate() {
        return children.get(0).evaluate();
    }

    @Override
    public void buildIR() {
        buildExpIR();
    }

    public Value buildExpIR() {
        return ((AddExp) children.get(0)).buildExpIR();
    }
}
