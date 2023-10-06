package analysis;

import java.util.ArrayList;

public class Iter {
    private final ArrayList<Token> tokens;
    private int pos;
    private Token curToken;

    public Iter(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.pos = -1;
        this.curToken = null;
    }

    public boolean hasNext() {
        return pos + 1 < tokens.size();
    }

    public Token next() {
        pos++;
        curToken = tokens.get(pos);
        return curToken;
    }

    public Token preview(int offset) {
        if (pos + offset < tokens.size())
            return tokens.get(pos + offset);
        return null;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
