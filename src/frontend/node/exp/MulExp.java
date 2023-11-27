package frontend.node.exp;

import frontend.node.Terminator;
import midend.ir.type.IntegerType;
import midend.ir.value.Value;
import util.NodeType;
import frontend.node.Node;

public class MulExp extends Node implements ValueHolder {
    public MulExp() {
        super(NodeType.MULEXP);
    }

    @Override
    public int evaluate() {
        int sum = children.get(0).evaluate();
        if (children.size() > 1) {
            for (int i = 2; i < children.size(); i++) {
                switch (((Terminator) children.get(i - 1)).getVal().getType()) {
                    case MULT -> sum *= children.get(i).evaluate();
                    case DIV -> sum /= children.get(i).evaluate();
                    default -> sum %= children.get(i).evaluate();
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
            return ((UnaryExp) children.get(0)).buildExpIR();
        } else {
            Value x = ((MulExp) children.get(0)).buildExpIR();
            Value y = ((UnaryExp) children.get(2)).buildExpIR();
            return switch (((Terminator) children.get(1)).getVal().getType()) {
                case MULT -> irBuilder.buildMul(IntegerType.I32, x, y);
                case DIV -> irBuilder.buildSdiv(IntegerType.I32, x, y);
                default -> irBuilder.buildSrem(IntegerType.I32, x, y);
            };
        }
    }
}
