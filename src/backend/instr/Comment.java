package backend.instr;

import util.OpCode;

public class Comment extends Instr {

    private final String content;

    public Comment(String content) {
        super(OpCode.COMMENT);
        this.content = content;
    }

    @Override
    public String toString() {
        return "\n" + opCode + " " + content;
    }
}
