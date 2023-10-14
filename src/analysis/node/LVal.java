package analysis.node;

import analysis.Token;
import analysis.node.exp.Exp;
import symbol.ConSymbol;
import symbol.Manager;
import symbol.Symbol;
import util.DataType;
import util.ErrorType;
import util.NodeType;

public class LVal extends Node {
    public LVal() {
        super(NodeType.LVAL);
    }

    @Override
    public void check() {
        // error c
        Manager manager = Manager.getInstance();
        if (manager.getSymbol(getIdentity().getVal()) == null)
            submitError(getIdentity().getLineNum(), ErrorType.C);
        super.check();
    }

    public boolean isConst() {
        Manager manager = Manager.getInstance();
        Symbol symbol = manager.getSymbol(getIdentity().getVal());
        return symbol instanceof ConSymbol;
    }

    @Override
    public DataType getDataType() {
        Manager manager = Manager.getInstance();
        Symbol symbol = manager.getSymbol(getIdentity().getVal());
        if (symbol == null) {
            return DataType.VOID;
        } else {
            int dim = symbol.getDim();
            for (Node child : getChildren()) {
                if (child instanceof Exp)
                    dim--;
            }
            return switch (dim) {
                case 2 -> DataType.TWO;
                case 1 -> DataType.ONE;
                case 0 -> DataType.INT;
                default -> DataType.VOID;
            };
        }
    }

    public Token getIdentity() {
        return ((Terminator) getChildren().get(0)).getVal();
    }
}
