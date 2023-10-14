package util;

import analysis.Handler;
import analysis.Token;
import analysis.node.CompUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class IOer {
    public static ArrayList<String> readLines(String filePath) {
        File file = new File(filePath);
        ArrayList<String> lines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().replace("\r", "");
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("exception in readLines");
        }
        return lines;
    }

    public static void printLex(ArrayList<Token> tokens) {
        try {
            PrintStream ps = new PrintStream("output.txt");
            System.setOut(ps);
            tokens.forEach(System.out::println);
            ps.close();
        } catch (FileNotFoundException e) {
            System.out.println("exception in printLex");
        }
    }

    public static void printParse(CompUnit treeRoot) {
        try {
            PrintStream ps = new PrintStream("output.txt");
            System.setOut(ps);
            treeRoot.traverse();
            ps.close();
        } catch (FileNotFoundException e) {
            System.out.println("exception in printParse");
        }
    }

    public static void printError() {
        try {
            PrintStream ps = new PrintStream("error.txt");
            System.setOut(ps);
            System.out.println(Handler.getInstance());
            ps.close();
        } catch (FileNotFoundException e) {
            System.out.println("exception in printError");
        }

    }
}
