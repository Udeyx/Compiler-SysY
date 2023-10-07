package analysis;

import analysis.node.*;
import analysis.node.Number;

import java.util.ArrayList;
import java.util.EnumSet;

public class Parser {
    private final Iter iter;

    public Parser(Iter iter) {
        this.iter = iter;
    }

    public CompUnit parseCompUnit() {
        CompUnit compUnit = new CompUnit();
        while (iter.hasNext()) {
            switch (iter.preview(1).getType()) {
                case CONSTTK -> compUnit.addChild(parseDecl());
                case VOIDTK -> compUnit.addChild(parseFuncDef());
                default -> { // type should be "int"
                    if (iter.preview(3).getType().equals(TokenType.LPARENT)) {
                        if (iter.preview(2).getType().equals(TokenType.MAINTK))
                            compUnit.addChild(parseMainFuncDef());
                        else
                            compUnit.addChild(parseFuncDef());
                    } else
                        compUnit.addChild(parseDecl());
                }
            }
        }
        return compUnit;
    }

    private Decl parseDecl() {
        Decl decl = new Decl();
        if (iter.preview(1).getType().equals(TokenType.CONSTTK))
            decl.addChild(parseConstDecl());
        else
            decl.addChild(parseVarDecl());
        return decl;
    }

    private ConstDecl parseConstDecl() {
        ConstDecl constDecl = new ConstDecl();
        constDecl.addChild(new Terminator(iter.next())); // add the "const"
        constDecl.addChild(parseBType()); // read the "int"
        constDecl.addChild(parseConstDef());
        while (iter.preview(1).getType().equals(TokenType.COMMA)) {
            constDecl.addChild(new Terminator(iter.next())); // add the ','
            constDecl.addChild(parseConstDef());
        }
        constDecl.addChild(safelyRead(TokenType.SEMICN)); // add the ';'
        return constDecl;
    }

    private BType parseBType() {
        BType bType = new BType();
        bType.addChild(new Terminator(iter.next()));
        return bType;
    }

    private ConstDef parseConstDef() {
        ConstDef constDef = new ConstDef();
        constDef.addChild(new Terminator(iter.next())); // add the ident
        while (iter.preview(1).getType().equals(TokenType.LBRACK)) {
            constDef.addChild(new Terminator(iter.next())); // add the '['
            constDef.addChild(parseConstExp());
            constDef.addChild(safelyRead(TokenType.RBRACK)); // add the ']'
        }
        constDef.addChild(new Terminator(iter.next())); // add the '='
        constDef.addChild(parseConstInitVal());
        return constDef;
    }

    private ConstInitVal parseConstInitVal() {
        ConstInitVal constInitVal = new ConstInitVal();
        if (iter.preview(1).getType().equals(TokenType.LBRACE)) {
            constInitVal.addChild(new Terminator(iter.next())); // add '{'
            if (iter.preview(1).getType().equals(TokenType.RBRACE))
                constInitVal.addChild(new Terminator(iter.next())); // add '}'
            else {
                constInitVal.addChild(parseConstInitVal());
                while (iter.preview(1).getType().equals(TokenType.COMMA)) {
                    constInitVal.addChild(new Terminator(iter.next())); // add ','
                    constInitVal.addChild(parseConstInitVal());
                }
                constInitVal.addChild(new Terminator(iter.next())); // add '}'
            }
        } else
            constInitVal.addChild(parseConstExp());
        return constInitVal;
    }

    private VarDecl parseVarDecl() {
        VarDecl varDecl = new VarDecl();
        varDecl.addChild(parseBType());
        varDecl.addChild(parseVarDef());
        while (iter.preview(1).getType().equals(TokenType.COMMA)) {
            varDecl.addChild(new Terminator(iter.next())); // add ','
            varDecl.addChild(parseVarDef());
        }
        varDecl.addChild(safelyRead(TokenType.SEMICN)); // add ';'
        return varDecl;
    }

