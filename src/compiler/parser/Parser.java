/**
 * @Author: turk
 * @Description: Sintaksni analizator.
 */

package compiler.parser;

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
import static compiler.lexer.TokenType.*;
import static common.RequireNonNull.requireNonNull;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
<<<<<<< HEAD
<<<<<<< HEAD
=======
import static common.RequireNonNull.requireNonNull;

import java.io.PrintStream;
import java.util.*;
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main

import common.Report;
import compiler.lexer.Position;
import compiler.lexer.Symbol;
import compiler.lexer.TokenType;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
import compiler.parser.ast.Ast;
import compiler.parser.ast.def.*;
import compiler.parser.ast.expr.*;
import compiler.parser.ast.type.Atom;
import compiler.parser.ast.type.Type;
import compiler.parser.ast.type.TypeName;
import compiler.parser.ast.type.Array;
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main

public class Parser {
    /**
     * Seznam leksikalnih simbolov.
     */
    private final List<Symbol> symbols;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
    private Stack<Symbol> stack;
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
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
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
    public Ast parse() {
        var ast = parseSource();
        return ast;
    }

    private Ast parseSource() {
        dump("source -> defs .");
        var defs = parseDefs();
        return defs;
    }

    private Defs parseDefs() {
        List<Def> definitions = new ArrayList<Def>();
        dump("defs -> def defs2 .");

        var def = parseDef();
        definitions.add(def);

        var defs = parseDefs2();
        assert defs != null;
        definitions.addAll(defs.definitions);

        assert def != null;
        return new Defs(new Position(def.position.start, defs.position.end), definitions);
    }

    private Def parseDef() {
        switch (check()) {
            case KW_TYP:
                dump("def -> type_def .");
                return parseTypeDef();
            case KW_FUN:
                dump("def -> fun_def .");
                return parseFunDef();
            case KW_VAR:
                dump("def -> var_def .");
                return parseVarDef();
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa definicije!");
                return null;
        }
    }

    private Defs parseDefs2() {
        List<Def> definitions = new ArrayList<Def>();
        Position.Location start;
        Position.Location end;

<<<<<<< HEAD
<<<<<<< HEAD
=======
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
                parseTypeDef();
                break;
            case KW_FUN:
                dump("def -> fun_def .");
                parseFunDef();
                break;
            case KW_VAR:
                dump("def -> var_def .");
                parseVarDef();
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa definicije!");
        }
    }

    private void parseDefs2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_SEMICOLON:
                dump("defs2 -> ';' def defs2 .");
                skip();
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                var def = parseDef();
                definitions.add(def);
                start = def.position.start;
                var defs = parseDefs2();
                assert defs != null;
                definitions.addAll(defs.definitions);
                end = defs.position.end;
                break;
            case EOF:
                dump("defs2 -> .");
<<<<<<< HEAD
<<<<<<< HEAD
                // zadnja pozicija ni EOF, ampak simbol prej
                start = this.symbols.get(this.symbols.size() - 2).position.start;
                end = this.symbols.get(this.symbols.size() - 2).position.end;
=======
                start = getSymbol().position.start;
                end = getSymbol().position.end;
                end = new Position.Location(end.line, end.column - 1); // ne štejemo EOF
>>>>>>> repo3/main
=======
                // zadnja pozicija ni EOF, ampak simbol prej
                start = this.symbols.get(this.symbols.size() - 2).position.start;
                end = this.symbols.get(this.symbols.size() - 2).position.end;
>>>>>>> repo4/main
                skip();
                break;
            case OP_RBRACE:
                dump("defs2 -> .");
<<<<<<< HEAD
<<<<<<< HEAD
                // Defs se zaključi s simbolom prej, ne z '}'
                start = this.symbols.get(this.pozicijaSimbola - 1).position.start;
                end = this.symbols.get(this.pozicijaSimbola - 1).position.end;
=======
                start = getSymbol().position.start;
                end = getSymbol().position.end;
>>>>>>> repo3/main
=======
                // Defs se zaključi s simbolom prej, ne z '}'
                start = this.symbols.get(this.pozicijaSimbola - 1).position.start;
                end = this.symbols.get(this.pozicijaSimbola - 1).position.end;
