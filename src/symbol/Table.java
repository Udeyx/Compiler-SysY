package symbol;

import java.util.HashMap;

public class Table {
    private final HashMap<String, Symbol> symbolMap;

    public Table() {
        this.symbolMap = new HashMap<>();
    }

    public Symbol getSymbol(String name) {
        return symbolMap.get(name);
    }

    public boolean addSymbol(Symbol symbol) {
        if (symbolMap.containsKey(symbol.getName()))
            return false;
        symbolMap.put(symbol.getName(), symbol);
        return true;
    }
}
