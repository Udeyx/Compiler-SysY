package midend.ir;

import backend.MIPSBuilder;
import midend.ir.value.Function;
import midend.ir.value.GlobalVar;
import midend.ir.value.StringLiteral;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Module {
    private static final Module MODULE = new Module();
    private final ArrayList<GlobalVar> globalVars;
    private final ArrayList<StringLiteral> stringLiterals;
    private final ArrayList<Function> functions;

    private Module() {
        this.globalVars = new ArrayList<>();
        this.stringLiterals = new ArrayList<>();
        this.functions = new ArrayList<>();
        onInit();
    }

    private void onInit() {
        functions.add(Function.GETINT);
        functions.add(Function.PUTINT);
        functions.add(Function.PUTCH);
        functions.add(Function.PUTSTR);
    }

    public static Module getInstance() {
        return MODULE;
    }

    public void addGlobalVar(GlobalVar globalVar) {
        globalVars.add(globalVar);
    }

    public void addStringLiteral(StringLiteral stringLiteral) {
        stringLiterals.add(stringLiteral);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    @Override
    public String toString() {
        return stringLiterals.stream().map(StringLiteral::toString)
                .collect(Collectors.joining("\n")) + "\n"
                + globalVars.stream().map(GlobalVar::toString)
                .collect(Collectors.joining("\n")) + "\n"
                + functions.stream().map(Function::toString)
                .collect(Collectors.joining("\n"));
    }

    public void buildMIPS() {
        MIPSBuilder.getInstance().buildData();
        globalVars.forEach(GlobalVar::buildMIPS);
        stringLiterals.forEach(StringLiteral::buildMIPS);
        MIPSBuilder.getInstance().buildText();
        MIPSBuilder.getInstance().buildJal("main");
        MIPSBuilder.getInstance().buildJ("end");
        functions.forEach(Function::buildMIPS);
        MIPSBuilder.getInstance().buildLabel("end");
    }

    public void buildFIFOMIPS() {
        MIPSBuilder.getInstance().buildData();
        globalVars.forEach(GlobalVar::buildMIPS);
        stringLiterals.forEach(StringLiteral::buildMIPS);
        MIPSBuilder.getInstance().buildText();
        MIPSBuilder.getInstance().buildJal("main");
        MIPSBuilder.getInstance().buildJ("end");
        functions.forEach(Function::buildFIFOMIPS);
        MIPSBuilder.getInstance().buildLabel("end");
    }
}
