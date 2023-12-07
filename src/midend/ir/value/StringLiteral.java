package midend.ir.value;

import midend.ir.type.ArrayType;
import midend.ir.type.PointerType;
import midend.ir.type.Type;

public class StringLiteral extends Value {
    private final String content;

    public StringLiteral(String name, PointerType type, String content) {
        super(name, type);
        this.content = content;
    }

    @Override
    public String toString() {
        return name + " = " + "constant " + ((PointerType) type).getEleType().toString() + "c\"" + content + "\\00\"";
    }

    @Override
    public void buildMIPS() {
//        super.buildMIPS();
        mipsBuilder.buildAsciiz(name, content.replace("\n", "\\n"));
    }

    @Override
    public void buildFIFOMIPS() {
        mipsBuilder.buildAsciiz(name, content.replace("\n", "\\n"));
    }
}
