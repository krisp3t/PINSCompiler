/**
 * @ Author: turk
 * @ Description: Analizator klicnih zapisov.
 */

package compiler.frm;

import static common.RequireNonNull.requireNonNull;

import common.Report;
import compiler.common.Visitor;
import compiler.parser.ast.def.*;
import compiler.parser.ast.def.FunDef.Parameter;
import compiler.parser.ast.expr.*;
import compiler.parser.ast.type.Array;
import compiler.parser.ast.type.Atom;
import compiler.parser.ast.type.TypeName;
import compiler.seman.common.NodeDescription;
import compiler.seman.type.type.Type;

import java.util.ArrayList;
import java.util.List;

public class FrameEvaluator implements Visitor {
    /**
     * Opis definicij funkcij in njihovih klicnih zapisov.
     */
    private NodeDescription<Frame> frames;

    /**
     * Opis definicij spremenljivk in njihovih dostopov.
     */
    private NodeDescription<Access> accesses;

    /**
     * Opis vozlišč in njihovih definicij.
     */
    private final NodeDescription<Def> definitions;

    /**
     * Opis vozlišč in njihovih podatkovnih tipov.
     */
    private final NodeDescription<Type> types;

    private int staticLevel = 1;
    private int offset = 0;


    public FrameEvaluator(
            NodeDescription<Frame> frames,
            NodeDescription<Access> accesses,
            NodeDescription<Def> definitions,
            NodeDescription<Type> types
    ) {
        requireNonNull(frames, accesses, definitions, types);
        this.frames = frames;
        this.accesses = accesses;
        this.definitions = definitions;
        this.types = types;
    }

