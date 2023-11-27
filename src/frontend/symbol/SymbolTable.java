package frontend.symbol;

import java.util.HashMap;

public class SymbolTable {
    private final HashMap<String, Symbol> symbolMap;

    public SymbolTable() {
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
