package frontend.node.exp;

import util.NodeType;
import frontend.node.Node;

import java.util.ArrayList;

public class LAndExp extends Node {
    public LAndExp() {
        super(NodeType.LANDEXP);
    }

    public ArrayList<EqExp> toFlat() {
        ArrayList<EqExp> flatCond = new ArrayList<>();
        if (children.size() == 1) {
            flatCond.add((EqExp) children.get(0));
        } else {
            flatCond.addAll(((LAndExp) children.get(0)).toFlat());
            flatCond.add((EqExp) children.get(2));
        }
        return flatCond;
    }
}
