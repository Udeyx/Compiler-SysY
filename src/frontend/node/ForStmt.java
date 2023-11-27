package frontend.node;

import frontend.node.exp.Exp;
import frontend.node.exp.LVal;
import midend.ir.value.Value;
import util.NodeType;

public class ForStmt extends Node {
    public ForStmt() {
        super(NodeType.FORSTMT);
    }

    @Override
    public void buildIR() {
        Value tar = ((LVal) children.get(0)).getLValPointer();
        Value src = ((Exp) children.get(2)).buildExpIR();
        irBuilder.buildStore(src, tar);
    }
}
