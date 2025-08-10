package Parser;
 
import java.util.List; 

public class Parser {

    /**
     * This class is a placeholder for the SQL parser.
     * It currently does not implement any parsing logic.
     * The Lexer class is used to tokenize SQL statements.
     * 
     * Example usage:
     * Lexer lexer = new Lexer("SELECT id FROM users WHERE active = true");
     * List<Token> tokens = lexer.tokenize();
     * 
     * The output will be a list of tokens representing the SQL statement:
     * [KEYWORD: SELECT]
     * [IDENTIFIER: id]
     * [KEYWORD: FROM]
     * [IDENTIFIER: users]
     * [KEYWORD: WHERE]
     * [IDENTIFIER: active]
     * [SYMBOL: =]
     * [BOOLEAN_LITERAL: true]
     * * This class can be extended in the future to implement actual parsing logic.
     */

    public Parser(String sql) {
        // legacy path: parse only, no execution
        parse(sql);
    }

    public static Object parse(String sql) {
        Lexer lexer = new Lexer(sql);
        List<Token> tokens = lexer.tokenize();

        String firstToken = tokens.isEmpty() ? "" : tokens.get(0).value.toUpperCase();

        switch (firstToken) {
            // DDL (Data Definition Language)
            case "CREATE":
                if (tokens.size() > 1 && tokens.get(1).value.equalsIgnoreCase("DATABASE")) {
                    if (tokens.size() < 3) {
                        System.out.println("Syntax error: CREATE DATABASE <name>");
                        return null;
                    }
                    String dbName = tokens.get(2).value;
                    return new CreateDatabaseNode(dbName);
                } else if (tokens.size() > 1 && tokens.get(1).value.equalsIgnoreCase("TABLE")) {
                    // CREATE TABLE name (c1,c2,...)
                    if (tokens.size() < 5) { System.out.println("Syntax error: CREATE TABLE <name> (col1,col2,...)"); return null; }
                    String table = tokens.get(2).value;
                    int i = 3;
                    if (!"(".equals(tokens.get(i).value)) { System.out.println("Syntax error: expected '('"); return null; }
                    i++;
                    java.util.List<String> cols = new java.util.ArrayList<>();
                    while (i < tokens.size() && !")".equals(tokens.get(i).value)) {
                        if (!",".equals(tokens.get(i).value)) cols.add(tokens.get(i).value);
                        i++;
                    }
                    if (i >= tokens.size() || !")".equals(tokens.get(i).value)) { System.out.println("Syntax error: missing ')'"); return null; }
                    return new CreateTableNode(table, cols);
                } else {
                    System.out.println("Unknown CREATE statement");
                    return null;
                }
            case "USE":
                if (tokens.size() < 2) {
                    System.out.println("Syntax error: USE <database>");
                    return null;
                }
                return new UseDatabaseNode(tokens.get(1).value);
            case "ALTER":
                System.out.println("Handle ALTER - DDL");
                return null;
            case "DROP":
                if (tokens.size() > 1 && tokens.get(1).value.equalsIgnoreCase("DATABASE")) {
                    if (tokens.size() < 3) { System.out.println("Syntax error: DROP DATABASE <name>"); return null; }
                    return new DropDatabaseNode(tokens.get(2).value);
                }
                System.out.println("Handle DROP - DDL");
                return null;

            // DML (Data Manipulation Language)
            case "INSERT": {
                // INSERT INTO table [(c1,c2,...)] VALUES (v1,v2,...)
                if (tokens.size() < 4 || !tokens.get(1).value.equalsIgnoreCase("INTO")) { System.out.println("Syntax error: INSERT INTO <table> [(cols)] VALUES (...)"); return null; }
                String table = tokens.get(2).value;
                int i = 3;
                List<String> cols = null;
                if (i < tokens.size() && tokens.get(i).value.equals("(")) {
                    i++; cols = new java.util.ArrayList<>();
                    while (i < tokens.size() && !tokens.get(i).value.equals(")")) {
                        // collect column names, skip commas
                        if (!",".equals(tokens.get(i).value)) {
                            if (tokens.get(i).type == TokenType.IDENTIFIER || tokens.get(i).type == TokenType.KEYWORD) {
                                cols.add(tokens.get(i).value);
                            }
                        }
                        i++;
                    }
                    if (i < tokens.size() && ")".equals(tokens.get(i).value)) {
                        i++; // skip ')'
                    }
                }
                    if (i >= tokens.size() || !tokens.get(i).value.equalsIgnoreCase("VALUES")) { System.out.println("Syntax error: expected VALUES"); return null; }
                i++;
                    if (i >= tokens.size() || !tokens.get(i).value.equals("(")) { System.out.println("Syntax error: VALUES ( ... )"); return null; }
                i++;
                    List<String> vals = new java.util.ArrayList<>();
                    while (i < tokens.size() && !tokens.get(i).value.equals(")")) {
                        String v = tokens.get(i).value;
                        if (!",".equals(v)) {
                            // strip quotes only for string tokens; numbers/idents are already raw
                            vals.add(v.replaceAll("^'|'$", ""));
                        }
                        i++;
                    }
                    if (i < tokens.size() && ")".equals(tokens.get(i).value)) i++; // consume ')'
                return new InsertNode(table, cols, vals);
            }
            case "UPDATE":
                // UPDATE table SET c=v [, c2=v2] [WHERE c = v]
                if (tokens.size() < 4 || !tokens.get(2).value.equalsIgnoreCase("SET")) { System.out.println("Syntax error: UPDATE <table> SET col=val [...]"); return null; }
                {
                    String table = tokens.get(1).value;
                    java.util.Map<String,String> updates = new java.util.LinkedHashMap<>();
                    int i = 3;
                    while (i + 2 < tokens.size()) {
                        String col = tokens.get(i).value; i++;
                        if (!tokens.get(i).value.equals("=")) break; i++;
                        String val = tokens.get(i).value.replaceAll("^'|'$", ""); i++;
                        updates.put(col, val);
                        if (i < tokens.size() && tokens.get(i).value.equals(",")) { i++; continue; } else break;
                    }
                    ConditionNode where = null;
                    if (i < tokens.size() && tokens.get(i).value.equalsIgnoreCase("WHERE")) {
                        i++;
                        if (i + 2 < tokens.size() && tokens.get(i+1).value.equals("=")) {
                            where = new ConditionNode(tokens.get(i).value, "=", tokens.get(i+2).value.replaceAll("^'|'$", ""));
                        }
                    }
                    return new UpdateNode(table, where, updates);
                }
            case "DELETE":
                // DELETE FROM table [WHERE c = v]
                if (tokens.size() < 3 || !tokens.get(1).value.equalsIgnoreCase("FROM")) { System.out.println("Syntax error: DELETE FROM <table> [WHERE ...]"); return null; }
                {
                    String table = tokens.get(2).value;
                    int i = 3;
                    ConditionNode where = null;
                    if (i < tokens.size() && tokens.get(i).value.equalsIgnoreCase("WHERE")) {
                        i++;
                        if (i + 2 < tokens.size() && tokens.get(i+1).value.equals("=")) {
                            where = new ConditionNode(tokens.get(i).value, "=", tokens.get(i+2).value.replaceAll("^'|'$", ""));
                        }
                    }
                    return new DeleteNode(table, where);
                }

            // DQL (Data Query Language)
            case "SELECT": {
                if (tokens.size() > 1 && tokens.get(1).value.equalsIgnoreCase("DATABASE")) {
                    return new SelectDatabaseNode();
                }
                // SELECT cols FROM table [WHERE c = v] [ORDER BY c1,c2,...]
                int i = 1;
                java.util.List<String> cols = new java.util.ArrayList<>();
                if (tokens.get(i).value.equals("*")) { cols.add("*"); i++; }
                else {
                    while (i < tokens.size() && !tokens.get(i).value.equalsIgnoreCase("FROM")) {
                        if (!tokens.get(i).value.equals(",")) cols.add(tokens.get(i).value);
                        i++;
                    }
                }
                if (i >= tokens.size() || !tokens.get(i).value.equalsIgnoreCase("FROM")) { System.out.println("Syntax error: SELECT ... FROM <table>"); return null; }
                i++;
                if (i >= tokens.size()) { System.out.println("Syntax error: missing table name"); return null; }
                String table = tokens.get(i++).value;

                // Optional JOIN parsing: JOIN table2 ON a.col = b.col
                if (i < tokens.size() && tokens.get(i).value.equalsIgnoreCase("JOIN")) {
                    i++;
                    if (i >= tokens.size()) { System.out.println("Syntax error: JOIN <table>"); return null; }
                    String rightTable = tokens.get(i++).value;
                    if (!(i < tokens.size() && tokens.get(i).value.equalsIgnoreCase("ON"))) { System.out.println("Syntax error: JOIN ... ON ..."); return null; }
                    i++;
                    if (i + 2 >= tokens.size() || !"=".equals(tokens.get(i+1).value)) { System.out.println("Syntax error: ON a = b"); return null; }
                    String leftCol = tokens.get(i).value; String rightCol = tokens.get(i+2).value; i += 3;
                    return new SelectJoinNode(table, rightTable, leftCol, rightCol, cols.isEmpty()? java.util.List.of("*") : cols);
                }
                ConditionNode where = null; java.util.List<String> orderBy = null;
                if (i < tokens.size() && tokens.get(i).value.equalsIgnoreCase("WHERE")) {
                    i++;
                    if (i + 2 < tokens.size()) {
                        String col = tokens.get(i).value; String op = tokens.get(i+1).value; String val = tokens.get(i+2).value.replaceAll("^'|'$", "");
                        // accept =, !=, <>, >, <, >=, <=
                        if (op.equals("=") || op.equals("!=") || op.equals("<>") || op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=")) {
                            where = new ConditionNode(col, op, val);
                            i += 3;
                        }
                    }
                }
                if (i < tokens.size() && tokens.get(i).value.equalsIgnoreCase("ORDER") && i+1 < tokens.size() && tokens.get(i+1).value.equalsIgnoreCase("BY")) {
                    i += 2; orderBy = new java.util.ArrayList<>();
                    while (i < tokens.size()) {
                        String v = tokens.get(i).value;
                        if (v.equalsIgnoreCase("LIMIT") || v.equalsIgnoreCase("OFFSET")) break;
                        if (!v.equals(",")) orderBy.add(v);
                        i++;
                    }
                }
                Integer limit = null; Integer offset = null;
                // In our lexer, tokens include KEYWORD LIMIT/OFFSET if provided, so scan remaining tokens for LIMIT/OFFSET
                // Simple pattern: ... [ORDER BY ...] [LIMIT n] [OFFSET m]
                for (int j = i; j < tokens.size(); j++) {
                    String v = tokens.get(j).value;
                    if (v.equalsIgnoreCase("LIMIT") && j+1 < tokens.size()) {
                        try { limit = Integer.parseInt(tokens.get(j+1).value); } catch (Exception ignore) {}
                    }
                    if (v.equalsIgnoreCase("OFFSET") && j+1 < tokens.size()) {
                        try { offset = Integer.parseInt(tokens.get(j+1).value); } catch (Exception ignore) {}
                    }
                }
                return new SelectNode(table, cols.isEmpty()? java.util.List.of("*") : cols, where, orderBy, limit, offset);
            }

            // SHOW
            case "SHOW": {
                if (tokens.size() >= 2 && tokens.get(1).value.equalsIgnoreCase("DATABASES")) {
                    return new ShowDatabasesNode();
                }
                if (tokens.size() >= 2 && tokens.get(1).value.equalsIgnoreCase("TABLES")) {
                    // Optional: SHOW TABLES FROM <db>
                    String dbName = null;
                    if (tokens.size() >= 4 && tokens.get(2).value.equalsIgnoreCase("FROM")) {
                        dbName = tokens.get(3).value;
                    }
                    return new ShowTablesNode(dbName);
                }
                System.out.println("Syntax error: SHOW DATABASES | SHOW TABLES [FROM <db>]");
                return null;
            }

            // DCL (Data Control Language)
            case "GRANT":
                System.out.println("Handle GRANT - DCL");
                return null;
            case "REVOKE":
                System.out.println("Handle REVOKE - DCL");
                return null;

            // TCL (Transaction Control Language)
            case "BEGIN":
                System.out.println("Handle BEGIN - TCL");
                return null;
            case "COMMIT":
                System.out.println("Handle COMMIT - TCL");
                return null;
            case "ROLLBACK":
                System.out.println("Handle ROLLBACK - TCL");
                return null;

            default:
                System.out.println("Unknown or unsupported SQL statement: " + firstToken);
                return null;
        }
    }
}