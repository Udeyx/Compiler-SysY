package midend.optimizer;

import midend.ir.value.BasicBlock;
import midend.ir.value.Function;
import midend.ir.value.instruction.BranchInst;
import midend.ir.value.instruction.Instruction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class DelDeadCode extends Pass {
    @Override
    protected void run() {
        delDeadCode();
        delDeadFunction();
        delDeadBlock();
    }

    private void delDeadCode() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            // del dead insts
            for (int i = function.getBlocks().size() - 1; i >= 0; i--) {
                delDeadCodeInBlock(function.getBlocks().get(i));
            }
            for (int i = 0; i < function.getBlocks().size(); i++) {
                delDeadCodeInBlock(function.getBlocks().get(i));
            }
            for (int i = function.getBlocks().size() - 1; i >= 0; i--) {
                delDeadCodeInBlock(function.getBlocks().get(i));
            }
            // del dead functions
        }
    }

    private void delDeadCodeInBlock(BasicBlock block) {
        for (int i = block.getInsts().size() - 1; i >= 0; i--) {
            Instruction inst = block.getInsts().get(i);
            if (inst.canBeDel()) {
                inst.delUsesFromOperands();
                block.delInst(i);
            }
        }
    }

    private void delDeadFunction() {
        module.getFunctions().removeIf(function -> !function.isUseful()
                && !function.getName().equals("@main"));
    }

    private void delDeadBlock() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            for (Iterator<BasicBlock> it = function.getBlocks().iterator(); it.hasNext(); ) {
                BasicBlock block = it.next();
                /*
                    当block和nextBlock是一对一关系的时候是肯定可以合并的
                    block一对多nextBlock是肯定不能合并的
                    block只有一个后继，但next有多个输入的时候，如果block只有一个br，也是可以合并，相当于删了block
                 */
                if (block.getNextBbs().size() == 1) {
                    BasicBlock nextBlock = block;
                    for (BasicBlock nbb : block.getNextBbs())
                        nextBlock = nbb;
                    if (nextBlock.getPrevBbs().size() == 1) {
                        mergeToNext(block, nextBlock);
                        it.remove();
                        block.replaceUseOfThisWith(nextBlock);
                    }
                }
            }
        }
    }

    private void mergeToNext(BasicBlock nowBlock, BasicBlock nextBlock) {
        ArrayList<Instruction> usefulInsts = new ArrayList<>();
        for (int i = 0; i < nowBlock.getInsts().size() - 1; i++) {
            usefulInsts.add(nowBlock.getInsts().get(i));
        }
        nextBlock.addInstsAtHead(usefulInsts);
        for (BasicBlock prevBlock : nowBlock.getPrevBbs()) {
            /*
                在删除旧的边时，不用再维护nowBlock的prevBbs了
                因为nowBlock已经要没了
                这样还能避免一边遍历一边删
            */
            prevBlock.getNextBbs().remove(nowBlock);
            // modify edge
            prevBlock.addNextBb(nextBlock);
            nextBlock.addPrevBb(prevBlock);
        }
    }
}
