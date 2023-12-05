import frontend.*;
import frontend.node.CompUnit;
import midend.ir.Module;
import midend.optimizer.Optimizer;
import util.IOer;
import util.TaskType;

import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        TaskType taskType = TaskType.OPT;
        boolean dev = false;
        String filePath = "testfile.txt";

        // read source file to source string
        String source = IOer.readFile(filePath);

        switch (taskType) {
            case LEX -> {
                ArrayList<Token> tokens = new Lexer(source).tokenize();

                IOer.printLex(tokens);
            }
            case PARSE -> {
                ArrayList<Token> tokens = new Lexer(source).tokenize();
                CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();

                IOer.printParse(compUnit);
            }
            case CHECK -> {
                ArrayList<Token> tokens = new Lexer(source).tokenize();
                CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();
                compUnit.check();

                IOer.printError();
            }
            case IR -> {
                ArrayList<Token> tokens = new Lexer(source).tokenize();
                CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();
                compUnit.check();
                if (Handler.getInstance().hasError()) {
                    IOer.printError();
                    return;
                }
                compUnit.buildIR();

                IOer.printIR();
            }
            case MIPS -> {
                ArrayList<Token> tokens = new Lexer(source).tokenize();
                CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();
                compUnit.check();
                if (Handler.getInstance().hasError()) {
                    IOer.printError();
                    return;
                }
                compUnit.buildIR();
                Module.getInstance().buildMIPS();

                IOer.printMIPS();
            }
            case OPT -> {
                ArrayList<Token> tokens = new Lexer(source).tokenize();
                CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();
                compUnit.check();
                if (Handler.getInstance().hasError()) {
                    IOer.printError();
                    return;
                }
                compUnit.buildIR();
                if (dev)
                    IOer.printIR();
                Optimizer.getInstance().optimize();
                Module.getInstance().buildMIPS();
                IOer.printMIPS();
            }
        }
    }
}
