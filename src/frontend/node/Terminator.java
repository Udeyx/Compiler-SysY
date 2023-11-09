package frontend.node;

import frontend.*;
import util.NodeType;

public class Terminator extends Node {
    private final Token val;

    public Terminator(Token val) {
        super(NodeType.TERMINATOR);
        this.val = val;
    }

    public Token getVal() {
        return val;
    }


    @Override
    public String toString() {
        return val.toString();
    }
}
