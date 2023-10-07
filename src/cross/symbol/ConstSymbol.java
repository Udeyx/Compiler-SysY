package cross.symbol;

import cross.Symbol;

public class ConstSymbol extends Symbol {
    private final int dim;

    public ConstSymbol(String name, int lineNum, int dim) {
        super(name, lineNum);
        this.dim = dim;
    }
}
