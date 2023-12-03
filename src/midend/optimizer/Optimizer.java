package midend.optimizer;

import midend.ir.Module;

import java.util.ArrayList;

public class Optimizer {
    private final ArrayList<Pass> passes;
    private final Module module;
    private final static Optimizer OPTIMIZER = new Optimizer();

    private Optimizer() {
        this.passes = new ArrayList<>();
        this.module = Module.getInstance();
    }

    public static Optimizer getInstance() {
        return OPTIMIZER;
    }

    public void optimize() {
        passes.add(new Mem2Reg());
        passes.forEach(Pass::run);
    }
}
