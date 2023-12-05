package midend.optimizer;

import midend.ir.Module;
import util.IOer;

public class Optimizer {
    private final static Optimizer OPTIMIZER = new Optimizer();
    private final boolean dev;

    private Optimizer() {
        this.dev = false;
    }

    public static Optimizer getInstance() {
        return OPTIMIZER;
    }

    public void optimize() {
        new Mem2Reg().run();
        if (dev)
            IOer.printPhiIR();
        new EliminatePhi().run();
        if (dev)
            IOer.printMoveIR();
    }
}
