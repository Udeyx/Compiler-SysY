package cross;

public class Symbol {
    private final String name;
    private final int lineNum;

    public Symbol(String name, int lineNum) {
        this.name = name;
        this.lineNum = lineNum;
    }

    public String getName() {
        return name;
    }
}
