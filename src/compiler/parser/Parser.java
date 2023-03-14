/**
 * @Author: turk
 * @Description: Sintaksni analizator.
 */

package compiler.parser;

import static common.RequireNonNull.requireNonNull;

import java.io.PrintStream;
import java.util.*;

import common.Report;
import compiler.lexer.Position;
import compiler.lexer.Symbol;
import compiler.lexer.TokenType;

public class Parser {
    /**
     * Seznam leksikalnih simbolov.
     */
    private final List<Symbol> symbols;
    private Stack<Symbol> stack;
    private int pozicijaSimbola = 0;

    /**
     * Ciljni tok, kamor izpisujemo produkcije. Če produkcij ne želimo izpisovati,
     * vrednost opcijske spremenljivke nastavimo na Optional.empty().
     */
    private final Optional<PrintStream> productionsOutputStream;

    public Parser(List<Symbol> symbols, Optional<PrintStream> productionsOutputStream) {
        requireNonNull(symbols, productionsOutputStream);
        this.symbols = symbols;
        this.productionsOutputStream = productionsOutputStream;
    }

    private TokenType check() {
        return getSymbol().tokenType;
    }

    private Symbol getSymbol() {
        return this.symbols.get(this.pozicijaSimbola);
    }

    private void skip() {
        if (this.pozicijaSimbola < (this.symbols.size() - 1))
            this.pozicijaSimbola++;
    }

    /**
     * Izvedi sintaksno analizo.
     */
    public void parse() {
        parseSource();
    }

    private void parseSource() {
        dump("source -> defs .");
        parseDefs();
    }

    private void parseDefs() {
        dump("defs -> def defs2 .");
        parseDef();
        parseDefs2();
    }

    private void parseDef() {
        switch (check()) {
            case KW_TYP:
                dump("def -> type_def .");
                skip();
                parseTypeDef();
                break;
            case KW_FUN:
                dump("def -> fun_def .");
                skip();
                parseFunDef();
                break;
            case KW_VAR:
                dump("def -> var_def .");
                skip();
                parseVarDef();
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa definicije!");
        }
    }

    private void parseDefs2() {
        switch (check()) {
            case OP_SEMICOLON:
                dump("defs2 -> ';' def defs2 .");
                skip();
                parseDef();
                parseDefs2();
                break;
            case EOF:
                dump("defs2 -> .");
                skip();
                break;
            case OP_RBRACE:
                dump("defs2 -> .");
                // skipamo v staršu in preverimo
                break;
            default:
                Report.error(getSymbol().position, "Manjka ';' med ločnicami definicij ali '}' na koncu!");
        }
    }

    private void parseTypeDef() {
        dump("type_def -> typ id ':' type .");
        // typ že skipan
        if (check() == TokenType.IDENTIFIER)
            skip();
        else
            Report.error(getSymbol().position, "Manjka identifier pri definiciji tipa!");

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji tipa!");

        parseType();
    }

    private void parseType() {
        switch (check()) {
            case IDENTIFIER:
                dump("type -> id .");
                skip();
                break;
            case AT_LOGICAL:
                dump("type -> logical .");
                skip();
                break;
            case AT_INTEGER:
                dump("type -> integer .");
                skip();
                break;
            case AT_STRING:
                dump("type -> string .");
                skip();
                break;
            case KW_ARR:
                dump("type -> arr '['int_const']' type .");
                skip(); // arr
                if (check() == TokenType.OP_LBRACKET)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '[' pri definiciji arraya!");
                if (check() == TokenType.C_INTEGER)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka konstanta integer pri definiciji arraya!");
                if (check() == TokenType.OP_RBRACKET)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ']' pri definiciji arraya!");

                parseType();
                break;
            default:
                Report.error(getSymbol().position, "Nepravilen tip!");
        }
    }

    private void parseFunDef() {
        dump("fun_def -> fun id '('params')' ':' type '=' expr .");
        // fun že skippano
        if (check() == TokenType.IDENTIFIER)
            skip();
        else
            Report.error(getSymbol().position, "Manjka identifier pri definiciji tipa!");
        if (check() == TokenType.OP_LPARENT)
            skip();
        else
            Report.error(getSymbol().position, "Manjka '(' pri definiciji funkcije za parametre!");

        parseParams();

        if (check() == TokenType.OP_RPARENT)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ')' pri definiciji funkcije za parametre!");

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji funkcije za določitev tipa!");

        parseType();

        if (check() == TokenType.OP_ASSIGN)
            skip();
        else
            Report.error(getSymbol().position, "Manjka '=' pri definiciji funkcije!");

        parseExpr();
    }

    private void parseParams() {
        dump("params -> param params2 .");
        parseParam();
        parseParams2();
    }

