package util;

public enum OpCode {
    SYSCALL("syscall"),
    COMMENT("#"), LABEL(""),
    ADDU("addu"), SUBU("subu"), SLL("sll"), // R instr
    MULT("mult"), DIV("div"), MFLO("mflo"), MFHI("mfhi"),
    LW("lw"), SW("sw"),
    BNE("bne"),
    ADDI("addi"),
    J("j"), JAL("jal"), JR("jr"),
    LI("li"), LA("la");
    private final String val;

    OpCode(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
