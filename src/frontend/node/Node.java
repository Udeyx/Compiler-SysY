package frontend.node;

import frontend.Error;
import frontend.Handler;
import frontend.symbol.SymbolManager;
import midend.ir.IRBuilder;
import midend.ir.Module;
import util.DataType;
import util.ErrorType;
import util.NodeType;

import java.util.ArrayList;
import java.util.HashMap;

public class Node {
    private final NodeType nodeType;
    protected final ArrayList<Node> children;
    protected final SymbolManager manager;
    protected final IRBuilder irBuilder;

    public Node(NodeType nodeType) {
        this.nodeType = nodeType;
        this.children = new ArrayList<>();
        this.manager = SymbolManager.getInstance();
        this.irBuilder = IRBuilder.getInstance();
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

    public void buildIR() {
        children.forEach(Node::buildIR);
    }

    public int evaluate() {
        return 0;
    }

    @Override
    public String toString() {
        return "<" + nodeType + ">";
    }
}
