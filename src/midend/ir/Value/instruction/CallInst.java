package midend.ir.Value.instruction;

import backend.Register;
import midend.ir.Type.FunctionType;
import midend.ir.Type.VoidType;
import midend.ir.Value.Argument;
import midend.ir.Value.Function;
import midend.ir.Value.Value;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class CallInst extends Instruction {
    private final Function function;
    private final ArrayList<Argument> arguments;
    private final Value tar;

    public CallInst(Function function, ArrayList<Argument> arguments, Value tar) {
        super(tar == null ? "" : tar.getName(), ((FunctionType) function.getType()).getReturnType());
        this.function = function;
        this.arguments = arguments;
        this.tar = tar;
    }

    @Override
    public String toString() {
        String prefix = "";
        if (tar != null)
            prefix += tar.getName() + " = ";
        prefix += "call " + type + " " + function.getName() + "(";
        String argsStr = arguments.stream().map(Argument::toString).collect(Collectors.joining(", "));
        return prefix + argsStr + ")";
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        if (function.equals(Function.PUTINT)) {
            Argument realArg = arguments.get(arguments.size() - 1);
            if (Character.isDigit(realArg.getName().charAt(0))) {
                mipsBuilder.buildLi(Register.A0, Integer.parseInt(realArg.getName()));
            } else {
                int srcPos = mipsBuilder.getSymbolPos(realArg.getName());
                mipsBuilder.buildLw(Register.A0, srcPos, Register.SP);
            }
            mipsBuilder.buildLi(Register.V0, 1);
            mipsBuilder.buildSyscall();
            return;
        } else if (function.equals(Function.GETINT)) {
            mipsBuilder.buildLi(Register.V0, 5);
            mipsBuilder.buildSyscall();
            if (tar.getName().charAt(0) == '@') {
                mipsBuilder.buildLa(Register.T1, tar.getName());
                mipsBuilder.buildSw(Register.V0, 0, Register.T1);
            } else {
                int tarPos = mipsBuilder.allocStackSpace(tar.getName());
                mipsBuilder.buildSw(Register.V0, tarPos, Register.SP);
            }
            return;
        } else if (function.equals(Function.PUTCH)) {
            Argument realArg = arguments.get(arguments.size() - 1);
            mipsBuilder.buildLi(Register.A0, Integer.parseInt(realArg.getName()));
            mipsBuilder.buildLi(Register.V0, 11);
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
            Argument curArg = arguments.get(i);
            pushPos -= 4;
            if (Character.isDigit(curArg.getName().charAt(0))) {
                mipsBuilder.buildLi(Register.T0, Integer.parseInt(curArg.getName()));
                mipsBuilder.buildSw(Register.T0, pushPos, Register.SP);
            } else if (curArg.getName().charAt(0) == '@') {
                mipsBuilder.buildLa(Register.T0, curArg.getName());
                mipsBuilder.buildSw(Register.T0, pushPos, Register.SP);
            } else {
                int lvPos = mipsBuilder.getSymbolPos(curArg.getName());
                mipsBuilder.buildLw(Register.T0, lvPos, Register.SP);
                mipsBuilder.buildSw(Register.T0, pushPos, Register.SP);
            }
        }
        // 跳到第一个参数的位置，使得在被调用函数中可以通过0($sp)来取得第一个参数的值
        mipsBuilder.buildLi(Register.T0, mipsBuilder.getStackTop() - 8);
        mipsBuilder.buildAddu(Register.SP, Register.SP, Register.T0);

        // 调用目标函数
        mipsBuilder.buildJal(function.getName());

        // 恢复ra, sp
        mipsBuilder.buildLw(Register.RA, 4, Register.SP);
        mipsBuilder.buildLw(Register.SP, 8, Register.SP);

        // 给tar声明栈空间，并把$v0的值塞进去
        if (!((FunctionType) function.getType()).getReturnType().equals(VoidType.VOID)) {
            int tarPos = mipsBuilder.allocStackSpace(tar.getName());
            mipsBuilder.buildSw(Register.V0, tarPos, Register.SP);
        }
    }
}