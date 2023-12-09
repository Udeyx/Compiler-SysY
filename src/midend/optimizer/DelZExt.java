package midend.optimizer;

import midend.ir.value.BasicBlock;
import midend.ir.value.Function;
import midend.ir.value.instruction.Instruction;
import midend.ir.value.instruction.ZExtInst;

import java.util.Iterator;

public class DelZExt extends Pass {
    @Override
    protected void run() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            for (BasicBlock block : function.getBlocks()) {
                for (Iterator<Instruction> it = block.getInsts().iterator(); it.hasNext(); ) {
                    Instruction inst = it.next();
                    if (inst instanceof ZExtInst zExtInst) {
                        zExtInst.replaceUseOfThisWith(zExtInst.getSrc());
                        zExtInst.delUsesFromOperands();
                        it.remove();
                    }
                }
            }
        }
    }
}
