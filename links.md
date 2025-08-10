### Parser:

https://dev.to/codingwithadam/introduction-to-lexers-parsers-and-interpreters-with-chevrotain-5c7b

![Lexer Parser Interpreter Diagram](https://media2.dev.to/dynamic/image/width=800%2Cheight=%2Cfit=scale-down%2Cgravity=auto%2Cformat=auto/https%3A%2F%2Fdev-to-uploads.s3.amazonaws.com%2Fuploads%2Farticles%2Fb1d3fu7q6vw4o1ckpkst.png)

Start
|
v
Initialize pos = 0, tokens = []
|
v
Is pos >= input length? ---Yes---> Add EOF token --> End
|
No
|
v
Read current character = input[pos]
|
v
Is current whitespace? --Yes--> pos++ and go back to "Is pos >= input length?"
|
No
|
v
Is current a symbol? --Yes--> Add SYMBOL token, pos++, go back to "Is pos >= input length?"
|
No
|
v
Is current a quote (' or ")? --Yes--> Read string literal token --> go back to "Is pos >= input length?"
|
No
|
v
Is current letter or underscore? --Yes--> Read word token:
|                                      - Collect letters/digits/underscore
|                                      - Convert to uppercase
|                                      - If in KEYWORDS set -> KEYWORD token
|                                      - Else if in TYPES set -> TYPE token
|                                      - Else if TRUE/FALSE -> BOOLEAN_LITERAL token
|                                      - Else IDENTIFIER token
|                                      --> Go back to "Is pos >= input length?"
|
No
|
v
Is current digit? --Yes--> Read number literal token --> Go back to "Is pos >= input length?"
|
No
|
v
Throw error: "Unexpected character"
