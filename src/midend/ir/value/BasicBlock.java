package midend.ir.value;

import midend.ir.type.LabelType;
import midend.ir.value.instruction.BranchInst;
import midend.ir.value.instruction.Instruction;
import midend.ir.value.instruction.ReturnInst;
import midend.optimizer.ParallelCopy;

import java.util.ArrayList;
import java.util.HashSet;

public class BasicBlock extends Value {
    private final ArrayList<Instruction> instructions;
    private final HashSet<BasicBlock> prevBbs;
    private final HashSet<BasicBlock> nextBbs;
    // 支配树
    private HashSet<BasicBlock> doms;
    private BasicBlock domParent;
    private final ArrayList<BasicBlock> domChildren;
    private final HashSet<BasicBlock> domFrontier;
    private final Function function;
    private boolean hasRetOrBr;
    private ArrayList<ParallelCopy> pcs;

    public BasicBlock(String name, Function function) {
        super(name, LabelType.LABEL);
        this.instructions = new ArrayList<>();
        this.prevBbs = new HashSet<>();
        this.nextBbs = new HashSet<>();
        this.doms = new HashSet<>();
        this.domParent = null;
        this.domChildren = new ArrayList<>();
        this.domFrontier = new HashSet<>();
        this.function = function;
        this.hasRetOrBr = false;
        this.pcs = new ArrayList<>();
    }

    public void addPC(ParallelCopy pc) {
        pcs.add(pc);
    }

    public Function getFunction() {
        return function;
    }

    public HashSet<BasicBlock> getDomFrontier() {
        return domFrontier;
    }

    public void addToDF(BasicBlock bb) {
        domFrontier.add(bb);
    }

    public void setDomParent(BasicBlock domParent) {
        this.domParent = domParent;
    }

    public BasicBlock getDomParent() {
        return domParent;
    }

    public void addDomChild(BasicBlock domChild) {
        domChildren.add(domChild);
    }

    public ArrayList<BasicBlock> getDomChildren() {
        return domChildren;
    }

    public boolean setDom(HashSet<BasicBlock> newDoms) {
        boolean modified = false;
        if (newDoms.size() != this.doms.size()) {
            modified = true;
        } else {
            for (BasicBlock newDom : newDoms) {
                if (!doms.contains(newDom)) {
                    modified = true;
                    break;
                }
            }
        }
        this.doms = newDoms;
        return modified;
    }

    public HashSet<BasicBlock> getDom() {
        return doms;
    }

    public void addPrevBb(BasicBlock bb) {
        prevBbs.add(bb);
    }

    public HashSet<BasicBlock> getPrevBbs() {
        return prevBbs;
    }

    public void addNextBb(BasicBlock bb) {
        nextBbs.add(bb);
    }

    public HashSet<BasicBlock> getNextBbs() {
        return nextBbs;
    }

    public boolean addInst(Instruction inst) {
        if (!hasRetOrBr) {
            instructions.add(inst);
            if (inst instanceof ReturnInst || inst instanceof BranchInst)
                hasRetOrBr = true;
            return true;
        }
        return false;
    }

    public void addInst(int pos, Instruction inst) {
        instructions.add(pos, inst);
    }

    public void delInst(int pos) {
        instructions.remove(pos);
    }

    public Instruction getLastInst() {
        return instructions.get(instructions.size() - 1);
    }

    public ArrayList<Instruction> getInsts() {
        return instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (int i = 0; i < instructions.size() - 1; i++) {
            sb.append("    ");
            sb.append(instructions.get(i));
            sb.append("\n");
        }
        pcs.stream().map(ParallelCopy::toString).forEach(sb::append);
        sb.append("    ").append(instructions.get(instructions.size() - 1)).append("\n");
        return sb.toString();
    }

    @Override
    public void buildMIPS() {
        mipsBuilder.buildLabel(name);
        for (int i = 0; i < instructions.size() - 1; i++) {
            instructions.get(i).buildMIPS();
        }
        pcs.forEach(ParallelCopy::buildMIPS);
        instructions.get(instructions.size() - 1).buildMIPS();
    }

    @Override
    public void buildFIFOMIPS() {
        mipsBuilder.buildLabel(name);
        for (int i = 0; i < instructions.size() - 1; i++) {
            instructions.get(i).buildFIFOMIPS();
        }
        pcs.forEach(ParallelCopy::buildFIFOMIPS);
        mipsBuilder.writeBackAll();
        instructions.get(instructions.size() - 1).buildFIFOMIPS();
    }
}
