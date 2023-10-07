package cross.symbol;

import cross.Symbol;

public class VarSymbol extends Symbol {
    private final int dim;

    public VarSymbol(String name, int lineNum, int dim) {
        super(name, lineNum);
        this.dim = dim;
    }
}
