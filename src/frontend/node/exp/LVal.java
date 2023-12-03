package frontend.node.exp;

import frontend.Token;
import frontend.node.Node;
import frontend.node.Terminator;
import frontend.symbol.ConSymbol;
import frontend.symbol.Symbol;
import midend.ir.type.IntegerType;
import midend.ir.type.PointerType;
import midend.ir.type.Type;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;
import midend.ir.value.instruction.GEPInst;
import util.DataType;
import util.ErrorType;
import util.NodeType;

public class LVal extends Node implements ValueHolder {
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
        Type symbolLLVMType = symbol.getLlvmObj().getType();
        if (symbol.getDataType().equals(DataType.INT)) {
            return irBuilder.buildLoad(symbol.getLlvmObj());
        } else {
            switch (getReduceDim()) {
                case 0 -> { // as param
                    if (symbolLLVMType.equals(new PointerType(IntegerType.I32))) // i32*
                        return symbol.getLlvmObj();
                    return irBuilder.buildGEPWithZeroPrep(symbol.getLlvmObj(), 0); // [n * i32]*
                }
                case 1 -> { // use content or as param
                    if (symbol.getDataType().equals(DataType.ONE)) { // 1d array use content
                        Value index = ((Exp) children.get(2)).buildExpIR();
                        GEPInst gepInst;
                        if (symbolLLVMType.equals(new PointerType(IntegerType.I32))) {
                            gepInst = irBuilder.buildGEP(symbol.getLlvmObj(), index);
                        } else {
                            gepInst = irBuilder.buildGEPWithZeroPrep(symbol.getLlvmObj(), index);
                        }
                        return irBuilder.buildLoad(gepInst);
                    } else { // 2d array use 1d param
                        Value index1 = ((Exp) children.get(2)).buildExpIR();
                        ConstantInt secondDimSize = irBuilder.buildConstantInt(symbol.getSecondDimSize());
                        Value baseAddr = irBuilder.buildMul(IntegerType.I32, index1, secondDimSize);
                        if (symbolLLVMType.equals(new PointerType(IntegerType.I32)))
                            return irBuilder.buildGEP(symbol.getLlvmObj(), baseAddr);
                        else
                            return irBuilder.buildGEPWithZeroPrep(symbol.getLlvmObj(), baseAddr);
                    }
                }
                default -> { // 2
                    Value index1 = ((Exp) children.get(2)).buildExpIR();
                    Value index2 = ((Exp) children.get(5)).buildExpIR();
                    ConstantInt secondDimSize = irBuilder.buildConstantInt(symbol.getSecondDimSize());
                    Value baseAddr = irBuilder.buildMul(IntegerType.I32, index1, secondDimSize);
                    Value index = irBuilder.buildAddWithLV(IntegerType.I32, index2, baseAddr);
                    GEPInst gepInst;
                    if (symbolLLVMType.equals(new PointerType(IntegerType.I32)))
                        gepInst = irBuilder.buildGEP(symbol.getLlvmObj(), index);
                    else
                        gepInst = irBuilder.buildGEPWithZeroPrep(symbol.getLlvmObj(), index);
                    return irBuilder.buildLoad(gepInst);
                }
            }
        }
    }

    public Value getLValPointer() {
        Symbol symbol = manager.getSymbol(getIdentity().getVal());
        Type symbolLLVMType = symbol.getLlvmObj().getType();
        if (symbol.getDataType().equals(DataType.INT)) {
            return symbol.getLlvmObj();
        } else {
            if (getReduceDim() == 1) { // real 1d array, index at 2
                Value index = ((Exp) children.get(2)).buildExpIR();
                if (symbolLLVMType.equals(new PointerType(IntegerType.I32))) {
                    return irBuilder.buildGEP(symbol.getLlvmObj(), index);
                } else {
                    return irBuilder.buildGEPWithZeroPrep(symbol.getLlvmObj(), index);
                }
            } else { // real 2d array, index at 2, 5
                Value index1 = ((Exp) children.get(2)).buildExpIR();
                Value index2 = ((Exp) children.get(5)).buildExpIR();
                ConstantInt secondDimSize = irBuilder.buildConstantInt(symbol.getSecondDimSize());
                Value baseAddr = irBuilder.buildMul(IntegerType.I32, index1, secondDimSize);
                Value index = irBuilder.buildAddWithLV(IntegerType.I32, index2, baseAddr);
                if (symbolLLVMType.equals(new PointerType(IntegerType.I32)))
                    return irBuilder.buildGEP(symbol.getLlvmObj(), index);
                else return irBuilder.buildGEPWithZeroPrep(symbol.getLlvmObj(), index);
            }
        }
    }
}
