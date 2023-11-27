package midend.ir;

import backend.MIPSBuilder;
import midend.ir.value.Function;
import midend.ir.value.GlobalVar;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Module {
    private static final Module MODULE = new Module();
    private final ArrayList<GlobalVar> globalVars;
    private final ArrayList<Function> functions;

    private Module() {
        this.globalVars = new ArrayList<>();
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

    public void addFunction(Function function) {
        functions.add(function);
    }

    @Override
    public String toString() {
        return globalVars.stream().map(GlobalVar::toString)
                .collect(Collectors.joining("\n")) + "\n"
                + functions.stream().map(Function::toString)
                .collect(Collectors.joining("\n"));
    }

    public void buildMIPS() {
        MIPSBuilder.getInstance().buildData();
        globalVars.forEach(GlobalVar::buildMIPS);
        MIPSBuilder.getInstance().buildText();
        MIPSBuilder.getInstance().buildJal("main");
        MIPSBuilder.getInstance().buildJ("end");
        functions.forEach(Function::buildMIPS);
        MIPSBuilder.getInstance().buildLabel("end");
    }
}
