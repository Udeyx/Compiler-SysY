package midend.ir;

import midend.ir.value.User;
import midend.ir.value.Value;

public class Use {
    private final Value value;
    private final User user;
    private final int pos;

    public Use(Value value, User user, int pos) {
        this.value = value;
        this.user = user;
        this.pos = pos;
    }

    public User getUser() {
        return user;
    }

    public int getPos() {
        return pos;
    }
}
