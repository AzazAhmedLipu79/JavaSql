# Query Reference and Examples

This reference documents supported SQL statements with example inputs and representative outputs. Timestamps and absolute paths may differ on your machine; row and count outputs should match.

## Setup
```sql
CREATE DATABASE demo;
USE demo;
CREATE TABLE people (id,name,city_id,age);
CREATE TABLE cities (id,city);
```
Example console output (truncated):
```
Checking if database exists: demo
Database does not exist: demo
Creating database: demo
Database created successfully: demo
Using database: demo
[INFO] Table created: .../storage/databases/demo/people
[INFO] Table created: .../storage/databases/demo/cities
```

## INSERT
```sql
INSERT INTO people (id,name,city_id,age) VALUES (1,'Alice',10,30);
INSERT INTO people (id,name,city_id,age) VALUES (2,'Bob',10,25);
INSERT INTO people (id,name,city_id,age) VALUES (3,'Charlie',20,35);
INSERT INTO cities (id,city) VALUES (10,'Paris');
INSERT INTO cities (id,city) VALUES (20,'London');
```
Output:
```
[INFO] Inserted into .../people/page_0.csv: {id=1, name=Alice, city_id=10, age=30}
[INFO] Inserted into .../people/page_0.csv: {id=2, name=Bob, city_id=10, age=25}
[INFO] Inserted into .../people/page_0.csv: {id=3, name=Charlie, city_id=20, age=35}
[INFO] Inserted into .../cities/page_0.csv: {id=10, city=Paris}
[INFO] Inserted into .../cities/page_0.csv: {id=20, city=London}
```

Notes:
- If appending a row exceeds the configured page size, a new page `page_N.csv` is created and the row is written there.

## SELECT (all rows)
```sql
SELECT * FROM people;
```
Output (unordered example):
```
{id=1, name=Alice, city_id=10, age=30}
{id=2, name=Bob, city_id=10, age=25}
{id=3, name=Charlie, city_id=20, age=35}
[INFO] Selected and printed 3 rows from .../people
```

## SELECT with WHERE and ORDER BY
```sql
SELECT * FROM people WHERE age = 28 ORDER BY name;
```
If you inserted Diana (20,28):
```
{id=4, name=Diana, city_id=20, age=28}
[INFO] Selected and printed 1 rows from .../people
```

Projection and ordering across columns:
```sql
SELECT name,age FROM people ORDER BY age,name;
```
Output:
```
{name=Diana, age=28}
{name=Alice, age=30}
{name=Charlie, age=35}
[INFO] Selected and printed 3 rows from .../people
```

## UPDATE with WHERE
```sql
UPDATE people SET age=26 WHERE name = 'Bob';
```
Output:
```
[INFO] Updated 1 rows across 1 pages in .../people
```

## DELETE with WHERE
```sql
DELETE FROM people WHERE name = 'Bob';
```
Output:
```
[INFO] Deleted 1 rows across 1 pages in .../people
```

## JOIN (equi-join)
Join people.city_id with cities.id:
```sql
SELECT * FROM people JOIN cities ON city_id = id;
```
Output (merged columns prefixed left./right.):
```
{left.id=1, left.name=Alice, left.city_id=10, left.age=30, right.id=10, right.city=Paris}
{left.id=3, left.name=Charlie, left.city_id=20, left.age=35, right.id=20, right.city=London}
{left.id=4, left.name=Diana, left.city_id=20, left.age=28, right.id=20, right.city=London}
[INFO] Join produced 3 rows
```

## SELECT DATABASE
```sql
SELECT DATABASE;
```
Output:
```
Current database: demo
```

## Errors (typical)
- No database selected:
```
RuntimeException: No database selected
```
- Table not found:
```
Table file not found: .../storage/databases/<db>/<table>/page_0.csv
```
- Syntax errors:
```
Syntax error: SELECT ... FROM <table>
```

## Tips
- Ensure you `USE <db>` before DML/DQL.
- Page size can be reduced in `storage/user_management/config.json` (storage.page_size_kb) to observe page rollover quickly.
- For clean E2E runs, use `tests/run_all.bat` which resets the demo DB.
