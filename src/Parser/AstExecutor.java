package Parser;

import Executor.Executor;
import Session.Session;
import FileManager.TableEngine;
import Util.Logger;

import java.nio.file.Path;
import java.util.*;

public class AstExecutor {
    private final Executor exec = Executor.getInstance();
    private final Session session = Session.getInstance();

    private Path tableCsv(String table) {
        String db = session.getCurrentDatabase();
        if (db == null) throw new RuntimeException("No database selected");
    return java.nio.file.Paths.get(System.getProperty("user.dir"), "storage", "databases", db, table);
    }

    private static java.util.function.Predicate<Map<String,String>> predicate(ConditionNode cond) {
        if (cond == null) {
            return new java.util.function.Predicate<Map<String,String>>() { public boolean test(Map<String,String> m){ return true; } };
        }
        String op = cond.op;
        return new java.util.function.Predicate<Map<String,String>>() {
            public boolean test(Map<String,String> m){
                String left = m.get(cond.column);
                String right = cond.value;
                if (left == null) left = "";
                // try numeric compare first
                int cmp;
                try {
                    double dl = Double.parseDouble(left);
                    double dr = Double.parseDouble(right);
                    cmp = Double.compare(dl, dr);
                } catch (Exception e) {
                    cmp = left.compareTo(right);
                }
                switch (op) {
                    case "=": return Objects.equals(left, right);
                    case "!=": case "<>": return !Objects.equals(left, right);
                    case ">": return cmp > 0;
                    case "<": return cmp < 0;
                    case ">=": return cmp >= 0;
                    case "<=": return cmp <= 0;
                    default: return false;
                }
            }
        };
    }

