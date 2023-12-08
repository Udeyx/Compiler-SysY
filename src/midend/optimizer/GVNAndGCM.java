package midend.optimizer;

import midend.ir.Use;
import midend.ir.value.BasicBlock;
import midend.ir.value.ConstantInt;
import midend.ir.value.Function;
import midend.ir.value.Value;
import midend.ir.value.instruction.*;
import util.BOType;

import java.util.*;

public class GVNAndGCM extends Pass {
    private final HashMap<String, Value> gvnMap;
    private final HashSet<Instruction> visitedVal;
    private final HashMap<Instruction, BasicBlock> earlyMap;
    private BasicBlock curRoot;

    public GVNAndGCM() {
        this.gvnMap = new HashMap<>();
        this.visitedVal = new HashSet<>();
        this.earlyMap = new HashMap<>();
        this.curRoot = null;
    }

    @Override
    protected void run() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            curRoot = function.getBlocks().get(0);
            calDepth(function);
            GVN(function);
            GCM(function);

            gvnMap.clear();
            visitedVal.clear();
            earlyMap.clear();
        }
    }

    private void GVN(Function function) {
        BasicBlock root = function.getBlocks().get(0);
        preTravel(root);
    }

    private void preTravel(BasicBlock root) {
        foldConst(root);
        for (Iterator<Instruction> it = root.getInsts().iterator(); it.hasNext(); ) {
            Instruction inst = it.next();
            if (inst instanceof GEPInst || inst instanceof ICmpInst || inst instanceof BinaryOperator
                    || inst instanceof ZExtInst || (inst instanceof CallInst && ((CallInst) inst).getFunction().isPure())) {
                String gvnHash = inst.calGVNHash();
                if (!gvnMap.containsKey(gvnHash)) {
                    gvnMap.put(gvnHash, inst);
                } else {
                    inst.replaceUseOfThisWith(gvnMap.get(gvnHash));
                    inst.delUsesFromOperands();
                    it.remove();
                }
            }
        }
        root.getDomChildren().forEach(this::preTravel);
    }

    private void GCM(Function function) {
        // 这里要先把pinned的指令给先行赋值并置为visited，确保真的pinned了
        // 需要这么做是因为一条pinned指令可能在pin被第二个循环遍历，导致没pin住
        for (BasicBlock block : function.getBlocks()) {
            for (Instruction inst : block.getInsts()) {
                if (!(inst instanceof GEPInst || inst instanceof ICmpInst
                        || inst instanceof BinaryOperator || inst instanceof ZExtInst
                        || (inst instanceof CallInst && ((CallInst) inst).getFunction().isPure()))) {
                    visitedVal.add(inst);
                    earlyMap.put(inst, block);
                }

            }
        }
        for (BasicBlock block : function.getBlocks()) {
            for (Instruction inst : block.getInsts()) {
                if (!(inst instanceof GEPInst || inst instanceof ICmpInst
                        || inst instanceof BinaryOperator || inst instanceof ZExtInst
                        || (inst instanceof CallInst && ((CallInst) inst).getFunction().isPure()))) {
                    for (Value operand : inst.getOperands()) {
                        if (operand instanceof Instruction instOp) {
                            scheduleEarly(instOp);
                        }
                    }
                }

            }
        }
        moveUp(function);
    }

    private void scheduleEarly(Instruction inst) {
        if (visitedVal.contains(inst))
            return;
        visitedVal.add(inst);
        earlyMap.put(inst, curRoot);
        for (Value input : inst.getOperands()) {
            if (input instanceof Instruction inputInst) {
                scheduleEarly(inputInst);
//                if (inst.toString().contains("%lv17 = sub i32")) {
//                    System.out.println(inputInst);
//                    System.out.println(earlyMap.get(inputInst).getName());
//                }
                if (earlyMap.get(inst).getDepth() < earlyMap.get(inputInst).getDepth()) {
                    earlyMap.put(inst, earlyMap.get(inputInst));
                }
            }
        }
    }

    private void moveUp(Function function) {
        for (BasicBlock block : function.getBlocks()) {
            for (Iterator<Instruction> it = block.getInsts().iterator(); it.hasNext(); ) {
                Instruction inst = it.next();
                if (inst instanceof GEPInst || inst instanceof ICmpInst
                        || inst instanceof BinaryOperator || inst instanceof ZExtInst
                        || (inst instanceof CallInst && ((CallInst) inst).getFunction().isPure())) {
                    /*
                        注意，这里inst可能不在earlyMap里面，这说明它以及所有使用它的指令
                        都没有被pinned使用，也就说明它们都是死代码！！！
                        由于在GVN之前并没有跑死代码删除，所以这种情况可能出现
                     */
                    if (earlyMap.containsKey(inst) && !earlyMap.get(inst).equals(block)) {
//                        System.out.println("cur inst is: " + inst + " its earlyBlock is: " + earlyMap.get(inst).getName());
                        earlyMap.get(inst).addInstBeforeLast(inst);
                        it.remove();
                    }
                }
            }
        }
    }

    private void foldConst(BasicBlock block) {
        for (Iterator<Instruction> it = block.getInsts().iterator(); it.hasNext(); ) {
            Instruction inst = it.next();
            if (inst instanceof BinaryOperator binInst) {
                // both is const
                if (binInst.firstIsConst() && binInst.secondIsConst()) {
                    ConstantInt constantInt = irBuilder.buildConstantInt(binInst.evaluate());
                    for (Use use : binInst.getUses()) {
                        if (use != null)
                            use.getUser().replaceOperand(use.getPos(), constantInt);
                    }
                    it.remove();
                } else if (binInst.firstIsConst()) {
                    BOType boType = binInst.getBoType();
                    ConstantInt conOp = (ConstantInt) binInst.getOp1();
                    Value nonConOp = binInst.getOp2();
                    switch (Integer.parseInt(conOp.getName())) {
                        case 0 -> {
                            if (boType.equals(BOType.ADD)) {
                                binInst.replaceUseOfThisWith(nonConOp);
                                binInst.delUsesFromOperands();
                                it.remove();
                            } else if (boType.equals(BOType.MUL) || boType.equals(BOType.SDIV)
                                    || boType.equals(BOType.SREM)) {
                                binInst.replaceUseOfThisWith(irBuilder.buildConstantInt(0));
                                binInst.delUsesFromOperands();
                                it.remove();
                            }
                        }
                        case 1 -> {
                            if (boType.equals(BOType.MUL)) {
                                binInst.replaceUseOfThisWith(nonConOp);
                                binInst.delUsesFromOperands();
                                it.remove();
                            }
                        }
                    }
                } else if (binInst.secondIsConst()) {
                    BOType boType = binInst.getBoType();
                    ConstantInt conOp = (ConstantInt) binInst.getOp2();
                    Value nonConOp = binInst.getOp1();
                    switch (Integer.parseInt(conOp.getName())) {
                        case 0 -> {
                            if (boType.equals(BOType.ADD) || boType.equals(BOType.SUB)) {
                                binInst.replaceUseOfThisWith(nonConOp);
                                binInst.delUsesFromOperands();
                                it.remove();
                            } else if (boType.equals(BOType.MUL)) {
                                binInst.replaceUseOfThisWith(irBuilder.buildConstantInt(0));
                                binInst.delUsesFromOperands();
                                it.remove();
                            }
                        }
                        case 1 -> {
                            if (boType.equals(BOType.MUL) || boType.equals(BOType.SDIV)) {
                                binInst.replaceUseOfThisWith(nonConOp);
                                binInst.delUsesFromOperands();
                                it.remove();
                            }
                        }
                    }
                } else if (binInst.getOp1().equals(binInst.getOp2())) {
                    if (binInst.getBoType().equals(BOType.SDIV)) {
                        binInst.replaceUseOfThisWith(irBuilder.buildConstantInt(1));
                        binInst.delUsesFromOperands();
                        it.remove();
                    }
                }
            }
        }
    }

    private void calDepth(Function function) {
        function.getBlocks().forEach(BasicBlock::calDepth);
    }
}