    private VarDef parseVarDef() {
        VarDef varDef = new VarDef();
        varDef.addChild(new Terminator(iter.next())); // add the ident
        while (iter.preview(1).getType().equals(TokenType.LBRACK)) {
            varDef.addChild(new Terminator(iter.next())); // add the '['
            varDef.addChild(parseConstExp());
            varDef.addChild(safelyRead(TokenType.RBRACK)); // add the ']'
        }
        if (iter.preview(1).getType().equals(TokenType.ASSIGN)) {
            varDef.addChild(new Terminator(iter.next())); // add the '='
            varDef.addChild(parseInitVal());
        }
        return varDef;
    }

    private InitVal parseInitVal() {
        InitVal initVal = new InitVal();
        if (iter.preview(1).getType().equals(TokenType.LBRACE)) {
            initVal.addChild(new Terminator(iter.next())); // add '{'
            if (!iter.preview(1).getType().equals(TokenType.RBRACE)) {
                initVal.addChild(parseInitVal());
                while (iter.preview(1).getType().equals(TokenType.COMMA)) {
                    initVal.addChild(new Terminator(iter.next())); // add ','
                    initVal.addChild(parseInitVal());
                }
            }
            initVal.addChild(new Terminator(iter.next())); // add '}'
        } else
            initVal.addChild(parseExp());
        return initVal;
    }

    private FuncDef parseFuncDef() {
        FuncDef funcDef = new FuncDef();
        funcDef.addChild(parseFuncType());
        funcDef.addChild(new Terminator(iter.next())); // add ident
        funcDef.addChild(new Terminator(iter.next())); // add '('
        if (iter.preview(1).getType().equals(TokenType.INTTK))
            funcDef.addChild(parseFuncFParams());
        funcDef.addChild(safelyRead(TokenType.RPARENT)); // add ')'
        funcDef.addChild(parseBlock());
        return funcDef;
    }

    private MainFuncDef parseMainFuncDef() {
        MainFuncDef mainFuncDef = new MainFuncDef();
        mainFuncDef.addChild(new Terminator(iter.next())); // add "int"
        mainFuncDef.addChild(new Terminator(iter.next())); // add "main"
        mainFuncDef.addChild(new Terminator(iter.next())); // add '('
        mainFuncDef.addChild(new Terminator(iter.next())); // add ')'
        mainFuncDef.addChild(parseBlock());
        return mainFuncDef;
    }

    private FuncType parseFuncType() {
        FuncType funcType = new FuncType();
        funcType.addChild(new Terminator(iter.next()));
        return funcType;
    }

    private FuncFParams parseFuncFParams() {
        FuncFParams funcFParams = new FuncFParams();
        funcFParams.addChild(parseFuncFParam());
        while (iter.preview(1).getType().equals(TokenType.COMMA)) {
            funcFParams.addChild(new Terminator(iter.next())); // add the ','
            funcFParams.addChild(parseFuncFParam());
        }
        return funcFParams;
    }

    private FuncFParam parseFuncFParam() {
        FuncFParam funcFParam = new FuncFParam();
        funcFParam.addChild(parseBType());
        funcFParam.addChild(new Terminator(iter.next())); // add the ident
        if (iter.preview(1).getType().equals(TokenType.LBRACK)) {
            funcFParam.addChild(new Terminator(iter.next())); // add the '['
            funcFParam.addChild(safelyRead(TokenType.RBRACK)); // add the ']'
            while (iter.preview(1).getType().equals(TokenType.LBRACK)) {
                funcFParam.addChild(new Terminator(iter.next())); // add the '['
                funcFParam.addChild(parseConstExp());
                funcFParam.addChild(safelyRead(TokenType.RBRACK)); // add the ']'
            }
        }
        return funcFParam;
    }

    private Block parseBlock() {
        Block block = new Block();
        block.addChild(new Terminator(iter.next())); // add the '{'
        while (!iter.preview(1).getType().equals(TokenType.RBRACE))
            block.addChild(parseBlockItem());
        block.addChild(new Terminator(iter.next())); // add the '}'
        return block;
    }

    private BlockItem parseBlockItem() {
        BlockItem blockItem = new BlockItem();
        if (iter.preview(1).getType().equals(TokenType.CONSTTK)
                || iter.preview(1).getType().equals(TokenType.INTTK))
            blockItem.addChild(parseDecl());
        else
            blockItem.addChild(parseStmt());
        return blockItem;
    }

