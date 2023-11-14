package midend.ir;

import frontend.node.exp.MulExp;
import frontend.symbol.FuncSymbol;
import frontend.symbol.SymbolManager;
import midend.ir.Type.*;
import midend.ir.Value.*;
import midend.ir.Value.instruction.*;
import util.ICmpType;

import java.util.ArrayList;

public class IRBuilder {
    private static final IRBuilder IR_BUILDER = new IRBuilder();
    private final SymbolManager manager;
    private final NameSpace nameSpace;
    private final Module module;
    private Function curFunction;
    private BasicBlock curBasicBlock;
    private BasicBlock curIncreaseBlock;
    private BasicBlock curFinalBlock;

    private IRBuilder() {
        this.module = Module.getInstance();
        this.manager = SymbolManager.getInstance();
        this.nameSpace = NameSpace.getInstance();
        this.curFunction = null;
        this.curBasicBlock = null;
        this.curIncreaseBlock = null;
        this.curFinalBlock = null;
    }

    public static IRBuilder getInstance() {
        return IR_BUILDER;
    }

    public Module getModule() {
        return module;
    }

    public void setCurFunction(Function function) {
        this.curFunction = function;
    }

    public Function getCurFunction() {
        return curFunction;
    }

    public BasicBlock getCurBasicBlock() {
        return curBasicBlock;
    }

    public void setCurBasicBlock(BasicBlock curBasicBlock) {
        this.curBasicBlock = curBasicBlock;
    }

    public BasicBlock getCurFinalBlock() {
        return curFinalBlock;
    }

    public void setCurFinalBlock(BasicBlock curFinalBlock) {
        this.curFinalBlock = curFinalBlock;
    }

    public BasicBlock getCurIncreaseBlock() {
        return curIncreaseBlock;
    }

    public void setCurIncreaseBlock(BasicBlock curIncreaseBlock) {
        this.curIncreaseBlock = curIncreaseBlock;
    }

    public GlobalVar buildGlobalVar(PointerType type, boolean isConst, ArrayList<Integer> initVal) {
        return new GlobalVar(nameSpace.allocGvName(), type, isConst, initVal);
    }

    public Function buildFunction(String name, Type type) {
        Function function = new Function(nameSpace.allocFuncName(name), type);
        nameSpace.addFunc(function);
        BasicBlock firstBlock = buildBasicBlock();
        curBasicBlock = firstBlock;
        function.addBasicBlock(firstBlock);
        return function;
    }

    public Param buildParam(Type type) {
        Param param = new Param(nameSpace.allocLvName(curFunction), type);
        curFunction.addParam(param);
        return param;
    }

    public BasicBlock buildBasicBlock() {
        return new BasicBlock(nameSpace.allocBBName());
    }

    public ConstantInt buildConstantInt(int val) {
        return buildConstantInt(IntegerType.I32, val);
    }

    public ConstantInt buildConstantInt(Type type, int val) {
        return new ConstantInt(type, val);
    }

    public Value buildLV(Type type) {
        return new Value(nameSpace.allocLvName(curFunction), type);
    }

    public AllocaInst buildAlloca(PointerType type) {
        AllocaInst allocaInst = new AllocaInst(nameSpace.allocLvName(curFunction), type);
        curBasicBlock.addInst(allocaInst);
        return allocaInst;
    }

    public StoreInst buildStore(Value src, Value tar) {
        StoreInst storeInst = new StoreInst(src, tar);
        curBasicBlock.addInst(storeInst);
        return storeInst;
    }

    public LoadInst buildLoad(Value src, Value tar) {
        LoadInst loadInst = new LoadInst(src, tar);
        curBasicBlock.addInst(loadInst);
        return loadInst;
    }

    public LoadInst buildLoad(Value src) {
        Value lv = buildLV(IntegerType.I32);
        return buildLoad(src, lv);
    }

    public GEPInst buildGEP(Value pointer, Value index) {
        Value lv = buildLV(new PointerType(IntegerType.I32));
        GEPInst gepInst = new GEPInst(pointer, lv);
        gepInst.addIndex(index);
        curBasicBlock.addInst(gepInst);
        return gepInst;
    }


    public GEPInst buildGEP(Value pointer, ArrayList<Integer> indices) {
        Value lv = buildLV(new PointerType(IntegerType.I32));
        GEPInst gepInst = new GEPInst(pointer, lv);
        for (int index : indices) {
            ConstantInt constIndex = buildConstantInt(index);
            gepInst.addIndex(constIndex);
        }
        curBasicBlock.addInst(gepInst);
        return gepInst;
    }

    public GEPInst buildGEPWithZeroPrep(Value pointer, Value index) {
        Value lv = buildLV(new PointerType(IntegerType.I32));
        GEPInst gepInst = new GEPInst(pointer, lv);
        ConstantInt zeroCon = buildConstantInt(0);
        gepInst.addIndex(zeroCon);
        gepInst.addIndex(index);
        curBasicBlock.addInst(gepInst);
        return gepInst;
    }

