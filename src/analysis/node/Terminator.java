package analysis.node;

import analysis.*;
import analysis.Error;

import java.util.ArrayList;

public class Terminator extends Node {
    private final Token val;

    public Terminator(Token val) {
        super(NodeType.TERMINATOR);
        this.val = val;
    }

    public Terminator(Token val, Error error) {
        super(NodeType.TERMINATOR);
        this.val = val;
        addError(error);
    }

    public Token getVal() {
        return val;
    }

    public void check() {
        if (val.getType().equals(TokenType.STRCON)) {
            String content = val.getVal();
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (!(c == '\n' || c == 32 || c == 33
                        || (c >= 40 && c <= 126 && c != '\\'))) {
                    addError(new Error(val.getLineNum(), ErrorType.A));
                    break; // to prevent multi error for one FormatString
                }
            }
        }
        if (!getErrors().isEmpty())
            System.out.println(getErrors().get(0));
    }

    @Override
    public String toString() {
        return val.toString();
    }
}
