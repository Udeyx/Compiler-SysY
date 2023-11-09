package midend.ir.Value.instruction;

import midend.ir.Type.FunctionType;
import midend.ir.Type.IntegerType;
import midend.ir.Value.Argument;
import midend.ir.Value.Function;
import midend.ir.Value.Value;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CallInst extends Instruction {
    private final Function function;
    private final ArrayList<Argument> arguments;
    private final Value tar;

    public CallInst(Function function, ArrayList<Argument> arguments, Value tar) {
        super(tar == null ? "" : tar.getName(), ((FunctionType) function.getType()).getReturnType());
        this.function = function;
        this.arguments = arguments;
        this.tar = tar;
    }

    @Override
    public String toString() {
        String prefix = "";
        if (tar != null)
            prefix += tar.getName() + " = ";
        prefix += "call " + type + " " + function.getName() + "(";
        String argsStr = arguments.stream().map(Argument::toString).collect(Collectors.joining(", "));
        return prefix + argsStr + ")";

    }
}
