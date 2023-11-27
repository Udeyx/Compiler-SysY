package frontend.node.decl;

import frontend.Token;
import frontend.node.Node;
import frontend.node.Terminator;
import frontend.node.exp.ConstExp;
import frontend.symbol.ConSymbol;
import midend.ir.type.ArrayType;
import midend.ir.type.IntegerType;
import midend.ir.type.PointerType;
import midend.ir.type.Type;
import midend.ir.value.GlobalVar;
import midend.ir.value.instruction.AllocaInst;
import midend.ir.value.instruction.GEPInst;
import util.ErrorType;
import util.NodeType;

import java.util.ArrayList;

public class ConstDef extends Node {
    private ConSymbol conSymbol;

    public ConstDef() {
        super(NodeType.CONSTDEF);
        this.conSymbol = null;
    }

    @Override
    public void check() {
        super.check();
        // error b
        conSymbol = new ConSymbol(getIdentity().getVal(), getDim());
        boolean success = manager.addSymbol(conSymbol);
        if (!success)
            submitError(getIdentity().getLineNum(), ErrorType.B);
    }

    @Override
    public void buildIR() {
        ArrayList<Integer> initVal = getInitVal();
        Type eleType = getDim() == 0 ? IntegerType.I32 : new ArrayType(IntegerType.I32, getEleNum());
        PointerType type = new PointerType(eleType);
        if (manager.isInGlobal()) {
            GlobalVar globalVar = irBuilder.buildGlobalVar(type, true, initVal);
            conSymbol.setLlvmObj(globalVar);
            conSymbol.setInitVal(initVal);
            manager.addSymbol(conSymbol);
            if (children.stream().filter(child -> child instanceof ConstExp)
                    .count() == 2) { // 2d array
                conSymbol.setSecondDimSize(children.get(5).evaluate());
            }
        } else {
            AllocaInst allocaInst = irBuilder.buildAlloca(type);
            conSymbol.setLlvmObj(allocaInst);
            conSymbol.setInitVal(initVal);
            manager.addSymbol(conSymbol);
            if (children.stream().filter(child -> child instanceof ConstExp)
                    .count() == 2) { // 2d array
                conSymbol.setSecondDimSize(children.get(5).evaluate());
            }
            if (type.equals(new PointerType(IntegerType.I32))) {
                irBuilder.buildStore(irBuilder.buildConstantInt(initVal.get(0)), allocaInst);
            } else {
                for (int i = 0; i < initVal.size(); i++) {
                    GEPInst gepInst = irBuilder.buildGEPWithZeroPrep(allocaInst, i);
                    irBuilder.buildStore(irBuilder.buildConstantInt(initVal.get(i)), gepInst);
                }
            }

        }
    }

    private int getEleNum() {
        int eleNum = 1;
        for (Node child : children) {
            if (child instanceof ConstExp)
                eleNum *= child.evaluate();
        }
        return eleNum;
    }

    private int getDim() {
        return (int) children.stream().filter(child -> child instanceof ConstExp).count();
    }

    private Token getIdentity() {
        return ((Terminator) children.get(0)).getVal();
    }

    private ArrayList<Integer> getInitVal() {
        return ((ConstInitVal) children.get(children.size() - 1)).getInitVal(getDim());
    }
}
