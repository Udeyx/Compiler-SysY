package backend;

import backend.directive.DataDirect;
import backend.directive.SpaceDirect;
import backend.directive.TextDirect;
import backend.directive.WordDirect;
import backend.instr.*;
import backend.instr.ext.CMPInstr;
import backend.instr.ext.LAInstr;
import backend.instr.ext.LIInstr;
import backend.instr.i.BNEInstr;
import backend.instr.j.JALInstr;
import backend.instr.j.JInstr;
import backend.instr.j.JRInstr;
import backend.instr.r.*;
import backend.instr.i.LWInstr;
import backend.instr.i.SWInstr;
import util.OpCode;

import java.util.ArrayList;
import java.util.HashMap;

public class MIPSBuilder {
    private static final MIPSBuilder MIPS_BUILDER = new MIPSBuilder();
    private final Target target;
    private final HashMap<String, Integer> symbolTable;
    private int stackTop;

    private MIPSBuilder() {
        this.target = Target.getInstance();
        this.symbolTable = new HashMap<>();
        this.stackTop = 0;
    }

    public static MIPSBuilder getInstance() {
        return MIPS_BUILDER;
    }

    public void enterFunction() {
        stackTop = 0;
        symbolTable.clear();
    }

    public int getSymbolPos(String name) {
        return symbolTable.get(name);
    }

    public int allocStackSpace(String name) {
        symbolTable.put(name, stackTop);
        stackTop -= 4;
        return stackTop + 4;
    }

    // 通过这个函数获得的是申请出空间最后一个元素的地址
    // 也就是最靠近栈顶的地址
    // 也就是偏移量最负的地址
    public int allocAnonymousStackSpace(int size) {
        stackTop -= size;
        return stackTop + 4;
    }

    public int getStackTop() {
        return stackTop;
    }

    public void buildWord(String name, ArrayList<Integer> val) {
        target.addDirect(new WordDirect(name, val));
    }

    public void buildSpace(String name, int eleNum) {
        target.addDirect(new SpaceDirect(name, eleNum));
    }

    public void buildData() {
        target.addDirect(new DataDirect());
    }

    public void buildText() {
        target.addDirect(new TextDirect());
    }

    public void buildComment(String content) {
        target.addInstr(new Comment(content));
    }

    public void buildLi(Register tar, int val) {
        target.addInstr(new LIInstr(tar, val));
    }

    public void buildLa(Register tar, String label) {
        if (label.charAt(0) == '@')
            target.addInstr(new LAInstr(tar, label.substring(1)));
        else
            target.addInstr(new LAInstr(tar, label));
    }

    public void buildLw(Register rt, int offset, Register base) {
        target.addInstr(new LWInstr(base, rt, offset));
    }

    public void buildSw(Register rt, int offset, Register base) {
        target.addInstr(new SWInstr(base, rt, offset));
    }

    public void buildAddu(Register rd, Register rs, Register rt) {
        target.addInstr(new ADDUInstr(rs, rt, rd));
    }

    public void buildSubu(Register rd, Register rs, Register rt) {
        target.addInstr(new SUBUInstr(rs, rt, rd));
    }

    public void buildMult(Register rs, Register rt) {
        target.addInstr(new MULTInstr(rs, rt));
    }

    public void buildDiv(Register rs, Register rt) {
        target.addInstr(new DIVInstr(rs, rt));
    }

    public void buildMflo(Register rd) {
        target.addInstr(new MFLOInstr(rd));
    }

    public void buildMfhi(Register rd) {
        target.addInstr(new MFHIInstr(rd));
    }

    public void buildJ(String label) {
        if (label.charAt(0) == '@')
            target.addInstr(new JInstr(label.substring(1)));
        else
            target.addInstr(new JInstr(label));
    }

    public void buildJal(String label) {
        if (label.charAt(0) == '@')
            target.addInstr(new JALInstr(label.substring(1)));
        else
            target.addInstr(new JALInstr(label));
    }

    public void buildJr(Register rs) {
        target.addInstr(new JRInstr(rs));
    }

    public void buildLabel(String label) {
        if (label.charAt(0) == '@')
            target.addInstr(new LabelInstr(label.substring(1)));
        else
            target.addInstr(new LabelInstr(label));
    }

    public void buildSyscall() {
        target.addInstr(new SyscallInstr());
    }

    public void buildBne(Register rs, Register rt, String label) {
        target.addInstr(new BNEInstr(rs, rt, label));
    }

    public void buildCmp(Register rd, Register rs, Register rt, OpCode opCode) {
        target.addInstr(new CMPInstr(rd, rs, rt, opCode));
    }

    public void buildSll(Register rd, Register rt, int shAmt) {
        target.addInstr(new SLLInstr(rd, rt, shAmt));
    }
}
