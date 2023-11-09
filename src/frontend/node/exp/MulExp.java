package frontend.node.exp;

import frontend.node.Terminator;
import midend.ir.Type.IntegerType;
import midend.ir.Value.Value;
import util.NodeType;
import frontend.node.Node;

public class MulExp extends Node {
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
            return switch (((Terminator) children.get(1)).getVal().getType()) {
                case MULT -> irBuilder.buildMul(IntegerType.I32, ((MulExp) children.get(0)).buildExpIR(),
                        ((UnaryExp) children.get(2)).buildExpIR());
                case DIV -> irBuilder.buildSdiv(IntegerType.I32, ((MulExp) children.get(0)).buildExpIR(),
                        ((UnaryExp) children.get(2)).buildExpIR());
                default -> {
                    Value xDivY = irBuilder.buildSdiv(IntegerType.I32, ((MulExp) children.get(0)).buildExpIR(),
                            ((UnaryExp) children.get(2)).buildExpIR());
                    Value xDivYMulY = irBuilder.buildMul(IntegerType.I32, xDivY,
                            ((UnaryExp) children.get(2)).buildExpIR());
                    yield irBuilder.buildSub(IntegerType.I32, ((MulExp) children.get(0)).buildExpIR(),
                            xDivYMulY);
                }
            };
        }
    }
}
