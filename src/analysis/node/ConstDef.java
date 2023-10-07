package analysis.node;

import analysis.*;
import analysis.Error;
import cross.SymManager;
import cross.Symbol;
import cross.symbol.ConstSymbol;

public class ConstDef extends Node {
    public ConstDef() {
        super(NodeType.CONSTDEF);
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
        Token identity = ((Terminator) getChildren().get(0)).getVal();
        SymManager.getInstance().addDefinition(new ConstSymbol(identity.getVal(), identity.getLineNum(),
                (getChildren().size() - 3) / 2));
    }
}
