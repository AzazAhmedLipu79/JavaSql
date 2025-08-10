package Parser;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class SqlVocabulary {
    // Use LinkedHashSet initialized from a list so accidental duplicates don't throw at class init.
    public static final Set<String> KEYWORDS = new LinkedHashSet<>(Arrays.asList(
        "CREATE", "DATABASE", "DATABASES", "USE", "TABLE", "DROP", "ALTER", "TRUNCATE",
        "INSERT", "INTO", "VALUES", "SHOW",
        "SELECT", "FROM", "WHERE", "UPDATE", "SET", "DELETE",
        "AND", "OR", "NOT", "ORDER", "BY", "GROUP", "HAVING",
        "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "ON", "AS", "DISTINCT",
        "LIMIT", "OFFSET", "UNION", "EXCEPT", "ALL",
        "CASE", "WHEN", "THEN", "ELSE", "END", "CAST", "CONVERT", "LIKE", "IN", "BETWEEN",
        "IS", "NULL", "NOT_NULL", "PRIMARY", "AUTO_INCREMENT", "PRIMARY_KEY", "DEFAULT", "UNIQUE", "IF", "EXISTS",
        "TRUE", "FALSE"
    ));

    public static final Set<String> TYPES = new LinkedHashSet<>(Arrays.asList(
        "INT", "STRING", "DOUBLE", "BOOL"
    ));

    public static final Set<Character> SYMBOLS = new LinkedHashSet<>(Arrays.asList(
        '(', ')', ',', ';', '*', '=', '<', '>', '!', '.', '+', '-', '/', '%'
    ));

    public static final Set<String> MultiCharSymbols = new LinkedHashSet<>(Arrays.asList(
        "<=", ">=", "<>", "==", "!=", "&&", "||"
    ));

    // Keep lowercase canonical values, Lexer will compare case-insensitively.
    public static final Set<String> BOOLEAN_LITERALS = new LinkedHashSet<>(Arrays.asList("true", "false"));

    public static boolean isKeyword(String word) {
        return word != null && KEYWORDS.contains(word.toUpperCase());
    }
}