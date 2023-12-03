package frontend.node;

import frontend.Token;
import frontend.node.exp.EqExp;
import frontend.node.exp.Exp;
import frontend.node.exp.LVal;
import frontend.symbol.FuncSymbol;
import midend.ir.value.*;
import midend.ir.value.instruction.CallInst;
import midend.ir.value.instruction.ICmpInst;
import util.*;

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
            ArrayList<Exp> realArgs = new ArrayList<>(
                    children.stream()
                            .filter(child -> child instanceof Exp)
                            .map(child -> (Exp) child)
                            .toList());
            int curArgPos = 0;
            StringBuilder curStr = new StringBuilder();
            for (int i = 1; i < fmtStr.length() - 1; i++) {
                char c = fmtStr.charAt(i);
                if (c == '%' && fmtStr.charAt(i + 1) == 'd') {
                    // print the str before %d if curStr is not empty
                    if (!curStr.isEmpty()) {
                        Value sl = irBuilder.buildStringLiteral(curStr.toString());
                        Value slPtr = irBuilder.buildGEPForPutStr(sl, 0);
                        irBuilder.buildCall(Function.PUTSTR, new ArrayList<>(List.of(slPtr)));
                        curStr = new StringBuilder();
                    }

                    Value arg = realArgs.get(curArgPos).buildExpIR();
                    irBuilder.buildCall(Function.PUTINT, new ArrayList<>(List.of(arg)));
                    i++;
                    curArgPos++;
                } else if (c == '\\' && fmtStr.charAt(i + 1) == 'n') {
                    curStr.append("\n");
                    i++;
                } else {
                    curStr.append(c);
                }
            }
            if (!curStr.toString().isEmpty()) {
                Value sl = irBuilder.buildStringLiteral(curStr.toString());
                Value slPtr = irBuilder.buildGEPForPutStr(sl, 0);
                irBuilder.buildCall(Function.PUTSTR, new ArrayList<>(List.of(slPtr)));
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
        } else if (isIf()) {
            buildIfIR();
        } else if (isFor()) {
            buildForIR();
        } else if (isContinue()) {
            irBuilder.buildNoCondBranch(irBuilder.getCurIncreaseBlock());
        } else if (isBreak()) {
            irBuilder.buildNoCondBranch(irBuilder.getCurFinalBlock());
        }
    }

    private void buildForIR() {
        // build the first forStmt
        if (children.get(2) instanceof ForStmt)
            children.get(2).buildIR();
        BasicBlock loopBody = irBuilder.buildBasicBlockWithCurFunc();
        BasicBlock finalBlock = irBuilder.buildBasicBlockWithCurFunc();
        BasicBlock condBlock = irBuilder.buildBasicBlockWithCurFunc();
        BasicBlock increaseBlock = irBuilder.buildBasicBlockWithCurFunc();
        irBuilder.buildNoCondBranch(condBlock); // jump from curBlock to condBlock

        // set the curIncreaseBlock and curFinalBlock
        BasicBlock preIncreaseBlock = irBuilder.getCurIncreaseBlock();
        BasicBlock preFinalBlock = irBuilder.getCurFinalBlock();
        irBuilder.setCurIncreaseBlock(increaseBlock);
        irBuilder.setCurFinalBlock(finalBlock);


        // build cond, set cond to 1 if cond doesn't exist
        irBuilder.setCurBasicBlock(condBlock);
        irBuilder.getCurFunction().addBasicBlock(condBlock);
        // build loop body's IR
        if (children.stream().noneMatch(child -> child instanceof Cond)) {
            irBuilder.buildNoCondBranch(loopBody); // jump to loopBody
        } else { // has implicit cond
            ArrayList<ArrayList<EqExp>> flatCond = children.stream()
                    .filter(child -> child instanceof Cond)
                    .map(child -> ((Cond) child).toFlat())
                    .toList()
                    .get(0);
            buildLOrIR(flatCond, loopBody, finalBlock);
        }
        // build loop body's IR
        irBuilder.setCurBasicBlock(loopBody);
        irBuilder.getCurFunction().addBasicBlock(loopBody);
        children.get(children.size() - 1).buildIR();
        irBuilder.buildNoCondBranch(increaseBlock);

        // build IR for the optional second forStmt
        irBuilder.setCurBasicBlock(increaseBlock);
        irBuilder.getCurFunction().addBasicBlock(increaseBlock);
        if (children.get(children.size() - 3) instanceof ForStmt)
            children.get(children.size() - 3).buildIR();

        // jump to condBlock
        irBuilder.buildNoCondBranch(condBlock);

        irBuilder.getCurFunction().addBasicBlock(finalBlock);
        irBuilder.setCurBasicBlock(finalBlock);

        // clear the curIncreaseBlock and curFinalBlock
        irBuilder.setCurFinalBlock(preFinalBlock);
        irBuilder.setCurIncreaseBlock(preIncreaseBlock);
    }

    private void buildIfIR() {
        ArrayList<ArrayList<EqExp>> flatCond = ((Cond) children.get(2)).toFlat();
        BasicBlock trueBlock = irBuilder.buildBasicBlockWithCurFunc();
        BasicBlock finalBlock = irBuilder.buildBasicBlockWithCurFunc();

        if (children.size() < 7) { // no else
            // start to build cond
            buildLOrIR(flatCond, trueBlock, finalBlock);

            // set and build true block
            irBuilder.setCurBasicBlock(trueBlock);
            irBuilder.getCurFunction().addBasicBlock(trueBlock);
            children.get(4).buildIR(); // build true stmt
            irBuilder.buildNoCondBranch(finalBlock);

        } else {
            BasicBlock falseBlock = irBuilder.buildBasicBlockWithCurFunc();

            // start to build cond
            buildLOrIR(flatCond, trueBlock, falseBlock);

            // set and build true block
            irBuilder.setCurBasicBlock(trueBlock);
            irBuilder.getCurFunction().addBasicBlock(trueBlock);
            children.get(4).buildIR(); // build true stmt
            irBuilder.buildNoCondBranch(finalBlock);

            // set and build false block
            irBuilder.setCurBasicBlock(falseBlock);
            irBuilder.getCurFunction().addBasicBlock(falseBlock);
            children.get(6).buildIR();
            irBuilder.buildNoCondBranch(finalBlock);
        }
        irBuilder.getCurFunction().addBasicBlock(finalBlock);
        irBuilder.setCurBasicBlock(finalBlock);
    }

    private void buildLOrIR(ArrayList<ArrayList<EqExp>> flatCond,
                            BasicBlock trueBlock, BasicBlock falseBlock) {
        // alloc a basicBlock for each LAndExp
        ArrayList<BasicBlock> lAndBlocks = new ArrayList<>();
        for (int i = 0; i < flatCond.size(); i++) {
            lAndBlocks.add(irBuilder.buildBasicBlockWithCurFunc());
        }

        // add br to the previous block
        irBuilder.buildNoCondBranch(lAndBlocks.get(0));

        for (int i = 0; i < flatCond.size() - 1; i++) { // this loop won't reach the last LAndExp
            irBuilder.setCurBasicBlock(lAndBlocks.get(i));
            buildLAndIR(flatCond.get(i), trueBlock, lAndBlocks.get(i + 1));
        }
        // build IR for the last LAndExp
        irBuilder.setCurBasicBlock(lAndBlocks.get(lAndBlocks.size() - 1));
        buildLAndIR(flatCond.get(flatCond.size() - 1), trueBlock, falseBlock);
    }

    private void buildLAndIR(ArrayList<EqExp> flatCond,
                             BasicBlock trueBlock, BasicBlock falseBlock) {
        // alloc a basicBlock for each EqExp except the first one
        ArrayList<BasicBlock> eqBlocks = new ArrayList<>();
        eqBlocks.add(irBuilder.getCurBasicBlock());
        for (int i = 0; i < flatCond.size() - 1; i++) {
            eqBlocks.add(irBuilder.buildBasicBlockWithCurFunc());
        }
        for (int i = 0; i < flatCond.size() - 1; i++) { // won't reach the last EqExp
            irBuilder.setCurBasicBlock(eqBlocks.get(i));
            Value cond = flatCond.get(i).buildExpIR();
            ConstantInt zeroCon = irBuilder.buildConstantInt(0);
            ICmpInst iCmpInst = irBuilder.buildICmpWithLV(ICmpType.NE, cond, zeroCon);
            irBuilder.buildBranch(iCmpInst, eqBlocks.get(i + 1), falseBlock);
        }
        // build IR for the last EqExp
        irBuilder.setCurBasicBlock(eqBlocks.get(eqBlocks.size() - 1));
        Value cond = flatCond.get(flatCond.size() - 1).buildExpIR();
        ConstantInt zeroCon = irBuilder.buildConstantInt(0);
        ICmpInst iCmpInst = irBuilder.buildICmpWithLV(ICmpType.NE, cond, zeroCon);
        irBuilder.buildBranch(iCmpInst, trueBlock, falseBlock);
        eqBlocks.forEach(irBuilder.getCurFunction()::addBasicBlock);
    }


    public boolean isReturn() { // return
        return (children.get(0) instanceof Terminator)
                && ((Terminator) children.get(0)).getVal().getType().equals(TokenType.RETURNTK);
    }

    private boolean isIf() {// if
        return children.get(0) instanceof Terminator
                && ((Terminator) children.get(0)).getVal().getType().equals(TokenType.IFTK);
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