# JavaSQL (CSV DBMS Minimal SQL)

A tiny SQL-like engine in Java over CSV files. Supports basic DDL (CREATE DATABASE, USE, CREATE TABLE), DML (INSERT/UPDATE/DELETE), and DQL (SELECT with WHERE/ORDER BY, simple JOIN).

## Features

- Databases as folders under `storage/databases/<db>`
- Tables as CSV files with a header row
- Statements supported:
  - CREATE DATABASE <name>
  - USE <name>
  - CREATE TABLE <name> (col1,col2,...)
  - INSERT INTO <table> [(col1,...)] VALUES (v1,v2,...)
  - UPDATE <table> SET col=val[, ...] [WHERE col = val]
  - DELETE FROM <table> [WHERE col = val]
  - SELECT [cols|*] FROM <table> [WHERE col = val] [ORDER BY c1,c2,...]
  - SELECT ... FROM t1 JOIN t2 ON leftCol = rightCol
  - SELECT DATABASE

Notes: WHERE supports equality only; JOIN is an equi-join with column names on each side.

## Build and Run

- Build:
  - Run `build.bat` (compiles to `out/`)
- Interactive REPL:
  - Run `run.bat` (starts `Main`)
- Execute a SQL file (single JVM, persistent session):
  - `java -cp out SqlRunner path\to\file.sql`

## Tests

- Smoke (DDL):
  - `tests\run_tests.bat`
- Programmatic storage tests (TableEngine):
  - `tests\run_harness.bat`
- End-to-end SQL:
  - After build, run `java -cp out SqlRunner tests\e2e.sql`

## Storage Layout

```
storage/
  databases/
    <db>/
      <table>/              # table directory
        page_0.csv          # header row defines columns
        page_1.csv          # additional pages created as needed
        ...
      meta/
        tables_info.csv
        columns.csv
```

See docs/queries.md for end-to-end examples and typical outputs.

## Code Map

- Parser/: Lexer, Parser, AST nodes, and AstExecutor
- Util/: TableEngine (CSV ops), Logger, Help
- Executor/: Database commands (create/use/show/drop)
- SqlRunner: Batch SQL runner (semicolon-separated; supports `--` comments)
- Main: Interactive REPL with help and table rendering

## Limitations

- Equality-only WHERE; no complex expressions
- No type system; everything is string-based
- No transactions, constraints, or indexes
- Minimal error reporting in parser

## Troubleshooting

- “No database selected”: Run `USE <db>` before DML/DQL
- “Table file not found”: Create table first (`CREATE TABLE`)
- Windows paths: scripts assume Windows PowerShell/CMD; use `\\` in SQL runner path
