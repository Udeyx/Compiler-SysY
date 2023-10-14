package analysis.node.exp;

import analysis.Token;
import analysis.node.Node;
import analysis.node.Terminator;
import analysis.node.func.FuncRParams;
import symbol.FuncSymbol;
import symbol.Manager;
import util.DataType;
import util.ErrorType;
import util.NodeType;

import java.util.ArrayList;

public class UnaryExp extends Node {
    public UnaryExp() {
        super(NodeType.UNARYEXP);
    }

    @Override
    public void check() {
        if (isFuncCall()) {
            Manager manager = Manager.getInstance();
            Token identity = ((Terminator) getChildren().get(0)).getVal();
            FuncSymbol funcSymbol = (FuncSymbol) manager.getSymbol(identity.getVal());
            // error c
            if (funcSymbol == null) {
                submitError(identity.getLineNum(), ErrorType.C);
            } else { // error d
                ArrayList<DataType> fParams = funcSymbol.getParamTypes();
                ArrayList<DataType> rParams = new ArrayList<>();
                if (getChildren().get(2) instanceof FuncRParams)
                    rParams = ((FuncRParams) getChildren().get(2)).getRParamDataTypes();
                if (fParams.size() != rParams.size()) {
                    submitError(identity.getLineNum(), ErrorType.D);
                } else { // error e
                    for (int i = 0; i < fParams.size(); i++) {
                        if (!fParams.get(i).equals(rParams.get(i))) {
                            submitError(identity.getLineNum(), ErrorType.E);
                            break;
                        }
                    }
                }
            }
        }
        super.check();
    }

    private boolean isPrimaryExp() {
        return getChildren().get(0) instanceof PrimaryExp;
    }

    private boolean isFuncCall() {
        return getChildren().get(0) instanceof Terminator;
    }

    @Override
    public DataType getDataType() {
        if (isPrimaryExp()) {
            return getChildren().get(0).getDataType();
        } else if (isFuncCall()) {
            Manager manager = Manager.getInstance();
            Token identity = ((Terminator) getChildren().get(0)).getVal();
            FuncSymbol funcSymbol = (FuncSymbol) manager.getSymbol(identity.getVal());
            if (funcSymbol != null)
                return funcSymbol.getDataType();
            else
                return DataType.VOID;
        } else {
            return super.getDataType();
        }
    }
}
