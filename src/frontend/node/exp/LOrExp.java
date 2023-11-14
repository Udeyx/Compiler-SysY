package frontend.node.exp;

import util.NodeType;
import frontend.node.Node;

import java.util.ArrayList;

public class LOrExp extends Node {
    public LOrExp() {
        super(NodeType.LOREXP);
    }

    public ArrayList<ArrayList<EqExp>> toFlat() {
        ArrayList<ArrayList<EqExp>> flatCond = new ArrayList<>();
        if (children.size() == 1) {
            flatCond.add(((LAndExp) children.get(0)).toFlat());
        } else {
            flatCond.addAll(((LOrExp) children.get(0)).toFlat());
            flatCond.add(((LAndExp) children.get(2)).toFlat());
        }
        return flatCond;
    }
}