>>>>>>> repo4/main
                break;
            default:
                Report.error(getSymbol().position, "Manjka ';' med ločnicami definicij ali '}' na koncu!");
                return null;
        }

        return new Defs(new Position(start, end), definitions);
    }

    private TypeDef parseTypeDef() {
        String name = null;
        dump("type_def -> typ id ':' type .");

        // typ
        Position.Location start = getSymbol().position.start;
        skip();

        if (check() == TokenType.IDENTIFIER) {
            name = getSymbol().lexeme;
            skip();
        } else {
            Report.error(getSymbol().position, "Manjka identifier pri definiciji tipa!");
        }
<<<<<<< HEAD
<<<<<<< HEAD
=======
                parseDef();
                parseDefs2();
                break;
            case EOF:
            case OP_RBRACE:
                dump("defs2 -> .");
                skip();
                break;
            default:
                Report.error(getSymbol().position, "Manjka ';' med ločnicami definicij ali '}' na koncu!");
        }
    }

    private void parseTypeDef() {
        dump("type_def -> typ id ':' type .");
        skip(); // typ
        if (check() == TokenType.IDENTIFIER)
            skip();
        else
            Report.error(getSymbol().position, "Manjka identifier pri definiciji tipa!");
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji tipa!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        var type = parseType();
        return new TypeDef(new Position(start, type.position.end), name, type);
    }

    private Type parseType() {
        Position pos;
        switch (check()) {
            case IDENTIFIER:
                dump("type -> id .");
                String name = getSymbol().lexeme;
                pos = getSymbol().position;
                skip();
                return new TypeName(pos, name);
            case AT_LOGICAL:
                dump("type -> logical .");
                pos = getSymbol().position;
                skip();
                return Atom.LOG(pos);
            case AT_INTEGER:
                dump("type -> integer .");
                pos = getSymbol().position;
                skip();
                return Atom.INT(pos);
            case AT_STRING:
                dump("type -> string .");
                pos = getSymbol().position;
                skip();
                return Atom.STR(pos);
            case KW_ARR:
                int size = 0;
                dump("type -> arr '['int_const']' type .");

                // arr
                Position.Location start = getSymbol().position.start;
                skip();

<<<<<<< HEAD
<<<<<<< HEAD
=======
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
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                if (check() == TokenType.OP_LBRACKET)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '[' pri definiciji arraya!");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                if (check() == TokenType.C_INTEGER) {
                    size = Integer.parseInt(getSymbol().lexeme);
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka konstanta integer pri definiciji arraya!");
                }
<<<<<<< HEAD
<<<<<<< HEAD
=======
                if (check() == TokenType.C_INTEGER)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka konstanta integer pri definiciji arraya!");
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                if (check() == TokenType.OP_RBRACKET)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ']' pri definiciji arraya!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                var type = parseType();
                assert type != null;
                Position.Location end = type.position.end;

                return new Array(new Position(start, end), size, type);
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa tipa!");
        }
        return null;
    }

    private FunDef parseFunDef() {
        String name = null;
        dump("fun_def -> fun id '('params')' ':' type '=' expr .");

        // fun
        Position.Location start = getSymbol().position.start;
        skip();

        if (check() == TokenType.IDENTIFIER) {
            name = getSymbol().lexeme;
            skip();
        } else {
            Report.error(getSymbol().position, "Manjka identifier pri definiciji tipa!");
        }
<<<<<<< HEAD
<<<<<<< HEAD
=======
                parseType();
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa tipa!");
        }
    }

    private void parseFunDef() {
        dump("fun_def -> fun id '('params')' ':' type '=' expr .");

        skip(); // fun

        if (check() == TokenType.IDENTIFIER)
            skip();
        else
            Report.error(getSymbol().position, "Manjka identifier pri definiciji tipa!");
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        if (check() == TokenType.OP_LPARENT)
            skip();
        else
            Report.error(getSymbol().position, "Manjka '(' pri definiciji funkcije za parametre!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
        var params = parseParams();
=======
        parseParams();
>>>>>>> repo2/main
=======
        var params = parseParams();
>>>>>>> repo3/main
=======
        var params = parseParams();
>>>>>>> repo4/main

        // RPARENT skippamo v params2

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji funkcije za določitev tipa!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
        var type = parseType();
=======
        parseType();
>>>>>>> repo2/main
=======
        var type = parseType();
>>>>>>> repo3/main
=======
        var type = parseType();
>>>>>>> repo4/main

        if (check() == TokenType.OP_ASSIGN)
            skip();
        else
            Report.error(getSymbol().position, "Manjka '=' pri definiciji funkcije!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        var body = parseExpr();

        assert body != null;
        return new FunDef(new Position(start, body.position.end), name, params, type, body);
    }

    private List<FunDef.Parameter> parseParams() {
        List<FunDef.Parameter> parameters = new ArrayList<>();
        dump("params -> param params2 .");

        FunDef.Parameter param = parseParam();
        parameters.add(param);

        List<FunDef.Parameter> params2 = parseParams2();
        parameters.addAll(params2);

        return parameters;
    }

    private FunDef.Parameter parseParam() {
        Position.Location start = null;
        String name = null;
        dump("param -> id ':' type .");
        if (check() == TokenType.IDENTIFIER) {
            start = getSymbol().position.start;
            name = getSymbol().lexeme;
            skip();
        } else {
            Report.error(getSymbol().position, "Manjka identfier pri definiciji parametra!");
        }
<<<<<<< HEAD
<<<<<<< HEAD
=======
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
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji parametra!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        Type type = parseType();

        assert type != null;
        return new FunDef.Parameter(new Position(start, type.position.end), name, type);
    }

    private List<FunDef.Parameter> parseParams2() {
        List<FunDef.Parameter> parameters = new ArrayList<>();
<<<<<<< HEAD
<<<<<<< HEAD
=======
        parseType();
    }

    private void parseParams2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_COMMA:
                dump("params2 -> ',' param params2 .");
                skip();
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                FunDef.Parameter param = parseParam();
                parameters.add(param);
                List<FunDef.Parameter> params = parseParams2();
                parameters.addAll(params);
<<<<<<< HEAD
<<<<<<< HEAD
=======
                parseParam();
                parseParams2();
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                break;
            case OP_RPARENT:
                dump("params2 -> .");
                skip();
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna definicija parametrov!");
        }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        return parameters;
    }

    private Expr parseExpr() {
        dump("expr -> logical_ior_expr expr2 .");
        var ior = parseLogicalIorExpr();
        var expr2 = parseExpr2(ior);

        if (expr2 == null)
            return ior;
        else
            return expr2;
    }

    private Where parseExpr2(Expr ior) {
        Position.Location end;
<<<<<<< HEAD
<<<<<<< HEAD
=======
    }

    private void parseExpr() {
        dump("expr -> logical_ior_expr expr2 .");
        parseLogicalIorExpr();
        parseExpr2();
    }

    private void parseExpr2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_LBRACE:
                dump("expr2 -> '{' WHERE defs '}' .");
                skip();
                if (check() == TokenType.KW_WHERE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka WHERE v expressionu!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                var defs = parseDefs();

                // RBRACE v defs2 (defs -> def defs2), ne skipamo v defs2, ampak tu
                if (check() == TokenType.OP_RBRACE) {
                    end = getSymbol().position.end;
                    skip();
                    return new Where(new Position(ior.position.start, end), ior, defs);
                } else {
                    Report.error(getSymbol().position, "Manjka '}' v expressionu!");
                }
<<<<<<< HEAD
<<<<<<< HEAD
=======
                parseDefs();

                // RBRACE skippamo v defs2 (defs -> def defs2)
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                break;
            case OP_SEMICOLON:
            case OP_COLON:
            case OP_RBRACKET:
            case OP_RPARENT:
            case OP_ASSIGN:
            case OP_COMMA:
            case OP_RBRACE:
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("expr2 -> .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                return null;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v expressionu!");
        }
        return null;
    }

    private Expr parseLogicalIorExpr() {
        dump("logical_ior_expr -> logical_and_expr logical_ior_expr2 .");
        var andExpr = parseLogicalAndExpr();
        return parseLogicalIorExpr2(andExpr);
    }

    private Expr parseLogicalIorExpr2(Expr andExprLeft) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v expressionu!");
        }
    }

    private void parseLogicalIorExpr() {
        dump("logical_ior_expr -> logical_and_expr logical_ior_expr2 .");
        parseLogicalAndExpr();
        parseLogicalIorExpr2();
    }

    private void parseLogicalIorExpr2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_OR:
                dump("logical_ior_expr2 -> '|' logical_and_expr logical_ior_expr2 .");
                skip();
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                var andExprRight = parseLogicalAndExpr();
                var bin = new Binary(new Position(andExprLeft.position.start, andExprRight.position.end), andExprLeft, Binary.Operator.OR, andExprRight);
                return parseLogicalIorExpr2(bin);
