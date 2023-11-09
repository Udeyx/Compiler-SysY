package frontend.node.func;

import frontend.Token;
import frontend.node.*;
import frontend.symbol.FuncSymbol;
import frontend.symbol.Symbol;
import midend.ir.Type.*;
import midend.ir.Value.Function;
import util.DataType;
import util.ErrorType;
import util.NodeType;
import util.TokenType;

import java.util.ArrayList;

public class FuncDef extends Node {
    private FuncSymbol funcSymbol;

    public FuncDef() {
        super(NodeType.FUNCDEF);
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

        setParams();
        super.check();
        // go out of func scope
        exit(false);

        // error g
        Block block = (Block) children.get(children.size() - 1);
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

    @Override
    public void buildIR() {
        manager.addSymbol(funcSymbol);

        Type returnType = funcSymbol.getDataType().equals(DataType.VOID) ? VoidType.VOID : IntegerType.I32;
        ArrayList<Type> argumentTypes = new ArrayList<>(
                funcSymbol.getParamTypes().stream().map(dt -> switch (dt) {
                    case ONE, TWO -> new PointerType(IntegerType.I32);
                    case INT -> IntegerType.I32;
                    default -> VoidType.VOID;
                }).toList()
        );
        FunctionType functionType = new FunctionType(argumentTypes, returnType);
        Function function = irBuilder.buildFunction(getIdentity().getVal(), functionType);
        funcSymbol.setLlvmObj(function);
        module.addFunction(function);

        // enter funcDef
        enter(true);

        super.buildIR();

        // add return if missed (only for void func)
        if (funcSymbol.getDataType().equals(DataType.VOID)) {
            Block block = (Block) children.get(children.size() - 1);
            if (block.getChildren().size() == 2) {
                irBuilder.buildReturn();
            } else {
                BlockItem lastItem = (BlockItem) block.getChildren().get(block.getChildren().size() - 2);
                if (lastItem.getChildren().get(0) instanceof Stmt) {
                    if (!((Stmt) lastItem.getChildren().get(0)).isReturn()) {
                        irBuilder.buildReturn();
                    }
                } else {
                    irBuilder.buildReturn();
                }
            }
        }

        // exit funcDef
        exit(true);
    }

    private FuncSymbol genSymbol() {
        FuncType funcType = (FuncType) children.get(0);
        TokenType returnType = ((Terminator) funcType.getChildren().get(0)).getVal().getType();
        DataType dataType = returnType.equals(TokenType.VOIDTK) ? DataType.VOID : DataType.INT;
        return new FuncSymbol(getIdentity().getVal(), dataType);
    }

    private Token getIdentity() {
        return ((Terminator) children.get(1)).getVal();
    }

    private void setParams() {
        if (children.get(3) instanceof FuncFParams) {
            FuncFParams funcFParams = (FuncFParams) children.get(3);
            for (DataType dataType : funcFParams.getFParamDataTypes()) {
                funcSymbol.addParam(dataType);
            }
        }
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
