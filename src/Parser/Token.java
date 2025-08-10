package Parser;


public class Token {
    public final TokenType type;
    public final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "[" + type + ": " + value + "]";
    }


    /* 
     * This class represents a token in the SQL parser. (Lexer)
     * Each token has a type (TokenType) and a value (String).
     * It is used to identify different components of an SQL statement.
     * For Example: [KEYWORD: SELECT], [IDENTIFIER: users], [KEYWORD: WHERE], [IDENTIFIER: id], [SYMBOL: =], [NUMBER_LITERAL: 10]
     * SELECT users WHERE id = 10
     */
}


