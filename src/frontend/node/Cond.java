package frontend.node;

import frontend.node.exp.EqExp;
import frontend.node.exp.LOrExp;
import util.NodeType;

import java.util.ArrayList;

public class Cond extends Node {
    public Cond() {
        super(NodeType.COND);
    }

    public ArrayList<ArrayList<EqExp>> toFlat() {
        return ((LOrExp) children.get(0)).toFlat();
    }
}
