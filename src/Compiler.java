import frontend.*;
import frontend.node.CompUnit;
import midend.ir.Module;
import util.IOer;

import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        String outputType = "mips";
        String filePath = "testfile.txt";

        // read source file to source string
        String source = IOer.readFile(filePath);

        // use Lexer to tokenize source to token list
        ArrayList<Token> tokens = new Lexer(source).tokenize();

        // parse token list to parsing tree
        CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();

        // check error
        compUnit.check();
        if (Handler.getInstance().hasError()) {
            IOer.printError();
            return;
        }

        // generate llvm representation
        compUnit.buildIR();

        // generate mips asm
        Module.getInstance().buildMIPS();

        // output
        switch (outputType) {
            case "lex" -> IOer.printLex(tokens);
            case "parse" -> IOer.printParse(compUnit);
            case "error" -> IOer.printError();
            case "ir" -> IOer.printIR();
            case "mips" -> IOer.printMIPS();
        }
    }
}
