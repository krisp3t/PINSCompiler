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
                Report.error(getSymbol().position, "Manjka ';' med ločnicami definicij!");
        }
    }

    private void parseTypeDef() {
        dump("type_def -> typ id ':' type .");
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
                skip();
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
        skip();
    }

    private void parseVarDef() {

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

