package analysis.node;

import analysis.Token;
import analysis.node.exp.ConstExp;
import symbol.Manager;
import symbol.VarSymbol;
import util.ErrorType;
import util.NodeType;

public class VarDef extends Node {
    public VarDef() {
        super(NodeType.VARDEF);
    }

    @Override
    public void check() {
        super.check();
        // error b
        Manager manager = Manager.getInstance();
        boolean success = manager.addSymbol(genSymbol());
        if (!success)
            submitError(getIdentity().getLineNum(), ErrorType.B);
    }

    private VarSymbol genSymbol() {
        int dim = 0;
        for (Node child : getChildren()) {
            if (child instanceof ConstExp)
                dim++;
        }
        return new VarSymbol(getIdentity().getVal(), dim);
    }

    private Token getIdentity() {
        return ((Terminator) getChildren().get(0)).getVal();
    }
}
