/**
 * @Author: turk
 * @Description: Sintaksni analizator.
 */

package compiler.parser;

import static compiler.lexer.TokenType.*;
import static common.RequireNonNull.requireNonNull;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import common.Report;
import compiler.lexer.Position;
import compiler.lexer.Symbol;
import compiler.lexer.TokenType;
import compiler.parser.ast.Ast;
import compiler.parser.ast.def.*;
import compiler.parser.ast.expr.*;
import compiler.parser.ast.type.Atom;
import compiler.parser.ast.type.Type;
import compiler.parser.ast.type.TypeName;
import compiler.parser.ast.type.Array;

public class Parser {
    /**
     * Seznam leksikalnih simbolov.
     */
    private final List<Symbol> symbols;
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

        switch (check()) {
            case OP_SEMICOLON:
                dump("defs2 -> ';' def defs2 .");
                skip();
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
                start = getSymbol().position.start;
                end = getSymbol().position.end;
                skip();
                break;
            case OP_RBRACE:
                dump("defs2 -> .");
                start = getSymbol().position.start;
                end = getSymbol().position.end;
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

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji tipa!");

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

                if (check() == TokenType.OP_LBRACKET)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '[' pri definiciji arraya!");
                if (check() == TokenType.C_INTEGER) {
                    size = Integer.parseInt(getSymbol().lexeme);
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka konstanta integer pri definiciji arraya!");
                }
                if (check() == TokenType.OP_RBRACKET)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ']' pri definiciji arraya!");

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
        if (check() == TokenType.OP_LPARENT)
            skip();
        else
            Report.error(getSymbol().position, "Manjka '(' pri definiciji funkcije za parametre!");

        var params = parseParams();

        // RPARENT skippamo v params2

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji funkcije za določitev tipa!");

        var type = parseType();

        if (check() == TokenType.OP_ASSIGN)
            skip();
        else
            Report.error(getSymbol().position, "Manjka '=' pri definiciji funkcije!");

        var body = parseExpr();

        assert body != null;
        return new FunDef(Position.zero(), name, params, type, body);
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

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji parametra!");

        Type type = parseType();

        assert type != null;
        return new FunDef.Parameter(new Position(start, type.position.end), name, type);
    }

    private List<FunDef.Parameter> parseParams2() {
        List<FunDef.Parameter> parameters = new ArrayList<>();
        switch (check()) {
            case OP_COMMA:
                dump("params2 -> ',' param params2 .");
                skip();
                FunDef.Parameter param = parseParam();
                parameters.add(param);
                List<FunDef.Parameter> params = parseParams2();
                parameters.addAll(params);
                break;
            case OP_RPARENT:
                dump("params2 -> .");
                skip();
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna definicija parametrov!");
        }
        return parameters;
    }

    private Block parseExpr() {
        List<Expr> expressions = new ArrayList<>();
        Position.Location start;

        dump("expr -> logical_ior_expr expr2 .");
        var ior = parseLogicalIorExpr();
        expressions.add(ior);
        start = ior.position.start;

        var expr2 = parseExpr2(ior);
        assert expr2 != null;
        expressions.addAll(expr2.expressions);

        return new Block(new Position(start, expr2.position.end), expressions);
    }

    private Where parseExpr2(Expr ior) {
        Position.Location start;
        Position.Location end;
        switch (check()) {
            case OP_LBRACE:
                dump("expr2 -> '{' WHERE defs '}' .");
                start = getSymbol().position.start;
                skip();
                if (check() == TokenType.KW_WHERE)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka WHERE v expressionu!");

                var defs = parseDefs();

                // RBRACE v defs2 (defs -> def defs2), ne skipamo v defs2, ampak tu
                if (check() == TokenType.OP_RBRACE) {
                    skip();
                    end = getSymbol().position.end;
                    return new Where(new Position(start, end), ior, defs);
                } else {
                    Report.error(getSymbol().position, "Manjka '}' v expressionu!");
                }
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
                return null;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v expressionu!");
        }
        return null;
    }

    private Expr parseLogicalIorExpr() {
        dump("logical_ior_expr -> logical_and_expr logical_ior_expr2 .");
        var andExpr = parseLogicalAndExpr();
        var iorExpr2 = parseLogicalIorExpr2();

        return null;
    }

    private Expr parseLogicalIorExpr2() {
        switch (check()) {
            case OP_OR:
                dump("logical_ior_expr2 -> '|' logical_and_expr logical_ior_expr2 .");
                skip();
                var andExpr = parseLogicalAndExpr();
                var iorExpr2 = parseLogicalIorExpr2();
                return null;
            // return new Expr(new Position(andExpr.position.start, iorExpr2.position.end));
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
            case EOF:
                dump("logical_ior_expr2 -> .");
                return null;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v logical ior expressionu!");
        }
        return null;
    }

    private Expr parseLogicalAndExpr() {
        dump("logical_and_expr -> compare_expr logical_and_expr2 .");
        parseCompareExpr();
        parseLogicalAndExpr2();

        return null;
    }