    private Stmt parseStmt() {
        Stmt stmt = new Stmt();
        switch (iter.preview(1).getType()) {
            case PRINTFTK -> {
                stmt.addChild(new Terminator(iter.next())); // add the "printf"
                stmt.addChild(new Terminator(iter.next())); // add the '('
                stmt.addChild(new Terminator(iter.next())); // add the formatString
                while (iter.preview(1).getType().equals(TokenType.COMMA)) {
                    stmt.addChild(new Terminator(iter.next())); // add the ','
                    stmt.addChild(parseExp());
                }
                stmt.addChild(safelyRead(TokenType.RPARENT)); // add the ')'
                stmt.addChild(safelyRead(TokenType.SEMICN)); // add the ';'
            }
            case RETURNTK -> {
                stmt.addChild(new Terminator(iter.next())); // add the "return"
                EnumSet<TokenType> expFirst = EnumSet.of(TokenType.LPARENT, TokenType.IDENFR,
                        TokenType.INTCON, TokenType.PLUS, TokenType.MINU, TokenType.NOT);
                if (expFirst.contains(iter.preview(1).getType()))
                    stmt.addChild(parseExp());
                stmt.addChild(safelyRead(TokenType.SEMICN)); // add the ';'
            }
            case BREAKTK, CONTINUETK -> {
                stmt.addChild(new Terminator(iter.next())); // add the "break" or "continue"
                stmt.addChild(safelyRead(TokenType.SEMICN)); // add the ';'
            }
            case FORTK -> {
                stmt.addChild(new Terminator(iter.next())); // add the 'for'
                stmt.addChild(new Terminator(iter.next())); // add the '('
                if (iter.preview(1).getType().equals(TokenType.IDENFR))
                    stmt.addChild(parseForStmt());
                stmt.addChild(safelyRead(TokenType.SEMICN)); // add the ';'
                EnumSet<TokenType> condFirst = EnumSet.of(TokenType.LPARENT, TokenType.IDENFR,
                        TokenType.INTCON, TokenType.PLUS, TokenType.MINU, TokenType.NOT);
                if (condFirst.contains(iter.preview(1).getType()))
                    stmt.addChild(parseCond());
                stmt.addChild(safelyRead(TokenType.SEMICN)); // add the ';'
                if (iter.preview(1).getType().equals(TokenType.IDENFR))
                    stmt.addChild(parseForStmt());
                stmt.addChild(safelyRead(TokenType.RPARENT)); // add the ')'
                stmt.addChild(parseStmt());
            }
            case IFTK -> {
                stmt.addChild(new Terminator(iter.next())); // add the "if"
                stmt.addChild(new Terminator(iter.next())); // add the '('
                stmt.addChild(parseCond());
                stmt.addChild(safelyRead(TokenType.RPARENT)); // add the ')'
                stmt.addChild(parseStmt());
                if (iter.preview(1).getType().equals(TokenType.ELSETK)) {
                    stmt.addChild(new Terminator(iter.next())); // add the "else"
                    stmt.addChild(parseStmt());
                }
            }
            case LBRACE -> stmt.addChild(parseBlock());
            default -> { // only ';' or [exp] ';' or two type of assign
                if (iter.preview(1).getType().equals(TokenType.SEMICN)) {
                    stmt.addChild(new Terminator(iter.next())); // add the ';'
                } else {
                    int realPos = iter.getPos();
                    Exp tryExp = parseExp();
                    if (iter.preview(1).getType().equals(TokenType.ASSIGN)) {
                        iter.setPos(realPos); // recall !!!
                        stmt.addChild(parseLVal());
                        stmt.addChild(new Terminator(iter.next())); // add the '='
                        if (iter.preview(1).getType().equals(TokenType.GETINTTK)) {
                            stmt.addChild(new Terminator(iter.next())); // add the "getint"
                            stmt.addChild(new Terminator(iter.next())); // add the '('
                            stmt.addChild(safelyRead(TokenType.RPARENT)); // add the ')'
                        } else {
                            stmt.addChild(parseExp());
                        }
                    } else
                        stmt.addChild(tryExp);
                    stmt.addChild(safelyRead(TokenType.SEMICN)); // add the ';'
                }
            }
        }
        return stmt;
    }

