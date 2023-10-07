package analysis.node;

import analysis.Node;
import analysis.NodeType;
import cross.SymManager;
import cross.SymTable;


public class CompUnit extends Node {
    public CompUnit() {
        super(NodeType.COMPUNIT);
    }

    @Override
    public void check() {
        SymManager.getInstance().push(new SymTable());
        super.check();
        SymManager.getInstance().pop(); // make the table list empty
    }
}
