package cross;

import java.util.ArrayList;

public class SymTable {
    private final ArrayList<Symbol> symbols;

    public SymTable() {
        this.symbols = new ArrayList<>();
    }

    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);
    }

    public boolean hasSymbol(String identity) {
        for (Symbol symbol : symbols) {
            if (symbol.getName().equals(identity))
                return true;
        }
        return false;
    }
}