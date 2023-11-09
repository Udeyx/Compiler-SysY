package midend.ir;

import midend.ir.Value.User;
import midend.ir.Value.Value;

public class Use {
    private final Value value;
    private final User user;
    private final int pos;

    public Use(Value value, User user, int pos) {
        this.value = value;
        this.user = user;
        this.pos = pos;
    }
}
