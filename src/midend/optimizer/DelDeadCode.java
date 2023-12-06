package midend.optimizer;

import midend.ir.Use;
import midend.ir.value.BasicBlock;
import midend.ir.value.Function;
import midend.ir.value.instruction.Instruction;

public class DelDeadCode extends Pass {
    @Override
    protected void run() {
        delDeadCode();
    }

    private void delDeadCode() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            for (int i = function.getBlocks().size() - 1; i >= 0; i--) {
                delDeadCodeInBlock(function.getBlocks().get(i));
            }
            for (int i = 0; i < function.getBlocks().size(); i++) {
                delDeadCodeInBlock(function.getBlocks().get(i));
            }
            for (int i = function.getBlocks().size() - 1; i >= 0; i--) {
                delDeadCodeInBlock(function.getBlocks().get(i));
            }
        }
    }

    private void delDeadCodeInBlock(BasicBlock block) {
//        System.out.println(block.getName());
        for (int i = block.getInsts().size() - 1; i >= 0; i--) {
            Instruction inst = block.getInsts().get(i);
//            System.out.printf("%s ", inst);
//            System.out.println(inst.canBeDel());
//            System.out.println("uses num is: " + inst.getUses().size());
//            System.out.println("///// start to print user /////");
//            for (Use use : block.getUses()) {
//                System.out.println(use);
//            }
//            System.out.println("///// end printing user /////");
            if (inst.canBeDel()) {
                inst.delUsesFromOperands();
                block.delInst(i);
            }
        }
    }
}
