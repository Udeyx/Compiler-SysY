package midend.ir;

import frontend.symbol.SymbolManager;
import midend.ir.type.*;
import midend.ir.value.*;
import midend.ir.value.instruction.*;
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

    // getter and setters
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

    // build non-instruction value
    public GlobalVar buildGlobalVar(PointerType type, boolean isConst, ArrayList<Integer> initVal) {
        GlobalVar globalVar = new GlobalVar(nameSpace.allocGvName(), type, isConst, initVal);
        module.addGlobalVar(globalVar);
        return globalVar;
    }

    public StringLiteral buildStringLiteral(String content) {
        StringLiteral stringLiteral = new StringLiteral(nameSpace.allocSlName(),
                new PointerType(new ArrayType(IntegerType.I8, content.length() + 1)),
                content);
        module.addStringLiteral(stringLiteral);
        return stringLiteral;
    }

    public Function buildFunction(String name, Type type) {
        Function function = new Function(nameSpace.allocFuncName(name), type);
        nameSpace.addFunc(function);
        BasicBlock firstBlock = buildBasicBlock(function);
        curBasicBlock = firstBlock;
        function.addBasicBlock(firstBlock);
        module.addFunction(function);
        return function;
    }

    public Param buildParam(Type type) {
        Param param = new Param(nameSpace.allocLvName(curFunction), type);
        curFunction.addParam(param);
        return param;
    }

    public BasicBlock buildBasicBlock(Function function) {
        return new BasicBlock(nameSpace.allocBBName(), function);
    }

    public BasicBlock buildBasicBlockWithCurFunc() {
        return buildBasicBlock(curFunction);
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

    // build instructions
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

    public GEPInst buildGEPForPutStr(Value pointer, int index) {
        Value lv = buildLV(new PointerType(IntegerType.I8));
        GEPInst gepInst = new GEPInst(pointer, lv);
        ConstantInt zeroCon = buildConstantInt(0);
        ConstantInt constIndex = buildConstantInt(index);
        gepInst.addIndex(zeroCon);
        gepInst.addIndex(constIndex);
        curBasicBlock.addInst(gepInst);
        return gepInst;
    }

    public CallInst buildCall(String funcName, ArrayList<Value> arguments) {
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

    public CallInst buildCall(Function function, ArrayList<Value> arguments) {
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

    public AddInst buildAddWithLV(Type type, Value operand1, Value operand2) {
        Value lv = buildLV(type);
        return buildAdd(type, operand1, operand2, lv);
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

    public SremInst buildSrem(Type type, Value operand1, Value operand2) {
        Value lv = buildLV(type);
        SremInst sremInst = new SremInst(type, operand1, operand2, lv);
        curBasicBlock.addInst(sremInst);
        return sremInst;
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

    public PhiInst buildPhi(Type type, Value option1, BasicBlock src1, Value option2,
                            BasicBlock src2, Value tar) {
        PhiInst phiInst = new PhiInst(type, option1, src1, option2, src2, tar, nameSpace.allocLvName(curFunction));
        curBasicBlock.addInst(0, phiInst);
        return phiInst;
    }


    public BranchInst buildBranch(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
        BranchInst branchInst = new BranchInst(cond, trueBlock, falseBlock);
        boolean added = curBasicBlock.addInst(branchInst);

        if (added) {
            curBasicBlock.addNextBb(trueBlock);
            trueBlock.addPrevBb(curBasicBlock);
            if (!trueBlock.equals(falseBlock)) {
                curBasicBlock.addNextBb(falseBlock);
                falseBlock.addPrevBb(curBasicBlock);
            }
        }
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