    private ForStmt parseForStmt() {
        ForStmt forStmt = new ForStmt();
        forStmt.addChild(parseLVal());
        forStmt.addChild(new Terminator(iter.next())); // add '='
        forStmt.addChild(parseExp());
        return forStmt;
    }


    private Exp parseExp() {
        Exp exp = new Exp();
        exp.addChild(parseAddExp());
        return exp;
    }

    private Cond parseCond() {
        Cond cond = new Cond();
        cond.addChild(parseLOrExp());
        return cond;
    }

    private LVal parseLVal() {
        LVal lVal = new LVal();
        lVal.addChild(new Terminator(iter.next())); // add the ident
        while (iter.preview(1).getType().equals(TokenType.LBRACK)) {
            lVal.addChild(new Terminator(iter.next())); // add the '['
            lVal.addChild(parseExp());
            lVal.addChild(safelyRead(TokenType.RBRACK)); // add the ']'
        }
        return lVal;
    }

    private PrimaryExp parsePrimaryExp() {
        PrimaryExp primaryExp = new PrimaryExp();
        switch (iter.preview(1).getType()) {
            case LPARENT -> {
                primaryExp.addChild(new Terminator(iter.next())); // add the '('
                primaryExp.addChild(parseExp());
                primaryExp.addChild(new Terminator(iter.next())); // add the ')'
            }
            case INTCON -> primaryExp.addChild(parseNumber());
            default -> primaryExp.addChild(parseLVal());
        }
        return primaryExp;
    }

    private Number parseNumber() {
        Number number = new Number();
        number.addChild(new Terminator(iter.next())); // add the IntConst
        return number;
    }

    private UnaryExp parseUnaryExp() {
        UnaryExp unaryExp = new UnaryExp();
        switch (iter.preview(1).getType()) {
            case PLUS, MINU, NOT -> {
                unaryExp.addChild(parseUnaryOp());
                unaryExp.addChild(parseUnaryExp());
            }
            case IDENFR -> {
                if (iter.preview(2).getType().equals(TokenType.LPARENT)) {
                    unaryExp.addChild(new Terminator(iter.next())); // add the ident
                    unaryExp.addChild(new Terminator(iter.next())); // add the '('
                    EnumSet<TokenType> funcRParamsFirst = EnumSet.of(TokenType.LPARENT, TokenType.IDENFR,
                            TokenType.INTCON, TokenType.PLUS, TokenType.MINU, TokenType.NOT);
                    if (funcRParamsFirst.contains(iter.preview(1).getType()))
                        unaryExp.addChild(parseFuncRParams());
                    unaryExp.addChild(safelyRead(TokenType.RPARENT)); // add the ')'
                } else
                    unaryExp.addChild(parsePrimaryExp());
            }
            default -> unaryExp.addChild(parsePrimaryExp()); // must be PrimaryExp
        }
        return unaryExp;
    }

    private UnaryOp parseUnaryOp() {
        UnaryOp unaryOp = new UnaryOp();
        unaryOp.addChild(new Terminator(iter.next())); // add the '+' '-' '!'
        return unaryOp;
    }

    private FuncRParams parseFuncRParams() {
        FuncRParams funcRParams = new FuncRParams();
        funcRParams.addChild(parseExp());
        while (iter.preview(1).getType().equals(TokenType.COMMA)) {
            funcRParams.addChild(new Terminator(iter.next())); // add the ','
            funcRParams.addChild(parseExp());
        }
        return funcRParams;
    }

    private MulExp parseMulExp() {
        ArrayList<Node> raw = new ArrayList<>();
        raw.add(parseUnaryExp());
        while (iter.preview(1).getType().equals(TokenType.MULT)
                || iter.preview(1).getType().equals(TokenType.DIV)
                || iter.preview(1).getType().equals(TokenType.MOD)) {
            raw.add(new Terminator(iter.next())); // add the '*' '/' '%'
            raw.add(parseUnaryExp());
        }
        return (MulExp) toLeftRecursion(raw, NodeType.MULEXP);
    }

