package midend.ir.Value;

import midend.ir.Type.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;


public class Function extends User {
    private final ArrayList<BasicBlock> basicBlocks;
    private final ArrayList<Param> params;
    public static final Function GETINT = new Function("@getint",
            new FunctionType(new ArrayList<>(), IntegerType.I32));
    public static final Function PUTINT = new Function("@putint",
            new FunctionType(new ArrayList<>(List.of(IntegerType.I32)), VoidType.VOID));
    public static final Function PUTCH = new Function("@putch",
            new FunctionType(new ArrayList<>(List.of(IntegerType.I32)), VoidType.VOID));
    public static final Function PUTSTR = new Function("@putstr",
            new FunctionType(new ArrayList<>(List.of(new PointerType(IntegerType.I8))), VoidType.VOID));
    public static final ArrayList<Function> LIB_FUNC = new ArrayList<>(Arrays.asList(GETINT, PUTINT, PUTCH, PUTSTR));

    public Function(String name, Type type) {
        super(name, type);
        this.basicBlocks = new ArrayList<>();
        this.params = new ArrayList<>();
    }

    public void addParam(Param param) {
        params.add(param);
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }

    @Override
    public String toString() {
        if (this.equals(Function.PUTCH)) {
            return "declare void @putch(i32)\n";
        } else if (this.equals(Function.GETINT)) {
            return "declare i32 @getint()\n";
        } else if (this.equals(Function.PUTINT)) {
            return "declare void @putint(i32)\n";
        } else if (this.equals(Function.PUTSTR)) {
            return "declare void @putstr(i8*)\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ");
        sb.append(((FunctionType) type).getReturnType());
        sb.append(" ");
        sb.append(name);
        FunctionType functionType = (FunctionType) type;
        sb.append("(");
        sb.append(
                params.stream().map(Param::toString).
                        collect(Collectors.joining(", "))
        );
        sb.append(")");
        sb.append(" {\n");
        sb.append(
                basicBlocks.stream().map(BasicBlock::toString)
                        .collect(Collectors.joining("\n"))
        );
        sb.append("}\n");
        return sb.toString();
    }

    @Override
    public void buildMIPS() {
        if (name.equals("@getint") || name.equals("@putint")
                || name.equals("@putch") || name.equals("@putstr")) return;

        // clear name space and stack
        mipsBuilder.enterFunction();
        mipsBuilder.buildLabel(name);

        // 把参数的名字加入符号表
        for (int i = params.size() - 1; i >= 0; i--)
            mipsBuilder.allocStackSpace(params.get(i).getName());

        // build所有block
        basicBlocks.forEach(BasicBlock::buildMIPS);
    }
}
