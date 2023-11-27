package backend.directive;

public class AsciizDirect extends Direct {
    private final String name;
    private final String content;

    public AsciizDirect(String name, String content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public String toString() {
        return name.substring(1) + ": .asciiz \"" + content + "\"";
    }
}
