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

    public void addChild(int pos, Node node) {
        children.add(pos, node);
    }

    public void traverse() { // postorder traversal
        children.forEach(Node::traverse);
        if (!type.equals(NodeType.BLOCKITEM)
                && !type.equals(NodeType.DECL)
                && !type.equals(NodeType.BTYPE))
            System.out.println(this);
    }

    @Override
    public String toString() {
        return "<" + type + ">";
    }
}
