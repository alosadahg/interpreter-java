package interpreter;
enum TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET, COMMA, COLON,
    DOT, MINUS, PLUS, SLASH, STAR, NEW_LINE, MODULO, SHARP, CONCAT,

    //One or two character token pair
    NOT_EQUAL, ASSIGN, EQUAL_EVAL, GREATER_THAN, 
    GREATER_OR_EQUAL, LESS_THAN, LESS_OR_EQUAL, 

    //Keywords
    BEGIN, END, DISPLAY, SCAN, IF, ELSE, WHILE, CODE, TRUE, FALSE,

    //Logical Operators
    OR, AND, NOT,

    //data types
    STRING, INT, FLOAT, BOOL, CHAR, 

    TYPESTRING, TYPEINT, TYPEFLOAT, TYPEBOOL, TYPECHAR,

    IDENTIFIER, NULL,

    EOF
}