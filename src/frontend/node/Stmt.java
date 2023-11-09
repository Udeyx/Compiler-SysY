package frontend.node;

import frontend.Token;
import frontend.node.exp.Exp;
import frontend.node.exp.LVal;
import frontend.symbol.FuncSymbol;
import midend.ir.Value.Argument;
import midend.ir.Value.ConstantInt;
import midend.ir.Value.Function;
import midend.ir.Value.Value;
import midend.ir.Value.instruction.CallInst;
import util.DataType;
import util.ErrorType;
import util.NodeType;
import util.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Stmt extends Node {
    public Stmt() {
        super(NodeType.STMT);
    }

    @Override
    public void check() {
        if (isPrintf()) {
            // error a
            Token fmtStrToken = ((Terminator) children.get(2)).getVal();
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
            for (Node child : children) {
                if (child instanceof Exp)
                    expNum++;
            }
            if (percentNum != expNum) {
                Token printfToken = ((Terminator) children.get(0)).getVal();
                submitError(printfToken.getLineNum(), ErrorType.L);
            }
        } else if (isReturn()) {
            FuncSymbol funcSymbol = manager.getCurFunc();
            Token returnToken = ((Terminator) children.get(0)).getVal();
            if (funcSymbol != null && funcSymbol.getDataType().equals(DataType.VOID)
                    && children.get(1) instanceof Exp)
                submitError(returnToken.getLineNum(), ErrorType.F);
        } else if (isAssignOrGetInt()) {
            LVal lVal = (LVal) children.get(0);
            if (lVal.isConst())
                submitError(lVal.getIdentity().getLineNum(), ErrorType.H);
        } else if (isBreak() || isContinue()) {
            if (manager.getLoopDepth() <= 0) {
                Token first = ((Terminator) children.get(0)).getVal();
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

    @Override
    public void buildIR() {
        if (isGetInt()) {
            Value tar = ((LVal) children.get(0)).getLValPointer();
            CallInst tempVal = irBuilder.buildCall(Function.GETINT, new ArrayList<>());
            irBuilder.buildStore(tempVal, tar);
        } else if (isPrintf()) {
            String fmtStr = ((Terminator) children.get(2)).getVal().getVal();
            ArrayList<Exp> realArgs = new ArrayList<>();
            children.stream().filter(child -> child instanceof Exp)
                    .map(child -> (Exp) child)
                    .forEach(realArgs::add);
            int curArgPos = 0;
            for (int i = 1; i < fmtStr.length() - 1; i++) {
                char c = fmtStr.charAt(i);
                if (c == '%' && fmtStr.charAt(i + 1) == 'd') {
                    Value arg = realArgs.get(curArgPos).buildExpIR();
                    irBuilder.buildCall(Function.PUTINT, new ArrayList<>(List.of(arg)));
                    i++;
                    curArgPos++;
                } else if (c == '\\' && fmtStr.charAt(i + 1) == 'n') {
                    ConstantInt constChar = irBuilder.buildConstantInt('\n');
                    irBuilder.buildCall(Function.PUTCH, new ArrayList<>(List.of(constChar)));
                } else {
                    ConstantInt constChar = irBuilder.buildConstantInt(c);
                    irBuilder.buildCall(Function.PUTCH, new ArrayList<>(List.of(constChar)));
                }
            }
        } else if (isAssign()) {
            Value tar = ((LVal) children.get(0)).getLValPointer();
            Value src = ((Exp) children.get(2)).buildExpIR();
            irBuilder.buildStore(src, tar);
        } else if (isExp()) {
            children.get(0).buildIR();
        } else if (isReturn()) {
            if (children.stream().noneMatch(child -> child instanceof Exp)) {
                irBuilder.buildReturn();
            } else {
                Value src = ((Exp) children.get(1)).buildExpIR();
                irBuilder.buildReturn(src);
            }
        } else if (isBlock()) {
            manager.addScope();
            super.buildIR();
            manager.delScope();
        }
    }

    public boolean isReturn() { // return
        return (children.get(0) instanceof Terminator)
                && ((Terminator) children.get(0)).getVal().getType().equals(TokenType.RETURNTK);
    }

    private boolean isAssignOrGetInt() {
        return children.get(0) instanceof LVal;
    }

    private boolean isGetInt() {
        return children.get(0) instanceof LVal
                && children.get(2) instanceof Terminator;
    }

    private boolean isAssign() {
        return isAssignOrGetInt() && !isGetInt();
    }

    private boolean isFor() {
        return (children.get(0) instanceof Terminator)
                && ((Terminator) children.get(0)).getVal().getType().equals(TokenType.FORTK);
    }

    private boolean isPrintf() {
        return (children.get(0) instanceof Terminator)
                && ((Terminator) children.get(0)).getVal().getType().equals(TokenType.PRINTFTK);
    }

    private boolean isBreak() {
        return (children.get(0) instanceof Terminator)
                && ((Terminator) children.get(0)).getVal().getType().equals(TokenType.BREAKTK);
    }

    private boolean isContinue() {
        return (children.get(0) instanceof Terminator)
                && ((Terminator) children.get(0)).getVal().getType().equals(TokenType.CONTINUETK);
    }

    private boolean isBlock() {
        return children.get(0) instanceof Block;
    }

    private boolean isExp() {
        return children.get(0) instanceof Exp;
    }
}