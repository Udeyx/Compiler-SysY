package midend.optimizer;

import midend.ir.IRBuilder;
import midend.ir.Module;

public class Pass {
    protected final Module module;
    protected final IRBuilder irBuilder;

    protected Pass() {
        this.module = Module.getInstance();
        this.irBuilder = IRBuilder.getInstance();
    }

    protected void run() {
    }
}
