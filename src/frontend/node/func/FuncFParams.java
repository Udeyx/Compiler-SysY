package frontend.node.func;

import util.DataType;
import util.NodeType;
import frontend.node.Node;

import java.util.ArrayList;

public class FuncFParams extends Node {
    public FuncFParams() {
        super(NodeType.FUNCFPARAMS);
    }

    public ArrayList<DataType> getFParamDataTypes() {
        ArrayList<DataType> fParamDataTypes = new ArrayList<>();
        for (Node child : children) {
            if (child instanceof FuncFParam) {
                fParamDataTypes.add(((FuncFParam) child).getFParamDataType());
            }
        }
        return fParamDataTypes;
    }
}
