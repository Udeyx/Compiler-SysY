package frontend.node.decl;

import frontend.node.Node;
import frontend.node.exp.Exp;
import midend.ir.Value.Value;
import util.NodeType;

import java.util.ArrayList;

public class InitVal extends Node {
    public InitVal() {
        super(NodeType.INITVAL);
    }

    public ArrayList<Integer> getInitVal(int dim) {
        ArrayList<Integer> initVal = new ArrayList<>();
        if (dim == 0) {
            initVal.add(children.get(0).evaluate());
        } else {
            children.stream().filter(child -> child instanceof InitVal)
                    .map(child -> (InitVal) child)
                    .map(child -> child.getInitVal(dim - 1))
                    .forEach(initVal::addAll);
        }
        return initVal;
    }

    public ArrayList<Value> getInitValues() {
        ArrayList<Value> initValues = new ArrayList<>();
        if (children.size() == 1) {
            initValues.add(((Exp) children.get(0)).buildExpIR());
        } else {
            children.stream().filter(child -> child instanceof InitVal)
                    .map(child -> ((InitVal) child).getInitValues())
                    .forEach(initValues::addAll);
        }
        return initValues;
    }
}
