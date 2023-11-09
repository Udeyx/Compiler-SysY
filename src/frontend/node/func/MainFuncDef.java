package frontend.node.func;

import frontend.Token;
import frontend.node.*;
import frontend.symbol.FuncSymbol;
import midend.ir.Type.*;
import midend.ir.Value.Function;
import util.DataType;
import util.ErrorType;
import util.NodeType;

import java.util.ArrayList;

public class MainFuncDef extends Node {
    private FuncSymbol funcSymbol;

    public MainFuncDef() {
        super(NodeType.MAINFUNCDEF);
        this.funcSymbol = null;
    }

    @Override
    public void check() {
        // error b
        funcSymbol = genSymbol();
        boolean success = manager.addSymbol(funcSymbol);
        if (!success)
            submitError(getIdentity().getLineNum(), ErrorType.B);

        // go into new scope
        enter(false);

        super.check();

        // go out of func scope
        exit(false);

        // error g
        Block block = (Block) children.get(children.size() - 1);
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

    @Override
    public void buildIR() {
        manager.addSymbol(funcSymbol);

        Type returnType = IntegerType.I32;
        FunctionType functionType = new FunctionType(new ArrayList<>(), returnType);
        Function function = irBuilder.buildFunction("main", functionType);
        funcSymbol.setLlvmObj(function);
        module.addFunction(function);

        // enter funcDef
        enter(true);

        super.buildIR();

        // exit funcDef
        exit(true);
    }

    private FuncSymbol genSymbol() {
        return new FuncSymbol("main", DataType.INT);
    }

    private Token getIdentity() {
        return ((Terminator) children.get(1)).getVal();
    }

    private void enter(boolean genning) {
        manager.setCurFunc(funcSymbol);
        manager.addScope();
        if (genning)
            irBuilder.setCurFunction((Function) funcSymbol.getLlvmObj());
        manager.setInGlobal(false);
    }

    private void exit(boolean genning) {
        manager.delScope();
        manager.setCurFunc(null);
        if (genning)
            irBuilder.setCurFunction(null);
        manager.setInGlobal(true);
    }
}
