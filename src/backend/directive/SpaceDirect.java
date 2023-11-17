package backend.directive;

public class SpaceDirect extends Direct {
    private final String name;
    private final int eleNum;

    public SpaceDirect(String name, int eleNum) {
        this.name = name;
        this.eleNum = eleNum;
    }

    @Override
    public String toString() {
        return name.substring(1) + ": .space " + eleNum * 4;
    }
}
