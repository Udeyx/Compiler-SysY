package cross;

import java.util.ArrayList;

public class SymManager {
    private static final SymManager SYM_MANAGER = new SymManager();
    private final ArrayList<SymTable> tables;
    private int top;

    private SymManager() {
        this.tables = new ArrayList<>();
        this.top = -1;
    }

    public static SymManager getInstance() {
        return SYM_MANAGER;
    }

    public void push(SymTable table) {
        top++;
        tables.add(top, table);
    }

    public SymTable pop() {
        if (top >= 0) {
            top--;
            return tables.get(top + 1);
        }
        return null;
    }

    public SymTable peek() {
        return top >= 0 ? tables.get(top) : null;
    }

    public boolean isRepeated(String identity) {
        return peek().hasSymbol(identity);
    }

    public void addDefinition(Symbol symbol) {
        peek().addSymbol(symbol);
    }
}