=======
                parseLogicalAndExpr();
                parseLogicalIorExpr2();
>>>>>>> repo2/main
=======
                var andExprRight = parseLogicalAndExpr();
                var bin = new Binary(new Position(andExprLeft.position.start, andExprRight.position.end), andExprLeft, Binary.Operator.OR, andExprRight);
                return parseLogicalIorExpr2(bin);
>>>>>>> repo3/main
=======
                var andExprRight = parseLogicalAndExpr();
                var bin = new Binary(new Position(andExprLeft.position.start, andExprRight.position.end), andExprLeft, Binary.Operator.OR, andExprRight);
                return parseLogicalIorExpr2(bin);
>>>>>>> repo4/main
            case OP_SEMICOLON:
            case OP_COLON:
            case OP_RBRACKET:
            case OP_RPARENT:
            case OP_ASSIGN:
            case OP_COMMA:
            case OP_LBRACE:
            case OP_RBRACE:
            case KW_THEN:
            case KW_ELSE:
<<<<<<< HEAD
<<<<<<< HEAD
            case EOF:
                dump("logical_ior_expr2 -> .");
<<<<<<< HEAD
=======
                dump("logical_ior_expr2 -> .");
                return andExprLeft;
            case EOF:
                dump("logical_ior_expr2 -> .");
                // Position.Location end =
