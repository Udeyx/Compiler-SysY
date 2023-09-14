import analysis.Lexer;
import analysis.Token;
import analysis.TokenType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) {
        String filePath = "test/test.txt";
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
        tokens.forEach(System.out::println);

    }
}
