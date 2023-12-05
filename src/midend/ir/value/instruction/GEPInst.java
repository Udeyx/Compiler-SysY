package midend.ir.value.instruction;

import backend.Register;
import midend.ir.type.PointerType;
import midend.ir.value.ConstantInt;
import midend.ir.value.Value;

import java.util.ArrayList;

public class GEPInst extends Instruction {
    private final Value tar;
    // operands: pointer, index1, index2, ......

    public GEPInst(Value pointer, Value tar) {
        super(tar.getName(), tar.getType());
        this.tar = tar;
        // maintain use def
        pointer.addUse(this, 0);
        this.operands.add(pointer);
    }

    public void addIndex(Value index) {
        index.addUse(this, operands.size());
        this.operands.add(index);
    }

    @Override
    public String toString() {
        Value pointer = operands.get(0);
        ArrayList<Value> indices = new ArrayList<>();
        for (int i = 1; i < operands.size(); i++) {
            indices.add(operands.get(i));
        }
        StringBuilder sb = new StringBuilder();
        sb.append(tar.getName());
        sb.append(" = getelementptr ");
        sb.append(((PointerType) pointer.getType()).getEleType());
        sb.append(", ");
        sb.append(pointer.getType()).append(" ");
        sb.append(pointer.getName()).append(", ");
        for (int i = 0; i < indices.size(); i++) {
            sb.append(indices.get(i).getType()).append(" ");
            sb.append(indices.get(i).getName());
            if (i < indices.size() - 1)
                sb.append(", ");
        }
        return sb.toString();
    }

    /**
     * 考虑到操作数是指针，具体要执行一下几步
     * 1. 首先找到指针变量
     * 2. 用lw取出指针变量的内容，即其所指东西的地址
     * 3. 用取出的地址加上偏移量就可得到目标指针的地址
     * 4. 给tar分配空间(tar就是该存储目标指针的地方)，并把目标指针存进去
     */
    @Override
    public void buildMIPS() {
        super.buildMIPS();
        // 由于全局变量是向上生长的，所以局部的数组也要向上生长，否则传参就寄了
        // 因为形参一定不是全局变量，因此没有办法判断数组到底是向哪个方向长
        // 所以需要全部处理成向上长的
        // 而且mips里面的字面量是按照字节来的，所以要乘4
        // 总的来看就是base + 4 * offset
        Value pointer = operands.get(0);
        ArrayList<Value> indices = new ArrayList<>();
        for (int i = 1; i < operands.size(); i++) {
            indices.add(operands.get(i));
        }
        Value index = indices.get(indices.size() - 1);
        // T0存偏移，T1存基地址，记得偏移要乘4!!!
        if (index instanceof ConstantInt constantInt) {
            mipsBuilder.buildLi(Register.K0, constantInt);
        } else {
            int offPos = mipsBuilder.getSymbolPos(index.getName());
            mipsBuilder.buildLw(Register.K0, offPos, Register.SP);
        }
        mipsBuilder.buildSll(Register.K0, Register.K0, 2);

        if (pointer.getName().charAt(0) == '@') {
            mipsBuilder.buildLa(Register.K1, pointer.getName());
        } else {
            int srcPos = mipsBuilder.getSymbolPos(pointer.getName());
            mipsBuilder.buildLw(Register.K1, srcPos, Register.SP);
        }
        mipsBuilder.buildAddu(Register.K0, Register.K1, Register.K0);

        // 局部变量等于基地址减去偏移量！！！全局变量不知道

        int tarPos = mipsBuilder.allocStackSpace(tar.getName());
        mipsBuilder.buildSw(Register.K0, tarPos, Register.SP);
    }
}