>>>>>>> repo3/main
=======
            case EOF:
                dump("logical_ior_expr2 -> .");
>>>>>>> repo4/main
                return andExprLeft;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v logical ior expressionu!");
        }
        return null;
    }

    private Expr parseLogicalAndExpr() {
        dump("logical_and_expr -> compare_expr logical_and_expr2 .");
        var compareExpr = parseCompareExpr();
        return parseLogicalAndExpr2(compareExpr);
    }

    private Expr parseLogicalAndExpr2(Expr compareExprLeft) {
<<<<<<< HEAD
<<<<<<< HEAD
=======
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v logical ior expressionu!");
        }
    }

    private void parseLogicalAndExpr() {
        dump("logical_and_expr -> compare_expr logical_and_expr2 .");
        parseCompareExpr();
        parseLogicalAndExpr2();
    }

    private void parseLogicalAndExpr2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_AND:
                dump("logical_and_expr2 -> '&' compare_expr logical_and_expr2 .");
                skip();
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main

                // TODO: preveri pozicije
                var compareExprRight = parseCompareExpr();
                var bin = new Binary(new Position(compareExprLeft.position.start, compareExprRight.position.end), compareExprLeft, Binary.Operator.AND, compareExprRight);
                return parseLogicalAndExpr2(bin);

<<<<<<< HEAD
<<<<<<< HEAD
=======
                parseCompareExpr();
                parseLogicalAndExpr2();
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
            case OP_SEMICOLON:
            case OP_COLON:
            case OP_RBRACKET:
            case OP_RPARENT:
            case OP_ASSIGN:
            case OP_COMMA:
            case OP_LBRACE:
            case OP_RBRACE:
            case OP_OR:
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("logical_and_expr2 -> .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                return compareExprLeft;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v logical and expressionu!");
        }
        return null;
    }

    private Expr parseCompareExpr() {
        dump("compare_expr -> add_expr compare_expr2 .");
        var addExpr = parseAddExpr();
        return parseCompareExpr2(addExpr);
    }

    private Expr parseCompareExpr2(Expr addExprLeft) {
        Binary.Operator op = null;
<<<<<<< HEAD
<<<<<<< HEAD
=======
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v logical and expressionu!");
        }
    }

    private void parseCompareExpr() {
        dump("compare_expr -> add_expr compare_expr2 .");
        parseAddExpr();
        parseCompareExpr2();
    }

    private void parseCompareExpr2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_EQ:
            case OP_NEQ:
            case OP_LEQ:
            case OP_GEQ:
            case OP_LT:
            case OP_GT:
                dump("compare_expr2 -> '" + getSymbol().lexeme + "' add_expr .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                switch (check()) {
                    case OP_EQ -> op = Binary.Operator.EQ;
                    case OP_NEQ -> op = Binary.Operator.NEQ;
                    case OP_LEQ -> op = Binary.Operator.LEQ;
                    case OP_GEQ -> op = Binary.Operator.GEQ;
                    case OP_LT -> op = Binary.Operator.LT;
                    case OP_GT -> op = Binary.Operator.GT;
                }
                skip();

                var addExprRight = parseAddExpr();
                return new Binary(new Position(addExprLeft.position.start, addExprRight.position.end), addExprLeft, op, addExprRight);

