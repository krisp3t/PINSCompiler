/**
 * @ Author: turk
 * @ Description: Vrste leksikalnih simbolov.
 */

package compiler.lexer;

public enum TokenType {
    /**
     * Konec datoteke.
     */
    EOF,

    /**
     * Ključne besede:
     */
    KW_ARR,
    KW_ELSE,
    KW_FOR,
    KW_FUN,
    KW_IF,
    KW_THEN,
    KW_TYP,
    KW_VAR,
    KW_WHERE,
    KW_WHILE,

    /**
     * Atomarni podatkovni tipi:
     */
    AT_LOGICAL, // atomarni tip `logical`
    AT_INTEGER, // atomarni tip `integer`
    AT_STRING,  // atomarni tip `string`

    /**
     * Konstante:
     */
    C_LOGICAL, // logična konstanta (true/false)
    C_INTEGER, // celoštevilska konstanta
    C_STRING,  // znakovna konstanta

    /**
     * Ime.
     */
    IDENTIFIER,

    /**
     * Operatorji:
     */
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo2/main
    OP_ADD,       // +
    OP_SUB,       // -
    OP_MUL,       // *
    OP_DIV,       // /
    OP_MOD,       // %

    OP_AND,       // &
    OP_OR,        // |
    OP_NOT,       // !
    
    OP_EQ,        // ==
    OP_NEQ,       // !=
    OP_LT,        // <
    OP_GT,        // >
    OP_LEQ,       // <=
    OP_GEQ,       // >=

    OP_LPARENT,   // (
    OP_RPARENT,   // )
    OP_LBRACKET,  // [
    OP_RBRACKET,  // ]
    OP_LBRACE,    // {
    OP_RBRACE,    // }

    OP_COLON,     // :
    OP_SEMICOLON, // ;
    OP_DOT,       // .
    OP_COMMA,     // ,
    OP_ASSIGN     // =
<<<<<<< HEAD
=======
    OP_ADD,       // + (43)
    OP_SUB,       // - (45)
    OP_MUL,       // * (42)
    OP_DIV,       // / (47)
    OP_MOD,       // % (37)

    OP_AND,       // & (38)
    OP_OR,        // | (124)
    OP_NOT,       // ! (33)

    OP_EQ,        // == (61 61)
    OP_NEQ,       // != (33 61)
    OP_LT,        // < (60)
    OP_GT,        // > (62)
    OP_LEQ,       // <= (60 61)
    OP_GEQ,       // >= (62 61)

    OP_LPARENT,   // ( (40)
    OP_RPARENT,   // ) (41)
    OP_LBRACKET,  // [ (91)
    OP_RBRACKET,  // ] (93)
    OP_LBRACE,    // { (123)
    OP_RBRACE,    // } (125)

    OP_COLON,     // : (58)
    OP_SEMICOLON, // ; (59)
    OP_DOT,       // . (46)
    OP_COMMA,     // , (44)
    OP_ASSIGN     // = (61)
>>>>>>> repo1/main
=======
>>>>>>> repo2/main
}
