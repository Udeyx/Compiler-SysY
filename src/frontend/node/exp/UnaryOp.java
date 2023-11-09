package frontend.node.exp;

import frontend.node.Node;
import frontend.node.Terminator;
import util.NodeType;
import util.TokenType;

public class UnaryOp extends Node {
    public UnaryOp() {
        super(NodeType.UNARYOP);
    }

    public TokenType getUnaryOp() {
        return ((Terminator) children.get(0)).getVal().getType();
    }
}
