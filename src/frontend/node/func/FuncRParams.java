package frontend.node.func;

import frontend.node.exp.Exp;
import midend.ir.value.Value;
import util.DataType;
import util.NodeType;
import frontend.node.Node;

import java.util.ArrayList;

public class FuncRParams extends Node {
    public FuncRParams() {
        super(NodeType.FuncRParams);
    }

    public ArrayList<DataType> getRParamDataTypes() {
        ArrayList<DataType> rParamDataTypes = new ArrayList<>();
        for (Node child : children) {
            if (child instanceof Exp)
                rParamDataTypes.add(child.getDataType());
        }
        return rParamDataTypes;
    }

    public ArrayList<Value> buildArgumentsIR() {
        ArrayList<Value> arguments = new ArrayList<>();
        children.stream().filter(child -> child instanceof Exp)
                .map(child -> ((Exp) child).buildExpIR())
                .forEach(arguments::add);
        return arguments;
    }
}
