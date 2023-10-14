package analysis.node.exp;

import analysis.node.LVal;
import analysis.node.Number;
import util.DataType;
import util.NodeType;
import analysis.node.Node;

public class PrimaryExp extends Node {
    public PrimaryExp() {
        super(NodeType.PRIMARYEXP);
    }

    @Override
    public DataType getDataType() {
        Node first = getChildren().get(0);
        if (first instanceof LVal) {
            return first.getDataType();
        } else if (first instanceof Number) {
            return DataType.INT;
        } else {
            return super.getDataType();
        }
    }
}
