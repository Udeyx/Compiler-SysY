package frontend.node.exp;

import frontend.node.Terminator;
import midend.ir.Type.IntegerType;
import midend.ir.Value.Value;
import util.ICmpType;
import util.NodeType;
import frontend.node.Node;
import util.TokenType;

public class EqExp extends Node implements ValueHolder {
    public EqExp() {
        super(NodeType.EQEXP);
    }

    @Override
    public void buildIR() {
        buildExpIR();
    }

    @Override
    public Value buildExpIR() {
        if (children.size() == 1) {
            return ((RelExp) children.get(0)).buildExpIR();
        } else {
            Value x = ((EqExp) children.get(0)).buildExpIR();
            Value y = ((RelExp) children.get(2)).buildExpIR();
            return irBuilder.buildICmpWithLV(getICmpType(), x, y);
        }
    }

    private ICmpType getICmpType() {
        return ((Terminator) children.get(1)).getVal().getType().equals(TokenType.EQL) ? ICmpType.EQ
                : ICmpType.NE;
    }
}
