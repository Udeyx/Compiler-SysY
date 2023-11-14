package frontend.node.exp;

import frontend.node.Terminator;
import midend.ir.Type.IntegerType;
import midend.ir.Value.Value;
import midend.ir.Value.instruction.AddInst;
import util.NodeType;
import frontend.node.Node;
import util.TokenType;

public class AddExp extends Node implements ValueHolder {
    public AddExp() {
        super(NodeType.ADDEXP);
    }

    @Override
    public int evaluate() {
        int sum = children.get(0).evaluate();
        if (children.size() > 1) {
            for (int i = 2; i < children.size(); i++) {
                if (((Terminator) children.get(i - 1)).getVal().getType().equals(TokenType.PLUS)) {
                    sum += children.get(i).evaluate();
                } else {
                    sum -= children.get(i).evaluate();
                }
            }
        }
        return sum;
    }

    @Override
    public void buildIR() {
        buildExpIR();
    }

    public Value buildExpIR() {
        if (children.size() == 1) {
            return ((MulExp) children.get(0)).buildExpIR();
        } else {
            Value x = ((AddExp) children.get(0)).buildExpIR();
            Value y = ((MulExp) children.get(2)).buildExpIR();
            if (isAdd())
                return irBuilder.buildAdd(IntegerType.I32, x, y);
            else  // is sub
                return irBuilder.buildSub(IntegerType.I32, x, y);
        }
    }

    private boolean isAdd() {
        return children.size() > 1
                && ((Terminator) children.get(1)).getVal().getType().equals(TokenType.PLUS);
    }

}
