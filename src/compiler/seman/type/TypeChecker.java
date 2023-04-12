/**
 * @ Author: turk
 * @ Description: Preverjanje tipov.
 */

package compiler.seman.type;

import static common.RequireNonNull.requireNonNull;

import common.Report;
import compiler.common.Visitor;
import compiler.parser.ast.def.*;
import compiler.parser.ast.def.FunDef.Parameter;
import compiler.parser.ast.expr.*;
import compiler.parser.ast.type.*;
import compiler.seman.common.NodeDescription;
import compiler.seman.type.type.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TypeChecker implements Visitor {
    /**
     * Opis vozlišč in njihovih definicij.
     */
    private final NodeDescription<Def> definitions;

    /**
     * Opis vozlišč, ki jim priredimo podatkovne tipe.
     */
    private NodeDescription<Type> types;

    /**
     * Seznam obiskanih definicij (da se ne zaciklamo).
     */
    private HashSet<Def> visited = new HashSet<>();

    public TypeChecker(NodeDescription<Def> definitions, NodeDescription<Type> types) {
        requireNonNull(definitions, types);
        this.definitions = definitions;
        this.types = types;
    }

    @Override
    public void visit(Call call) {
        // TODO: preveri ujemanje s parametri funkcije
        for (Expr argument : call.arguments)
            argument.accept(this);

        if (definitions.valueFor(call).isEmpty())
            Report.error(call.position, "Funkcija " + call.name + " ni definirana!");


        Def def = definitions.valueFor(call).get();
        if (!(def instanceof FunDef))
            Report.error(call.position, call.name + " ni funkcija!");
        FunDef funDef = (FunDef) def;


        if (types.valueFor(funDef).isEmpty()) // gre skozi v drugem obhodu
            return;

        if (call.arguments.toArray().length != funDef.parameters.toArray().length)
            Report.error(call.position, "Število argumentov se ne ujema s številom parametrov funkcije");

        for (int i = 0; i < call.arguments.toArray().length; i++) {
            Expr argument = call.arguments.get(i);
            Parameter parameter = funDef.parameters.get(i);
            if (types.valueFor(argument).isPresent() && types.valueFor(parameter).isPresent()) {
                Type argType = types.valueFor(argument).get();
                Type paramType = types.valueFor(parameter).get();
                if (!argType.equals(paramType))
                    Report.error(argument.position, "Tip argumenta se ne ujema s tipom parametra");
            } else {
                Report.error(argument.position, "Tipa argumenta ni bilo mogoče določiti");
            }
        }

        if (types.valueFor(funDef.type).isPresent())
            types.store(types.valueFor(funDef.type).get(), call);
        else
            Report.error(call.position, "Tipa funkcije ni bilo mogoče določiti");
    }

    @Override
    public void visit(Binary binary) {
        binary.left.accept(this);
        binary.right.accept(this);

        if (!(types.valueFor(binary.left).isPresent() && types.valueFor(binary.right).isPresent()))
            Report.error(binary.position, "Tipov v binary expressionu ni bilo mogoče določiti");

        // { expr1 = expr2 }
        if (binary.operator.equals(Binary.Operator.ASSIGN)) {
            Type t1 = types.valueFor(binary.left).get();
            Type t2 = types.valueFor(binary.right).get();
            if (!t1.equals(t2))
                Report.error(binary.position, "Tipa v binary expressionu morata biti enaka!");
            // return type t1==t2
            types.store(t1, binary);
            return;
        }

        // &, |
        if (binary.operator.isAndOr()) {
            Type t1 = types.valueFor(binary.left).get();
            Type t2 = types.valueFor(binary.right).get();
            if (!(t1.isLog() && t2.isLog()))
                Report.error(binary.position, "Pričakovan tip v AND/OR izrazu je LOGICAL!");

            // return type LOGICAL
            types.store(new Type.Atom(Type.Atom.Kind.LOG), binary);
            return;
        }

        // +, -, *, /, %
        if (binary.operator.isArithmetic()) {
            Expr[] nodes = {binary.left, binary.right};
            for (Expr node : nodes) {
                Type t = types.valueFor(node).get();
                if (!t.isInt())
                    Report.error(node.position, "Pričakovan tip v aritmetičnem izrazu je INTEGER!");
            }
            // return type INTEGER
            types.store(new Type.Atom(Type.Atom.Kind.INT), binary);
            return;
        }

        // ==, !=, <=, >=, <, >
        if (binary.operator.isComparison()) {
            Type t1 = types.valueFor(binary.left).get();
            Type t2 = types.valueFor(binary.right).get();
            if (!t1.equals(t2))
                Report.error(binary.position, "Tipa v binary expressionu morata biti enaka!");
            if (!(t1.isInt() || t1.isLog()) || !(t2.isInt() || t2.isLog()))
                Report.error(binary.position, "Pričakovan tip v primerjalnem izrazu je INTEGER ali LOGICAL!");

            // return type LOGICAL
            types.store(new Type.Atom(Type.Atom.Kind.LOG), binary);
        }

        // ARR
        // TODO: check size
        if (binary.operator.equals(Binary.Operator.ARR)) {
            Type t1 = types.valueFor(binary.left).get();
            Type t2 = types.valueFor(binary.right).get();
            if (!t1.isArray())
                Report.error(binary.position, "Pričakovan tip v array izrazu je ARRAY!");
            if (!t2.isInt())
                Report.error(binary.position, "Pričakovan tip v array izrazu je INTEGER!");
            if (t1 instanceof Type.Array t) {
                // return type t
                types.store(t.type, binary);
            }
        }
    }

    @Override
    public void visit(Block block) {
        for (Expr expr : block.expressions) {
            expr.accept(this);
        }
        if (types.valueFor(block.expressions.get(block.expressions.toArray().length - 1)).isPresent()) {
            // return type zadnji expr
            Type t = types.valueFor(block.expressions.get(block.expressions.toArray().length - 1)).get();
            types.store(t, block);
        } else {
            Report.error(block.position, "Tipa bloka ni bilo mogoče določiti");
        }
    }

    @Override
    public void visit(For forLoop) {
        forLoop.counter.accept(this);
        forLoop.low.accept(this);
        forLoop.high.accept(this);
        forLoop.step.accept(this);
        forLoop.body.accept(this);

        Expr[] nodes = {forLoop.low, forLoop.high, forLoop.step};
        for (Expr node : nodes) {
            if (types.valueFor(node).isPresent()) {
                Type t = types.valueFor(node).get();
                if (!t.isInt())
                    Report.error(node.position, "Pričakovan tip v for loopu je INTEGER!");
            } else {
                Report.error(node.position, "Tipa v for loopu ni bilo mogoče določiti");
            }
        }

        // return type VOID
        types.store(new Type.Atom(Type.Atom.Kind.VOID), forLoop);
    }

    @Override
    public void visit(Name name) {
        if (definitions.valueFor(name).isEmpty())
            Report.error(name.position, "Ime " + name.name + " ni bilo definirano!");

        Def def = definitions.valueFor(name).get();
        if (def instanceof VarDef d) {
            if (types.valueFor(d.type).isPresent()) {
                Type t = types.valueFor(d.type).get();
                types.store(t, name);
            } else {
                Report.error(name.position, "Tipa " + name.name + " ni bilo mogoče določiti");
            }
            // TODO: Add typename
        } else if (def instanceof Parameter d) {
            if (types.valueFor(d.type).isPresent()) {
                Type t = types.valueFor(d.type).get();
                types.store(t, name);
            } else {
                Report.error(name.position, "Tipa " + name.name + " ni bilo mogoče določiti");
            }
        }
    }

    @Override
    public void visit(IfThenElse ifThenElse) {
        ifThenElse.condition.accept(this);
        ifThenElse.thenExpression.accept(this);
        ifThenElse.elseExpression.ifPresent(expr -> expr.accept(this));

        if (types.valueFor(ifThenElse.condition).isPresent()) {
            Type t = types.valueFor(ifThenElse.condition).get();
            if (!t.isLog())
                Report.error(ifThenElse.condition.position, "Pričakovan tip v if stavku je LOGICAL!");
        } else {
            Report.error(ifThenElse.condition.position, "Tipa v if stavku ni bilo mogoče določiti");
        }

        // return type VOID
        types.store(new Type.Atom(Type.Atom.Kind.VOID), ifThenElse);
    }

    @Override
    public void visit(Literal literal) {
        Type.Atom.Kind kind = switch (literal.type) {
            case INT -> Type.Atom.Kind.INT;
            case LOG -> Type.Atom.Kind.LOG;
            case STR -> Type.Atom.Kind.STR;
        };
        this.types.store(new Type.Atom(kind), literal);
    }

    @Override
    public void visit(Unary unary) {
        unary.expr.accept(this);

        if (types.valueFor(unary.expr).isEmpty())
            Report.error(unary.position, "Tipa v unary expressionu ni bilo mogoče določiti");

        // !
        if (unary.operator.equals(Unary.Operator.NOT)) {
            Type t = types.valueFor(unary.expr).get();
            if (!t.isLog())
                Report.error(unary.position, "Pričakovan tip v NOT izrazu je LOGICAL!");
            // return type LOGICAL
            types.store(new Type.Atom(Type.Atom.Kind.LOG), unary);
            return;
        }

        // +, -
        if (unary.operator.equals(Unary.Operator.ADD) || unary.operator.equals(Unary.Operator.SUB)) {
            Type t = types.valueFor(unary.expr).get();
            if (!t.isInt())
                Report.error(unary.position, "Pričakovan tip v unary ADD/SUB izrazu je INTEGER!");
            // return type INTEGER
            types.store(new Type.Atom(Type.Atom.Kind.INT), unary);
        }
    }

    @Override
    public void visit(While whileLoop) {
        whileLoop.condition.accept(this);
        whileLoop.body.accept(this);

        if (types.valueFor(whileLoop.condition).isPresent()) {
            Type t = types.valueFor(whileLoop.condition).get();
            if (!t.isLog())
                Report.error(whileLoop.condition.position, "Pričakovan tip v while stavku je LOGICAL!");
        } else {
            Report.error(whileLoop.condition.position, "Tipa v while stavku ni bilo mogoče določiti");
        }

        // return type VOID
        types.store(new Type.Atom(Type.Atom.Kind.VOID), whileLoop);
    }

    @Override
    public void visit(Where where) {
        where.defs.accept(this); // 2 obhoda v Defs
        where.expr.accept(this);
        // return type expr
        if (types.valueFor(where.expr).isPresent())
            types.store(types.valueFor(where.expr).get(), where);
        else
            Report.error(where.position, "Tipa v WHERE stavku ni bilo mogoče določiti");
    }

    @Override
    public void visit(Defs defs) {
        // Prvi obhod
        for (Def def : defs.definitions) {
            def.accept(this);
        }

        // Drugi obhod
        for (Def def : defs.definitions) {
            def.accept(this);
        }
    }

    @Override
    public void visit(FunDef funDef) {
        // return type
        funDef.type.accept(this);
        // type parametrov
        for (Parameter parameter : funDef.parameters) {
            parameter.type.accept(this);
        }
        // imena parametrov
        for (Parameter parameter : funDef.parameters) {
            parameter.accept(this);
        }

        if (types.valueFor(funDef.type).isEmpty())
            Report.error(funDef.position, "Return tipa funkcije ni bilo mogoče določiti");
        types.store(types.valueFor(funDef.type).get(), funDef.type);

        // expression
        funDef.body.accept(this);

        if (types.valueFor(funDef.body).isEmpty())
            Report.error(funDef.position, "Tipa telesa funkcije ni bilo mogoče določiti");
        Type body = types.valueFor(funDef.body).get();
        Type ret = types.valueFor(funDef.type).get();

        if (!body.equals(ret))
            Report.error(funDef.position, "Tipa telesa funkcije in return se ne ujemata");

        List<Type> params = new ArrayList<>();
        for (Parameter parameter : funDef.parameters) {
            if (types.valueFor(parameter.type).isEmpty())
                Report.error(parameter.position, "Tipa parametra funkcije ni bilo mogoče določiti");
            params.add(types.valueFor(parameter.type).get());
        }

        types.store(new Type.Function(params, ret), funDef);

    }

    @Override
    public void visit(TypeDef typeDef) {
        typeDef.type.accept(this);

        // TODO: preveri, če typ obstaja
        if (types.valueFor(typeDef.type).isEmpty())
            Report.error(typeDef.position, "Tip " + typeDef.type + "ne obstaja!");

        Type t = types.valueFor(typeDef.type).get();
        if (types.valueFor(typeDef).isEmpty())
            types.store(t, typeDef);
    }

    @Override
    public void visit(VarDef varDef) {
        varDef.type.accept(this);

        // TODO: preveri, če typ obstaja
        if (types.valueFor(varDef.type).isEmpty())
            Report.error(varDef.position, "Tip " + varDef.type + "ne obstaja!");

        Type t = types.valueFor(varDef.type).get();
        types.store(t, varDef);

    }

    @Override
    public void visit(Parameter parameter) {
        parameter.type.accept(this);

        // TODO: preveri, če typ obstaja
        if (types.valueFor(parameter.type).isEmpty())
            Report.error(parameter.position, "Tip " + parameter.type + "ne obstaja!");

        Type t = types.valueFor(parameter.type).get();
        types.store(t, parameter);
    }

    @Override
    public void visit(Array array) {
        array.type.accept(this);

        // TODO: preveri, če typ obstaja
        if (types.valueFor(array.type).isEmpty())
            Report.error(array.position, "Tip " + array.type + "ne obstaja!");

        Type t = types.valueFor(array.type).get();
        types.store(new Type.Array(array.size, t), array);
    }

    @Override
    public void visit(Atom atom) {
        Type.Atom.Kind kind = switch (atom.type) {
            case INT -> Type.Atom.Kind.INT;
            case LOG -> Type.Atom.Kind.LOG;
            case STR -> Type.Atom.Kind.STR;
        };
        this.types.store(new Type.Atom(kind), atom);
    }

    @Override
    public void visit(TypeName name) {
        if (definitions.valueFor(name).isEmpty())
            Report.error(name.position, "TypeName ne obstaja!");

        Def d = definitions.valueFor(name).get();
        if (d instanceof TypeDef) {
            if (types.valueFor(((TypeDef) d).type).isEmpty()) {
                if (visited.contains(d))
                    Report.error(name.position, "Najden cikel v tipih!");
                visited.add(d);
                d.accept(this);
            }
            types.store(types.valueFor(((TypeDef) d).type).get(), name);
        } else {
            Report.error(name.position, "TypeName ni tip!");
        }
    }
}