    private void parseParam() {
        dump("param -> id ':' type .");
        if (check() == TokenType.IDENTIFIER)
            skip();
        else
            Report.error(getSymbol().position, "Manjka identfier pri definiciji parametra!");

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji parametra!");

        parseType();
    }

    private void parseParams2() {
        switch (check()) {
            case OP_COMMA:
                dump("params2 -> ',' param params2 .");
                skip();
                parseParam();
                parseParams2();
                break;
            case OP_RPARENT:
                dump("params2 -> .");
                // ne skipamo tu, ampak v staršu
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna definicija parametrov!");
        }
    }

    private void parseExpr() {
        dump("expr -> logical_ior_expr expr2 .");
        parseLogicalIorExpr();
        parseExpr2();
    }

    private void parseExpr2() {
        switch (check()) {
            case OP_LBRACE:
                dump("expr2 -> '{' WHERE defs '}' .");
                skip();
                if (check() == TokenType.KW_WHERE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka WHERE v expressionu!");

                parseDefs();

                if (check() == TokenType.OP_RBRACE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '}' v expressionu!");

                break;
            default:
                dump("expr2 -> .");
                break;
        }
    }

    private void parseLogicalIorExpr() {
        dump("logical_ior_expr -> logical_and_expr logical_ior_expr2 .");
        parseLogicalAndExpr();
        parseLogicalIorExpr2();
    }

    private void parseLogicalIorExpr2() {
        switch (check()) {
            case OP_OR:
                dump("logical_ior_expr2 -> '|' logical_and_expr logical_ior_expr2 .");
                skip();
                parseLogicalAndExpr();
                parseLogicalIorExpr2();
            default:
                dump("logical_ior_expr2 -> .");
                break;
        }
    }

    private void parseLogicalAndExpr() {
        dump("logical_and_expr -> compare_expr logical_and_expr2 .");
        parseCompareExpr();
        parseLogicalAndExpr2();
    }

    private void parseLogicalAndExpr2() {
        switch (check()) {
            case OP_AND:
                dump("logical_and_expr2 -> '&' compare_expr logical_and_expr2 .");
                skip();
                parseCompareExpr();
                parseLogicalAndExpr2();
            default:
                dump("logical_and_expr2 -> .");
                break;
        }
    }

    private void parseCompareExpr() {
        dump("compare_expr -> add_expr compare_expr2 .");
        parseAddExpr();
        parseCompareExpr2();
    }

    private void parseCompareExpr2() {
        final HashSet<TokenType> operatorji = new HashSet<>(Arrays.asList(TokenType.OP_EQ, TokenType.OP_NEQ, TokenType.OP_LEQ, TokenType.OP_GEQ, TokenType.OP_LT, TokenType.OP_GT));
        if (operatorji.contains(check())) {
            dump("compare_expr2 -> '" + getSymbol().lexeme + "' add_expr .");
            skip();
            parseAddExpr();
        } else {
            dump("compare_expr2 -> .");
        }
    }

    private void parseAddExpr() {
        dump("add_expr -> mul_expr add_expr2 .");
        parseMulExpr();
        parseAddExpr2();
    }

    private void parseAddExpr2() {
        switch (check()) {
            case OP_ADD:
                dump("add_expr2 -> '+' mul_expr add_expr2 .");
                skip();
                parseMulExpr();
                parseAddExpr2();
                break;
            case OP_SUB:
                dump("add_expr2 -> '-' mul_expr add_expr2 .");
                parseMulExpr();
                parseAddExpr2();
                break;
            default:
                dump("add_expr2 -> .");
                break;
        }
    }

    private void parseMulExpr() {
        dump("mul_expr -> pre_expr mul_expr2 .");
        parsePreExpr();
        parseMulExpr2();
    }

    private void parseMulExpr2() {
        switch (check()) {
            case OP_MUL:
                dump("mul_expr2 -> '*' pre_expr mul_expr2 .");
                skip();
                parsePreExpr();
                parseMulExpr2();
                break;
            case OP_DIV:
                dump("mul_expr2 -> '/' pre_expr mul_expr2 .");
                skip();
                parsePreExpr();
                parseMulExpr2();
                break;
            case OP_MOD:
                dump("mul_expr2 -> '%' pre_expr mul_expr2 .");
                skip();
                parsePreExpr();
                parseMulExpr2();
                break;
            default:
                dump("mul_expr2 -> .");
                break;
        }
    }

    private void parsePreExpr() {
        switch (check()) {
            case OP_ADD:
                dump("pre_expr -> '+'pre_expr .");
                skip();
                parsePreExpr();
                break;
            case OP_DIV:
                dump("pre_expr -> '-'pre_expr .");
                skip();
                parsePreExpr();
                break;
            case OP_NOT:
                dump("pre_expr -> '!'pre_expr .");
                skip();
                parsePreExpr();
                break;
            default:
                dump("pre_expr -> post_expr .");
                parsePostExpr();
                break;
        }
    }

    private void parsePostExpr() {
        dump("post_expr -> atom_expr post_expr2 .");
        parseAtomExpr();
        parsePostExpr2();
    }

    private void parsePostExpr2() {
        switch (check()) {
            case OP_LBRACKET:
                dump("post_expr2 -> '[' expr ']' post_expr2 .");
                skip();
                parseExpr();

                // TODO: preveri
                if (check() == TokenType.OP_RBRACKET)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ']' v expressionu!");

                parsePostExpr2();
                break;
            default:
                dump("post_expr2 -> .");
                break;
        }
    }

    private void parseAtomExpr() {
        switch (check()) {
            case C_LOGICAL:
                dump("atom_expr -> log_constant .");
                skip();
                break;
            case C_INTEGER:
                dump("atom_expr -> int_constant .");
                skip();
                break;
            case C_STRING:
                dump("atom_expr -> str_constant .");
                skip();
                break;
            case IDENTIFIER:
                dump("atom_expr -> id atom_expr2 .");
                skip();
                parseAtomExpr2();
                break;
            case OP_LPARENT:
                dump("atom_expr -> '(' exprs ')' .");
                skip();
                parseExprs();
                if (check() == TokenType.OP_RPARENT)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ')' v atom expressionu!");
                break;
            case OP_LBRACE:
                dump("atom_expr -> '{' atom_expr3 .");
                skip();
                parseAtomExpr3();
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa atom expressiona!");
        }
    }

    private void parseAtomExpr2() {
        switch (check()) {
            case OP_LPARENT:
                dump("atom_expr2 -> '(' exprs ')' .");
                skip();
                parseExprs();
                // RPARENT pogledamo v parseExprs
                break;
            default:
                dump("atom_expr2 -> .");
                break;
        }
    }

    private void parseAtomExpr3() {
        switch (check()) {
            case KW_IF:
                dump("atom_expr3 -> if expr then expr atom_expr4 .");
                skip();
                parseExpr();

                if (check() == TokenType.KW_THEN)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka 'then' v if stavku!");

                parseExpr();
                parseAtomExpr4();

                break;
            case KW_WHILE:
                dump("atom_expr3 -> while expr ':' expr '}' .");
                skip();

                parseExpr();
                if (check() == TokenType.OP_COLON)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ':' v while stavku!");

                parseExpr();

                if (check() == TokenType.OP_RBRACE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '}' v while stavku!");
                break;
            case KW_FOR:
                dump("atom_expr3 -> for id '=' expr ',' expr ',' expr ':' expr '}' .");
                skip();

                if (check() == TokenType.IDENTIFIER)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka identifier v for stavku!");

                if (check() == TokenType.OP_ASSIGN)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '=' v for stavku!");

                parseExpr();

                if (check() == TokenType.OP_COMMA)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ',' v for stavku!");

                parseExpr();

                if (check() == TokenType.OP_COMMA)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ',' v for stavku!");

                parseExpr();

                if (check() == TokenType.OP_COLON)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ':' v for stavku!");

                parseExpr();

                if (check() == TokenType.OP_RBRACE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '}' v for stavku!");

                break;
            default:
                dump("atom_expr3 -> expr '=' expr '}' .");

                parseExpr();

                if (check() == TokenType.OP_ASSIGN)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '=' v atom expressionu!");

                parseExpr();

                if (check() == TokenType.OP_RBRACE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '}' v atom expressionu!");
                break;
        }
    }

    private void parseAtomExpr4() {
        switch (check()) {
            case OP_RBRACE:
                dump("atom_expr4 -> '}' .");
                skip();
                break;
            case KW_ELSE:
                dump("atom_expr4 -> else expr '}' .");
                skip();
                parseExpr();
                // RBRACE preverimo v parseExpr
                break;
            default:
                Report.error(getSymbol().position, "Nepravilno zaključen if stavek!");
        }
    }

    private void parseExprs() {
        dump("exprs -> expr exprs2 .");
        parseExpr();
        parseExprs2();
    }

    private void parseExprs2() {
        switch (check()) {
            case OP_COMMA:
                dump("exprs2 -> ',' expr exprs2 .");
                skip();
                parseExpr();
                parseExprs2();
                break;
            default:
                dump("exprs2 -> .");
                break;
        }
    }


    private void parseVarDef() {
        dump("var_def -> var id ':' type .");
        if (check() == TokenType.IDENTIFIER)
            skip();
        else
            Report.error(getSymbol().position, "Manjka identifier pri definiciji spremenljivke!");

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji spremenljivke!");

        parseType();

    }

    /**
     * Izpiše produkcijo na izhodni tok.
     */
    private void dump(String production) {
        if (productionsOutputStream.isPresent()) {
            productionsOutputStream.get().println(production);
        }
    }
}

