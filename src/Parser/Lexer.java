package Parser;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    /* 
     * 
     * This class is responsible for tokenizing the input SQL string.
     * It reads the input character by character and generates tokens based on the rules defined in Token
     * and SqlVocabulary classes.
     * For example, it will convert the input "SELECT users WHERE id = 10" into tokens like:
     * [KEYWORD: SELECT], [IDENTIFIER: users], [KEYWORD: WHERE], [IDENTIFIER: id], [SYMBOL: =], [NUMBER_LITERAL: 10]
     * 
     * Algorithm:
     * 1. Initialize an empty list of tokens.
     * 2. Read the input string character by character.
     * 3. Skip whitespace characters.
     * 4. If the character is a letter or underscore, read until the end of the identifier or keyword.
     * 5. If the character is a digit, read until the end of the number literal, checking for decimal points to distinguish between integer and double literals.
     * 6. If the character is a single quote, read until the closing quote to form a string literal.
     * 7. If the character is 't' or 'f', read until the end of the boolean literal.
     * 8. If the character is a symbol (like '=', '+', '-', etc.), create a token for it. Additionally, check if the next character forms a multi-character symbol (like '<=', '>=', etc.) and create a token for that.
     * 9. If an unrecognized character is found, throw an exception.
     * 10. Finally, add an EOF token to signify the end of input.
     * This lexer will help in breaking down SQL statements into manageable tokens for further parsing and analysis.
     * Example usage:
     * Lexer lexer = new Lexer("SELECT users WHERE id = 10");
     * List<Token> tokens = lexer.tokenize();
     * tokens.forEach(System.out::println);
     * This will output:
     * [KEYWORD: SELECT]
     * [IDENTIFIER: users]
     * [KEYWORD: WHERE]
     * [IDENTIFIER: id]
     * [SYMBOL: =]
     * [NUMBER_LITERAL: 10]
     * 
     */

    private final String input;
    private final int length;

    public Lexer(String input) {
        this.input = input;
        this.length = input.length();
    }

    private String errorAround(int index) {
        int start = Math.max(0, index - 10);
        int end = Math.min(length, index + 10);
        return input.substring(start, end);
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < length) {
            char current = input.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(current)) {
                i++;
                continue;
            }

            // Identifiers or Keywords
            if (Character.isLetter(current) || current == '_') {
                int start = i;
                while (i < length && (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    i++;
                }
                String word = input.substring(start, i);
                String upper = word.toUpperCase();
                if (SqlVocabulary.isKeyword(upper)) {
                    tokens.add(new Token(TokenType.KEYWORD, word));
                } else if (SqlVocabulary.BOOLEAN_LITERALS.contains(word.toLowerCase())) {
                    tokens.add(new Token(TokenType.BOOLEAN_LITERAL, word));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, word));
                }
                continue;
            }

            // Integer or Double Literals
            if (Character.isDigit(current)) {
                StringBuilder number = new StringBuilder();
                int point_count = 0;
                boolean isDouble = false;
                while (i < length && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    if (input.charAt(i) == '.') {
                        isDouble = true;
                        point_count++;
                        
                        if (point_count > 1) { 
                            throw new IllegalArgumentException("Invalid number format at position " + i + ": " + errorAround(i));
                        }
                    }
                    number.append(input.charAt(i));
                    i++;
                }
                if (isDouble) {
                    tokens.add(new Token(TokenType.DOUBLE_LITERAL, number.toString()));
                } else {
                    tokens.add(new Token(TokenType.INTEGER_LITERAL, number.toString()));
                }
                continue;
            }


            // String literals (single quotes)
            if (current == '\'') {
                i++; // skip opening quote
                StringBuilder str = new StringBuilder();
                while (i < length) {
                    char ch = input.charAt(i);
                    if (ch == '\'') {
                        // handle escaped single quote by doubling ''
                        if (i + 1 < length && input.charAt(i + 1) == '\'') {
                            str.append('\'');
                            i += 2;
                            continue;
                        }
                        i++; // consume closing quote
                        break;
                    } else {
                        str.append(ch);
                        i++;
                    }
                }
                if (i > length) {
                    throw new IllegalArgumentException("Unterminated string literal at position " + i + ": " + errorAround(i));
                }
                tokens.add(new Token(TokenType.STRING_LITERAL, str.toString()));
                continue;
            }

            // Note: boolean literals are handled in the identifier/keyword branch above

            // Symbols
            if (SqlVocabulary.SYMBOLS.contains(current)) {

                // if next character forms a multi-character symbol
                if (i + 1 < length) {
                    String nextTwoChars = input.substring(i, Math.min(i + 2, length));
                    if (SqlVocabulary.MultiCharSymbols.contains(nextTwoChars)) {
                        tokens.add(new Token(TokenType.SYMBOL, nextTwoChars));
                        i += 2; // skip the next character as well
                        continue;
                    }
                }
                
                tokens.add(new Token(TokenType.SYMBOL, String.valueOf(current)));
                i++;
                continue;
            }

            // Unrecognized character
            throw new IllegalArgumentException("Unrecognized character at position " + i + ": " + errorAround(i));
        }

        // End of input
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}
