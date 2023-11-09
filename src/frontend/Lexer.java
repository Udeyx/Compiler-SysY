package frontend;

import util.TokenType;

import java.util.ArrayList;

public class Lexer {
    private int pos;
    private int lineNum;
    private char curChar;

    private final String source;

    public Lexer(String source) {
        this.pos = -1;
        this.lineNum = 1;
        this.source = source;
        this.curChar = 0;
    }


    public ArrayList<Token> tokenize() {
        ArrayList<Token> tokens = new ArrayList<>();
        while (hasNext()) {
            StringBuilder sb = new StringBuilder();
            char c = next();
            if (isBlank(c)) { // '\n', '\r',  ' ', '\t'
                if (isLineBreak(c))
                    lineNum++;
            } else if (c == '/') { // comment or div
                sb.append(c);
                if (hasNext()) {
                    switch (preview(1)) {
                        case '/' -> {
                            next(); // read "//"
                            if (!hasNext())
                                break;

                            c = next(); // read the char next to "//"
                            while (!isLineBreak(c) && hasNext())
                                c = next();
                            lineNum++; // already read the '\n' or at end
                        }
                        case '*' -> {
                            next(); // read "*/"
                            if (!hasNext())
                                break;

                            c = next(); // read the char next to "/*"
                            while (true) {
                                if (c == '*' && hasNext() && preview(1) == '/') {
                                    next(); // read the "*/"
                                    break;
                                } else {
                                    if (isLineBreak(c))
                                        lineNum++;
                                    if (!hasNext())
                                        break;
                                    c = next();
                                }
                            }
                        }
                        default -> tokens.add(new Token(sb.toString(), tellType(sb.toString()), lineNum));
                    }
                } else {
                    tokens.add(new Token(sb.toString(), tellType(sb.toString()), lineNum));
                }
            } else { // not blank and not comment and not '/'
                if (c == '_' || Character.isLetter(c)) { // ident or keyword
                    sb.append(c);
                    while (hasNext()
                            && (preview(1) == '_' || Character.isLetterOrDigit(preview(1)))) {
                        sb.append(next());
                    }
                } else if (Character.isDigit(c)) {
                    sb.append(c);
                    while (hasNext() && Character.isDigit(preview(1)))
                        sb.append(next());
                } else if (c == '"') {
                    sb.append(c);
                    while (hasNext()) {
                        c = next();
                        sb.append(c);
                        if (c == '"')
                            break;
                    }
                } else if ("+-*%;,()[]{}".contains(String.valueOf(c))) {
                    sb.append(c);
                } else if (c == '&' || c == '|') {
                    sb.append(c);
                    if (hasNext() && preview(1) == c)
                        sb.append(next());
                } else if ("!><=".contains(String.valueOf(c))) {
                    sb.append(c);
                    if (hasNext() && preview(1) == '=')
                        sb.append(next());
                } else {
                    System.out.printf("fuck, c == %c", c);
                }
                if (!sb.toString().isEmpty())
                    tokens.add(new Token(sb.toString(), tellType(sb.toString()), lineNum));
            }
        }
        return tokens;
    }

    private boolean isBlank(char c) {
        return "\n \t\r".contains(String.valueOf(c));
    }

    private boolean isLineBreak(char c) {
        return c == '\n';
    }

    private boolean hasNext() {
        return pos + 1 < source.length();
    }

    private char next() {
        pos++;
        curChar = source.charAt(pos);
        return curChar;
    }

    private char preview(int offset) {
        return source.charAt(pos + offset);
    }


    private TokenType tellType(String token) { // pure function
        return switch (token) {
            case "main" -> TokenType.MAINTK;
            case "const" -> TokenType.CONSTTK;
            case "int" -> TokenType.INTTK;
            case "break" -> TokenType.BREAKTK;
            case "continue" -> TokenType.CONTINUETK;
            case "if" -> TokenType.IFTK;
            case "else" -> TokenType.ELSETK;
            case "!" -> TokenType.NOT;
            case "&&" -> TokenType.AND;
            case "||" -> TokenType.OR;
            case "for" -> TokenType.FORTK;
            case "getint" -> TokenType.GETINTTK;
            case "printf" -> TokenType.PRINTFTK;
            case "return" -> TokenType.RETURNTK;
            case "+" -> TokenType.PLUS;
            case "-" -> TokenType.MINU;
            case "void" -> TokenType.VOIDTK;
            case "*" -> TokenType.MULT;
            case "/" -> TokenType.DIV;
            case "%" -> TokenType.MOD;
            case "<" -> TokenType.LSS;
            case "<=" -> TokenType.LEQ;
            case ">" -> TokenType.GRE;
            case ">=" -> TokenType.GEQ;
            case "==" -> TokenType.EQL;
            case "!=" -> TokenType.NEQ;
            case "=" -> TokenType.ASSIGN;
            case ";" -> TokenType.SEMICN;
            case "," -> TokenType.COMMA;
            case "(" -> TokenType.LPARENT;
            case ")" -> TokenType.RPARENT;
            case "[" -> TokenType.LBRACK;
            case "]" -> TokenType.RBRACK;
            case "{" -> TokenType.LBRACE;
            case "}" -> TokenType.RBRACE;
            default -> {
                char c = token.charAt(0);
                if (c == '"')
                    yield TokenType.STRCON;
                else if (Character.isDigit(c))
                    yield TokenType.INTCON;
                else yield TokenType.IDENFR;
            }
        };
    }
}
