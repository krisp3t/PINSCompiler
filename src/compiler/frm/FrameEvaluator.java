/**
 * @ Author: turk
 * @ Description: Analizator klicnih zapisov.
 */

package compiler.frm;

import static common.RequireNonNull.requireNonNull;

import common.Constants;
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
import java.util.Stack;

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

    private int staticLevel = 0;
    private final Stack <Frame.Builder> builderStack = new Stack<>();


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
        int argumentsSize = 0;
        for (Expr argument : call.arguments) {
            argument.accept(this);
            if (types.valueFor(argument).isPresent()) {
                Type t = types.valueFor(argument).get();
                argumentsSize += t.sizeInBytesAsParam();
            }
        }
        var builder = this.builderStack.pop();
        // TODO: ?
        argumentsSize += Constants.WordSize;
        builder.addFunctionCall(argumentsSize);

        this.builderStack.push(builder);
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
    }


    @Override
    public void visit(IfThenElse ifThenElse) {
        ifThenElse.condition.accept(this);
        ifThenElse.thenExpression.accept(this);
        ifThenElse.elseExpression.ifPresent(expr -> expr.accept(this));
    }


    @Override
    public void visit(Literal literal) {
    }


    @Override
    public void visit(Unary unary) {
        // TODO Auto-generated method stub
        unary.expr.accept(this);
    }


    @Override
    public void visit(While whileLoop) {
        whileLoop.condition.accept(this);
        whileLoop.body.accept(this);
    }


    @Override
    public void visit(Where where) {
        where.expr.accept(this);
        where.defs.accept(this);
    }


    @Override
    public void visit(Defs defs) {
        for (Def def : defs.definitions) {
            def.accept(this);
        }
    }


    @Override
    public void visit(FunDef funDef) {
        this.staticLevel++;

        Frame.Label funLabel;
        if (this.staticLevel > 1)
            funLabel = Frame.Label.nextAnonymous();
        else
            funLabel = Frame.Label.named(funDef.name);


        // Klicni zapis
        Frame.Builder klicniZapis = new Frame.Builder(funLabel, this.staticLevel);

        // Static Link
        klicniZapis.addParameter(Constants.WordSize);
        builderStack.push(klicniZapis);

        // Parametri
        for (Parameter parameter : funDef.parameters) {
            parameter.accept(this);
        }

        // Lokalne spremenljivke
        funDef.body.accept(this);

        Frame f = klicniZapis.build();
        frames.store(f, funDef);
        this.staticLevel--;
    }


    @Override
    public void visit(TypeDef typeDef) {
        typeDef.type.accept(this);
    }


    @Override
    public void visit(VarDef varDef) {
        varDef.type.accept(this);

        Access acc;
        if (types.valueFor(varDef.type).isPresent()) {
            Type t = types.valueFor(varDef.type).get();
            if (this.staticLevel > 0) {
                var builder = builderStack.pop();
                acc = new Access.Local(t.sizeInBytes(), builder.addLocalVariable(t.sizeInBytes()), this.staticLevel);
                builderStack.push(builder);
            } else {
                acc = new Access.Global(t.sizeInBytes(), Frame.Label.named(varDef.name));
            }
            accesses.store(acc, varDef);
        }
    }


    @Override
    public void visit(Parameter parameter) {
        parameter.type.accept(this);

        if (types.valueFor(parameter.type).isPresent()) {
            Type t = types.valueFor(parameter.type).get();
            var builder = builderStack.pop();
            Access.Parameter p = new Access.Parameter(t.sizeInBytesAsParam(), builder.addParameter(t.sizeInBytesAsParam()), this.staticLevel);
            accesses.store(p, parameter);
            builderStack.push(builder);
        }
    }


    @Override
    public void visit(Array array) {
        array.type.accept(this);
    }


    @Override
    public void visit(Atom atom) {
    }


    @Override
    public void visit(TypeName name) {
    }
}
