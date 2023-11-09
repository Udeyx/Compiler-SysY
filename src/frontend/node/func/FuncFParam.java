package frontend.node.func;

import frontend.Token;
import frontend.node.Node;
import frontend.node.Terminator;
import frontend.symbol.VarSymbol;
import midend.ir.Type.IntegerType;
import midend.ir.Type.PointerType;
import midend.ir.Type.Type;
import midend.ir.Value.Param;
import midend.ir.Value.instruction.AllocaInst;
import util.DataType;
import util.ErrorType;
import util.NodeType;
import util.TokenType;


public class FuncFParam extends Node {
    private VarSymbol varSymbol;

    public FuncFParam() {
        super(NodeType.FUNCFPARAM);
        this.varSymbol = null;
    }

    @Override
    public void check() {
        // error b
        varSymbol = genSymbol();
        boolean success = manager.addSymbol(varSymbol);
        if (!success)
            submitError(getIdentity().getLineNum(), ErrorType.B);
        super.check();
    }

    @Override
    public void buildIR() {
        Type originType = getDim() == 0 ? IntegerType.I32 : new PointerType(IntegerType.I32);
        Param param = irBuilder.buildParam(originType);
        if (originType.equals(IntegerType.I32)) {
            PointerType type = new PointerType(originType);
            AllocaInst allocaInst = irBuilder.buildAlloca(type);
            irBuilder.buildStore(param, allocaInst); // add store inst to cur basic block
            varSymbol.setLlvmObj(allocaInst);
            manager.addSymbol(varSymbol);
        } else {
            varSymbol.setLlvmObj(param);
            manager.addSymbol(varSymbol);
        }
    }

    public DataType getFParamDataType() {
        return genSymbol().getDataType();
    }

    private VarSymbol genSymbol() {
        return new VarSymbol(getIdentity().getVal(), getDim());
    }

    private int getDim() {
        return (int) children.stream()
                .filter(
                        child -> child instanceof Terminator
                                && ((Terminator) child).getVal().getType().equals(TokenType.LBRACK)
                )
                .count();
    }

    private Token getIdentity() {
        return ((Terminator) children.get(1)).getVal();
    }
}
