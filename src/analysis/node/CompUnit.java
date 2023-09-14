package analysis.node;

import analysis.Node;
import analysis.NodeType;
import analysis.Token;

import java.util.ArrayList;

public class CompUnit extends Node {
    private final ArrayList<Node> children;

    public CompUnit() {
        super(NodeType.COMPUNIT);
        this.children = new ArrayList<>();
    }

    @Override
    public void addChild(Node node) {
        children.add(node);
    }
}
