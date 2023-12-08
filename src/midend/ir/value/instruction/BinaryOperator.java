package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.Type;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;
import util.BOType;

import java.util.List;

public class BinaryOperator extends Instruction {

    private final BOType boType;
    protected final Value tar;
    // operands: operand1, operand2

    public BinaryOperator(Type type, Value operand1, Value operand2, Value tar, BOType boType) {
        super(tar.getName(), type);
        this.tar = tar;
        this.boType = boType;
        // maintain def use
        operand1.addUse(this, 0);
        operand2.addUse(this, 1);
        this.operands.addAll(List.of(operand1, operand2));
    }

    @Override
    public String toString() {
        return tar.getName() + " = " + boType.toString() + " " + type + " "
                + operands.get(0).getName() + ", " + operands.get(1).getName();
    }

    @Override
    public void buildMIPS() {
        super.buildMIPS();
        Value operand1 = operands.get(0);
        Value operand2 = operands.get(1);
        if (operand1 instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K0, constantInt);
        } else {
            int offset = mipsBuilder.getSymbolPos(operand1);
            mipsBuilder.buildLw(Register.K0, offset, Register.SP);
        }

        if (operand2 instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K1, constantInt);
        } else {
            int offset = mipsBuilder.getSymbolPos(operand2);
            mipsBuilder.buildLw(Register.K1, offset, Register.SP);
        }

        switch (boType) {
            case ADD -> mipsBuilder.buildAddu(Register.K0, Register.K0, Register.K1);
            case SUB -> mipsBuilder.buildSubu(Register.K0, Register.K0, Register.K1);
            case MUL -> {
                mipsBuilder.buildMult(Register.K0, Register.K1);
                mipsBuilder.buildMflo(Register.K0);
            }
            case SDIV -> {
                mipsBuilder.buildDiv(Register.K0, Register.K1);
                mipsBuilder.buildMflo(Register.K0);
            }
            case SREM -> {
                mipsBuilder.buildDiv(Register.K0, Register.K1);
                mipsBuilder.buildMfhi(Register.K0);
            }
        }


        int tarPos = mipsBuilder.allocStackSpace(this);
        mipsBuilder.buildSw(Register.K0, tarPos, Register.SP);
    }

    @Override
    public void buildFIFOMIPS() {
        super.buildFIFOMIPS();
        Value operand1 = operands.get(0);
        Value operand2 = operands.get(1);
        Register op1Reg;
        Register op2Reg;
        if (operand1 instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K0, constantInt);
            op1Reg = Register.K0;
        } else {
            op1Reg = mipsBuilder.getSymbolReg(operand1);
            if (op1Reg == null) {
                int offset = mipsBuilder.getSymbolPos(operand1);
                mipsBuilder.buildLw(Register.K0, offset, Register.SP);
                op1Reg = Register.K0;
            }
        }

        if (operand2 instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K1, constantInt);
            op2Reg = Register.K1;
        } else {
            op2Reg = mipsBuilder.getSymbolReg(operand2);
            if (op2Reg == null) {
                int offset = mipsBuilder.getSymbolPos(operand2);
                mipsBuilder.buildLw(Register.K1, offset, Register.SP);
                op2Reg = Register.K1;
            }
        }

        int tarPos = mipsBuilder.allocStackSpace(this);
        Register tarReg = mipsBuilder.allocReg(this);
        switch (boType) {
            case ADD -> mipsBuilder.buildAddu(tarReg, op1Reg, op2Reg);
            case SUB -> mipsBuilder.buildSubu(tarReg, op1Reg, op2Reg);
            case MUL -> {
                mipsBuilder.buildMult(op1Reg, op2Reg);
                mipsBuilder.buildMflo(tarReg);
            }
            case SDIV -> {
                mipsBuilder.buildDiv(op1Reg, op2Reg);
                mipsBuilder.buildMflo(tarReg);
            }
            case SREM -> {
                mipsBuilder.buildDiv(op1Reg, op2Reg);
                mipsBuilder.buildMfhi(tarReg);
            }
        }
    }

    public boolean firstIsConst() {
        return operands.get(0) instanceof ConstantInt;
    }

    public boolean secondIsConst() {
        return operands.get(1) instanceof ConstantInt;
    }

    public int evaluate() {
        int op1 = Integer.parseInt(operands.get(0).getName());
        int op2 = Integer.parseInt(operands.get(1).getName());
        return switch (boType) {
            case ADD -> op1 + op2;
            case SUB -> op1 - op2;
            case MUL -> op1 * op2;
            case SDIV -> op1 / op2;
            case SREM -> op1 % op2;
        };
    }

    @Override
    public String calGVNHash() {
        Value op1 = operands.get(0);
        Value op2 = operands.get(1);
        return switch (boType) {
            case ADD, MUL -> op1.getName().compareTo(op2.getName()) > 0 ?
                    boType + op1.getName() + op2.getName() :
                    boType + op2.getName() + op1.getName();
            default -> boType + op1.getName() + op2.getName();
        };
    }

    public Value getOp1() {
        return operands.get(0);
    }

    public Value getOp2() {
        return operands.get(1);
    }

    public BOType getBoType() {
        return boType;
    }
}
