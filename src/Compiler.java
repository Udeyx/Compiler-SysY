import analysis.Iter;
import analysis.Lexer;
import analysis.Parser;
import analysis.Token;
import analysis.node.CompUnit;
import util.IOer;

import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        String outputType = "error";
        String filePath = "testfile.txt";
        ArrayList<String> lines = IOer.readLines(filePath);
        ArrayList<Token> tokens = new Lexer().tokenize(lines);
        CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();
        compUnit.check();
        switch (outputType) {
            case "lex" -> IOer.printLex(tokens);
            case "parse" -> IOer.printParse(compUnit);
            case "error" -> IOer.printError();
        }
    }
}