    public GEPInst buildGEPWithZeroPrep(Value pointer, int index) {
        Value lv = buildLV(new PointerType(IntegerType.I32));
        GEPInst gepInst = new GEPInst(pointer, lv);
        ConstantInt zeroCon = buildConstantInt(0);
        ConstantInt constIndex = buildConstantInt(index);
        gepInst.addIndex(zeroCon);
        gepInst.addIndex(constIndex);
        curBasicBlock.addInst(gepInst);
        return gepInst;
    }

    public CallInst buildCall(String funcName, ArrayList<Value> values) {
        ArrayList<Argument> arguments = new ArrayList<>();
        values.stream().map(Argument::new).forEach(arguments::add);
        Function function = (Function) manager.getSymbol(funcName).getLlvmObj();
        CallInst callInst;
        if (((FunctionType) function.getType()).getReturnType().equals(VoidType.VOID)) {
            callInst = new CallInst(function, arguments, null);
        } else {
            Value lv = buildLV(((FunctionType) function.getType()).getReturnType());
            callInst = new CallInst(function, arguments, lv);
        }
        curBasicBlock.addInst(callInst);
        return callInst;
    }

    public CallInst buildCall(Function function, ArrayList<Value> values) {
        ArrayList<Argument> arguments = new ArrayList<>();
        values.stream().map(Argument::new).forEach(arguments::add);
        CallInst callInst;
        if (((FunctionType) function.getType()).getReturnType().equals(VoidType.VOID)) {
            callInst = new CallInst(function, arguments, null);
        } else {
            Value lv = buildLV(((FunctionType) function.getType()).getReturnType());
            callInst = new CallInst(function, arguments, lv);
        }
        curBasicBlock.addInst(callInst);
        return callInst;
    }

    public ICmpInst buildICmp(ICmpType iCmpType, Value operand1, Value operand2, Value tar) {
        ICmpInst iCmpInst = new ICmpInst(iCmpType, operand1, operand2, tar);
        curBasicBlock.addInst(iCmpInst);
        return iCmpInst;
    }

    public ICmpInst buildICmpWithLV(ICmpType iCmpType, Value operand1, Value operand2) {
        Value lv = buildLV(IntegerType.I1);
        Value fixedOp1 = operand1.getType().equals(IntegerType.I32) ? operand1 :
                buildZExtWithLV(operand1, IntegerType.I32);
        Value fixedOp2 = operand2.getType().equals(IntegerType.I32) ? operand2 :
                buildZExtWithLV(operand2, IntegerType.I32);
        return buildICmp(iCmpType, fixedOp1, fixedOp2, lv);
    }


    public AddInst buildAdd(Type type, Value operand1, Value operand2, Value tar) {
        AddInst addInst = new AddInst(type, operand1, operand2, tar);
        curBasicBlock.addInst(addInst);
        return addInst;
    }

    public AddInst buildAdd(Type type, Value operand1, Value operand2) {
        Value lv = buildLV(type);
        AddInst addInst = new AddInst(type, operand1, operand2, lv);
        curBasicBlock.addInst(addInst);
        return addInst;
    }

    public SubInst buildSub(Type type, Value operand1, Value operand2, Value tar) {
        SubInst subInst = new SubInst(type, operand1, operand2, tar);
        curBasicBlock.addInst(subInst);
        return subInst;
    }

    public SubInst buildSub(Type type, Value operand1, Value operand2) {
        Value lv = buildLV(type);
        SubInst subInst = new SubInst(type, operand1, operand2, lv);
        curBasicBlock.addInst(subInst);
        return subInst;
    }

    public MulInst buildMul(Type type, Value operand1, Value operand2) {
        Value lv = buildLV(type);
        MulInst mulInst = new MulInst(type, operand1, operand2, lv);
        curBasicBlock.addInst(mulInst);
        return mulInst;
    }

    public SdivInst buildSdiv(Type type, Value operand1, Value operand2) {
        Value lv = buildLV(type);
        SdivInst sdivInst = new SdivInst(type, operand1, operand2, lv);
        curBasicBlock.addInst(sdivInst);
        return sdivInst;
    }

    public ReturnInst buildReturn(Value src) {
        ReturnInst returnInst = new ReturnInst(src);
        curBasicBlock.addInst(returnInst);
        return returnInst;
    }

    public ReturnInst buildReturn() {
        ReturnInst returnInst = new ReturnInst();
        curBasicBlock.addInst(returnInst);
        return returnInst;
    }

    public BranchInst buildBranch(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        BranchInst branchInst = new BranchInst(cond, trueBlock, falseBlock);
        curBasicBlock.addInst(branchInst);
        return branchInst;
    }

    public BranchInst buildNoCondBranch(BasicBlock tarBlock) {
        Value oneI1 = buildConstantInt(IntegerType.I1, 1);
        return buildBranch(oneI1, tarBlock, tarBlock);
    }

    public ZExtInst buildZExt(Value src, Value tar) {
        ZExtInst zExtInst = new ZExtInst(src, tar);
        curBasicBlock.addInst(zExtInst);
        return zExtInst;
    }

    public ZExtInst buildZExtWithLV(Value src, Type tarType) {
        Value lv = buildLV(tarType);
        return buildZExt(src, lv);
    }


}
