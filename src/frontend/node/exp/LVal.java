package frontend.node.exp;

import frontend.Token;
import frontend.node.Node;
import frontend.node.Terminator;
import frontend.symbol.ConSymbol;
import frontend.symbol.Symbol;
import midend.ir.Type.IntegerType;
import midend.ir.Type.PointerType;
import midend.ir.Value.Value;
import midend.ir.Value.instruction.GEPInst;
import midend.ir.Value.instruction.LoadInst;
import midend.ir.Value.instruction.StoreInst;
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
        if (manager.getSymbol(getIdentity().getVal()) == null)
            submitError(getIdentity().getLineNum(), ErrorType.C);
        super.check();
    }

    public boolean isConst() {
        Symbol symbol = manager.getSymbol(getIdentity().getVal());
        return symbol instanceof ConSymbol;
    }

    @Override
    public DataType getDataType() {
        Symbol symbol = manager.getSymbol(getIdentity().getVal());
        if (symbol == null) {
            return DataType.VOID;
        } else {
            return switch (symbol.getDim() - getReduceDim()) {
                case 2 -> DataType.TWO;
                case 1 -> DataType.ONE;
                case 0 -> DataType.INT;
                default -> DataType.VOID;
            };
        }
    }

    public int getReduceDim() {
        return (int) children.stream().filter(child -> child instanceof Exp).count();
    }


    @Override
    public int evaluate() {
        Symbol symbol = manager.getSymbol(getIdentity().getVal());
        if (children.size() == 1)
            return symbol.getInitVal().get(0);
        else {
            if (getReduceDim() == 1) { // 1d array
                return symbol.getInitVal().get(children.get(2).evaluate());
            } else { // 2d array
                int index = children.get(2).evaluate() * children.get(5).evaluate();
                return symbol.getInitVal().get(index);
            }
        }
    }

    public Token getIdentity() {
        return ((Terminator) children.get(0)).getVal();
    }

    @Override
    public void buildIR() {
        super.buildIR();
    }

    public Value buildExpIR() {
        Symbol symbol = manager.getSymbol(getIdentity().getVal());
        if (symbol.getLlvmObj().getType().equals(new PointerType(IntegerType.I32))) {
            return irBuilder.buildLoad(symbol.getLlvmObj());
        } else { // 2d array should use MulInst
            System.out.println(symbol.getName() + " " + symbol.getLlvmObj().getType());
            if (getReduceDim() == 0) {
                return symbol.getLlvmObj();
            } else if (getReduceDim() == 1) { // real 1d array, index at 2
                Value index = ((Exp) children.get(2)).buildExpIR();
                GEPInst gepInst = irBuilder.buildGEP(symbol.getLlvmObj(), index);
                return irBuilder.buildLoad(gepInst);
            } else { // real 2d array, index at 2, 5
                Value index1 = ((Exp) children.get(2)).buildExpIR();
                Value index2 = ((Exp) children.get(5)).buildExpIR();
                Value index = irBuilder.buildMul(IntegerType.I32, index1, index2);
                GEPInst gepInst = irBuilder.buildGEP(symbol.getLlvmObj(), index);
                return irBuilder.buildLoad(gepInst);
            }
        }
    }

    public Value getLValPointer() {
        Symbol symbol = manager.getSymbol(getIdentity().getVal());
        if (symbol.getLlvmObj().getType().equals(new PointerType(IntegerType.I32))) {
            return symbol.getLlvmObj();
        } else {
            if (getReduceDim() == 1) { // real 1d array, index at 2
                Value index = ((Exp) children.get(2)).buildExpIR();
                return irBuilder.buildGEP(symbol.getLlvmObj(), index);
            } else { // real 2d array, index at 2, 5
                Value index1 = ((Exp) children.get(2)).buildExpIR();
                Value index2 = ((Exp) children.get(5)).buildExpIR();
                Value index = irBuilder.buildMul(IntegerType.I32, index1, index2);
                return irBuilder.buildGEP(symbol.getLlvmObj(), index);
            }
        }
    }
}
