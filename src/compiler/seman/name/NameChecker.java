/**
 * @ Author: turk
 * @ Description: Preverjanje in razreševanje imen.
 */

package compiler.seman.name;

import static common.RequireNonNull.requireNonNull;

import common.Report;
import compiler.common.Visitor;
import compiler.lexer.Position;
import compiler.parser.ast.Ast;
import compiler.parser.ast.def.*;
import compiler.parser.ast.def.FunDef.Parameter;
import compiler.parser.ast.expr.*;
import compiler.parser.ast.type.*;
import compiler.seman.common.NodeDescription;
import compiler.seman.name.env.SymbolTable;
import compiler.seman.name.env.SymbolTable.DefinitionAlreadyExistsException;

import java.util.Optional;

public class NameChecker implements Visitor {
    /**
     * Opis vozlišč, ki jih povežemo z njihovimi
     * definicijami.
     */
    private NodeDescription<Def> definitions;

    /**
     * Simbolna tabela.
     */
    private SymbolTable symbolTable;

    /**
     * Ustvari nov razreševalnik imen.
     */
    public NameChecker(
            NodeDescription<Def> definitions,
            SymbolTable symbolTable
    ) {
        requireNonNull(definitions, symbolTable);
        this.definitions = definitions;
        this.symbolTable = symbolTable;
    }

    @Override
    public void visit(Call call) {
        // Preveri obstoj funkcije
        if (symbolTable.definitionFor(call.name).isEmpty())
            Report.error(call.position, "Funkcija " + call.name + " ni definirana!");
        else {
            Def forNode = symbolTable.definitionFor(call.name).get();
            definitions.store(forNode, call);
        }

        // Preveri argumente
        for (Expr argument : call.arguments)
            argument.accept(this);
    }

    @Override
    public void visit(Binary binary) {
        binary.left.accept(this);
        binary.right.accept(this);
    }

    @Override
    public void visit(Block block) {
        for (Expr expr : block.expressions) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(For forLoop) {
        forLoop.counter.accept(this);
        forLoop.low.accept(this);
        forLoop.high.accept(this);
        forLoop.step.accept(this);
        forLoop.body.accept(this);
    }

    @Override
    public void visit(Name name) {
        // Preveri obstoj imena
        if (symbolTable.definitionFor(name.name).isEmpty())
            Report.error(name.position, "Identifier " + name.name + " ni definiran!");
        else {
            Def forNode = symbolTable.definitionFor(name.name).get();
            definitions.store(forNode, name);
        }
    }

    @Override
    public void visit(IfThenElse ifThenElse) {
        ifThenElse.condition.accept(this);
        ifThenElse.thenExpression.accept(this);
        ifThenElse.elseExpression.ifPresent(expr -> expr.accept(this));
    }

    @Override
    public void visit(Literal literal) {
        // ne naredimo nič
    }

    @Override
    public void visit(Unary unary) {
        unary.expr.accept(this);
    }

    @Override
    public void visit(While whileLoop) {
        whileLoop.condition.accept(this);
        whileLoop.body.accept(this);
    }

    @Override
    public void visit(Where where) {
        symbolTable.pushScope();
        where.defs.accept(this); // 2 obhoda v Defs
        where.expr.accept(this);
        symbolTable.popScope();
    }

    @Override
    public void visit(Defs defs) {
        // Prvi obhod
        for (Def def : defs.definitions) {
            try {
                symbolTable.insert(def);
            } catch (DefinitionAlreadyExistsException e) {
                Report.error(def.position, "Definicija " + def.name + " že obstaja!");
            }
        }

        // Drugi obhod
        for (Def def : defs.definitions) {
            def.accept(this);
        }
    }

    @Override
    public void visit(FunDef funDef) {
        // 1. obhod
        // return type
        funDef.type.accept(this);
        // type parametrov
        for (Parameter parameter : funDef.parameters) {
            parameter.type.accept(this);
        }

        // 2. obhod - v novem scopu
        symbolTable.pushScope();
        // imena parametrov
        for (Parameter parameter : funDef.parameters) {
            parameter.accept(this);
        }
        // expression
        funDef.body.accept(this);
        symbolTable.popScope();
    }

    @Override
    public void visit(TypeDef typeDef) {
        typeDef.type.accept(this);
    }

    @Override
    public void visit(VarDef varDef) {
        varDef.type.accept(this);
    }

    @Override
    public void visit(Parameter parameter) {
        parameter.type.accept(this);
        try {
            symbolTable.insert(parameter);
        } catch (DefinitionAlreadyExistsException e) {
            Report.error(parameter.position, "Definicija " + parameter.name + " že obstaja!");
        }
    }

    @Override
    public void visit(Array array) {
        array.type.accept(this);
    }

    @Override
    public void visit(Atom atom) {
        // ne naredimo nič
    }

    @Override
    public void visit(TypeName name) {
        if (symbolTable.definitionFor(name.identifier).isEmpty())
            Report.error(name.position, "Tip " + name.identifier + " ni definiran!");
        else {
            Def forNode = symbolTable.definitionFor(name.identifier).get();
            definitions.store(forNode, name);
        }
    }
}
