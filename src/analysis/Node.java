package analysis;

public abstract class Node {
    private final NodeType type;

    public Node(NodeType type) {
        this.type = type;
    }

    public abstract void addChild(Node node);
}
