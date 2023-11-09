package frontend.node.exp;

import midend.ir.Value.Value;
import util.DataType;
import util.NodeType;
import frontend.node.Node;

public class PrimaryExp extends Node {
    public PrimaryExp() {
        super(NodeType.PRIMARYEXP);
    }

    @Override
    public DataType getDataType() {
        Node first = children.get(0);
        if (first instanceof LVal) {
            return first.getDataType();
        } else if (first instanceof Number) {
            return DataType.INT;
        } else {
            return super.getDataType();
        }
    }

    @Override
    public int evaluate() {
        if (children.size() == 3) { // '(' Exp ')'
            return children.get(1).evaluate();
        } else {
            return children.get(0).evaluate();
        }
    }

    @Override
    public void buildIR() {
        buildExpIR();
    }

    public Value buildExpIR() {
        if (children.get(0) instanceof LVal) {
            return ((LVal) children.get(0)).buildExpIR();
        } else if (children.get(0) instanceof Number) {
            return ((Number) children.get(0)).buildExpIR();
        } else { // '(' Exp ')'
            return ((Exp) children.get(1)).buildExpIR();
        }
    }
}