    @Override
    public void visit(Call call) {
        // TODO Auto-generated method stub
        for (Expr argument : call.arguments)
            argument.accept(this);

        if (definitions.valueFor(call).isEmpty())
            return;


        Def def = definitions.valueFor(call).get();
        if (!(def instanceof FunDef))
            Report.error(call.position, call.name + " ni funkcija!");
        FunDef funDef = (FunDef) def;


        if (types.valueFor(funDef).isEmpty()) // gre skozi v drugem obhodu
            // če rekurzivna funkcija, posebej obravnavamo
            if (!call.name.equals(funDef.name))
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
                return;
            }
        }

        if (types.valueFor(funDef.type).isPresent())
            types.store(types.valueFor(funDef.type).get(), call);
        else
            return;
    }


    @Override
    public void visit(Binary binary) {
        // TODO Auto-generated method stub
        binary.left.accept(this);
        binary.right.accept(this);

        if (!(types.valueFor(binary.left).isPresent() && types.valueFor(binary.right).isPresent()))
            return;

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
        // TODO Auto-generated method stub
        for (Expr expr : block.expressions) {
            expr.accept(this);
        }
        if (types.valueFor(block.expressions.get(block.expressions.toArray().length - 1)).isPresent()) {
            // return type zadnji expr
            Type t = types.valueFor(block.expressions.get(block.expressions.toArray().length - 1)).get();
            types.store(t, block);
        }
    }


    @Override
    public void visit(For forLoop) {
        // TODO Auto-generated method stub
        forLoop.counter.accept(this);
        forLoop.low.accept(this);
        forLoop.high.accept(this);
        forLoop.step.accept(this);
        forLoop.body.accept(this);

        Expr[] nodes = {forLoop.counter, forLoop.low, forLoop.high, forLoop.step};
        for (Expr node : nodes) {
            if (types.valueFor(node).isPresent()) {
                Type t = types.valueFor(node).get();
                if (!t.isInt())
                    Report.error(node.position, "Pričakovan tip v for loopu je INTEGER!");
            } else {
                return;
            }
        }

        // return type VOID
        types.store(new Type.Atom(Type.Atom.Kind.VOID), forLoop);
    }


    @Override
    public void visit(Name name) {
        // TODO Auto-generated method stub
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
        } else if (def instanceof Parameter d) {
            if (types.valueFor(d.type).isPresent()) {
                Type t = types.valueFor(d.type).get();
                types.store(t, name);
            }
        }
    }


    @Override
    public void visit(IfThenElse ifThenElse) {
        // TODO Auto-generated method stub
        ifThenElse.condition.accept(this);
        ifThenElse.thenExpression.accept(this);
        ifThenElse.elseExpression.ifPresent(expr -> expr.accept(this));

        if (types.valueFor(ifThenElse.condition).isPresent()) {
            Type t = types.valueFor(ifThenElse.condition).get();
            if (!t.isLog())
                Report.error(ifThenElse.condition.position, "Pričakovan tip v if stavku je LOGICAL!");
        } else {
            return;
        }

        // return type VOID
        types.store(new Type.Atom(Type.Atom.Kind.VOID), ifThenElse);
    }


    @Override
    public void visit(Literal literal) {
        // TODO Auto-generated method stub
        Type.Atom.Kind kind = switch (literal.type) {
            case INT -> Type.Atom.Kind.INT;
            case LOG -> Type.Atom.Kind.LOG;
            case STR -> Type.Atom.Kind.STR;
        };
        this.types.store(new Type.Atom(kind), literal);
    }


    @Override
    public void visit(Unary unary) {
        // TODO Auto-generated method stub
        unary.expr.accept(this);

        if (types.valueFor(unary.expr).isEmpty())
            return;

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
        // TODO Auto-generated method stub
        whileLoop.condition.accept(this);
        whileLoop.body.accept(this);

        if (types.valueFor(whileLoop.condition).isPresent()) {
            Type t = types.valueFor(whileLoop.condition).get();
            if (!t.isLog())
                Report.error(whileLoop.condition.position, "Pričakovan tip v while stavku je LOGICAL!");
        } else {
            return;
        }

        // return type VOID
        types.store(new Type.Atom(Type.Atom.Kind.VOID), whileLoop);
    }


    @Override
    public void visit(Where where) {
        where.defs.accept(this);

        for (Def def : where.defs.definitions) {
            if (def instanceof VarDef vd) {
                if (types.valueFor(vd.type).isPresent()) {
                    Type t = types.valueFor(vd.type).get();
                    this.offset -= t.sizeInBytes();
                    Access.Local local = new Access.Local(t.sizeInBytes(), this.offset, this.staticLevel);
                    accesses.store(local, vd);
                }
            }
        }

        where.expr.accept(this);

    }


    @Override
    public void visit(Defs defs) {
        // TODO Auto-generated method stub
        // Prvi obhod
        for (Def def : defs.definitions) {
            def.accept(this);
        }
    }


    @Override
    public void visit(FunDef funDef) {
        Frame.Label funLabel;
        if (this.staticLevel == 1)
            funLabel = Frame.Label.named(funDef.name);
        else
            funLabel = Frame.Label.nextAnonymous();


        Frame.Builder klicniZapis = new Frame.Builder(funLabel, this.staticLevel);

        // Static Link
        klicniZapis.addParameter(4);

        // Parametri
        for (Parameter parameter : funDef.parameters) {
            parameter.accept(this);
            if (accesses.valueFor(parameter).isPresent()) {
                Access a = accesses.valueFor(parameter).get();
                klicniZapis.addParameter(a.size);
            }
        }

        this.offset = 0;

        // Lokalne spremenljivke
        funDef.body.accept(this);
        if (funDef.body instanceof Where w) {
            for (Def d: w.defs.definitions) {
                if (d instanceof VarDef vd) {
                    if (accesses.valueFor(vd).isPresent()) {
                        Access a = accesses.valueFor(vd).get();
                        klicniZapis.addLocalVariable(a.size);
                        // TODO: nested where
                    }
                }
            }
        }

        Frame f = klicniZapis.build();
        frames.store(f, funDef);

        // Ponastavi offset
        this.offset = 0;

    }


    @Override
    public void visit(TypeDef typeDef) {
        // TODO Auto-generated method stub
        typeDef.type.accept(this);
    }


    @Override
    public void visit(VarDef varDef) {
        // TODO Auto-generated method stub
        varDef.type.accept(this);

        // TODO: globalne spremenljivke
        /*
        if (types.valueFor(varDef.type).isPresent()) {
            Type t = types.valueFor(varDef.type).get();
            Access.Global g = new Access.Global(t.sizeInBytes(), Frame.Label.named(varDef.name));
            accesses.store(g, varDef);
        }
         */
    }


    @Override
    public void visit(Parameter parameter) {
        // TODO Auto-generated method stub
        parameter.type.accept(this);

        if (types.valueFor(parameter.type).isPresent()) {
            Type t = types.valueFor(parameter.type).get();
            // TODO: staticLevel
            this.offset = this.offset + t.sizeInBytesAsParam();
            Access.Parameter p = new Access.Parameter(t.sizeInBytesAsParam(), this.offset, this.staticLevel);
            accesses.store(p, parameter);
        }
    }


    @Override
    public void visit(Array array) {
        // TODO Auto-generated method stub
        array.type.accept(this);

        if (types.valueFor(array.type).isEmpty())
            Report.error(array.position, "Tip " + array.type + "ne obstaja!");

        Type t = types.valueFor(array.type).get();
        types.store(new Type.Array(array.size, t), array);
    }


    @Override
    public void visit(Atom atom) {
        // TODO Auto-generated method stub
        Type.Atom.Kind kind = switch (atom.type) {
            case INT -> Type.Atom.Kind.INT;
            case LOG -> Type.Atom.Kind.LOG;
            case STR -> Type.Atom.Kind.STR;
        };
        this.types.store(new Type.Atom(kind), atom);
    }


    @Override
    public void visit(TypeName name) {
        // TODO Auto-generated method stub
        if (definitions.valueFor(name).isEmpty())
            Report.error(name.position, "TypeName ne obstaja!");

        Def d = definitions.valueFor(name).get();
        if (d instanceof TypeDef) {
            if (types.valueFor(((TypeDef) d).type).isEmpty()) {
                /*
                if (visited.contains(d))
                    Report.error(name.position, "Najden cikel v tipih!");
                visited.add(d);

                 */
                d.accept(this);
            }
            types.store(types.valueFor(((TypeDef) d).type).get(), name);
        } else {
            Report.error(name.position, "TypeName ni tip!");
        }
    }
}
