package analysis;

public class Token {
    private final String val;
    private final TokenType type;
    private final int lineNum;

    public Token(String val, TokenType type, int lineNum) {
        this.val = val;
        this.type = type;
        this.lineNum = lineNum;
    }

    public String getVal() {
        return val;
    }

    public int getLineNum() {
        return lineNum;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type + " " + val;
    }
}
