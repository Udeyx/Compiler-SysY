package midend.optimizer;

import midend.ir.Module;
import util.IOer;

public class Optimizer {
    private final static Optimizer OPTIMIZER = new Optimizer();
    private final boolean dev;

    private Optimizer() {
        this.dev = false;
    }

    public boolean isDev() {
        return dev;
    }

    public static Optimizer getInstance() {
        return OPTIMIZER;
    }

    public void optimize() {
        // mem2reg
        new Mem2Reg().run();

        // del dead code
        new DelDeadCode().run();
        if (dev)
            IOer.printPhiIR();

        // eliminate phi
        new EliminatePhi().run();
        if (dev)
            IOer.printMoveIR();
    }
}
