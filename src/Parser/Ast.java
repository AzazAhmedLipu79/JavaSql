package Parser;

import java.util.List;

// Base class for all nodes
abstract class ASTNode { }

// Simple condition: column op value (only '=' supported for now)
class ConditionNode extends ASTNode {
    final String column;
    final String op;
    final String value;
    ConditionNode(String column, String op, String value) { this.column = column; this.op = op; this.value = value; }
}

// DDL
class CreateDatabaseNode extends ASTNode {
    final String dbName;
    CreateDatabaseNode(String dbName) { this.dbName = dbName; }
}


class UseDatabaseNode extends ASTNode {
    final String dbName;
    UseDatabaseNode(String dbName) { this.dbName = dbName; }
}

class SelectDatabaseNode extends ASTNode { SelectDatabaseNode() { } }

class CreateTableNode extends ASTNode {
    final String tableName;
    final List<String> columns; // header names
    CreateTableNode(String tableName, List<String> columns) { this.tableName = tableName; this.columns = columns; }
}

class DropTableNode extends ASTNode {
    final String tableName;
    DropTableNode(String tableName) { this.tableName = tableName; }
}

// Supporting class
class Column {
    final String name;
    final String type;
    Column(String name, String type) { this.name = name; this.type = type; }
}


class AlterTableNode extends ASTNode {
    final String tableName;
    final String operation; // e.g., "ADD COLUMN", "DROP COLUMN"
    AlterTableNode(String tableName, String operation) { this.tableName = tableName; this.operation = operation; }
}


class DropDatabaseNode extends ASTNode {
    final String dbName;
    DropDatabaseNode(String dbName) { this.dbName = dbName; }
}

class SelectNode extends ASTNode {
    final String tableName;
    final List<String> columns; // * or explicit
    final ConditionNode where; // nullable
    final List<String> orderBy; // nullable
    final Integer limit; // nullable
    final Integer offset; // nullable
    SelectNode(String tableName, List<String> columns, ConditionNode where, List<String> orderBy, Integer limit, Integer offset) {
        this.tableName = tableName; this.columns = columns; this.where = where; this.orderBy = orderBy; this.limit = limit; this.offset = offset;
    }
}

class InsertNode extends ASTNode {
    final String tableName;
    final List<String> columns; // nullable
    final List<String> values;
    InsertNode(String tableName, List<String> columns, List<String> values) { this.tableName = tableName; this.columns = columns; this.values = values; }
}


class UpdateNode extends ASTNode {
    final String tableName;
    final ConditionNode where; // nullable
    final java.util.Map<String,String> updates;
    UpdateNode(String tableName, ConditionNode where, java.util.Map<String,String> updates) { this.tableName = tableName; this.where = where; this.updates = updates; }
}


class DeleteNode extends ASTNode {
    final String tableName;
    final ConditionNode where; // nullable
    DeleteNode(String tableName, ConditionNode where) { this.tableName = tableName; this.where = where; }
}

class SelectJoinNode extends ASTNode {
    final String leftTable;
    final String rightTable;
    final String leftCol;
    final String rightCol;
    final List<String> selectColumns; // optional; if null or ["*"], print all
    SelectJoinNode(String leftTable, String rightTable, String leftCol, String rightCol, List<String> selectColumns) {
        this.leftTable = leftTable; this.rightTable = rightTable; this.leftCol = leftCol; this.rightCol = rightCol; this.selectColumns = selectColumns;
    }
}

class GrantNode extends ASTNode {
    final String privilege;
    final String user;
    final String objectName; // e.g., database or table
    GrantNode(String privilege, String user, String objectName) { this.privilege = privilege; this.user = user; this.objectName = objectName; }
}


class RevokeNode extends ASTNode {
    final String privilege;
    final String user;
    final String objectName; // e.g., database or table
    RevokeNode(String privilege, String user, String objectName) { this.privilege = privilege; this.user = user; this.objectName = objectName; }
}

class ShowDatabasesNode extends ASTNode { ShowDatabasesNode() { } }

class ShowTablesNode extends ASTNode {
    final String dbName;
    ShowTablesNode(String dbName) { this.dbName = dbName; }
}

class ShowColumnsNode extends ASTNode {
    final String tableName;
    ShowColumnsNode(String tableName) { this.tableName = tableName; }
}

class ShowPrivilegesNode extends ASTNode {
    final String user;
    ShowPrivilegesNode(String user) { this.user = user; }
}

class ShowUsersNode extends ASTNode { ShowUsersNode() { } }
class ShowCurrentDatabaseNode extends ASTNode { ShowCurrentDatabaseNode() { } }

class ShowStatusNode extends ASTNode { ShowStatusNode() { } }

class ShowNode extends ASTNode {
    final String objectType; // e.g., "databases", "tables", "columns", etc.
    final String objectName; // Optional, e.g., database name for "tables" or table name for "columns"

    ShowNode(String objectType, String objectName) {
    this.objectType = objectType;
    this.objectName = objectName;
    }
}
class ShowUsersPrivilegesNode extends ASTNode {
    final String user;

    ShowUsersPrivilegesNode(String user) {
        this.user = user;
    }
    @Override public String toString() { return "ShowUsersPrivilegesNode{" + "user='" + user + '\'' + '}'; }
}