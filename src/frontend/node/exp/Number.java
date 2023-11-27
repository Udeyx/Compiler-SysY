package frontend.node.exp;

import frontend.node.Node;
import frontend.node.Terminator;
import midend.ir.value.Value;
import util.NodeType;

public class Number extends Node implements ValueHolder {
    public Number() {
        super(NodeType.NUMBER);
    }

    @Override
    public int evaluate() {
        return Integer.parseInt(((Terminator) children.get(0)).getVal().getVal());
    }

    @Override
    public void buildIR() {
        buildExpIR();
    }

    public Value buildExpIR() {
        int numVal = Integer.parseInt(((Terminator) children.get(0)).getVal().getVal());
        return irBuilder.buildConstantInt(numVal);
    }
}
