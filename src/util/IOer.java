package util;

import backend.Target;
import frontend.Handler;
import frontend.Token;
import frontend.node.CompUnit;
import midend.ir.Module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class IOer {
    public static String readFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().replace("\r", "");
                sb.append(line);
                sb.append("\n");
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("exception in readFile");
        }
        return sb.toString();
    }

    public static void printLex(ArrayList<Token> tokens) {
        try {
            PrintStream prePs = System.out;
            PrintStream ps = new PrintStream("output.txt");
            System.setOut(ps);
            tokens.forEach(System.out::println);
            ps.close();
            System.setOut(prePs);
        } catch (FileNotFoundException e) {
            System.out.println("exception in printLex");
        }
    }

    public static void printParse(CompUnit treeRoot) {
        try {
            PrintStream prePs = System.out;
            PrintStream ps = new PrintStream("output.txt");
            System.setOut(ps);
            treeRoot.traverse();
            ps.close();
            System.setOut(prePs);
        } catch (FileNotFoundException e) {
            System.out.println("exception in printParse");
        }
    }

    public static void printError() {
        try {
            PrintStream prePs = System.out;
            PrintStream ps = new PrintStream("error.txt");
            System.setOut(ps);
            System.out.println(Handler.getInstance());
            ps.close();
            System.setOut(prePs);
        } catch (FileNotFoundException e) {
            System.out.println("exception in printError");
        }
    }

    public static void printIR() {
        try {
            PrintStream prePs = System.out;
            PrintStream ps = new PrintStream("llvm_ir.txt");
            System.setOut(ps);
            System.out.println(Module.getInstance());
            ps.close();
            System.setOut(prePs);
        } catch (FileNotFoundException e) {
            System.out.println("exception in printIR");
        }
    }

    public static void printPhiIR() {
        try {
            PrintStream prePs = System.out;
            PrintStream ps = new PrintStream("llvm_ir_phi.txt");
            System.setOut(ps);
            System.out.println(Module.getInstance());
            ps.close();
            System.setOut(prePs);
        } catch (FileNotFoundException e) {
            System.out.println("exception in printIR");
        }
    }

    public static void printMoveIR() {
        try {
            PrintStream prePs = System.out;
            PrintStream ps = new PrintStream("llvm_ir_move.txt");
            System.setOut(ps);
            System.out.println(Module.getInstance());
            ps.close();
            System.setOut(prePs);
        } catch (FileNotFoundException e) {
            System.out.println("exception in printIR");
        }
    }

    public static void printMIPS() {
        try {
            PrintStream prePs = System.out;
            PrintStream ps = new PrintStream("mips.txt");
            System.setOut(ps);
            System.out.println(Target.getInstance());
            ps.close();
            System.setOut(prePs);
        } catch (FileNotFoundException e) {
            System.out.println("exception in printIR");
        }
    }
}
