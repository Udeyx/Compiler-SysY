package midend.optimizer;

import midend.ir.Use;
import midend.ir.type.ArrayType;
import midend.ir.type.PointerType;
import midend.ir.value.BasicBlock;
import midend.ir.value.Function;
import midend.ir.value.User;
import midend.ir.value.Value;
import midend.ir.value.instruction.*;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Mem2Reg extends Pass {
    private final HashMap<Value, Stack<Value>> reachDefs;
    private final HashMap<Function, HashSet<Value>> varsPerFunc;

    public Mem2Reg() {
        this.reachDefs = new HashMap<>();
        this.varsPerFunc = new HashMap<>();
    }

    @Override
    protected void run() {
        removeUselessBlock();
        getAllVar();
        calDom();
        buildDomTree();
        calFrontier();
        insertPhi();
        rename();
    }

    private void rename() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            irBuilder.setCurFunction(function);
            HashSet<Value> vars = varsPerFunc.get(function);
            vars.forEach(var -> reachDefs.put(var, new Stack<>()));
            preTravel(function.getBlocks().get(0));
            reachDefs.clear();
        }
    }

    private void preTravel(BasicBlock root) {
//        System.out.println("cur root is: " + root.getName());
        HashSet<Value> vars = varsPerFunc.get(root.getFunction());
        HashMap<Value, Integer> defineNum = new HashMap<>();
        vars.forEach(var -> defineNum.put(var, 0));
        for (Iterator<Instruction> it = root.getInsts().iterator(); it.hasNext(); ) {
            Instruction inst = it.next();
            if (inst instanceof LoadInst loadInst) {
                if (vars.contains(loadInst.getSrc())) {
                    Value nowValue;
                    if (reachDefs.get(loadInst.getSrc()).isEmpty())
                        nowValue = irBuilder.buildConstantInt(0);
                    else
                        nowValue = reachDefs.get(loadInst.getSrc()).peek();
                    loadInst.replaceUseOfThisWith(nowValue);
                    inst.delUsesFromOperands();
                    it.remove();
                }
            } else if (inst instanceof StoreInst storeInst) {
                if (vars.contains(storeInst.getTar())) {
                    reachDefs.get(storeInst.getTar()).push(storeInst.getSrc());
                    int preNum = defineNum.get(storeInst.getTar());
                    defineNum.put(storeInst.getTar(), preNum + 1);
                    inst.delUsesFromOperands();
                    it.remove();
                }
            } else if (inst instanceof PhiInst phiInst) {
                reachDefs.get(phiInst.getTar()).push(phiInst);
                int preNum = defineNum.get(phiInst.getTar());
                defineNum.put(phiInst.getTar(), preNum + 1);
            } else if (inst instanceof AllocaInst) {
                if (!(((PointerType) inst.getType()).getEleType() instanceof ArrayType)) {
                    inst.delUsesFromOperands();
                    it.remove();
                }
            }
        }
//        System.out.println("//////////\ncur root is: " + root.getName() + "\n//////////");

        for (BasicBlock nextBlock : root.getNextBbs()) {
//            System.out.println("next bb is: " + nextBlock.getName());
            for (Instruction inst : nextBlock.getInsts()) {
                if (inst instanceof PhiInst phiInst) {
//                    System.out.println("tar of phi is: " + phiInst.getName());
                    if (reachDefs.get(phiInst.getTar()).isEmpty())
                        phiInst.addAnOption(irBuilder.buildConstantInt(0), root);
                    else
                        phiInst.addAnOption(reachDefs.get(phiInst.getTar()).peek(), root);
                } else {
                    break;
                }
            }
        }

        for (BasicBlock child : root.getDomChildren()) {
            preTravel(child);
        }

        for (Value var : vars) {
            for (int i = 0; i < defineNum.get(var); i++) {
                reachDefs.get(var).pop();
            }
        }
    }


    private void insertPhi() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            irBuilder.setCurFunction(function);
            HashSet<Value> vars = varsPerFunc.get(function);
            for (Value var : vars) {
                HashSet<BasicBlock> defineSet = new HashSet<>();
                HashSet<BasicBlock> added = new HashSet<>();
                for (BasicBlock block : function.getBlocks()) {
                    for (Instruction inst : block.getInsts()) {
                        if (inst instanceof StoreInst && ((StoreInst) inst).getTar().equals(var))
                            defineSet.add(block);
                    }
                }
//                System.out.println("cur var is: " + var.getName());
//                System.out.println("var's defineSet: " + defineSet.stream().map(Value::getName).collect(Collectors.joining(" ")));
                while (!defineSet.isEmpty()) {
                    BasicBlock x = null;
                    for (BasicBlock temp : defineSet) {
                        x = temp;
                        break;
                    }
                    defineSet.remove(x);
//                    System.out.println("cur x: " + x.getName());
//                    System.out.println("x.DF: " + x.getDomFrontier().stream().map(Value::getName).collect(Collectors.joining(" ")));
                    for (BasicBlock y : x.getDomFrontier()) {
                        irBuilder.setCurBasicBlock(y);
                        if (!added.contains(y)) {
//                            System.out.println("insert phi to: " + y.getName());
                            irBuilder.buildPhi(((PointerType) var.getType()).getEleType(), null, null, null, null, var);
                            added.add(y);
                            defineSet.add(y);
                        }
                    }
                }
//                System.out.println("\ncome to next var\n");
            }
        }
    }

    /*
        这里只取出所有非数组变量，因为数组本身就需要放在内存里
        取的方法简单，只要取出所有type不为array*的alloca就行了
        注意，虽然数组传参后，也是i32*，但是没有alloca，因此不存在
        误删数组的情况
     */
    private void getAllVar() {
        for (Function function : module.getFunctions()) {
            HashSet<Value> vars = function.getBlocks().stream()
                    .map(BasicBlock::getInsts)
                    .flatMap(List::stream)
                    .filter(inst -> inst instanceof AllocaInst
                            && !(((PointerType) inst.getType()).getEleType() instanceof ArrayType))
                    .collect(Collectors.toCollection(HashSet::new));
            varsPerFunc.put(function, vars);
        }
    }

    // 这里是遍历CFG图，不是遍历支配树！！！
    // 注意CFG是有向图，因此对于每一个b，只找它的前驱
    private void calFrontier() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            for (BasicBlock b : function.getBlocks()) {
//                System.out.println("cur b is: " + b.getName());
//                System.out.println("b's prev are: " + b.getPrevBbs().stream().map(Value::getName).collect(Collectors.joining(" ")));
                for (BasicBlock a : b.getPrevBbs()) {
                    BasicBlock x = a;
                    while (!(!x.equals(b) && b.getDom().contains(x))) {
//                        System.out.println(b.getName() + " is add to " + x.getName() + "'s DF");
                        x.addToDF(b);
                        x = x.getDomParent();
                    }
                }
//                System.out.println("next DF\n");
            }
        }
    }

    private void buildDomTree() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            for (int i = 0; i < function.getBlocks().size(); i++) {
                BasicBlock bb = function.getBlocks().get(i);
                HashSet<BasicBlock> strictDoms = bb.getDom().stream()
                        .filter(blk -> !blk.equals(bb))
                        .collect(Collectors.toCollection(HashSet::new));
                for (BasicBlock dom1 : strictDoms) {
                    boolean isImmediate = true;
                    for (BasicBlock dom2 : strictDoms) {
                        if (dom1 != dom2 && dom2.getDom().contains(dom1)) {
                            isImmediate = false;
                            break;
                        }
                    }
                    if (isImmediate) {
                        bb.setDomParent(dom1);
                        dom1.addDomChild(bb);
                        break;
                    }
                }
            }
        }
    }

    private void calDom() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            // initialize
            // 这里一定要注意不能改get出来的数据！！！
            BasicBlock startBb = function.getBlocks().get(0);
            startBb.setDom(new HashSet<>(List.of(startBb)));
            HashSet<BasicBlock> allBbs = new HashSet<>(function.getBlocks());
            for (int i = 1; i < function.getBlocks().size(); i++) {
                function.getBlocks().get(i).setDom(allBbs);
            }
            while (true) {
                boolean hasChange = false;
                for (BasicBlock bb : function.getBlocks()) {
                    HashSet<BasicBlock> inSet = new HashSet<>();
                    if (!bb.getPrevBbs().isEmpty())
                        inSet.addAll(allBbs);
                    bb.getPrevBbs().stream()
                            .map(BasicBlock::getDom)
                            .forEach(inSet::retainAll);
                    inSet.add(bb);
                    hasChange = hasChange || bb.setDom(inSet);
                }
                if (!hasChange)
                    break;
            }
        }
    }

    private void removeUselessBlock() {
        for (Function function : module.getFunctions()) {
            if (Function.LIB_FUNC.contains(function))
                continue;
            BasicBlock startBlock = function.getBlocks().get(0);
            for (Iterator<BasicBlock> it = function.getBlocks().iterator(); it.hasNext(); ) {
                BasicBlock block = it.next();
                if (!block.equals(startBlock) && block.getPrevBbs().isEmpty()) {
                    for (BasicBlock nextBlock : block.getNextBbs()) {
                        nextBlock.getPrevBbs().remove(block);
                    }
                    block.getInsts().forEach(User::delUsesFromOperands);
                    it.remove();
                }
            }
        }
    }

    private void printDomTree() {
        for (Function function : module.getFunctions()) {
            for (BasicBlock block : function.getBlocks()) {
                System.out.println(block.getName());
                System.out.println("parent: " + ((block.getDomParent() == null) ?
                        "" : block.getDomParent().getName()));
                System.out.println("children");
                System.out.println(
                        block.getDomChildren().stream()
                                .map(BasicBlock::getName)
                                .collect(Collectors.joining(" "))
                );
                System.out.println("\n");
            }
        }
    }

    private void printCFG() {
        for (Function function : module.getFunctions()) {
            for (BasicBlock block : function.getBlocks()) {
                System.out.println(block.getName());
                System.out.println(
                        block.getPrevBbs().stream()
                                .map(BasicBlock::getName)
                                .collect(Collectors.joining(" "))
                );
                System.out.println(
                        block.getNextBbs().stream()
                                .map(BasicBlock::getName)
                                .collect(Collectors.joining(" "))
                );
                System.out.println();
                System.out.println();
            }
        }
    }

    private void printDom() {
        for (Function function : module.getFunctions()) {
            for (BasicBlock block : function.getBlocks()) {
                System.out.println(block.getName());
                System.out.println(
                        block.getDom().stream()
                                .map(BasicBlock::getName)
                                .collect(Collectors.joining(" "))
                );
                System.out.println();
                System.out.println();
            }
        }
    }


    private void printDF() {
        for (Function function : module.getFunctions()) {
            for (BasicBlock block : function.getBlocks()) {
                System.out.println("cur blk is: " + block.getName());
                System.out.println(
                        block.getDomFrontier().stream()
                                .map(BasicBlock::getName)
                                .collect(Collectors.joining(" "))
                );
                System.out.println("next one\n");
            }
        }
    }
}
