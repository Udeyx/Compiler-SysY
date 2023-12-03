package midend.ir.value;

import midend.ir.type.LabelType;
import midend.ir.value.instruction.BranchInst;
import midend.ir.value.instruction.Instruction;
import midend.ir.value.instruction.ReturnInst;

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

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (Instruction inst : instructions) {
            sb.append("    ");
            sb.append(inst);
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public void buildMIPS() {
        mipsBuilder.buildLabel(name);
        instructions.forEach(Instruction::buildMIPS);
    }
}
