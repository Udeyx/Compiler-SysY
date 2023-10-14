package symbol;

import java.util.ArrayList;

public class Manager {
    private final ArrayList<Table> tables;
    private static final Manager MANAGER = new Manager();
    private int loopDepth;
    private FuncSymbol curFunc;

    private Manager() {
        this.tables = new ArrayList<>();
        this.loopDepth = 0;
        this.curFunc = null;
    }

    public static Manager getInstance() {
        return MANAGER;
    }

    private Table peek() {
        return tables.get(tables.size() - 1);
    }

    public void addScope() {
        tables.add(new Table());
    }

    public void delScope() {
        tables.remove(tables.size() - 1);
    }

    public boolean addSymbol(Symbol symbol) {
        return peek().addSymbol(symbol);
    }

    public Symbol getSymbol(String name) {
        for (int i = tables.size() - 1; i >= 0; i--) {
            Symbol temp = tables.get(i).getSymbol(name);
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
