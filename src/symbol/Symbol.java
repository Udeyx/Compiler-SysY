package symbol;

import util.DataType;

public class Symbol {
    private final String name;
    private final DataType dataType;
    private final int dim;

    public Symbol(String name, DataType dataType) {
        this.name = name;
        this.dataType = dataType;
        this.dim = switch (dataType) {
            case TWO -> 2;
            case ONE -> 1;
            case INT -> 0;
            default -> -1;
        };
    }

    public Symbol(String name, int dim) {
        this.name = name;
        this.dim = dim;
        this.dataType = switch (dim) {
            case 2 -> DataType.TWO;
            case 1 -> DataType.ONE;
            case 0 -> DataType.INT;
            default -> DataType.VOID;
        };
    }

    public String getName() {
        return name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public int getDim() {
        return dim;
    }
}
