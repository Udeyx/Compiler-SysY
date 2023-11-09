package frontend.node.decl;

import frontend.node.Node;
import util.NodeType;

import java.util.ArrayList;

public class ConstInitVal extends Node {
    public ConstInitVal() {
        super(NodeType.CONSTINITVAL);
    }

    public ArrayList<Integer> getInitVal(int dim) {
        ArrayList<Integer> initVal = new ArrayList<>();
        if (dim == 0) {
            initVal.add(children.get(0).evaluate());
        } else {
            children.stream().filter(child -> child instanceof ConstInitVal)
                    .map(child -> (ConstInitVal) child)
                    .map(child -> child.getInitVal(dim - 1))
                    .forEach(initVal::addAll);
        }
        return initVal;
    }
}
