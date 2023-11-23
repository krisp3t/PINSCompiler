/**
 * @ Author: turk
 * @ Description: Preverjanje in razreševanje imen.
 */

package compiler.seman.name;

import static common.RequireNonNull.requireNonNull;

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import common.Constants;
=======
>>>>>>> repo4/main
=======
>>>>>>> repo5/main
=======
>>>>>>> repo6/main
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

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import java.util.*;
=======
import java.util.Optional;
>>>>>>> repo4/main
=======
import java.util.Optional;
>>>>>>> repo5/main
=======
import java.util.Optional;
>>>>>>> repo6/main

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

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
    static final HashSet<String> STD_KNJIZNICA = new HashSet<>(Arrays.asList(Constants.printStringLabel, Constants.printIntLabel, Constants.printLogLabel, Constants.randIntLabel, Constants.seedLabel));
    static final FunDef PRINT_INT_DEF = new FunDef(
            Position.zero(),
            Constants.printIntLabel,
            new ArrayList<>(List.of(new Parameter(Position.zero(), "_", Atom.INT(Position.zero())))),
            Atom.INT(Position.zero()),
            new Literal(Position.zero(), "0", Atom.Type.INT));
    static final FunDef PRINT_STR_DEF = new FunDef(
            Position.zero(),
            Constants.printStringLabel,
            new ArrayList<>(List.of(new Parameter(Position.zero(), "_", Atom.STR(Position.zero())))),
            Atom.STR(Position.zero()),
            new Literal(Position.zero(), "", Atom.Type.STR));
    static final FunDef PRINT_LOG_DEF = new FunDef(
            Position.zero(),
            Constants.printLogLabel,
            new ArrayList<>(List.of(new Parameter(Position.zero(), "_", Atom.LOG(Position.zero())))),
            Atom.LOG(Position.zero()),
            new Literal(Position.zero(), "false", Atom.Type.LOG));
    static final FunDef RAND_INT_DEF = new FunDef(
            Position.zero(),
            Constants.randIntLabel,
            new ArrayList<>(List.of(
                    new Parameter(Position.zero(), "_", Atom.INT(Position.zero())),
                    new Parameter(Position.zero(), "__", Atom.INT(Position.zero())))),
            Atom.INT(Position.zero()),
            new Literal(Position.zero(), "0", Atom.Type.INT));
    static final FunDef SEED_DEF = new FunDef(
            Position.zero(),
            Constants.seedLabel,
            new ArrayList<>(List.of(new Parameter(Position.zero(), "_", Atom.INT(Position.zero())))),
            Atom.INT(Position.zero()),
            new Literal(Position.zero(), "0", Atom.Type.INT));


    @Override
    public void visit(Call call) {
        // Preskoči, če del standardne knjižnice. Preverimo v typecheckerju
        if (STD_KNJIZNICA.contains(call.name)) {
            switch (call.name) {
                case Constants.printIntLabel:
                    definitions.store(PRINT_INT_DEF, call);
                    break;
                case Constants.printStringLabel:
                    definitions.store(PRINT_STR_DEF, call);
                    break;
                case Constants.printLogLabel:
                    definitions.store(PRINT_LOG_DEF, call);
                    break;
                case Constants.randIntLabel:
                    definitions.store(RAND_INT_DEF, call);
                    break;
                case Constants.seedLabel:
                    definitions.store(SEED_DEF, call);
                    break;
            }
            for (Expr argument : call.arguments)
                argument.accept(this);
            return;
        }

=======
    @Override
    public void visit(Call call) {
>>>>>>> repo4/main
=======
    @Override
    public void visit(Call call) {
>>>>>>> repo5/main
=======
    @Override
    public void visit(Call call) {
>>>>>>> repo6/main
        // Preveri obstoj funkcije
        if (symbolTable.definitionFor(call.name).isEmpty())
            Report.error(call.position, "Funkcija " + call.name + " ni definirana!");
        else {
            Def forNode = symbolTable.definitionFor(call.name).get();
            if (!(forNode instanceof FunDef))
                Report.error(call.position, call.name + " ni funkcija!");
            else
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

        // Prepreči funkcija[]
        if (binary.left instanceof Name left && symbolTable.definitionFor(left.name).isPresent()) {
            if (symbolTable.definitionFor(left.name).get() instanceof FunDef)
                Report.error(binary.position, "Uporaba funkcije " + left.name + " kot array!");
        }
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
            // Prepreči imenovanje funkcije (funkcija mora biti vedno klicana)
            if (forNode instanceof FunDef)
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                Report.error(name.position, "Nedovoljena uporaba funkcije " + name.name + " kot spremenljivke!");
            else if (forNode instanceof TypeDef)
                Report.error(name.position, "Nedovoljena uporaba tipa " + name.name + " kot spremenljivke!");
=======
                Report.error(name.position, "Nedovoljena uporaba funkcije " + name.name + " kot spremenljivka!");
>>>>>>> repo4/main
=======
                Report.error(name.position, "Nedovoljena uporaba funkcije " + name.name + " kot spremenljivke!");
            else if (forNode instanceof TypeDef)
                Report.error(name.position, "Nedovoljena uporaba tipa " + name.name + " kot spremenljivke!");
>>>>>>> repo5/main
=======
                Report.error(name.position, "Nedovoljena uporaba funkcije " + name.name + " kot spremenljivke!");
            else if (forNode instanceof TypeDef)
                Report.error(name.position, "Nedovoljena uporaba tipa " + name.name + " kot spremenljivke!");
>>>>>>> repo6/main
            else
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
            if (!(forNode instanceof TypeDef))
                Report.error(name.position, "Identifier " + name.identifier + " ni tip!");
            else
                definitions.store(forNode, name);
        }
    }
}
