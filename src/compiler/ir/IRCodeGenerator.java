/**
 * @ Author: turk
 * @ Description: Generator vmesne kode.
 */

package compiler.ir;

import static common.RequireNonNull.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import common.Constants;
import common.Report;
import compiler.common.Visitor;
import compiler.frm.Access;
import compiler.frm.Frame;
import compiler.frm.Frame.Label;
import compiler.ir.chunk.Chunk;
import compiler.ir.code.IRNode;
import compiler.ir.code.expr.*;
import compiler.ir.code.stmt.*;
import compiler.parser.ast.def.*;
import compiler.parser.ast.def.FunDef.Parameter;
import compiler.parser.ast.expr.*;
import compiler.parser.ast.type.Array;
import compiler.parser.ast.type.Atom;
import compiler.parser.ast.type.TypeName;
import compiler.seman.common.NodeDescription;
import compiler.seman.type.type.Type;

public class IRCodeGenerator implements Visitor {
    /**
     * Preslikava iz vozlišč AST v vmesno kodo.
     */
    private NodeDescription<IRNode> imcCode;

    /**
     * Razrešeni klicni zapisi.
     */
    private final NodeDescription<Frame> frames;

    /**
     * Razrešeni dostopi.
     */
    private final NodeDescription<Access> accesses;

    /**
     * Razrešene definicije.
     */
    private final NodeDescription<Def> definitions;

    /**
     * Razrešeni tipi.
     */
    private final NodeDescription<Type> types;

    /**
     * **Rezultat generiranja vmesne kode** - seznam fragmentov.
     */
    public List<Chunk> chunks = new ArrayList<>();

    public IRCodeGenerator(
            NodeDescription<IRNode> imcCode,
            NodeDescription<Frame> frames,
            NodeDescription<Access> accesses,
            NodeDescription<Def> definitions,
            NodeDescription<Type> types
    ) {
        requireNonNull(imcCode, frames, accesses, definitions, types);
        this.types = types;
        this.imcCode = imcCode;
        this.frames = frames;
        this.accesses = accesses;
        this.definitions = definitions;
    }

    @Override
    public void visit(Call call) {
        for (Expr argument : call.arguments)
            argument.accept(this);
    }

    @Override
    public void visit(Binary binary) {
        binary.left.accept(this);
        binary.right.accept(this);

        if (imcCode.valueFor(binary.left).isEmpty() || imcCode.valueFor(binary.right).isEmpty())
            Report.error(binary.position, "Manjka IMC za binary.left ali binary.right!");

        IRExpr lhs = (IRExpr) imcCode.valueFor(binary.left).get();
        IRExpr rhs = (IRExpr) imcCode.valueFor(binary.right).get();
        BinopExpr.Operator op = null;
        switch (binary.operator) {
            case ADD -> op = BinopExpr.Operator.ADD;
            case SUB -> op = BinopExpr.Operator.SUB;
            case MUL -> op = BinopExpr.Operator.MUL;
            case DIV -> op = BinopExpr.Operator.DIV;
            case MOD -> op = BinopExpr.Operator.MOD;
            case AND -> op = BinopExpr.Operator.AND;
            case OR -> op = BinopExpr.Operator.OR;
            case EQ -> op = BinopExpr.Operator.EQ;
            case NEQ -> op = BinopExpr.Operator.NEQ;
            case LT -> op = BinopExpr.Operator.LT;
            case GT -> op = BinopExpr.Operator.GT;
            case LEQ -> op = BinopExpr.Operator.LEQ;
            case GEQ -> op = BinopExpr.Operator.GEQ;
            default -> Report.error(binary.position, "Neznani operator!");
        }

        BinopExpr b = new BinopExpr(lhs, rhs, op);
        imcCode.store(b, binary);
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
        int constant;
        if (literal.type.equals(Atom.Type.LOG))
            constant = literal.value.equalsIgnoreCase("true") ? 1 : 0;
        else if (literal.type.equals(Atom.Type.INT))
            constant = Integer.parseInt(literal.value);
        else
            return;

        ConstantExpr c = new ConstantExpr(constant);
        imcCode.store(c, literal);
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
        where.defs.accept(this);
        where.expr.accept(this);

        if (this.imcCode.valueFor(where.expr).isEmpty())
            Report.error(where.position, "IMC za where.expr ni najden!");

        // generiranje fragmenta
        IRExpr e = (IRExpr) this.imcCode.valueFor(where.expr).get();
        this.imcCode.store(e, where);
    }

    @Override
    public void visit(Defs defs) {
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
        // expression
        funDef.body.accept(this);

        // generiranje fragmenta
        if (this.frames.valueFor(funDef).isEmpty())
            Report.error(funDef.position, "Frame za FunDef ni najden!");
        if (this.imcCode.valueFor(funDef.body).isEmpty())
            Report.error(funDef.position, "IMC koda za FunDef body ni najdena!");

        Frame frame = this.frames.valueFor(funDef).get();

        // Pričakujemo expression
        IRNode n = this.imcCode.valueFor(funDef.body).get();
        IRExpr e = (IRExpr) n;
        ExpStmt imc = new ExpStmt(e);

        Chunk f = new Chunk.CodeChunk(frame, imc);
        this.chunks.add(f);
    }

    @Override
    public void visit(TypeDef typeDef) {
        typeDef.type.accept(this);
    }

    @Override
    public void visit(VarDef varDef) {
        if (this.accesses.valueFor(varDef).isEmpty() || this.types.valueFor(varDef).isEmpty())
            Report.error(varDef.position, "Access ali tip za VarDef ni najden!");

        Access a = this.accesses.valueFor(varDef).get();
        Type t = this.types.valueFor(varDef).get();
        Chunk g;

        // Globalne
        if (a instanceof Access.Global globalAccess) {
            if (t.isStr())
                g = new Chunk.DataChunk(globalAccess, "");
            else
                g = new Chunk.GlobalChunk(globalAccess);
            this.chunks.add(g);
        } else {
            // Lokalni stringi
            if (t.isStr()) {
                Access.Global globalAccess = new Access.Global(Constants.WordSize, Label.nextAnonymous());
                g = new Chunk.DataChunk(globalAccess, "");
                this.chunks.add(g);
            }
        }

        varDef.type.accept(this);
    }

    @Override
    public void visit(Parameter parameter) {
        parameter.type.accept(this);
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
