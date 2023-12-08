package backend;

import backend.directive.*;
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
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;
import util.OpCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MIPSBuilder {
    private static final MIPSBuilder MIPS_BUILDER = new MIPSBuilder();
    private final Target target;
    private final HashMap<Value, Integer> symbolTable;
    private int stackTop;
    private final ArrayList<VRPair> vrPairs;
    private final HashMap<Value, Register> val2reg;
    private final HashMap<Register, Value> reg2val;

    private MIPSBuilder() {
        this.target = Target.getInstance();
        this.symbolTable = new HashMap<>();
        this.stackTop = 0;
        this.vrPairs = new ArrayList<>();
        this.val2reg = new HashMap<>();
        this.reg2val = new HashMap<>();
    }

    public static MIPSBuilder getInstance() {
        return MIPS_BUILDER;
    }

    public void enterFunction() {
        stackTop = 0;
        symbolTable.clear();
        initVRRelation();
    }

    private void initVRRelation() {
        vrPairs.clear();
        val2reg.clear();
        reg2val.clear();
        reg2val.put(Register.T0, null);
        reg2val.put(Register.T1, null);
        reg2val.put(Register.T2, null);
        reg2val.put(Register.T3, null);
        reg2val.put(Register.T4, null);
        reg2val.put(Register.T5, null);
        reg2val.put(Register.T6, null);
        reg2val.put(Register.T7, null);
        reg2val.put(Register.T8, null);
        reg2val.put(Register.T9, null);
        reg2val.put(Register.S0, null);
        reg2val.put(Register.S1, null);
        reg2val.put(Register.S2, null);
        reg2val.put(Register.S3, null);
        reg2val.put(Register.S4, null);
        reg2val.put(Register.S5, null);
        reg2val.put(Register.S6, null);
        reg2val.put(Register.S7, null);
    }

    public Register allocReg(Value value) {
        if (vrPairs.size() < 18) { // no need to grab
            Register reg = null;
            for (Map.Entry<Register, Value> entry : reg2val.entrySet()) {
                if (entry.getValue() == null) {
                    reg = entry.getKey();
                    vrPairs.add(new VRPair(value, reg));
                    val2reg.put(value, reg);
                    reg2val.put(reg, value);
                    break;
                }
            }
            return reg;
        } else {
            // remove old value
            VRPair vrPair = vrPairs.remove(0);
            Value preVal = vrPair.getVal();
            Register reg = vrPair.getReg();
            val2reg.remove(preVal);
            reg2val.put(reg, null);

            // write back to mem
            /*
                preValue只可能是I32，因为目前想做的只是把LLVM的虚拟寄存器转换为实体寄存器，
                并不会干预原来操作指针的alloca, load, store
                也就是和AddInst的翻译类似，而不是store那样
             */
            int preValuePos = getSymbolPos(preVal);
            buildSw(reg, preValuePos, Register.SP);

            // alloc reg for new value
            vrPairs.add(new VRPair(value, reg));
            val2reg.put(value, reg);
            reg2val.put(reg, value);
            return reg;
        }
    }

    public void writeBackAll() {
        for (VRPair vrPair : vrPairs) {
            Value preVal = vrPair.getVal();
            Register reg = vrPair.getReg();
            val2reg.remove(preVal);
            reg2val.put(reg, null);
            int preValuePos = getSymbolPos(preVal);
            buildSw(reg, preValuePos, Register.SP);
        }
        vrPairs.clear();
    }

    public Register getSymbolReg(Value value) {
        return val2reg.get(value);
    }

    public int getSymbolPos(Value value) {
        return symbolTable.get(value);
    }

    public boolean hasSymbol(Value value) {
        return symbolTable.containsKey(value);
    }

    public int allocStackSpace(Value value) {
        symbolTable.put(value, stackTop);
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

    public void buildAsciiz(String name, String content) {
        target.addDirect(new AsciizDirect(name, content));
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

    public void buildLi(Register tar, ConstantInt val) {
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
