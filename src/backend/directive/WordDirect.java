package backend.directive;

import java.util.ArrayList;

public class WordDirect extends Direct {
    private final String name;
    private final ArrayList<Integer> val;

    public WordDirect(String name, ArrayList<Integer> val) {
        this.name = name;
        this.val = val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name.substring(1)).append(": .word ");
        for (int i = val.size() - 1; i >= 0; i--) {
            sb.append(val.get(i));
            if (i > 0)
                sb.append(", ");
        }
        return sb.toString();
    }
}
