package analysis.node;

import symbol.Manager;
import util.NodeType;


public class CompUnit extends Node {
    public CompUnit() {
        super(NodeType.COMPUNIT);
    }

    @Override
    public void check() {
        Manager manager = Manager.getInstance();
        manager.addScope();
        super.check();
        manager.delScope();
    }
}
