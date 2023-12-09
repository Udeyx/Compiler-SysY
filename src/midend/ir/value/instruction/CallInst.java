package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.FunctionType;
import midend.ir.type.VoidType;
import midend.ir.value.ConstantInt;
import midend.ir.value.Function;
import midend.ir.value.GlobalVar;
import midend.ir.value.Value;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class CallInst extends Instruction {
    private final Value tar;
    // operands: function, arg1, arg2, ......

    public CallInst(Function function, ArrayList<Value> arguments, Value tar) {
        super(tar == null ? "" : tar.getName(), ((FunctionType) function.getType()).getReturnType());
        this.tar = tar;
        // maintain use def
        function.addUse(this, 0);
        for (int i = 0; i < arguments.size(); i++) {
            arguments.get(i).addUse(this, i + 1);
        }
        this.operands.add(function);
        this.operands.addAll(arguments);
    }

    public Function getFunction() {
        return (Function) operands.get(0);
    }

    @Override
    public String calGVNHash() {
        Function function = (Function) operands.get(0);
        ArrayList<Value> arguments = new ArrayList<>();
        for (int i = 1; i < operands.size(); i++) {
            arguments.add(operands.get(i));
        }
        String prefix = "call " + type + " " + function.getName() + "(";
        String argsStr = arguments.stream().map(Value::asArg).collect(Collectors.joining(", "));
        return prefix + argsStr + ")";
    }

    @Override
    public String toString() {
        Function function = (Function) operands.get(0);
        ArrayList<Value> arguments = new ArrayList<>();
        for (int i = 1; i < operands.size(); i++) {
            arguments.add(operands.get(i));
        }
        String prefix = "";
        if (tar != null)
            prefix += tar.getName() + " = ";
        prefix += "call " + type + " " + function.getName() + "(";
        String argsStr = arguments.stream().map(Value::asArg).collect(Collectors.joining(", "));
        return prefix + argsStr + ")";
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        Function function = (Function) operands.get(0);
        ArrayList<Value> arguments = new ArrayList<>();
        for (int i = 1; i < operands.size(); i++) {
            arguments.add(operands.get(i));
        }
        if (function.equals(Function.PUTINT)) {
            Value realArg = arguments.get(arguments.size() - 1);
            if (realArg instanceof ConstantInt constantInt) {
                mipsBuilder.buildLi(Register.A0, constantInt);
            } else {
                int srcPos = mipsBuilder.getSymbolPos(realArg);
                mipsBuilder.buildLw(Register.A0, srcPos, Register.SP);
            }
            mipsBuilder.buildLi(Register.V0, 1);
            mipsBuilder.buildSyscall();
            return;
        } else if (function.equals(Function.GETINT)) {
            mipsBuilder.buildLi(Register.V0, 5);
            mipsBuilder.buildSyscall();
            int tarPos = mipsBuilder.allocStackSpace(this);
            mipsBuilder.buildSw(Register.V0, tarPos, Register.SP);
            return;
        } else if (function.equals(Function.PUTCH)) {
            Value realArg = arguments.get(arguments.size() - 1);
            mipsBuilder.buildLi(Register.A0, (ConstantInt) realArg);
            mipsBuilder.buildLi(Register.V0, 11);
            mipsBuilder.buildSyscall();
            return;
        } else if (function.equals(Function.PUTSTR)) {
            Value realArg = arguments.get(arguments.size() - 1);
            int srcPos = mipsBuilder.getSymbolPos(realArg);
            mipsBuilder.buildLw(Register.A0, srcPos, Register.SP);
            mipsBuilder.buildLi(Register.V0, 4);
            mipsBuilder.buildSyscall();
            return;
        }

        // 先压sp和ra
        int pushPos = mipsBuilder.getStackTop();
        mipsBuilder.buildSw(Register.SP, pushPos, Register.SP);
        pushPos -= 4;
        mipsBuilder.buildSw(Register.RA, pushPos, Register.SP);
        // 再压参数，参数可能是int, 全局变量或局部变量
        for (int i = arguments.size() - 1; i >= 0; i--) {
            Value curArg = arguments.get(i);
            pushPos -= 4;
            if (curArg instanceof ConstantInt constantInt) {
                mipsBuilder.buildLi(Register.K0, constantInt);
                mipsBuilder.buildSw(Register.K0, pushPos, Register.SP);
            } else if (curArg instanceof GlobalVar) {
                mipsBuilder.buildLa(Register.K0, curArg.getName());
                mipsBuilder.buildSw(Register.K0, pushPos, Register.SP);
            } else {
                int lvPos = mipsBuilder.getSymbolPos(curArg);
                mipsBuilder.buildLw(Register.K0, lvPos, Register.SP);
                mipsBuilder.buildSw(Register.K0, pushPos, Register.SP);
            }
        }
        // 跳到第一个参数的位置，使得在被调用函数中可以通过0($sp)来取得第一个参数的值
        mipsBuilder.buildLi(Register.K0, mipsBuilder.getStackTop() - 8);
        mipsBuilder.buildAddu(Register.SP, Register.SP, Register.K0);

        // 调用目标函数
        mipsBuilder.buildJal(function.getName());

        // 恢复ra, sp
        mipsBuilder.buildLw(Register.RA, 4, Register.SP);
        mipsBuilder.buildLw(Register.SP, 8, Register.SP);

        // 给tar声明栈空间，并把$v0的值塞进去
        if (!((FunctionType) function.getType()).getReturnType().equals(VoidType.VOID)) {
            int tarPos = mipsBuilder.allocStackSpace(this);
            mipsBuilder.buildSw(Register.V0, tarPos, Register.SP);
        }
    }

    @Override
    public boolean canBeDel() {
        Function function = (Function) operands.get(0);
        return function.isPure() && uses.stream().noneMatch(Objects::nonNull);
    }

    @Override
    public void buildFIFOMIPS() {
        super.buildFIFOMIPS();
        Function function = (Function) operands.get(0);
        System.out.println(function.getName());
        ArrayList<Value> arguments = new ArrayList<>();
        for (int i = 1; i < operands.size(); i++) {
            arguments.add(operands.get(i));
        }
        if (function.equals(Function.PUTINT)) {
            Value realArg = arguments.get(arguments.size() - 1);
            if (realArg instanceof ConstantInt constantInt) {
                mipsBuilder.buildLi(Register.A0, constantInt);
            } else {
                Register argReg = mipsBuilder.getSymbolReg(realArg);
                if (argReg != null) {
                    mipsBuilder.buildAddu(Register.A0, Register.ZERO, argReg);
                } else {
                    int srcPos = mipsBuilder.getSymbolPos(realArg);
                    mipsBuilder.buildLw(Register.A0, srcPos, Register.SP);
                }
            }
            mipsBuilder.buildLi(Register.V0, 1);
            mipsBuilder.buildSyscall();
            return;
        } else if (function.equals(Function.GETINT)) {
            mipsBuilder.buildLi(Register.V0, 5);
            mipsBuilder.buildSyscall();
            int tarPos = mipsBuilder.allocStackSpace(this);
            Register tarReg = mipsBuilder.allocReg(this);
            mipsBuilder.buildAddu(tarReg, Register.ZERO, Register.V0);
            return;
        } else if (function.equals(Function.PUTCH)) {
            Value realArg = arguments.get(arguments.size() - 1);
            mipsBuilder.buildLi(Register.A0, (ConstantInt) realArg);
            mipsBuilder.buildLi(Register.V0, 11);
            mipsBuilder.buildSyscall();
            return;
        } else if (function.equals(Function.PUTSTR)) {
            Value realArg = arguments.get(arguments.size() - 1);
            Register srcReg = mipsBuilder.getSymbolReg(realArg);
            if (srcReg != null) {
                mipsBuilder.buildAddu(Register.A0, Register.ZERO, srcReg);
            } else {
                int srcPos = mipsBuilder.getSymbolPos(realArg);
                mipsBuilder.buildLw(Register.A0, srcPos, Register.SP);
            }
            mipsBuilder.buildLi(Register.V0, 4);
            mipsBuilder.buildSyscall();
            return;
        }

        // 写回所有寄存器
        mipsBuilder.writeBackAll();

        // 先压sp和ra
        int pushPos = mipsBuilder.getStackTop();
        mipsBuilder.buildSw(Register.SP, pushPos, Register.SP);
        pushPos -= 4;
        mipsBuilder.buildSw(Register.RA, pushPos, Register.SP);
        // 再压参数，参数可能是int, 全局变量或局部变量
        for (int i = arguments.size() - 1; i >= 0; i--) {
            Value curArg = arguments.get(i);
            pushPos -= 4;
            if (curArg instanceof ConstantInt constantInt) {
                mipsBuilder.buildLi(Register.K0, constantInt);
                mipsBuilder.buildSw(Register.K0, pushPos, Register.SP);
            } else if (curArg instanceof GlobalVar) {
                mipsBuilder.buildLa(Register.K0, curArg.getName());
                mipsBuilder.buildSw(Register.K0, pushPos, Register.SP);
            } else {
                int lvPos = mipsBuilder.getSymbolPos(curArg);
                mipsBuilder.buildLw(Register.K0, lvPos, Register.SP);
                mipsBuilder.buildSw(Register.K0, pushPos, Register.SP);
            }
        }
        // 跳到第一个参数的位置，使得在被调用函数中可以通过0($sp)来取得第一个参数的值
        mipsBuilder.buildLi(Register.K0, mipsBuilder.getStackTop() - 8);
        mipsBuilder.buildAddu(Register.SP, Register.SP, Register.K0);

        // 调用目标函数
        mipsBuilder.buildJal(function.getName());

        // 恢复ra, sp
        mipsBuilder.buildLw(Register.RA, 4, Register.SP);
        mipsBuilder.buildLw(Register.SP, 8, Register.SP);

        // 给tar声明栈空间，并把$v0的值塞进去
        if (!((FunctionType) function.getType()).getReturnType().equals(VoidType.VOID)) {
            int tarPos = mipsBuilder.allocStackSpace(this);
            Register tarReg = mipsBuilder.allocReg(this);
            mipsBuilder.buildAddu(tarReg, Register.ZERO, Register.V0);
        }
    }
}
