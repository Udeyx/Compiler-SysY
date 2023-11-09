package frontend.node;

import util.NodeType;


public class CompUnit extends Node {
    public CompUnit() {
        super(NodeType.COMPUNIT);
    }

    @Override
    public void check() {
        manager.addScope();
        super.check();
        manager.delScope();
    }

    @Override
    public void buildIR() {
        manager.addScope();
        super.buildIR();
        manager.delScope();
    }
}
