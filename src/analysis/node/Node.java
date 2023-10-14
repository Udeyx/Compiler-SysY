package analysis.node;

import analysis.Error;
import analysis.Handler;
import util.DataType;
import util.ErrorType;
import util.NodeType;

import java.util.ArrayList;
import java.util.HashMap;

public class Node {
    private final NodeType nodeType;
    private final ArrayList<Node> children;

    public Node(NodeType nodeType) {
        this.nodeType = nodeType;
        this.children = new ArrayList<>();
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

    public void submitError(int lineNum, ErrorType errorType) {
        Handler.getInstance().recordError(new Error(lineNum, errorType));
    }

    public void check() {
        children.forEach(Node::check);
    }

    public DataType getDataType() {
        HashMap<DataType, Integer> dataTypeToDim = new HashMap<>();
        dataTypeToDim.put(DataType.VOID, -1);
        dataTypeToDim.put(DataType.INT, 0);
        dataTypeToDim.put(DataType.ONE, 1);
        dataTypeToDim.put(DataType.TWO, 2);
        int dim = -1;
        for (Node child : children) {
            int childDim = dataTypeToDim.get(child.getDataType());
            if (childDim > dim) {
                dim = childDim;
            }
        }
        return switch (dim) {
            case 2 -> DataType.TWO;
            case 1 -> DataType.ONE;
            case 0 -> DataType.INT;
            default -> DataType.VOID;
        };
    }

    public void traverse() { // postorder traversal
        children.forEach(Node::traverse);
        if (!nodeType.equals(NodeType.BLOCKITEM)
                && !nodeType.equals(NodeType.DECL)
                && !nodeType.equals(NodeType.BTYPE))
            System.out.println(this);
    }

    @Override
    public String toString() {
        return "<" + nodeType + ">";
    }
}
