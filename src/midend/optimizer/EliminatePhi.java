package midend.optimizer;

import midend.ir.value.BasicBlock;
import midend.ir.value.Function;
import midend.ir.value.Value;
import midend.ir.value.instruction.BranchInst;
import midend.ir.value.instruction.Instruction;
import midend.ir.value.instruction.PhiInst;

import java.util.*;

public class EliminatePhi extends Pass {
    @Override
    protected void run() {
        insertPC();
    }

    private void modifyEdge(BasicBlock preBlock, BasicBlock interBlock, BasicBlock nowBlock) {
        // del edge
        preBlock.getNextBbs().remove(nowBlock);
        nowBlock.getPrevBbs().remove(preBlock);
        // modify old edge
        BranchInst branchInst = (BranchInst) preBlock.getLastInst();
        branchInst.changeDes(nowBlock, interBlock);
        preBlock.addNextBb(interBlock);
        interBlock.addPrevBb(preBlock);
        // add new edge
        irBuilder.setCurBasicBlock(interBlock);
        irBuilder.buildNoCondBranch(nowBlock);

    }

    private void insertPC() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            irBuilder.setCurFunction(function);
            HashMap<BasicBlock, BasicBlock> insertPos = new HashMap<>();
            for (BasicBlock block : function.getBlocks()) {
                HashMap<BasicBlock, BasicBlock> changeMap = new HashMap<>();
                HashMap<BasicBlock, ParallelCopy> pcs = new HashMap<>();
                for (BasicBlock preBlock : block.getPrevBbs()) {
                    ParallelCopy pc = new ParallelCopy();
                    if (preBlock.getNextBbs().size() > 1) {
                        BasicBlock interBlock = irBuilder.buildBasicBlockWithCurFunc();
                        insertPos.put(interBlock, block);
                        // record new edge and add pc to preBlock
                        interBlock.addPC(pc);
                        pcs.put(interBlock, pc);
                        changeMap.put(preBlock, interBlock);
                    } else {
                        preBlock.addPC(pc);
                        pcs.put(preBlock, pc);
                        changeMap.put(preBlock, preBlock);
                    }
                }
                changeMap.entrySet().stream()
                        .filter(entry -> !entry.getKey().equals(entry.getValue()))
                        .forEach(entry -> modifyEdge(entry.getKey(), entry.getValue(), block));

                for (Iterator<Instruction> it = block.getInsts().iterator(); it.hasNext(); ) {
                    Instruction inst = it.next();
                    if (inst instanceof PhiInst phiInst) {
                        HashMap<BasicBlock, HashSet<Value>> optSrcMap = phiInst.getOptSrcMap();
                        for (Map.Entry<BasicBlock, HashSet<Value>> entry : optSrcMap.entrySet()) {
                            BasicBlock optBlock = entry.getKey();
                            BasicBlock newOpt = changeMap.get(optBlock);
                            HashSet<Value> srcSet = entry.getValue();
                            srcSet.forEach(var -> pcs.get(newOpt).addCopy(phiInst, var));
                        }
                        it.remove();
                    } else {
                        break;
                    }
                }
            }
            insertPos.forEach((newBlock, value) -> irBuilder.getCurFunction().addBlockBefore(newBlock, value));
        }
    }

}
