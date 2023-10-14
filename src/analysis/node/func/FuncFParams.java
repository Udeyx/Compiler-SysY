package analysis.node.func;

import symbol.Manager;
import util.DataType;
import util.NodeType;
import analysis.node.Node;

import java.util.ArrayList;

public class FuncFParams extends Node {
    public FuncFParams() {
        super(NodeType.FUNCFPARAMS);
    }

    public ArrayList<DataType> getFParamDataTypes() {
        ArrayList<DataType> fParamDataTypes = new ArrayList<>();
        for (Node child : getChildren()) {
            if (child instanceof FuncFParam) {
                fParamDataTypes.add(((FuncFParam) child).getFParamDataType());
            }
        }
        return fParamDataTypes;
    }
}
