package analysis.node.func;

import analysis.Token;
import analysis.node.*;
import symbol.FuncSymbol;
import symbol.Manager;
import util.DataType;
import util.ErrorType;
import util.NodeType;
import util.TokenType;

public class MainFuncDef extends Node {
    public MainFuncDef() {
        super(NodeType.MAINFUNCDEF);
    }

    @Override
    public void check() {
        // error b
        Manager manager = Manager.getInstance();
        FuncSymbol funcSymbol = genSymbol();
        boolean success = manager.addSymbol(funcSymbol);
        if (!success)
            submitError(getIdentity().getLineNum(), ErrorType.B);

        // go into new scope
        manager.setCurFunc(funcSymbol);
        manager.addScope();

        super.check();

        // go out of func scope
        manager.delScope();
        manager.setCurFunc(null);

        // error g
        Block block = (Block) getChildren().get(getChildren().size() - 1);
        int blockChildNum = block.getChildren().size();
        Token rBraceToken = ((Terminator) block.getChildren().get(blockChildNum - 1)).getVal();
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

    private FuncSymbol genSymbol() {
        return new FuncSymbol("main", DataType.INT);
    }

    private Token getIdentity() {
        return ((Terminator) getChildren().get(1)).getVal();
    }
}
