package Parser;

public enum TokenType {
    KEYWORD, // SQL keywords like SELECT, INSERT, etc.
    IDENTIFIER, // Table names, column names, etc.
    TYPE, // String, Integer, Double, Boolean
    STRING_LITERAL, 
    INTEGER_LITERAL,
    DOUBLE_LITERAL,  
    BOOLEAN_LITERAL,
    SYMBOL, // Operators and punctuation like =, +, -, *, /, (, ), etc.
    EOF // End of file
}