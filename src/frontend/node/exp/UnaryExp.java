package frontend.node.exp;

import frontend.Token;
import frontend.node.Node;
import frontend.node.Terminator;
import frontend.node.func.FuncRParams;
import frontend.symbol.FuncSymbol;
import midend.ir.type.IntegerType;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;
import util.*;

import java.util.ArrayList;

public class UnaryExp extends Node implements ValueHolder {
    public UnaryExp() {
        super(NodeType.UNARYEXP);
    }

    @Override
    public void check() {
        if (isFuncCall()) {
            Token identity = ((Terminator) children.get(0)).getVal();
            FuncSymbol funcSymbol = (FuncSymbol) manager.getSymbol(identity.getVal());
            // error c
            if (funcSymbol == null) {
                submitError(identity.getLineNum(), ErrorType.C);
            } else { // error d
                ArrayList<DataType> fParams = funcSymbol.getParamTypes();
                ArrayList<DataType> rParams = new ArrayList<>();
                if (children.get(2) instanceof FuncRParams)
                    rParams = ((FuncRParams) children.get(2)).getRParamDataTypes();
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
        return children.get(0) instanceof PrimaryExp;
    }

    private boolean isFuncCall() {
        return children.get(0) instanceof Terminator;
    }

    @Override
    public DataType getDataType() {
        if (isPrimaryExp()) {
            return children.get(0).getDataType();
        } else if (isFuncCall()) {
            Token identity = ((Terminator) children.get(0)).getVal();
            FuncSymbol funcSymbol = (FuncSymbol) manager.getSymbol(identity.getVal());
            if (funcSymbol != null)
                return funcSymbol.getDataType();
            else
                return DataType.VOID;
        } else {
            return super.getDataType();
        }
    }

    @Override
    public int evaluate() {
        int sum;
        if (isPrimaryExp()) {
            sum = children.get(0).evaluate();
        } else if (isFuncCall()) { // can't evaluate function !!!
            sum = 0;
        } else { // UnaryOp UnaryExp
            sum = children.get(1).evaluate();
            if (((UnaryOp) children.get(0)).getUnaryOp().equals(TokenType.MINU))
                sum = -sum;
        }
        return sum;
    }

    @Override
    public void buildIR() {
        buildExpIR();
    }

    public Value buildExpIR() {
        if (isPrimaryExp()) {
            return ((PrimaryExp) children.get(0)).buildExpIR();
        } else if (isFuncCall()) {
            String funcName = ((Terminator) children.get(0)).getVal().getVal();
            return irBuilder.buildCall(funcName, buildArgumentsIR());
        } else { // UnaryOp UnaryExp
            return switch (((UnaryOp) children.get(0)).getUnaryOp()) {
                case PLUS -> ((UnaryExp) children.get(1)).buildExpIR();
                case MINU -> {
                    ConstantInt zeroCon = irBuilder.buildConstantInt(0);
                    yield irBuilder.buildSub(IntegerType.I32, zeroCon,
                            ((UnaryExp) children.get(1)).buildExpIR());
                }
                default -> { // !
                    ConstantInt zeroCon = irBuilder.buildConstantInt(0);
                    yield irBuilder.buildICmpWithLV(ICmpType.EQ, zeroCon,
                            ((UnaryExp) children.get(1)).buildExpIR());
                }
            };
        }
    }

    public ArrayList<Value> buildArgumentsIR() {
        if (children.stream().noneMatch(child -> child instanceof FuncRParams))
            return new ArrayList<>();
        return ((FuncRParams) children.get(children.size() - 2)).buildArgumentsIR();
    }
}
