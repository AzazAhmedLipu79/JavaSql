# Architecture and Implementation

This project is a minimal SQL-like DBMS implemented in Java with CSV-backed storage. It provides:
- Parser + Lexer producing a data-only AST.
- An interpreter (AstExecutor) that executes AST nodes using an Executor and a CSV TableEngine.
- CSV tables stored in paged files inside a per-table directory.

## High-level components

- Parser/
  - Lexer: tokenizes SQL input (case-insensitive keywords, numbers, strings with '' escape, symbols).
  - Parser: constructs AST nodes for a minimal SQL subset.
  - Ast.java: data-only AST node classes (Create/Use/SelectDatabase, CreateTable, Insert, Update, Delete, Select, SelectJoin, ConditionNode).
  - AstExecutor: executes AST nodes by delegating to Executor/Session/TableEngine.
  - SqlVocabulary: keywords, symbols, and helpers.
- Executor/
  - Executor: database-level operations (create/use).
- CatalogManager/
  - Creates databases by copying the blueprint; verifies existence.
- Session/
  - Tracks current database during a session.
- Util/
  - TableEngine: CSV storage engine with paged layout and CRUD/select/join operations.
  - Logger: console + file logging.
  - CsvParser/Json/AccessDataFolder: small utilities.
- SqlRunner
  - Executes a .sql file in a single JVM, with semicolon-statement splitting and -- comments.
- Main
  - Interactive REPL that parses and executes each entered statement.

## Storage model

- storage/databases/<db>/<table>/
  - page_0.csv, page_1.csv, ... (header in page_0.csv)
  - meta/
- Page size: configured via storage/user_management/config.json at `storage.page_size_kb` (default 64 KB if absent).
- Insert policy: append to the last page; if the added row would exceed the page size, roll over to a new page.
- Select/Update/Delete: full scan across all pages in the table directory.
- Join: nested-loop join after loading both tables across all pages.

## Execution flow

1. Input SQL is tokenized by Lexer.
2. Parser builds an AST node (e.g., InsertNode).
3. AstExecutor interprets the AST:
   - DB commands (create/use/show) go to Executor.
   - Table commands use TableEngine with the current database from Session.
4. TableEngine handles CSV IO, pagination, and operations.

## Error handling
- Parser prints syntax errors for unsupported/malformed statements.
- Runtime errors are caught and printed by AstExecutor.
- Logger records info-level events for storage operations.

## Limitations
- WHERE only supports equality comparisons.
- No types; all values are strings.
- No transactions or indexes (paged files are append-only with rollover).
- JOIN is an equi-join only.
