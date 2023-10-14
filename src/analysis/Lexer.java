package analysis;

import util.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Lexer {
    public ArrayList<Token> tokenize(ArrayList<String> lines) {
        ArrayList<String> noComment = removeComment(lines);
        ArrayList<Token> tokens = new ArrayList<>();
        for (int i = 0; i < noComment.size(); i++)
            tokens.addAll(tokenizeALine(noComment.get(i), i));
        return tokens;
    }


    private ArrayList<Token> tokenizeALine(String line, int lineNum) { // pure function
        ArrayList<Token> res = new ArrayList<>();
        int len = line.length();
        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);
            StringBuilder sb = new StringBuilder();
            if (!"\n \t\r".contains(String.valueOf(c))) {
                if (c == '_' || Character.isLetter(c)) {
                    while (c == '_' || Character.isLetterOrDigit(c)) {
                        sb.append(c);
                        i++;
                        if (i >= len) {
                            break;
                        }
                        c = line.charAt(i);
                    }
                    i--;
                } else if (Character.isDigit(c)) {
                    while (Character.isDigit(c)) {
                        sb.append(c);
                        i++;
                        if (i >= len) {
                            break;
                        }
                        c = line.charAt(i);
                    }
                    i--;
                } else if (c == '"') {
                    sb.append(c);
                    do {
                        i++;
                        if (i >= len) {
                            break;
                        }
                        c = line.charAt(i);
                        sb.append(c);
                    } while (c != '"');
                } else if ("+-*/%;,()[]{}".contains(String.valueOf(c))) {
                    sb.append(c);
                } else if (c == '|' || c == '&') {
                    sb.append(c);
                    if (i + 1 < len && line.charAt(i + 1) == c) {
                        sb.append(c);
                        i++;
                    }
                } else if (c == '!') {
                    sb.append(c);
                    if (i + 1 < len && line.charAt(i + 1) == '=') {
                        sb.append('=');
                        i++;
                    }
                } else if (c == '<') {
                    sb.append(c);
                    if (i + 1 < len && line.charAt(i + 1) == '=') {
                        sb.append('=');
                        i++;
                    }
                } else if (c == '>') {
                    sb.append(c);
                    if (i + 1 < len && line.charAt(i + 1) == '=') {
                        sb.append('=');
                        i++;
                    }
                } else if (c == '=') {
                    sb.append(c);
                    if (i + 1 < len && line.charAt(i + 1) == '=') {
                        sb.append('=');
                        i++;
                    }
                } else {
                    System.out.printf("fuck, c == %c", c);
                }
                res.add(new Token(sb.toString(), tellType(sb.toString()), lineNum));
            }
        }
        return res;
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

    private ArrayList<String> removeComment(ArrayList<String> lines) {
        ArrayList<String> noInlineComment = lines.stream()
                .map(this::removeInlineComment)
                .collect(Collectors.toCollection(ArrayList::new));
        return removeMultiLineComment(noInlineComment);
    }

    private String removeInlineComment(String line) {
        boolean inStr = false;
        for (int i = 0; i < line.length() - 1; i++) { // i < len - 1 !
            if (inStr) {
                if (line.charAt(i) == '"') {
                    inStr = false;
                }
            } else {
                if (line.charAt(i) == '"') {
                    inStr = true;
                } else if (line.charAt(i) == '/' && line.charAt(i + 1) == '/') {
                    return line.substring(0, i);
                }

            }

        }
        return line;
    }

    private ArrayList<String> removeMultiLineComment(ArrayList<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
            sb.append("\n");
        }
        String line = sb.toString();
        boolean inStr = false;
        boolean inComment = false;
        StringBuilder ab = new StringBuilder(); // answer builder
        for (int i = 0; i < line.length() - 1; i++) { // i < len - 1 !
            char c = line.charAt(i);
            if (inComment) {
                if (c == '\n') {
                    ab.append(c);
                } else if (c == '*' && line.charAt(i + 1) == '/') {
                    inComment = false;
                    i += 1; // or '/' in "*/" will be in the result
                }
            } else {
                if (inStr) {
                    if (c == '"') {
                        inStr = false;
                    }
                } else {
                    if (c == '"') {
                        inStr = true;
                    } else if (c == '/' && line.charAt(i + 1) == '*') {
                        inComment = true;
                    }
                }
                if (!inComment) {
                    ab.append(c);
                }
            }
        }
        return new ArrayList<>(Arrays.asList(ab.toString().split("\n")));
    }
}
