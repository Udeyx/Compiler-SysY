package analysis;

import java.util.ArrayList;

public class Node {
    private final NodeType type;
    private final ArrayList<Node> children;
    private final ArrayList<Error> errors;

    public Node(NodeType type) {
        this.type = type;
        this.children = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public void addChild(Node node) {
        children.add(node);
    }

    public void addChild(int pos, Node node) {
        children.add(pos, node);
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void addError(Error error) {
        errors.add(error);
    }

    public ArrayList<Error> check() {
        return errors;
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
