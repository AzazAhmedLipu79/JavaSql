# Usage Guide

## Build
- Run `build.bat` in the project root; outputs to `out/`.

## REPL
- Run `run.bat` to start an interactive console.
- Example:
  - `CREATE DATABASE demo;`
  - `USE demo;`
  - `CREATE TABLE people (id,name,city_id,age);`
  - `INSERT INTO people (id,name,city_id,age) VALUES (1,'Alice',10,30);`
  - `SELECT * FROM people;`

## Run SQL file
- `java -cp out SqlRunner tests\e2e.sql`
- SqlRunner supports `--` comments and splits on `;`.

## Supported SQL
- DDL
  - `CREATE DATABASE <name>`
  - `USE <name>`
  - `CREATE TABLE <name> (col1,col2,...)`
- DML
  - `INSERT INTO <table> [(col1,...)] VALUES (v1,v2,...)`
  - `UPDATE <table> SET col=val[, ...] [WHERE col = val]`
  - `DELETE FROM <table> [WHERE col = val]`
- DQL
  - `SELECT [cols|*] FROM <table> [WHERE col = val] [ORDER BY c1,c2,...]`
  - `SELECT ... FROM t1 JOIN t2 ON leftCol = rightCol`
  - `SELECT DATABASE`

See also: `docs/queries.md` for a narrative, example-driven reference with outputs.

## Pagination
- Tables are directories; new pages are created as `page_N.csv` when the current page would exceed the configured size.
- Configure page size in `storage/user_management/config.json` at `storage.page_size_kb`.

## Troubleshooting
- "No database selected": `USE <db>` before DML/DQL.
- "Table file not found": `CREATE TABLE` first.
- Double/duplicate rows across runs: clean the database directory or re-run `tests\run_all.bat` which resets the e2e db.
