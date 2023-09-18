package analysis.node;

import analysis.Node;
import analysis.NodeType;
import analysis.Token;

public class Terminator extends Node {
    private final Token val;

    public Terminator(Token val) {
        super(NodeType.TERMINATOR);
        this.val = val;
    }

    @Override
    public String toString() {
        return val.toString();
    }
}
