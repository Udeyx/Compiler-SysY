package frontend.node.exp;

import util.NodeType;
import frontend.node.Node;

public class ConstExp extends Node {
    public ConstExp() {
        super(NodeType.CONSTEXP);
    }

    @Override
    public int evaluate() {
        return children.get(0).evaluate();
    }
}
