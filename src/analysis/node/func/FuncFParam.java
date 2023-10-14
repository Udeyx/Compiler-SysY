package analysis.node.func;

import analysis.Token;
import analysis.node.Node;
import analysis.node.Terminator;
import symbol.Manager;
import symbol.VarSymbol;
import util.DataType;
import util.ErrorType;
import util.NodeType;
import util.TokenType;


public class FuncFParam extends Node {

    public FuncFParam() {
        super(NodeType.FUNCFPARAM);
    }

    @Override
    public void check() {
        // error b
        Manager manager = Manager.getInstance();
        boolean success = manager.addSymbol(genSymbol());
        if (!success)
            submitError(getIdentity().getLineNum(), ErrorType.B);
        super.check();
    }

    public DataType getFParamDataType() {
        return genSymbol().getDataType();
    }

    private VarSymbol genSymbol() {
        int dim = 0;
        for (Node child : getChildren()) {
            if (child instanceof Terminator && ((Terminator) child).getVal().getType().equals(TokenType.LBRACK))
                dim++;
        }
        return new VarSymbol(getIdentity().getVal(), dim);
    }

    private Token getIdentity() {
        return ((Terminator) getChildren().get(1)).getVal();
    }
}
