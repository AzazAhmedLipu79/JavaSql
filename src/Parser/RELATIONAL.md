| SQL Clause  | RA Operator        | Notes                  |
| ----------- | ------------------ | ---------------------- |
| `SELECT`    | π (projection)    | Choose columns         |
| `FROM`      | base tables        | Start point            |
| `WHERE`     | σ (selection)     | Apply row filters      |
| `JOIN`      | ⨝ (join)          | θ-join on condition   |
| `GROUP BY`  | γ (grouping)      | Needs extended RA      |
| `HAVING`    | σ (on aggregates) | After grouping         |
| `ORDER BY`  | τ (sorting)       | Not in classical RA    |
| `DISTINCT`  | implicit / δ      | Optional deduplication |
| `UNION`     | ∪                 | Set union              |
| `INTERSECT` | ∩                 | Set intersection       |
| `EXCEPT`    | −                 | Set difference         |
