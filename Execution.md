### Implementation Plan

#### Step 1: SQL Parsing

* Use or build a parser to parse SQL strings into AST.
* You can start with a parser generator like ANTLR with an existing SQL grammar for Java.
* Alternatively, build a simple recursive-descent parser for a subset of SQL.

**Tips:**

* Start with DDL commands (`CREATE TABLE`, `DROP TABLE`) first to define schema.
* Then parse simple `SELECT` statements with limited clauses.

#### Step 2: Catalog Management

* Design data structures for catalog: Table schemas, columns, types, constraints.
* Save catalog info on disk (e.g., JSON or binary format) for persistence.
* Load catalog on DB start.

#### Step 3: Storage Layer

* Implement file-based storage for tables.
* Design simple page/record format (fixed-length or variable-length).
* Support basic file operations: create, read, write, delete.
* Implement heap file storage initially (append-only).

#### Step 4: Query Execution

* Design a basic executor that can do table scans and apply filters (`WHERE`).
* Support projection (select specific columns).
* Add support for ordering, grouping gradually.
* Implement `INSERT`, `UPDATE`, and `DELETE` commands on storage.

#### Step 5: Extend SQL Support

* Gradually add support for joins, indexes (e.g., B+ trees).
* Support transactions and concurrency control (locks, MVCC).
* Add query optimization techniques.