    private void parseLogicalAndExpr2() {
        switch (check()) {
            case OP_AND:
                dump("logical_and_expr2 -> '&' compare_expr logical_and_expr2 .");
                skip();
                parseCompareExpr();
                parseLogicalAndExpr2();
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
        switch (check()) {
            case OP_EQ:
            case OP_NEQ:
            case OP_LEQ:
            case OP_GEQ:
            case OP_LT:
            case OP_GT:
                dump("compare_expr2 -> '" + getSymbol().lexeme + "' add_expr .");
                skip();
                parseAddExpr();
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
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("compare_expr2 -> .");
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
        switch (check()) {
            case OP_ADD:
            case OP_SUB:
                dump("add_expr2 -> '" + getSymbol().lexeme + "' mul_expr add_expr2 .");
                skip();
                parseMulExpr();
                parseAddExpr2();
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
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("add_expr2 -> .");
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
        switch (check()) {
            case OP_MUL:
            case OP_DIV:
            case OP_MOD:
                dump("mul_expr2 -> '" + getSymbol().lexeme + "' pre_expr mul_expr2 .");
                skip();
                parsePreExpr();
                parseMulExpr2();
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
            case KW_THEN:
            case KW_ELSE:
            case EOF:
                dump("mul_expr2 -> .");
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v multiplicative expressionu!");
        }
    }

    private void parsePreExpr() {
        switch (check()) {
            case OP_ADD:
            case OP_SUB:
            case OP_NOT:
                dump("pre_expr -> '" + getSymbol().lexeme + "'pre_expr .");
                skip();
                parsePreExpr();
                break;
            case IDENTIFIER:
            case OP_LPARENT:
            case OP_LBRACE:
            case C_LOGICAL:
            case C_INTEGER:
            case C_STRING:
                dump("pre_expr -> post_expr .");
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
                break;
            default:
                Report.error(getSymbol().position, "Nepričakovan znak v postfix expressionu!");

        }
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
                List<Expr> expressions = new ArrayList<>();
                dump("atom_expr -> id atom_expr2 .");
                pos = getSymbol().position;
                val = getSymbol().lexeme;
                var id = new Name(pos, val);
                expressions.add(id);
                skip();

                var atomExpr2 = parseAtomExpr2();
                expressions.add(atomExpr2);

                return new Block(new Position(id.position.start, atomExpr2.position.end), expressions);
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
                start = getSymbol().position.start;
                skip();
                // TODO: passaj start
                return parseAtomExpr3();
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa atom expressiona!");
        }
        return null;
    }

    private Expr parseAtomExpr2() {
        switch (check()) {
            case OP_LPARENT:
                dump("atom_expr2 -> '(' exprs ')' .");
                skip();
                parseExprs();
                if (check() == TokenType.OP_RPARENT)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ')' v atom expressionu!");
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
                break;
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa atom expressiona!");
        }
    }

    private Expr parseAtomExpr3() {
        Position.Location start;
        Position.Location end = null;
        Expr condition, thenExpression, elseExpression, body;
        switch (check()) {
            case KW_IF:
                dump("atom_expr3 -> if expr then expr atom_expr4 .");
                start = getSymbol().position.start;
                skip();
                condition = parseExpr();

                if (check() == TokenType.KW_THEN) {
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka 'then' v if stavku!");
                }

                thenExpression = parseExpr();
                elseExpression = parseAtomExpr4();

                if (elseExpression != null)
                    return new IfThenElse(new Position(start, elseExpression.position.end), condition, thenExpression, elseExpression);
                else
                    return new IfThenElse(new Position(start, thenExpression.position.end), condition, thenExpression);

            case KW_WHILE:
                dump("atom_expr3 -> while expr ':' expr '}' .");
                start = getSymbol().position.start;
                skip();

                condition = parseExpr();
                if (check() == TokenType.OP_COLON)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ':' v while stavku!");

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
                start = getSymbol().position.start;
                skip();

                Name counter = null;
                if (check() == TokenType.IDENTIFIER) {
                    counter = new Name(getSymbol().position, getSymbol().lexeme);
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka identifier v for stavku!");
                }

                if (check() == TokenType.OP_ASSIGN)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka '=' v for stavku!");

                var low = parseExpr();

                if (check() == TokenType.OP_COMMA)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ',' v for stavku!");

                var high = parseExpr();

                if (check() == TokenType.OP_COMMA)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ',' v for stavku!");

                var step = parseExpr();

                if (check() == TokenType.OP_COLON)
                    skip();
                else
                    Report.error(getSymbol().position, "Manjka ':' v for stavku!");

                body = parseExpr();

                if (check() == TokenType.OP_RBRACE) {
                    end = getSymbol().position.end;
                    skip();
                } else {
                    Report.error(getSymbol().position, "Manjka '}' v for stavku!");
                }

                return new For(new Position(start, end), counter, low, high, step, body);

            case IDENTIFIER:
            case OP_LBRACKET:
            case OP_LBRACE:
            case OP_ADD:
            case OP_SUB:
            case OP_NOT:
            case C_LOGICAL:
            case C_INTEGER:
            case C_STRING:
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
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa atom expressiona!");
        }
        return null;
    }

    private Expr parseAtomExpr4() {
        Position pos;
        switch (check()) {
            case OP_RBRACE:
                dump("atom_expr4 -> '}' .");
                pos = getSymbol().position;
                skip();
                return new Block(pos, new ArrayList<>());
            case KW_ELSE:
                dump("atom_expr4 -> else expr '}' .");
                skip();
                var expr = parseExpr();
                List<Expr> expressions = new ArrayList<>();
                expressions.add(expr);

                if (check() == TokenType.OP_RBRACE) {
                    pos = getSymbol().position;
                    skip();
                    return new Block(pos, expressions);
                } else {
                    Report.error(getSymbol().position, "Manjka '}' v if-then-else stavku!");
                }
                break;
            default:
                Report.error(getSymbol().position, "Nepravilno zaključen if stavek!");
        }
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

        switch (check()) {
            case OP_COMMA:
                dump("exprs2 -> ',' expr exprs2 .");
                skip();
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
            default:
                Report.error(getSymbol().position, "Nepravilna sintaksa definicij!");
                break;
        }
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

        if (check() == TokenType.OP_COLON)
            skip();
        else
            Report.error(getSymbol().position, "Manjka ':' pri definiciji spremenljivke!");

        var type = parseType();
        assert type != null;
        return new VarDef(new Position(start, type.position.end), name, type);
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

