package analysis.node;

import analysis.Node;
import analysis.NodeType;
import analysis.Token;

public class Decl extends Node {
    private final Node child; // child could be ConstDecl or VarDecl

    public Decl(Node specificDecl) {
        super(NodeType.DECL);
        this.child = specificDecl;
    }

    @Override
    public void addChild(Node node) { // meaningless since Decl can only have one child
    }
}
