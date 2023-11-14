package frontend.node.exp;

import frontend.node.Terminator;
import midend.ir.Type.IntegerType;
import midend.ir.Value.Value;
import midend.ir.Value.instruction.AddInst;
import util.ICmpType;
import util.NodeType;
import frontend.node.Node;

public class RelExp extends Node implements ValueHolder {
    public RelExp() {
        super(NodeType.RELEXP);
    }

    @Override
    public void buildIR() {
        buildExpIR();
    }

    /*
    产生的Value可以有两种类型
    一种是只有一个AddExp，这是I32的
    另一种是经历了一次或多次比较，这是I1的
     */
    @Override
    public Value buildExpIR() {
        if (children.size() == 1) {
            return ((AddExp) children.get(0)).buildExpIR();
        } else { // 如果left类型不是I32，就要转换为I32
            Value left = ((RelExp) children.get(0)).buildExpIR();
            if (!left.getType().equals(IntegerType.I32)) {
                left = irBuilder.buildZExtWithLV(left, IntegerType.I32);
            }
            ICmpType iCmpType = switch (((Terminator) children.get(1)).getVal().getType()) {
                case LSS -> ICmpType.SLT;
                case LEQ -> ICmpType.SLE;
                case GRE -> ICmpType.SGT;
                default -> ICmpType.SGE; // >=
            };
            return irBuilder.buildICmpWithLV(iCmpType, left, ((AddExp) children.get(2)).buildExpIR());
        }
    }
}
