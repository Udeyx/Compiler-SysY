package midend.ir;

import midend.ir.value.Function;

import java.util.HashMap;

public class NameSpace {
    private static final NameSpace NAME_SPACE = new NameSpace();
    private int funcNum;
    private int gvNum;
    private HashMap<Function, Integer> lvNum;
    private int bbNum;
    private int slNum;

    private NameSpace() {
        this.funcNum = 0;
        this.gvNum = 0;
        this.lvNum = new HashMap<>();
        this.bbNum = 0;
        this.slNum = 0;
    }

    public static NameSpace getInstance() {
        return NAME_SPACE;
    }

    public String allocGvName() {
        String gvName = "@gv" + gvNum;
        gvNum++;
        return gvName;
    }

    public String allocSlName() {
        String slName = "@s" + slNum;
        slNum++;
        return slName;
    }

    public String allocBBName() {
        String bbName = "b" + bbNum;
        bbNum++;
        return bbName;
    }

    public String allocFuncName(String originName) {
        if (originName.equals("main"))
            return "@main";
        String funcName = "@f" + funcNum;
        funcNum++;
        return funcName;
    }

    public String allocLvName(Function function) {
        String lvName = "%lv" + lvNum.get(function);
        lvNum.put(function, lvNum.get(function) + 1);
        return lvName;
    }

    public void addFunc(Function function) {
        lvNum.put(function, 0);
    }

}
