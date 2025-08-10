import Parser.Parser;
import Parser.AstExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SqlRunner {
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || "--help".equalsIgnoreCase(args[0]) || "-h".equalsIgnoreCase(args[0])) {
            System.out.println("Usage: java -cp out SqlRunner <sql-file>\n");
            System.out.println("Runs semicolon-separated SQL statements from the file in a single session.\n");
            System.out.println("Tips:");
            System.out.println("  - Lines starting with -- are treated as comments");
            System.out.println("  - See docs/queries.md (or docs/queries.html) for examples");
            System.out.println("  - For interactive mode, run: java -cp out Main");
            return;
        }
        Path file = Path.of(args[0]);
        if (!Files.exists(file)) {
            System.out.println("SQL file not found: " + file.toAbsolutePath());
            System.exit(1);
        }
        List<String> lines = Files.readAllLines(file);
        StringBuilder buf = new StringBuilder();
        List<String> statements = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            // remove inline comments starting with --
            int cIdx = trimmed.indexOf("--");
            if (cIdx >= 0) {
                trimmed = trimmed.substring(0, cIdx).trim();
            }
            if (trimmed.isEmpty()) continue;
            buf.append(trimmed).append(' ');
            int idx;
            while ((idx = buf.indexOf(";")) >= 0) {
                String stmt = buf.substring(0, idx).trim();
                if (!stmt.isEmpty()) statements.add(stmt);
                buf.delete(0, idx + 1);
            }
        }
        if (buf.toString().trim().length() > 0) statements.add(buf.toString().trim());

        AstExecutor exec = new AstExecutor();
        for (String sql : statements) {
            System.out.println(">>> " + sql);
            Object ast = Parser.parse(sql);
            exec.execute(ast);
        }
        System.out.println("Done.");
    }
}