    public void execute(Object node) {
        if (node == null) return;
        // helper to render rows in a simple table
        java.util.function.Consumer<java.util.List<java.util.Map<String,String>>> renderTable = rows -> {
            if (rows == null || rows.isEmpty()) return;
            // gather columns from first row
            java.util.List<String> cols = new java.util.ArrayList<>(rows.get(0).keySet());
            // compute widths
            int[] w = new int[cols.size()];
            for (int i = 0; i < cols.size(); i++) w[i] = Math.max(3, cols.get(i).length());
            for (var r : rows) for (int i = 0; i < cols.size(); i++) w[i] = Math.max(w[i], String.valueOf(r.getOrDefault(cols.get(i), "")).length());
            // header
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cols.size(); i++) { sb.append(String.format("%-" + w[i] + "s", cols.get(i))); if (i < cols.size()-1) sb.append("  "); }
            System.out.println(sb);
            sb.setLength(0);
            for (int i = 0; i < cols.size(); i++) { sb.append("-".repeat(w[i])); if (i < cols.size()-1) sb.append("  "); }
            System.out.println(sb);
            // rows
            for (var r : rows) {
                sb.setLength(0);
                for (int i = 0; i < cols.size(); i++) { sb.append(String.format("%-" + w[i] + "s", String.valueOf(r.getOrDefault(cols.get(i), "")))); if (i < cols.size()-1) sb.append("  "); }
                System.out.println(sb);
            }
        };
        if (node instanceof CreateDatabaseNode) {
            CreateDatabaseNode n = (CreateDatabaseNode) node;
            try { exec.executeCreateDatabase(n.dbName); } catch (Exception e) { System.out.println(e.getMessage()); }
        } else if (node instanceof UseDatabaseNode) {
            UseDatabaseNode n = (UseDatabaseNode) node;
            try { exec.executeUseDatabase(n.dbName); } catch (Exception e) { System.out.println(e.getMessage()); }
        } else if (node instanceof DropDatabaseNode) {
            DropDatabaseNode n = (DropDatabaseNode) node;
            try { exec.executeDropDatabase(n.dbName); } catch (Exception e) { System.out.println(e.getMessage()); }
        } else if (node instanceof SelectDatabaseNode) {
            exec.executeShowCurrentDatabase();
        } else if (node instanceof CreateTableNode) {
            CreateTableNode n = (CreateTableNode) node;
            Path dir = tableCsv(n.tableName);
            try {
                java.nio.file.Files.createDirectories(dir);
                java.nio.file.Files.createDirectories(dir.resolve("meta"));
                String header = String.join(",", n.columns);
                Path first = dir.resolve("page_0.csv");
                if (!java.nio.file.Files.exists(first)) {
                    java.nio.file.Files.writeString(first, header + System.lineSeparator());
                }
                Logger.info("Table created: " + dir);
            } catch (Exception e) { System.out.println(e.getMessage()); }
        } else if (node instanceof InsertNode) {
            InsertNode n = (InsertNode) node;
            try {
                Path dir = tableCsv(n.tableName);
                // Determine header for mapping
                List<String> cols;
                try {
                    TableEngine.Table t = TableEngine.loadDir(dir);
                    cols = (n.columns == null || n.columns.isEmpty()) ? t.columns : n.columns;
                } catch (Exception ex) {
                    cols = (n.columns == null || n.columns.isEmpty()) ? n.values.stream().map(v -> v).toList() : n.columns;
                }
                Map<String,String> row = new LinkedHashMap<>();
                for (int i = 0; i < cols.size(); i++) {
                    String v = (i < n.values.size()) ? n.values.get(i) : "";
                    row.put(cols.get(i), v);
                }
                // Read page size from config
                Path cfg = java.nio.file.Paths.get(System.getProperty("user.dir"), "storage", "user_management", "config.json");
                int pageKB = 64;
                try {
                    String js = java.nio.file.Files.readString(cfg);
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"page_size_kb\"\s*:\s*(\\d+)").matcher(js);
                    if (m.find()) pageKB = Integer.parseInt(m.group(1));
                } catch (Exception ignore) {}
                TableEngine.insertDir(dir, row, pageKB);
            } catch (Exception e) { System.out.println(e.getMessage()); }
        } else if (node instanceof UpdateNode) {
            UpdateNode n = (UpdateNode) node;
            try {
                Path dir = tableCsv(n.tableName);
                TableEngine.updateDir(dir, predicate(n.where), n.updates);
            } catch (Exception e) { System.out.println(e.getMessage()); }
        } else if (node instanceof DeleteNode) {
            DeleteNode n = (DeleteNode) node;
            try {
                Path dir = tableCsv(n.tableName);
                TableEngine.deleteDir(dir, predicate(n.where));
            } catch (Exception e) { System.out.println(e.getMessage()); }
        } else if (node instanceof SelectNode) {
            SelectNode n = (SelectNode) node;
            try {
                Path dir = tableCsv(n.tableName);
                List<Map<String,String>> rows = TableEngine.selectDir(dir, predicate(n.where), n.orderBy);
                // project columns if not '*'
                List<String> cols = n.columns;
                boolean star = cols.size() == 1 && "*".equals(cols.get(0));
                List<Map<String,String>> toPrint = new ArrayList<>();
                for (Map<String,String> r : rows) {
                    if (star) { toPrint.add(r); }
                    else {
                        Map<String,String> proj = new LinkedHashMap<>();
                        for (String c : cols) {
                            String val = r.get(c);
                            proj.put(c, val == null ? "" : val);
                        }
                        toPrint.add(proj);
                    }
                }
                // apply offset/limit
                int start = (n.offset != null && n.offset > 0) ? Math.min(n.offset, toPrint.size()) : 0;
                int end = toPrint.size();
                if (n.limit != null && n.limit >= 0) end = Math.min(start + n.limit, toPrint.size());
                List<Map<String,String>> window = (start == 0 && end == toPrint.size()) ? toPrint : toPrint.subList(start, end);
                if (!window.isEmpty()) renderTable.accept(window);
                else System.out.println("(0 rows)");
                Logger.info("Selected and printed " + rows.size() + " rows from " + dir);
            } catch (Exception e) { System.out.println(e.getMessage()); }
        } else if (node instanceof SelectJoinNode) {
            SelectJoinNode n = (SelectJoinNode) node;
            try {
                TableEngine.Table left = TableEngine.loadDir(tableCsv(n.leftTable));
                TableEngine.Table right = TableEngine.loadDir(tableCsv(n.rightTable));
                List<Map<String,String>> rows = TableEngine.join(left, right, n.leftCol, n.rightCol);
                if (!rows.isEmpty()) renderTable.accept(rows);
                else System.out.println("(0 rows)");
            } catch (Exception e) { System.out.println(e.getMessage()); }
        } else if (node instanceof ShowDatabasesNode) {
            exec.executeShowDatabases();
        } else if (node instanceof ShowTablesNode) {
            ShowTablesNode n = (ShowTablesNode) node;
            exec.executeShowTables(n.dbName);
        }
        // Extend with other node types as they’re parsed
    }
}
