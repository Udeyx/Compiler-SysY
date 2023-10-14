package analysis.node;

import analysis.Token;
import analysis.node.exp.ConstExp;
import symbol.ConSymbol;
import symbol.Manager;
import util.ErrorType;
import util.NodeType;

public class ConstDef extends Node {
    public ConstDef() {
        super(NodeType.CONSTDEF);
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

    private ConSymbol genSymbol() {
        int dim = 0;
        for (Node child : getChildren()) {
            if (child instanceof ConstExp)
                dim++;
        }
        return new ConSymbol(getIdentity().getVal(), dim);
    }

    private Token getIdentity() {
        return ((Terminator) getChildren().get(0)).getVal();
    }
}
