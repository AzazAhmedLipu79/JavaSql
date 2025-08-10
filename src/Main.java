
import Parser.Parser;
import Parser.AstExecutor;
import Util.Help;
import Session.Session;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AstExecutor executor = new AstExecutor();

    System.out.println("Java CSV DBMS — type 'help' for help, 'exit' to quit.");
        StringBuilder buf = new StringBuilder();

        while (true) {
            String curDb = Session.getInstance().getCurrentDatabase();
            String basePrompt = (curDb == null || curDb.isBlank()) ? "db" : curDb;
            System.out.print(buf.length() == 0 ? (basePrompt + "> ") : "...> ");
            String line;
            try {
                line = scanner.nextLine();
            } catch (java.util.NoSuchElementException eof) {
                break; // EOF/Ctrl+Z
            }
            String trimmed = line.trim();

            if (trimmed.equalsIgnoreCase("exit")) break;
            if (trimmed.equalsIgnoreCase("help") || trimmed.equalsIgnoreCase("--help") || trimmed.equalsIgnoreCase("-h")) {
                Help.print();
                continue;
            }
            if (trimmed.isEmpty()) continue;

            // If no buffer in progress and no ';', treat as single statement
            if (buf.length() == 0 && trimmed.indexOf(';') < 0) {
                Object ast = Parser.parse(trimmed);
                executor.execute(ast);
                continue;
            }

            // otherwise accumulate until ';'
            buf.append(trimmed).append(' ');
            int idx;
            while ((idx = buf.indexOf(";")) >= 0) {
                String stmt = buf.substring(0, idx).trim();
                buf.delete(0, idx + 1);
                if (stmt.isEmpty()) continue;
                Object ast = Parser.parse(stmt);
                executor.execute(ast);
            }
        }

        scanner.close();
        System.out.println("Goodbye!");
    }
}
