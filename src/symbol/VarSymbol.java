package symbol;

import util.DataType;

public class VarSymbol extends Symbol {
    public VarSymbol(String name, DataType dataType) {
        super(name, dataType);
    }

    public VarSymbol(String name, int dim) {
        super(name, dim);
    }
}
