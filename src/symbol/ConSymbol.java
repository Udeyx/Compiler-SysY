package symbol;

import util.DataType;

public class ConSymbol extends Symbol {
    public ConSymbol(String name, DataType dataType) {
        super(name, dataType);
    }

    public ConSymbol(String name, int dim) {
        super(name, dim);
    }
}
