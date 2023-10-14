package util;

public enum NodeType {
    COMPUNIT("CompUnit"),
    DECL("Decl"),
    CONSTDECL("ConstDecl"),
    BTYPE("BType"),
    CONSTDEF("ConstDef"),
    CONSTINITVAL("ConstInitVal"),
    VARDECL("VarDecl"),
    VARDEF("VarDef"),
    INITVAL("InitVal"),
    FUNCDEF("FuncDef"),
    MAINFUNCDEF("MainFuncDef"),
    FUNCTYPE("FuncType"),
    FUNCFPARAMS("FuncFParams"),
    FUNCFPARAM("FuncFParam"),
    BLOCK("Block"),
    BLOCKITEM("BlockItem"),
    STMT("Stmt"),
    FORSTMT("ForStmt"),
    EXP("Exp"),
    COND("Cond"),
    LVAL("LVal"),
    PRIMARYEXP("PrimaryExp"),
    NUMBER("Number"),
    UNARYEXP("UnaryExp"),
    UNARYOP("UnaryOp"),
    FuncRParams("FuncRParams"),
    MULEXP("MulExp"),
    ADDEXP("AddExp"),
    RELEXP("RelExp"),
    EQEXP("EqExp"),
    LANDEXP("LAndExp"),
    LOREXP("LOrExp"),
    CONSTEXP("ConstExp"),
    TERMINATOR("Terminator");
    private final String val;

    NodeType(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }
}
