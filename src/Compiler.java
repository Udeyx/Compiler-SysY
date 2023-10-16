import analysis.Iter;
import analysis.Lexer;
import analysis.Parser;
import analysis.Token;
import analysis.node.CompUnit;
import util.IOer;

import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        String outputType = "lex";
        String filePath = "testfile.txt";

        // read source file to source string
        String source = IOer.readFile(filePath);

        // use Lexer to tokenize source to token list
        ArrayList<Token> tokens = new Lexer(source).tokenize();

        // parse token list to parsing tree
        CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();

        // check error
        compUnit.check();

        // output
        switch (outputType) {
            case "lex" -> IOer.printLex(tokens);
            case "parse" -> IOer.printParse(compUnit);
            case "error" -> IOer.printError();
        }
    }
}
