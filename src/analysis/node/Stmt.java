package analysis.node;

import analysis.Token;
import analysis.node.exp.Exp;
import symbol.FuncSymbol;
import symbol.Manager;
import util.DataType;
import util.ErrorType;
import util.NodeType;
import util.TokenType;

public class Stmt extends Node {
    public Stmt() {
        super(NodeType.STMT);
    }

    @Override
    public void check() {
        Manager manager = Manager.getInstance();
        if (isPrintf()) {
            // error a
            Token fmtStrToken = ((Terminator) getChildren().get(2)).getVal();
            String fmtStr = fmtStrToken.getVal();
            for (int i = 1; i < fmtStr.length() - 1; i++) {
                char c = fmtStr.charAt(i);
                if (c == 32 || c == 33 || (c >= 40 && c <= 126 && c != '\\'))
                    continue;
                if (c == '\\' && fmtStr.charAt(i + 1) == 'n')
                    continue;
                if (c == '%' && fmtStr.charAt(i + 1) == 'd')
                    continue;
                submitError(fmtStrToken.getLineNum(), ErrorType.A);
                super.check();
                return;
            }
            // error l
            int percentNum = 0;
            for (int i = 1; i < fmtStr.length() - 1; i++) {
                char c = fmtStr.charAt(i);
                if (c == '%' && fmtStr.charAt(i + 1) == 'd')
                    percentNum++;
            }
            int expNum = 0;
            for (Node child : getChildren()) {
                if (child instanceof Exp)
                    expNum++;
            }
            if (percentNum != expNum) {
                Token printfToken = ((Terminator) getChildren().get(0)).getVal();
                submitError(printfToken.getLineNum(), ErrorType.L);
            }
        } else if (isReturn()) {
            FuncSymbol funcSymbol = manager.getCurFunc();
            Token returnToken = ((Terminator) getChildren().get(0)).getVal();
            if (funcSymbol != null && funcSymbol.getDataType().equals(DataType.VOID)
                    && getChildren().get(1) instanceof Exp)
                submitError(returnToken.getLineNum(), ErrorType.F);
        } else if (isAssignOrGetInt()) {
            LVal lVal = (LVal) getChildren().get(0);
            if (lVal.isConst())
                submitError(lVal.getIdentity().getLineNum(), ErrorType.H);
        } else if (isBreak() || isContinue()) {
            if (manager.getLoopDepth() <= 0) {
                Token first = ((Terminator) getChildren().get(0)).getVal();
                submitError(first.getLineNum(), ErrorType.M);
            }
        } else if (isFor()) {
            manager.setLoopDepth(manager.getLoopDepth() + 1);
            super.check();
            manager.setLoopDepth(manager.getLoopDepth() - 1);
            return;
        } else if (isBlock()) {
            manager.addScope();
            super.check();
            manager.delScope();
            return;
        }
        super.check();
    }


    public boolean isReturn() { // return
        return (getChildren().get(0) instanceof Terminator)
                && ((Terminator) getChildren().get(0)).getVal().getType().equals(TokenType.RETURNTK);
    }

    private boolean isAssignOrGetInt() {
        return getChildren().get(0) instanceof LVal;
    }

    private boolean isFor() {
        return (getChildren().get(0) instanceof Terminator)
                && ((Terminator) getChildren().get(0)).getVal().getType().equals(TokenType.FORTK);
    }

    private boolean isPrintf() {
        return (getChildren().get(0) instanceof Terminator)
                && ((Terminator) getChildren().get(0)).getVal().getType().equals(TokenType.PRINTFTK);
    }

    private boolean isBreak() {
        return (getChildren().get(0) instanceof Terminator)
                && ((Terminator) getChildren().get(0)).getVal().getType().equals(TokenType.BREAKTK);
    }

    private boolean isContinue() {
        return (getChildren().get(0) instanceof Terminator)
                && ((Terminator) getChildren().get(0)).getVal().getType().equals(TokenType.CONTINUETK);
    }

    private boolean isBlock() {
        return getChildren().get(0) instanceof Block;
    }
}