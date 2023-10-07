package analysis.node;

import analysis.*;
import analysis.Error;
import cross.SymManager;
import cross.symbol.VarSymbol;

public class VarDef extends Node {
    public VarDef() {
        super(NodeType.VARDEF);
    }

    @Override
    public void check() {
        Token identity = ((Terminator) getChildren().get(0)).getVal();
        SymManager manager = SymManager.getInstance();
        if (manager.isRepeated(identity.getVal())) {
            System.out.println(new Error(identity.getLineNum(), ErrorType.B));
        } else {
            registerSymbol();
        }
        super.check();
    }

    private void registerSymbol() {
        boolean hasAssign = false;
        for (Node node : getChildren()) {
            if (node instanceof Terminator && ((Terminator) node).getVal().getType().equals(TokenType.ASSIGN)) {
                hasAssign = true;
                break;
            }
        }
        int dim = hasAssign ? (getChildren().size() - 3) / 2 : (getChildren().size() - 1) / 2;
        Token identity = ((Terminator) getChildren().get(0)).getVal();
        SymManager.getInstance().addDefinition(new VarSymbol(identity.getVal(), identity.getLineNum(), dim));
    }
}
