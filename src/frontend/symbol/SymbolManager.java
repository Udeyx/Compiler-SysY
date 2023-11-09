package frontend.symbol;

import java.util.ArrayList;

public class SymbolManager {
    private static final SymbolManager SYMBOL_MANAGER = new SymbolManager();
    private final ArrayList<SymbolTable> symbolTables;
    private int loopDepth;
    private FuncSymbol curFunc;
    private boolean inGlobal;

    private SymbolManager() {
        this.symbolTables = new ArrayList<>();
        this.loopDepth = 0;
        this.curFunc = null;
        this.inGlobal = true;
    }

    public boolean isInGlobal() {
        return inGlobal;
    }

    public void setInGlobal(boolean inGlobal) {
        this.inGlobal = inGlobal;
    }

    public static SymbolManager getInstance() {
        return SYMBOL_MANAGER;
    }

    private SymbolTable peek() {
        return symbolTables.get(symbolTables.size() - 1);
    }

    public void addScope() {
        symbolTables.add(new SymbolTable());
    }

    public void delScope() {
        symbolTables.remove(symbolTables.size() - 1);
    }

    public boolean addSymbol(Symbol symbol) {
        return peek().addSymbol(symbol);
    }

    public Symbol getSymbol(String name) {
        for (int i = symbolTables.size() - 1; i >= 0; i--) {
            Symbol temp = symbolTables.get(i).getSymbol(name);
            if (temp != null)
                return temp;
        }
        return null;
    }

    public void setCurFunc(FuncSymbol curFunc) {
        this.curFunc = curFunc;
    }

    public FuncSymbol getCurFunc() {
        return curFunc;
    }

    public int getLoopDepth() {
        return loopDepth;
    }

    public void setLoopDepth(int loopDepth) {
        this.loopDepth = loopDepth;
    }
}