<<<<<<< HEAD
<<<<<<< HEAD
=======
                skip();
                parseAddExpr();
                break;
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
            case OP_SEMICOLON:
            case OP_COLON:
            case OP_RBRACKET:
            case OP_RPARENT:
            case OP_ASSIGN:
            case OP_COMMA:
            case OP_LBRACE:
            case OP_RBRACE:
            case OP_OR:
            case OP_AND:
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("compare_expr2 -> .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                return addExprLeft;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v compare expressionu!");
        }
        return null;
    }

    private Expr parseAddExpr() {
        dump("add_expr -> mul_expr add_expr2 .");
        var mulExpr = parseMulExpr();
        return parseAddExpr2(mulExpr);
    }

    private Expr parseAddExpr2(Expr mulExprLeft) {
        Binary.Operator op = null;
<<<<<<< HEAD
<<<<<<< HEAD
=======
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v compare expressionu!");
        }
    }

    private void parseAddExpr() {
        dump("add_expr -> mul_expr add_expr2 .");
        parseMulExpr();
        parseAddExpr2();
    }

    private void parseAddExpr2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_ADD:
            case OP_SUB:
                dump("add_expr2 -> '" + getSymbol().lexeme + "' mul_expr add_expr2 .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                switch (check()) {
                    case OP_ADD -> op = Binary.Operator.ADD;
                    case OP_SUB -> op = Binary.Operator.SUB;
                }
                skip();
                var mulExprRight = parseMulExpr();
                var bin = new Binary(new Position(mulExprLeft.position.start, mulExprRight.position.end), mulExprLeft, op, mulExprRight);
                return parseAddExpr2(bin);
<<<<<<< HEAD
<<<<<<< HEAD
=======
                skip();
                parseMulExpr();
                parseAddExpr2();
                break;
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
            case OP_SEMICOLON:
            case OP_COLON:
            case OP_RBRACKET:
            case OP_RPARENT:
            case OP_ASSIGN:
            case OP_COMMA:
            case OP_LBRACE:
            case OP_RBRACE:
            case OP_OR:
            case OP_AND:
            case OP_EQ:
            case OP_NEQ:
            case OP_LEQ:
            case OP_GEQ:
            case OP_LT:
            case OP_GT:
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("add_expr2 -> .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                return mulExprLeft;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v additive expressionu!");
        }
        return null;
    }

    private Expr parseMulExpr() {
        dump("mul_expr -> pre_expr mul_expr2 .");
        var preExpr = parsePreExpr();
        return parseMulExpr2(preExpr);
    }

    private Expr parseMulExpr2(Expr preExprLeft) {
        Binary.Operator op = null;
<<<<<<< HEAD
<<<<<<< HEAD
=======
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v additive expressionu!");
        }
    }

    private void parseMulExpr() {
        dump("mul_expr -> pre_expr mul_expr2 .");
        parsePreExpr();
        parseMulExpr2();
    }

    private void parseMulExpr2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_MUL:
            case OP_DIV:
            case OP_MOD:
                dump("mul_expr2 -> '" + getSymbol().lexeme + "' pre_expr mul_expr2 .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                switch (check()) {
                    case OP_MUL -> op = Binary.Operator.MUL;
                    case OP_DIV -> op = Binary.Operator.DIV;
                    case OP_MOD -> op = Binary.Operator.MOD;
                }
                skip();
                var preExprRight = parsePreExpr();
                var bin = new Binary(new Position(preExprLeft.position.start, preExprRight.position.end), preExprLeft, op, preExprRight);
                return parseMulExpr2(bin);
<<<<<<< HEAD
<<<<<<< HEAD
=======
                skip();
                parsePreExpr();
                parseMulExpr2();
                break;
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
            case OP_SEMICOLON:
            case OP_COLON:
            case OP_RBRACKET:
            case OP_RPARENT:
            case OP_ASSIGN:
            case OP_COMMA:
            case OP_LBRACE:
            case OP_RBRACE:
            case OP_OR:
            case OP_AND:
            case OP_EQ:
            case OP_NEQ:
            case OP_LEQ:
            case OP_GEQ:
            case OP_LT:
            case OP_GT:
            case OP_ADD:
            case OP_SUB:
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("mul_expr2 -> .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                return preExprLeft;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v multiplicative expressionu!");
        }
        return null;
    }

    private Expr parsePreExpr() {
<<<<<<< HEAD
<<<<<<< HEAD
=======
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v multiplicative expressionu!");
        }
    }

    private void parsePreExpr() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_ADD:
            case OP_SUB:
            case OP_NOT:
                dump("pre_expr -> '" + getSymbol().lexeme + "'pre_expr .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                var start = getSymbol().position.start;
                Unary.Operator op = null;
                switch (check()) {
                    case OP_ADD -> op = Unary.Operator.ADD;
                    case OP_SUB -> op = Unary.Operator.SUB;
                    case OP_NOT -> op = Unary.Operator.NOT;
                }
                skip();
                var expr = parsePreExpr();
                assert expr != null;
                return new Unary(new Position(start, expr.position.end), expr, op);
<<<<<<< HEAD
<<<<<<< HEAD
=======
                skip();
                parsePreExpr();
                break;
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
            case IDENTIFIER:
            case OP_LPARENT:
            case OP_LBRACE:
            case C_LOGICAL:
            case C_INTEGER:
            case C_STRING:
                dump("pre_expr -> post_expr .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                return parsePostExpr();
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v prefix expressionu!");
        }
        return null;
    }

    private Expr parsePostExpr() {
        dump("post_expr -> atom_expr post_expr2 .");
        var atomExpr = parseAtomExpr();
        return parsePostExpr2(atomExpr);
    }

    private Expr parsePostExpr2(Expr atomExpr) {
        Position.Location end = null;
<<<<<<< HEAD
<<<<<<< HEAD
=======
                parsePostExpr();
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v prefix expressionu!");
        }
    }

    private void parsePostExpr() {
        dump("post_expr -> atom_expr post_expr2 .");
        parseAtomExpr();
        parsePostExpr2();
    }

    private void parsePostExpr2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_LBRACKET:
                dump("post_expr2 -> '[' expr ']' post_expr2 .");
                skip();
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                var expr = parseExpr();

                // TODO: preveri
                if (check() == TokenType.OP_RBRACKET) {
                    end = getSymbol().position.end;
                    skip();
                    var bin = new Binary(new Position(atomExpr.position.start, end), atomExpr, Binary.Operator.ARR, expr);
                    return parsePostExpr2(bin);
                } else {
                    Report.error(getSymbol().position, "Manjka ']' v expressionu!");
                }
