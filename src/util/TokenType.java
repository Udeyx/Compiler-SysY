package util;

public enum TokenType {
    IDENFR, INTCON, STRCON,
    MAINTK, RETURNTK,
    CONSTTK, INTTK,
    BREAKTK, CONTINUETK, VOIDTK,
    IFTK, ELSETK, FORTK,
    GETINTTK, PRINTFTK,
    PLUS, MINU, MULT, DIV, MOD, // + - * / %
    NOT, AND, OR,
    LSS, LEQ, // < <=
    GRE, GEQ, // > >=
    EQL, NEQ, // == !=
    ASSIGN, // =
    SEMICN, COMMA, // ;,
    LPARENT, RPARENT, // ()
    LBRACK, RBRACK, // []
    LBRACE, RBRACE // {}
}
