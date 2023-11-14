package frontend.node.decl;

import frontend.Token;
import frontend.node.Node;
import frontend.node.Terminator;
import frontend.node.exp.ConstExp;
import frontend.symbol.VarSymbol;
import midend.ir.Type.ArrayType;
import midend.ir.Type.IntegerType;
import midend.ir.Type.PointerType;
import midend.ir.Type.Type;
import midend.ir.Value.GlobalVar;
import midend.ir.Value.Value;
import midend.ir.Value.instruction.AllocaInst;
import midend.ir.Value.instruction.GEPInst;
import util.ErrorType;
import util.NodeType;

import java.util.ArrayList;
import java.util.List;

public class VarDef extends Node {
    private VarSymbol varSymbol;

    public VarDef() {
        super(NodeType.VARDEF);
        this.varSymbol = null;
    }

    @Override
    public void check() {
        super.check();
        // error b
        varSymbol = new VarSymbol(getIdentity().getVal(), getDim());
        boolean success = manager.addSymbol(varSymbol);
        if (!success)
            submitError(getIdentity().getLineNum(), ErrorType.B);
    }

    @Override
    public void buildIR() {
        Type eleType = getDim() == 0 ? IntegerType.I32 : new ArrayType(IntegerType.I32, getEleSize());
        PointerType type = new PointerType(eleType);
        if (manager.isInGlobal()) {
            ArrayList<Integer> initVal = getInitVal();
            GlobalVar globalVar = irBuilder.buildGlobalVar(type, false, initVal);
            module.addGlobalVar(globalVar);
            varSymbol.setLlvmObj(globalVar);
            varSymbol.setInitVal(initVal);
            manager.addSymbol(varSymbol);
            if (children.stream().filter(child -> child instanceof ConstExp)
                    .count() == 2) { // 2d array
                varSymbol.setSecondDimSize(children.get(5).evaluate());
            }
        } else {
            AllocaInst allocaInst = irBuilder.buildAlloca(type);
            varSymbol.setLlvmObj(allocaInst);
            varSymbol.setInitVal(new ArrayList<>());
            manager.addSymbol(varSymbol);
            if (children.stream().filter(child -> child instanceof ConstExp)
                    .count() == 2) { // 2d array
                varSymbol.setSecondDimSize(children.get(5).evaluate());
            }
            if (children.get(children.size() - 1) instanceof InitVal) {
                ArrayList<Value> initValues = ((InitVal) children.get(children.size() - 1)).getInitValues();
                if (type.equals(new PointerType(IntegerType.I32))) {
                    irBuilder.buildStore(initValues.get(0), allocaInst);
                } else { // 1d array
                    for (int i = 0; i < initValues.size(); i++) {
                        GEPInst gepInst = irBuilder.buildGEPWithZeroPrep(allocaInst, i);
                        irBuilder.buildStore(initValues.get(i), gepInst);
                    }
                }
            }
        }
    }

    private int getDim() {
        return (int) children.stream().filter(child -> child instanceof ConstExp).count();
    }

    private Token getIdentity() {
        return ((Terminator) children.get(0)).getVal();
    }

    private ArrayList<Integer> getInitVal() {
        if (!(children.get(children.size() - 1) instanceof InitVal))
            return new ArrayList<>();
        return ((InitVal) children.get(children.size() - 1)).getInitVal(getDim());
    }

    private ArrayList<Integer> getEleSize() {
        ArrayList<Integer> eleSize = new ArrayList<>();
        children.stream().filter(child -> child instanceof ConstExp)
                .map(child -> ((ConstExp) child).evaluate())
                .forEach(eleSize::add);
        return eleSize;
    }
}
