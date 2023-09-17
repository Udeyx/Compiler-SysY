package analysis;

import analysis.node.*;

public class Parser {
    private final Iter iter;

    public Parser(Iter iter) {
        this.iter = iter;
    }

    public CompUnit parseCompUnit() {
        CompUnit compUnit = new CompUnit();
        while (iter.hasNext()) {
            Token curToken = iter.next();
            switch (curToken.getType()) {
                case CONSTTK -> compUnit.addChild(parseDecl());
                case VOIDTK -> compUnit.addChild(parseFuncDef());
                default -> {
                    Token nextToken = iter.preview(1);
                    assert nextToken.getType().equals(TokenType.INTTK);
                    Token nextNextToken = iter.preview(2);
                    if (nextNextToken.getType().equals(TokenType.LPARENT))
                        compUnit.addChild(parseFuncDef());
                    else compUnit.addChild(parseDecl());
                }
            }
        }
        return compUnit;
    }

    private Decl parseDecl() {
        Decl decl = new Decl();
        if (iter.peek().getType().equals(TokenType.CONSTTK))
            decl.addChild(parseConstDecl());
        else
            decl.addChild(parseVarDecl());
        return decl;
    }

    private FuncDef parseFuncDef() {
        FuncDef funcDef = new FuncDef();
        funcDef.addChild(parseFuncType());
        funcDef.addChild(new Terminator(iter.next())); // add ident
        funcDef.addChild(new Terminator(iter.next())); // add '('
        if (!iter.preview(1).getType().equals(TokenType.RPARENT))
            funcDef.addChild(parseFuncFParams());
        funcDef.addChild(new Terminator(iter.next())); // add ')'
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

    private FuncFParams parseFuncFParams() {
        FuncFParams funcFParams = new FuncFParams();
        funcFParams.addChild(parseFuncFParam());
        while (iter.next().getType().equals(TokenType.COMMA)) {
            funcFParams.addChild(new Terminator(iter.peek())); // add the ','
            funcFParams.addChild(parseFuncFParam());
        }
        iter.back(1); // don't read the token after the last param
        return funcFParams;
    }

    private FuncFParam parseFuncFParam() {
        FuncFParam funcFParam = new FuncFParam();
        funcFParam.addChild(new Terminator(iter.next())); // add the "int"
        funcFParam.addChild(new Terminator(iter.next())); // add the ident
        if (iter.preview(1).getType().equals(TokenType.LBRACK)) {
            funcFParam.addChild(new Terminator(iter.next())); // add the '['
            funcFParam.addChild(new Terminator(iter.next())); // add the ']'
            while (iter.preview(1).getType().equals(TokenType.LBRACK)) {
                funcFParam.addChild(new Terminator(iter.next())); // add the '['
                funcFParam.addChild(parseConstExp());
                funcFParam.addChild(new Terminator(iter.next())); // add the ']'
            }
        }
        return funcFParam;
    }

    private FuncType parseFuncType() {
        FuncType funcType = new FuncType();
        funcType.addChild(new Terminator(iter.next()));
        return funcType;
    }

    private Block parseBlock() {
        Block block = new Block();
        block.addChild(new Terminator(iter.next())); // add the '{'
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
                stmt.addChild(new Terminator(iter.next())); // read the "printf"
                stmt.addChild(new Terminator(iter.next())); // read the '('
            }

        }
        return stmt;
    }

    private ConstDecl parseConstDecl() {
        ConstDecl constDecl = new ConstDecl();
        constDecl.addChild(new Terminator(iter.peek())); // add const to children
        constDecl.addChild(new Terminator(iter.next())); // read the "int"
        while (iter.next().getType().equals(TokenType.IDENFR)) { // read the ident
            constDecl.addChild(parseConstDef());
            constDecl.addChild(new Terminator(iter.next())); // read the ',' or ';'
            if (iter.peek().getType().equals(TokenType.SEMICN))
                break;
        }
        return constDecl;
    }

    private VarDecl parseVarDecl() {
        VarDecl varDecl = new VarDecl();
        varDecl.addChild(new Terminator(iter.next())); // add "int"
        varDecl.addChild(parseVarDef());
        while (iter.next().getType().equals(TokenType.COMMA)) {
            varDecl.addChild(new Terminator(iter.peek())); // add ','
            varDecl.addChild(parseVarDef());
        }
        varDecl.addChild(new Terminator(iter.peek())); // add ';'
        return varDecl;
    }

    private VarDef parseVarDef() {
        VarDef varDef = new VarDef();
        varDef.addChild(new Terminator(iter.next())); // add ident
        if (iter.preview(1).getType().equals(TokenType.LBRACK)) {
            while (iter.next().getType().equals(TokenType.LBRACK)) {
                varDef.addChild(new Terminator(iter.peek())); // add '['
                varDef.addChild(parseConstExp());
                varDef.addChild(new Terminator(iter.next())); // add ']'
            }
            iter.back(1); // don't read the next token after ']'
        }
        if (iter.preview(1).getType().equals(TokenType.ASSIGN)) {
            varDef.addChild(new Terminator(iter.next())); // read the '='
            varDef.addChild(parseInitVal());
        }
        return varDef;
    }

    private InitVal parseInitVal() {
        InitVal initVal = new InitVal();
        if (iter.preview(1).getType().equals(TokenType.LBRACE)) {
            initVal.addChild(new Terminator(iter.next())); // add '{'
            if (iter.preview(1).getType().equals(TokenType.RBRACE)) {
                initVal.addChild(new Terminator(iter.next())); // add '}'
            } else {
                initVal.addChild(parseInitVal());
                while (iter.next().getType().equals(TokenType.COMMA)) {
                    initVal.addChild(new Terminator(iter.peek())); // add ','
                    initVal.addChild(parseInitVal());
                }
                initVal.addChild(new Terminator(iter.peek())); // add '}'
            }
        }
        return initVal;
    }

    private Exp parseExp() {
        Exp exp = new Exp();
        return exp;
    }

    private ConstDef parseConstDef() {
        ConstDef constDef = new ConstDef();
        constDef.addChild(new Terminator(iter.peek())); // add the ident
        while (iter.next().getType().equals(TokenType.LBRACK)) {
            constDef.addChild(parseConstExp());
            constDef.addChild(new Terminator(iter.next())); // read the ']'
        } // at the last time, '=' has been read
        constDef.addChild(new Terminator(iter.peek())); // add the '='
        constDef.addChild(parseConstInitVal());
        return constDef;
    }

    private ConstExp parseConstExp() {
        ConstExp constExp = new ConstExp();
        return constExp;
    }

    private ConstInitVal parseConstInitVal() {
        ConstInitVal constInitVal = new ConstInitVal();
        if (iter.preview(1).getType().equals(TokenType.LBRACE)) {
            constInitVal.addChild(new Terminator(iter.next())); // add '{'
            if (iter.preview(1).getType().equals(TokenType.RBRACE))
                constInitVal.addChild(new Terminator(iter.next())); // add '}'
            else {
                constInitVal.addChild(parseConstInitVal());
                while (iter.next().getType().equals(TokenType.COMMA)) {
                    constInitVal.addChild(new Terminator(iter.peek())); // add ','
                    constInitVal.addChild(parseConstInitVal());
                }
                constInitVal.addChild(new Terminator(iter.peek())); // add ';'
            }
        } else
            constInitVal.addChild(parseConstExp());
        return constInitVal;
    }
}
