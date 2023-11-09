package frontend.node.decl;

import frontend.Token;
import frontend.node.Node;
import frontend.node.Terminator;
import frontend.node.exp.ConstExp;
import frontend.symbol.ConSymbol;
import midend.ir.Type.ArrayType;
import midend.ir.Type.IntegerType;
import midend.ir.Type.PointerType;
import midend.ir.Type.Type;
import midend.ir.Value.ConstantInt;
import midend.ir.Value.GlobalVar;
import midend.ir.Value.Value;
import midend.ir.Value.instruction.AllocaInst;
import midend.ir.Value.instruction.GEPInst;
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
        Type eleType = getDim() == 0 ? IntegerType.I32 : new ArrayType(IntegerType.I32, initVal.size());
        PointerType type = new PointerType(eleType);
        if (manager.isInGlobal()) {
            GlobalVar globalVar = irBuilder.buildGlobalVar(type, true, initVal);
            module.addGlobalVar(globalVar);
            conSymbol.setLlvmObj(globalVar);
            conSymbol.setInitVal(initVal);
            manager.addSymbol(conSymbol);
        } else {
            AllocaInst allocaInst = irBuilder.buildAlloca(type);
            conSymbol.setLlvmObj(allocaInst);
            conSymbol.setInitVal(initVal);
            manager.addSymbol(conSymbol);
            if (type.equals(new PointerType(IntegerType.I32))) {
                irBuilder.buildStore(irBuilder.buildConstantInt(initVal.get(0)), allocaInst);
            } else {
                for (int i = 0; i < initVal.size(); i++) {
                    GEPInst gepInst = irBuilder.buildGEP(allocaInst, i);
                    irBuilder.buildStore(irBuilder.buildConstantInt(initVal.get(i)), gepInst);
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
        return ((ConstInitVal) children.get(children.size() - 1)).getInitVal(getDim());
    }
}
