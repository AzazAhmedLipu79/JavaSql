# A Minimal SQL-like DBMS over CSV with Paged Storage

Author: Project Team
Date: 2025-08-10

## Abstract
This paper presents a minimal SQL-like database management system (DBMS) implemented in Java over CSV storage. The system supports a practical subset of SQL (CREATE/USE/CREATE TABLE, INSERT/UPDATE/DELETE, SELECT with WHERE and ORDER BY, and equi-joins), a paged storage model with configurable file rollover, and a clean separation between parsing, execution, and storage. We describe the architecture, storage model, query pipeline, implementation, and an evaluation via reproducible tests. We conclude with limitations and a roadmap for types, indexes, durability, and concurrency.

## 1. Introduction
Minimal DBMS implementations are valuable for learning, prototyping, and controlled experiments. This work delivers a runnable, end-to-end DBMS with:
- A compact SQL subset sufficient for CRUD and simple analytics.
- CSV-backed storage organized into page files to emulate on-disk paging.
- Clear module boundaries: Parser → AST → AstExecutor → TableEngine.

The goals are clarity and demonstrable behavior via tests, not production scalability or durability.

## 2. Related Work
- SQLite: single-file embedded relational DB with robust SQL and ACID guarantees.
- DuckDB: in-process analytics DB with vectorized execution.
- CSV-based tools: ETL-friendly but typically lack indexes and transactions.
Our system intentionally trades performance and resilience for simplicity and transparency.

## 3. System Overview
The DBMS maps SQL to actions on paged CSV tables. A session tracks the current database. Statements execute through an interpreter over a data-only AST.

### Supported SQL
- DDL: `CREATE DATABASE`, `USE`, `CREATE TABLE`, `SELECT DATABASE`
- DML: `INSERT`, `UPDATE ... WHERE col = val`, `DELETE ... WHERE col = val`
- DQL: `SELECT [cols|*] FROM <table> [WHERE col = val] [ORDER BY c1,c2,...]`
- JOIN: `SELECT ... FROM t1 JOIN t2 ON leftCol = rightCol` (equi-join)

### Architecture (Block Diagram)
```
+------------------+      +-------------+       +----------+      +--------------+
|  SqlRunner/Main  | ---> |   Lexer     | --->  |   AST    | ---> |  AstExecutor |
+------------------+      +-------------+       +----------+      +--------------+
                                                                     |    |    |
                                                                     |    |    |
                                                                     |    |    +--> TableEngine (CSV paged IO)
                                                                     |    +------> Session (current DB)
                                                                     +----------> Executor (DB-level)

TableEngine -> Storage: storage/databases/<db>/<table>/page_0.csv, page_1.csv, ...
```

## 4. Storage Model and Paging
- Database layout: `storage/databases/<db>/<table>/`
  - Files: `page_0.csv`, `page_1.csv`, ...
  - `meta/` directory reserved for metadata.
- Page size configured in `storage/user_management/config.json` as `storage.page_size_kb` (default 64 KB).
- Insert policy: append to the last page; if adding a row would exceed page size, create `page_{N+1}.csv` and write there.
- Reads/updates/deletes: operate across pages; ORDER BY sorting in memory.
- Join: nested-loop over rows from both tables after loading across pages.

ASCII layout example:
```
storage/
  databases/
    demo/
      people/
        page_0.csv  # header + rows
        page_1.csv  # next page
        meta/
      cities/
        page_0.csv
        meta/
```

## 5. Query Processing Pipeline
1. Lexer tokenizes SQL (case-insensitive keywords, numbers, strings with '' escape, symbols).
2. Parser builds a data-only AST for supported statements; WHERE supports equality; JOIN supports ON equality.
3. AstExecutor interprets AST:
   - DB commands via Executor (create/use/show) and Session (current DB).
   - Table operations via TableEngine using the current database path.
4. TableEngine performs CSV IO, paging, filtering, ordering, and joining.

Sequence (INSERT example):
```
User SQL -> Lexer -> Parser(AST) -> AstExecutor -> resolve table dir
  -> TableEngine.insertDir(row, pageSizeKB)
      -> check last page size; rollover if needed -> write row -> log
```

## 6. Implementation Details
- Lexer: string literals with doubled-quote escape (''), case-insensitive keyword detection.
- Parser: handles CREATE/USE/CREATE TABLE, INSERT (implicit/explicit columns), UPDATE/DELETE (WHERE equality), SELECT (WHERE equality, ORDER BY), and JOIN ON equality.
- AST: data structures only; no side effects.
- AstExecutor: executes AST nodes; projects columns; logs row counts.
- TableEngine: paged table helpers (list pages, loadDir, insertDir, updateDir, deleteDir, selectDir) and nested-loop join.
- CatalogManager: database creation via blueprint copy; table directories initialized with `page_0.csv` header.
- SqlRunner: semicolon-separated statements; strips inline `--` comments; single JVM session execution.
- Logging: console + file (out/dbms.log) with INFO-level operation summaries.

## 7. Evaluation
Method: Windows environment; scripts under `tests/` orchestrate runs.
- Harness (programmatic TableEngine): insert/update/select/join/delete; validates CSV ops.
- Smoke SQL: CREATE/USE/SELECT DATABASE.
- E2E SQL: DDL + DML + DQL + JOIN across paged tables, resetting DB between runs.

Observations:
- Inserts land in `page_0.csv` and roll over when page is full (configurable if you reduce page_size_kb to a small value to force rollover).
- WHERE equality, ORDER BY, and JOIN produce expected outputs; counts logged.
- All tests pass via `tests/run_all.bat`.

## 8. Limitations
- Equality-only WHERE; no expression evaluation.
- No types/constraints; values are strings; no NULL semantics.
- Full scans and nested-loop joins; no indexes or cost-based planning.
- No durability (WAL, atomic rename) or crash recovery; single-user concurrency model.
- Config parsing is minimal; a robust JSON reader is recommended for future work.

## 9. Future Work
- Types and schema JSON in `meta/`; input coercion and constraint checks (NOT NULL, UNIQUE, PRIMARY KEY).
- Expression engine for comparisons, boolean ops, LIKE/IN.
- Indexes (B+Tree) and a simple planner (index scans, predicate pushdown).
- Hash join for equality; external sort for large ORDER BY.
- Durability: WAL/journal, atomic writes; compaction and free-space map.
- Concurrency: table-level locks or MVCC; per-thread sessions.
- SQL coverage: DROP/ALTER TABLE, SHOW TABLES, LIMIT/OFFSET.

## 10. Conclusion
This project demonstrates a clear, minimal DBMS with paged CSV storage and a functional SQL subset. It separates parsing, execution, and storage cleanly and is validated end-to-end with reproducible tests and logging. It serves as a practical base for education and incremental research toward indexes, optimization, and durability.

## Appendix A: Reproducibility
- Build: `build.bat`
- Run all tests: `tests\run_all.bat`
- Logs: `out/dbms.log`
- Data: `storage/databases/<db>/<table>/page_N.csv`

## Appendix B: Module Map
```
Parser/ (Lexer, Parser, AST nodes, AstExecutor, SqlVocabulary)
Executor/ (DB-level ops)
CatalogManager/ (database and table dir creation)
Session/ (current DB)
Util/ (TableEngine, Logger, AccessDataFolder)
SqlRunner.java (batch runner), Main.java (REPL)
```