<<<<<<< HEAD
<<<<<<< HEAD
=======
                parseExpr();

                // TODO: preveri
                if (check() == TokenType.OP_RBRACKET)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ']' v expressionu!");

                parsePostExpr2();
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                break;
            case OP_SEMICOLON:
            case OP_COLON:
            case OP_RBRACKET:
            case OP_RPARENT:
            case OP_ASSIGN:
            case OP_COMMA:
            case OP_LBRACE:
            case OP_RBRACE:
            case OP_OR:
            case OP_AND:
            case OP_EQ:
            case OP_NEQ:
            case OP_LEQ:
            case OP_GEQ:
            case OP_LT:
            case OP_GT:
            case OP_ADD:
            case OP_SUB:
            case OP_MUL:
            case OP_DIV:
            case OP_MOD:
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("post_expr2 -> .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                return atomExpr;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v postfix expressionu!");
        }
        return null;
    }

    private Expr parseAtomExpr() {
        Position pos;
        String val;
        Atom.Type type;
        Position.Location start, end = null;
        Block exprs;
        switch (check()) {
            case C_LOGICAL:
                dump("atom_expr -> log_constant .");
                pos = getSymbol().position;
                val = getSymbol().lexeme;
                type = Atom.Type.LOG;
                skip();
                return new Literal(pos, val, type);
            case C_INTEGER:
                dump("atom_expr -> int_constant .");
                pos = getSymbol().position;
                val = getSymbol().lexeme;
                type = Atom.Type.INT;
                skip();
                return new Literal(pos, val, type);
            case C_STRING:
                dump("atom_expr -> str_constant .");
                pos = getSymbol().position;
                val = getSymbol().lexeme;
                type = Atom.Type.STR;
                skip();
                return new Literal(pos, val, type);
            case IDENTIFIER:
                dump("atom_expr -> id atom_expr2 .");
                pos = getSymbol().position;
                val = getSymbol().lexeme;
                var id = new Name(pos, val);
                skip();

                return parseAtomExpr2(id);
            case OP_LPARENT:
                dump("atom_expr -> '(' exprs ')' .");
                start = getSymbol().position.start;
                skip();
                exprs = parseExprs();
                if (check() == TokenType.OP_RPARENT) {
                    end = getSymbol().position.end;
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka ')' v atom expressionu!");
                }
                return new Block(new Position(start, end), exprs.expressions);
            case OP_LBRACE:
                dump("atom_expr -> '{' atom_expr3 .");
                // '{' skipamo v parseAtomExpr3
                return parseAtomExpr3();
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa atom expressiona!");
        }
        return null;
    }

    private Expr parseAtomExpr2(Name id) {
        Position.Location end = null;
<<<<<<< HEAD
<<<<<<< HEAD
=======
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v postfix expressionu!");

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
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_LPARENT:
                dump("atom_expr2 -> '(' exprs ')' .");
                skip();
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                var exprs = parseExprs();
                if (check() == TokenType.OP_RPARENT) {
                    end = getSymbol().position.end;
                    skip();
                    return new Call(new Position(id.position.start, end), exprs.expressions, id.name);
                } else {
                    Report.error(getSymbol().position, "Manjka ')' v atom expressionu!");
                }
<<<<<<< HEAD
<<<<<<< HEAD
=======
                parseExprs();
                if (check() == TokenType.OP_RPARENT)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ')' v atom expressionu!");
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                // TODO: RPARENT pogledamo v parseExprs?
                break;
            case OP_SEMICOLON:
            case OP_COLON:
            case OP_LBRACKET:
            case OP_RBRACKET:
            case OP_RPARENT:
            case OP_ASSIGN:
            case OP_COMMA:
            case OP_LBRACE:
            case OP_RBRACE:
            case OP_OR:
            case OP_AND:
            case OP_EQ:
            case OP_NEQ:
            case OP_LEQ:
            case OP_GEQ:
            case OP_LT:
            case OP_GT:
            case OP_ADD:
            case OP_SUB:
            case OP_MUL:
            case OP_DIV:
            case OP_MOD:
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("atom_expr2 -> .");
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                return id;
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa atom expressiona!");
        }
        return null;
    }

    private Expr parseAtomExpr3() {
        Position.Location end = null;
        Expr condition, thenExpression, body;

        Position.Location start = getSymbol().position.start;
        skip(); // {
        switch (check()) {
            case KW_IF:
                dump("atom_expr3 -> if expr then expr atom_expr4 .");
                skip(); // if
                condition = parseExpr();

                if (check() == TokenType.KW_THEN) {
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka 'then' v if stavku!");
                }

                thenExpression = parseExpr();
                var ifExpr = new IfThenElse(new Position(start, thenExpression.position.end), condition, thenExpression);
                return parseAtomExpr4(ifExpr);


            case KW_WHILE:
                dump("atom_expr3 -> while expr ':' expr '}' .");
                skip(); // while

                condition = parseExpr();
<<<<<<< HEAD
<<<<<<< HEAD
=======
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa atom expressiona!");
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
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                if (check() == TokenType.OP_COLON)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ':' v while stavku!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                body = parseExpr();

                if (check() == TokenType.OP_RBRACE) {
                    end = getSymbol().position.end;
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka '}' v while stavku!");
                }

                return new While(new Position(start, end), condition, body);
            case KW_FOR:
                dump("atom_expr3 -> for id '=' expr ',' expr ',' expr ':' expr '}' .");
                skip(); // for

                Name counter = null;
                if (check() == TokenType.IDENTIFIER) {
                    counter = new Name(getSymbol().position, getSymbol().lexeme);
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka identifier v for stavku!");
                }
<<<<<<< HEAD
<<<<<<< HEAD
=======
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
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main

                if (check() == TokenType.OP_ASSIGN)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '=' v for stavku!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                var low = parseExpr();
=======
                parseExpr();
>>>>>>> repo2/main
=======
                var low = parseExpr();
>>>>>>> repo3/main
=======
                var low = parseExpr();
>>>>>>> repo4/main

                if (check() == TokenType.OP_COMMA)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ',' v for stavku!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                var high = parseExpr();
=======
                parseExpr();
>>>>>>> repo2/main
=======
                var high = parseExpr();
>>>>>>> repo3/main
=======
                var high = parseExpr();
>>>>>>> repo4/main

                if (check() == TokenType.OP_COMMA)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ',' v for stavku!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                var step = parseExpr();
=======
                parseExpr();
>>>>>>> repo2/main
=======
                var step = parseExpr();
>>>>>>> repo3/main
=======
                var step = parseExpr();
>>>>>>> repo4/main

                if (check() == TokenType.OP_COLON)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ':' v for stavku!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                body = parseExpr();

                if (check() == TokenType.OP_RBRACE) {
                    end = getSymbol().position.end;
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka '}' v for stavku!");
                }

                return new For(new Position(start, end), counter, low, high, step, body);

            case IDENTIFIER:
<<<<<<< HEAD
<<<<<<< HEAD
            case OP_LPARENT:
=======
                parseExpr();

                if (check() == TokenType.OP_RBRACE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '}' v for stavku!");

                break;
            case IDENTIFIER:
            case OP_LBRACKET:
>>>>>>> repo2/main
=======
            case OP_LBRACKET:
>>>>>>> repo3/main
=======
            case OP_LPARENT:
>>>>>>> repo4/main
            case OP_LBRACE:
            case OP_ADD:
            case OP_SUB:
            case OP_NOT:
            case C_LOGICAL:
            case C_INTEGER:
            case C_STRING:
                dump("atom_expr3 -> expr '=' expr '}' .");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                var expr1 = parseExpr();
=======
                parseExpr();
>>>>>>> repo2/main
=======
                var expr1 = parseExpr();
>>>>>>> repo3/main
=======
                var expr1 = parseExpr();
>>>>>>> repo4/main

                if (check() == TokenType.OP_ASSIGN)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '=' v atom expressionu!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                var expr2 = parseExpr();

                if (check() == TokenType.OP_RBRACE) {
                    end = getSymbol().position.end;
                    skip();
                    return new Binary(new Position(start, end), expr1, Binary.Operator.ASSIGN, expr2);
                } else {
                    Report.error(getSymbol().position, "Manjka '}' v atom expressionu!");
                }
<<<<<<< HEAD
<<<<<<< HEAD
=======
                parseExpr();

                if (check() == TokenType.OP_RBRACE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '}' v atom expressionu!");
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa atom expressiona!");
        }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        return null;
    }

    private Expr parseAtomExpr4(IfThenElse atomExpr3) {
        Position pos;
        switch (check()) {
            case OP_RBRACE:
                dump("atom_expr4 -> '}' .");
                pos = getSymbol().position;
                skip();
                return new IfThenElse(new Position(atomExpr3.position.start, pos.end), atomExpr3.condition, atomExpr3.thenExpression);
            case KW_ELSE:
                dump("atom_expr4 -> else expr '}' .");
                skip();
                var elseExpr = parseExpr();

                if (check() == TokenType.OP_RBRACE) {
                    pos = getSymbol().position;
                    skip();
                    return new IfThenElse(new Position(atomExpr3.position.start, pos.end), atomExpr3.condition, atomExpr3.thenExpression, elseExpr);
                } else {
                    Report.error(getSymbol().position, "Manjka '}' v if-then-else stavku!");
                }
<<<<<<< HEAD
<<<<<<< HEAD
=======
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
                if (check() == TokenType.OP_RBRACE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '}' v if-then-else stavku!");
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                break;
            default:
                Report.error(getSymbol().position, "Nepravilno zaključen if stavek!");
        }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        return null;
    }

    private Block parseExprs() {
        List<Expr> expressions = new ArrayList<>();
        Position.Location start;

        dump("exprs -> expr exprs2 .");
        var expr = parseExpr();
        expressions.add(expr);
        start = expr.position.start;
        var exprs2 = parseExprs2();
        expressions.addAll(exprs2.expressions);

        return new Block(new Position(start, exprs2.position.end), expressions);
    }

    private Block parseExprs2() {
        List<Expr> expressions = new ArrayList<>();
        Position.Location start;

<<<<<<< HEAD
<<<<<<< HEAD
=======
    }

    private void parseExprs() {
        dump("exprs -> expr exprs2 .");
        parseExpr();
        parseExprs2();
    }

    private void parseExprs2() {
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        switch (check()) {
            case OP_COMMA:
                dump("exprs2 -> ',' expr exprs2 .");
                skip();
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
                var expr = parseExpr();
                expressions.add(expr);
                start = expr.position.start;
                var exprs2 = parseExprs2();
                assert exprs2 != null;
                expressions.addAll(exprs2.expressions);
                return new Block(new Position(start, exprs2.position.end), expressions);
            case OP_RPARENT:
                dump("exprs2 -> .");
                return new Block(getSymbol().position, expressions);
<<<<<<< HEAD
<<<<<<< HEAD
=======
                parseExpr();
                parseExprs2();
                break;
            case OP_RPARENT:
                dump("exprs2 -> .");
                break;
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa definicij!");
                break;
        }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
        return null;
    }


    private VarDef parseVarDef() {
        String name = null;
        dump("var_def -> var id ':' type .");

        // var
        Position.Location start = getSymbol().position.start;
        skip();

        if (check() == TokenType.IDENTIFIER) {
            name = getSymbol().lexeme;
            skip();
        } else {
            Report.error(getSymbol().position, "Manjka identifier pri definiciji spremenljivke!");
        }
<<<<<<< HEAD
<<<<<<< HEAD
=======
    }


    private void parseVarDef() {
        dump("var_def -> var id ':' type .");
        skip(); // var

        if (check() == TokenType.IDENTIFIER)
            skip();
        else
            Report.error(getSymbol().position, "Manjka identifier pri definiciji spremenljivke!");
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
=======
>>>>>>> repo4/main

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji spremenljivke!");

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
        var type = parseType();
        assert type != null;
        return new VarDef(new Position(start, type.position.end), name, type);
=======
        parseType();

>>>>>>> repo2/main
=======
        var type = parseType();
        assert type != null;
        return new VarDef(new Position(start, type.position.end), name, type);
>>>>>>> repo3/main
=======
        var type = parseType();
        assert type != null;
        return new VarDef(new Position(start, type.position.end), name, type);
>>>>>>> repo4/main
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

