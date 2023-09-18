import analysis.*;
import analysis.node.CompUnit;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) {
        String filePath = "test/test1.txt";
        File file = new File(filePath);
        ArrayList<String> lines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().replace("\r", "");
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("fuck");
        }
        ArrayList<Token> tokens = new Lexer().tokenize(lines);
//        try {
//            PrintStream ps = new PrintStream(new File("output.txt"));
//            PrintStream console = System.out;
//            System.setOut(ps);
//            tokens.forEach(System.out::println);
//            System.setOut(console);
//        } catch (IOException e) {
//            System.out.println("fuck");
//        }
        CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();
        try {
            PrintStream ps = new PrintStream(new File("output.txt"));
            PrintStream console = System.out;
            System.setOut(ps);
            compUnit.traverse();
            System.setOut(console);
        } catch (IOException e) {
            System.out.println("fuck");
        }
    }
}
