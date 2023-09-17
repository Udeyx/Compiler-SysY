package analysis;

import java.util.ArrayList;

public class Node {
    private final NodeType type;
    private final ArrayList<Node> children;

    public Node(NodeType type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    public void addChild(Node node) {
        children.add(node);
    }
}
