package analysis.node.func;

import analysis.node.exp.Exp;
import util.DataType;
import util.NodeType;
import analysis.node.Node;

import java.util.ArrayList;

public class FuncRParams extends Node {
    public FuncRParams() {
        super(NodeType.FuncRParams);
    }

    public ArrayList<DataType> getRParamDataTypes() {
        ArrayList<DataType> rParamDataTypes = new ArrayList<>();
        for (Node child : getChildren()) {
            if (child instanceof Exp)
                rParamDataTypes.add(child.getDataType());
        }
        return rParamDataTypes;
    }
}
