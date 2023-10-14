package analysis.node.func;

import analysis.Token;
import analysis.node.*;
import symbol.FuncSymbol;
import symbol.Manager;
import util.DataType;
import util.ErrorType;
import util.NodeType;
import util.TokenType;

public class FuncDef extends Node {
    private FuncSymbol funcSymbol;

    public FuncDef() {
        super(NodeType.FUNCDEF);
        this.funcSymbol = null;
    }

    @Override
    public void check() {
        Manager manager = Manager.getInstance();
        // error b
        funcSymbol = genSymbol();
        boolean success = manager.addSymbol(funcSymbol);
        if (!success)
            submitError(getIdentity().getLineNum(), ErrorType.B);

        // go into new scope
        manager.setCurFunc(funcSymbol);
        manager.addScope();

        setParams();
        super.check();
        // go out of func scope
        manager.delScope();
        manager.setCurFunc(null);

        // error g
        Block block = (Block) getChildren().get(getChildren().size() - 1);
        int blockChildNum = block.getChildren().size();
        Token rBraceToken = ((Terminator) block.getChildren().get(blockChildNum - 1)).getVal();
        if (funcSymbol.getDataType().equals(DataType.INT)) {
            if (blockChildNum == 2) {
                submitError(rBraceToken.getLineNum(), ErrorType.G);
            } else {
                BlockItem lastItem = (BlockItem) block.getChildren().get(blockChildNum - 2);
                if (lastItem.getChildren().get(0) instanceof Stmt) {
                    if (!((Stmt) lastItem.getChildren().get(0)).isReturn()) {
                        submitError(rBraceToken.getLineNum(), ErrorType.G);
                    }
                } else {
                    submitError(rBraceToken.getLineNum(), ErrorType.G);
                }
            }

        }
    }

    private FuncSymbol genSymbol() {
        FuncType funcType = (FuncType) getChildren().get(0);
        TokenType returnType = ((Terminator) funcType.getChildren().get(0)).getVal().getType();
        DataType dataType = returnType.equals(TokenType.VOIDTK) ? DataType.VOID : DataType.INT;
        return new FuncSymbol(getIdentity().getVal(), dataType);
    }

    private Token getIdentity() {
        return ((Terminator) getChildren().get(1)).getVal();
    }

    private void setParams() {
        if (getChildren().get(3) instanceof FuncFParams) {
            FuncFParams funcFParams = (FuncFParams) getChildren().get(3);
            for (DataType dataType : funcFParams.getFParamDataTypes()) {
                funcSymbol.addParam(dataType);
            }
        }
    }

}
