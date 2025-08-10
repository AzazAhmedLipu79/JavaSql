package Util;

public class Help {
    public static void print() {
        System.out.println("Java CSV DBMS - Help\n");
        System.out.println("Supported statements:");
        System.out.println("  CREATE DATABASE <name>;");
        System.out.println("  DROP DATABASE <name>;");
        System.out.println("  USE <name>;");
        System.out.println("  CREATE TABLE <name> (col1,col2,...);");
        System.out.println("  INSERT INTO <table> [(c1,...)] VALUES (v1,v2,...);");
        System.out.println("  UPDATE <table> SET col=val[, ...] [WHERE col = val];");
        System.out.println("  DELETE FROM <table> [WHERE col = val];");
        System.out.println("  SELECT [*|c1,c2,...] FROM <table> [WHERE col = val] [ORDER BY c1,c2,...];");
        System.out.println("  SELECT ... FROM t1 JOIN t2 ON t1Col = t2Col;");
        System.out.println("  SELECT DATABASE;");
        System.out.println("  SHOW DATABASES; | SHOW TABLES [FROM <db>];\n");
        System.out.println("Notes:");
        System.out.println("  - WHERE supports equality only (col = value)");
        System.out.println("  - ORDER BY is ascending, one or more columns");
        System.out.println("  - JOIN supports equality only, inner join");
        System.out.println("\nTips:");
        System.out.println("  - Use 'help' or '--help' anytime");
        System.out.println("  - Docs: docs/index.html (open in a browser)");
        System.out.println("  - Query reference: docs/queries.md / docs/queries.html");
    }
}