    private AddExp parseAddExp() {
        ArrayList<Node> raw = new ArrayList<>();
        raw.add(parseMulExp());
        while (iter.preview(1).getType().equals(TokenType.PLUS)
                || iter.preview(1).getType().equals(TokenType.MINU)) {
            raw.add(new Terminator(iter.next())); // add the '+' '-'
            raw.add(parseMulExp());
        }
        return (AddExp) toLeftRecursion(raw, NodeType.ADDEXP);
    }

    private RelExp parseRelExp() {
        ArrayList<Node> raw = new ArrayList<>();
        raw.add(parseAddExp());
        while (iter.preview(1).getType().equals(TokenType.LSS)
                || iter.preview(1).getType().equals(TokenType.GRE)
                || iter.preview(1).getType().equals(TokenType.LEQ)
                || iter.preview(1).getType().equals(TokenType.GEQ)) {
            raw.add(new Terminator(iter.next())); // add the '<' '>' '<=' '>='
            raw.add(parseAddExp());
        }
        return (RelExp) toLeftRecursion(raw, NodeType.RELEXP);
    }

    private EqExp parseEqExp() {
        ArrayList<Node> raw = new ArrayList<>();
        raw.add(parseRelExp());
        while (iter.preview(1).getType().equals(TokenType.EQL)
                || iter.preview(1).getType().equals(TokenType.NEQ)) {
            raw.add(new Terminator(iter.next())); // add the "==" "!="
            raw.add(parseRelExp());
        }
        return (EqExp) toLeftRecursion(raw, NodeType.EQEXP);
    }

    private LAndExp parseLAndExp() {
        ArrayList<Node> raw = new ArrayList<>();
        raw.add(parseEqExp());
        while (iter.preview(1).getType().equals(TokenType.AND)) {
            raw.add(new Terminator(iter.next())); // add the "&&"
            raw.add(parseEqExp());
        }
        return (LAndExp) toLeftRecursion(raw, NodeType.LANDEXP);
    }

    private LOrExp parseLOrExp() {
        ArrayList<Node> raw = new ArrayList<>();
        raw.add(parseLAndExp());
        while (iter.preview(1).getType().equals(TokenType.OR)) {
            raw.add(new Terminator(iter.next())); // add the "||"
            raw.add(parseLAndExp());
        }
        return (LOrExp) toLeftRecursion(raw, NodeType.LOREXP);
    }

    private ConstExp parseConstExp() {
        ConstExp constExp = new ConstExp();
        constExp.addChild(parseAddExp());
        return constExp;
    }

    private Node toLeftRecursion(ArrayList<Node> raw, NodeType type) {
        Node fixed = switch (type) {
            case MULEXP -> new MulExp();
            case ADDEXP -> new AddExp();
            case RELEXP -> new RelExp();
            case EQEXP -> new EqExp();
            case LANDEXP -> new LAndExp();
            default -> new LOrExp();
        };
        fixed.addChild(raw.get(raw.size() - 1)); // add the rightest Node
        if (raw.size() > 1) {
            fixed.addChild(0, raw.get(raw.size() - 2));
            fixed.addChild(0, toLeftRecursion(new ArrayList<>(raw.subList(0, raw.size() - 2)), type));
        }
        return fixed;
    }

    private Node safelyRead(TokenType expectType) { // for ')', ']' ';'
        Token nextToken = iter.preview(1);
        if (nextToken.getType().equals(expectType)) {
            return new Terminator(iter.next());
        } else {
            String expectedChar = expectType.equals(TokenType.RPARENT) ? ")" :
                    expectType.equals(TokenType.RBRACK) ? "]" : ";";
            ErrorType errorType = expectType.equals(TokenType.RPARENT) ? ErrorType.J :
                    expectType.equals(TokenType.RBRACK) ? ErrorType.K : ErrorType.I;
            int expectedLineNum = iter.preview(0).getLineNum();
            return new Terminator(new Token(expectedChar, expectType, expectedLineNum),
                    new Error(expectedLineNum, errorType));
        }
    }
}