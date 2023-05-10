/**
 * @ Author: turk
 * @ Description: Generator vmesne kode.
 */

package compiler.ir;

import static common.RequireNonNull.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
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

    private Frame currentFrame = null;

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

        if (binary.operator.equals(Binary.Operator.ASSIGN)) {
            if (lhs instanceof MemExpr mem) {
                MoveStmt mov = new MoveStmt(mem, rhs);
                imcCode.store(mov, binary);
            } else {
                Report.error(binary.position, "Pričakovan MemExpr na levi strani assignmenta!");
            }


        } else if (binary.operator.equals(Binary.Operator.ARR)) {
            // TODO
        } else {
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

    }

    @Override
    public void visit(Block block) {
        for (Expr expr : block.expressions) {
            expr.accept(this);
        }

        // SeqStmt
        List<IRStmt> stmts = new ArrayList<>();
        for (int i = 0; i < block.expressions.size() - 1; i++) {
            if (imcCode.valueFor(block.expressions.get(i)).isEmpty())
                Report.error(block.position, "Manjka IMC za block.expressions(i)!");

            IRNode node = imcCode.valueFor(block.expressions.get(i)).get();
            if (node instanceof IRStmt stmt) {
                stmts.add(stmt);
            } else {
                ExpStmt stmt = new ExpStmt((IRExpr) node);
                stmts.add(stmt);
            }
        }
        SeqStmt s = new SeqStmt(stmts);

        // EseqExpr
        if (imcCode.valueFor(block.expressions.get(block.expressions.size() - 1)).isEmpty())
            Report.error(block.position, "Manjka IMC za block.expressions.get(block.expressions.size() - 1)!");
        IRNode node = imcCode.valueFor(block.expressions.get(block.expressions.size() - 1)).get();
        EseqExpr e;
        if (node instanceof IRExpr expr) {
            e = new EseqExpr(s, expr);
            imcCode.store(e, block);
        } else {
            Report.error(block.expressions.get(block.expressions.size() - 1).position, "Funkcija mora vračati expression!");
        }
    }

    @Override
    public void visit(For forLoop) {
        forLoop.counter.accept(this);
        forLoop.low.accept(this);
        forLoop.high.accept(this);
        forLoop.step.accept(this);
        forLoop.body.accept(this);

        if (imcCode.valueFor(forLoop.counter).isEmpty())
            Report.error(forLoop.counter.position, "Manjka IMC za forLoop.counter!");
        if (imcCode.valueFor(forLoop.low).isEmpty())
            Report.error(forLoop.counter.position, "Manjka IMC za forLoop.low!");
        if (imcCode.valueFor(forLoop.high).isEmpty())
            Report.error(forLoop.counter.position, "Manjka IMC za forLoop.high!");
        if (imcCode.valueFor(forLoop.step).isEmpty())
            Report.error(forLoop.counter.position, "Manjka IMC za forLoop.step!");
        if (imcCode.valueFor(forLoop.body).isEmpty())
            Report.error(forLoop.counter.position, "Manjka IMC za forLoop.body!");

        IRExpr cond = null;
        IRStmt body;


        MoveStmt init = new MoveStmt(
                (IRExpr) imcCode.valueFor(forLoop.counter).get(),
                (IRExpr) imcCode.valueFor(forLoop.low).get()
        );

        LabelStmt condLabel = new LabelStmt(Frame.Label.nextAnonymous());
        IRNode condNode = imcCode.valueFor(forLoop.high).get();
        if (condNode instanceof IRExpr) {
            cond = (IRExpr) condNode;
        } else {
            Report.error(forLoop.high.position, "Condition while loopa mora biti expression!");
        }
        BinopExpr lt = new BinopExpr(
                (IRExpr) imcCode.valueFor(forLoop.counter).get(),
                cond,
                BinopExpr.Operator.LT);

        LabelStmt thenLabel = new LabelStmt(Frame.Label.nextAnonymous());
        IRNode bodyNode = imcCode.valueFor(forLoop.body).get();
        if (bodyNode instanceof IRStmt) {
            body = (IRStmt) bodyNode;
        } else {
            body = new ExpStmt((IRExpr) bodyNode);
        }
        ExpStmt step = new ExpStmt(new BinopExpr(
                (IRExpr) imcCode.valueFor(forLoop.counter).get(),
                (IRExpr) imcCode.valueFor(forLoop.step).get(),
                BinopExpr.Operator.ADD
        ));
        JumpStmt jump = new JumpStmt(condLabel.label);
        LabelStmt elseLabel = new LabelStmt(Frame.Label.nextAnonymous());

        CJumpStmt c = new CJumpStmt(lt, thenLabel.label, elseLabel.label);
        List<IRStmt> stmts = new ArrayList<>(Arrays.asList(init, condLabel, c, thenLabel, body, step, jump, elseLabel));
        SeqStmt seq = new SeqStmt(stmts);

        imcCode.store(seq, forLoop);
    }


    @Override
    public void visit(Name name) {
        if (definitions.valueFor(name).isEmpty())
            Report.error(name.position, "Manjka definicija za name!");

        Def v = definitions.valueFor(name).get();

        if (accesses.valueFor(v).isEmpty())
            Report.error(name.position, "Manjka access za name!");

        Access a = accesses.valueFor(v).get();

        if (a instanceof Access.Global g) {
            MemExpr mem = new MemExpr(new NameExpr(g.label));
            imcCode.store(mem, name);
            // TODO
        } else if (a instanceof Access.Local l) {
            BinopExpr add = new BinopExpr(
                    NameExpr.FP(),
                    new ConstantExpr(l.offset),
                    BinopExpr.Operator.ADD
            );
            MemExpr mem = new MemExpr(add);
            imcCode.store(mem, name);
        } else if (a instanceof Access.Parameter p) {
            BinopExpr add = new BinopExpr(
                    NameExpr.FP(),
                    new ConstantExpr(p.offset),
                    BinopExpr.Operator.ADD
            );
            MemExpr mem = new MemExpr(add);
            imcCode.store(mem, name);
        }

        // TODO: drug SL


    }

    @Override
    public void visit(IfThenElse ifThenElse) {
        ifThenElse.condition.accept(this);
        ifThenElse.thenExpression.accept(this);
        ifThenElse.elseExpression.ifPresent(expr -> expr.accept(this));

        if (imcCode.valueFor(ifThenElse.condition).isEmpty())
            Report.error(ifThenElse.condition.position, "Manjka IMC za forLoop.counter!");
        if (imcCode.valueFor(ifThenElse.thenExpression).isEmpty())
            Report.error(ifThenElse.thenExpression.position, "Manjka IMC za forLoop.low!");
        if (ifThenElse.elseExpression.isPresent() && imcCode.valueFor(ifThenElse.elseExpression.get()).isEmpty())
            Report.error(ifThenElse.elseExpression.get().position, "Manjka IMC za forLoop.high!");

        IRExpr cond = null;
        IRNode condNode = imcCode.valueFor(ifThenElse.condition).get();
        if (condNode instanceof IRExpr) {
            cond = (IRExpr) condNode;
        } else {
            Report.error(ifThenElse.condition.position, "Condition if stavka mora biti expression!");
        }
        LabelStmt thenLabel = new LabelStmt(Frame.Label.nextAnonymous());
        LabelStmt elseLabel = new LabelStmt(Frame.Label.nextAnonymous());
        LabelStmt endLabel = new LabelStmt(Frame.Label.nextAnonymous());
        JumpStmt jump = new JumpStmt(endLabel.label);

        IRNode thenNode = imcCode.valueFor(ifThenElse.thenExpression).get();
        IRStmt thenBody;
        IRStmt elseBody;

        if (thenNode instanceof IRStmt) {
            thenBody = (IRStmt) thenNode;
        } else {
            thenBody = new ExpStmt((IRExpr) thenNode);
        }


        List<IRStmt> stmts;
        CJumpStmt c;
        if (ifThenElse.elseExpression.isPresent()) {
            IRNode elseNode = imcCode.valueFor(ifThenElse.elseExpression.get()).get();
            if (elseNode instanceof IRStmt) {
                elseBody = (IRStmt) elseNode;
            } else {
                elseBody = new ExpStmt((IRExpr) elseNode);
            }
            c = new CJumpStmt(cond, thenLabel.label, elseLabel.label);
            stmts = new ArrayList<>(Arrays.asList(c, thenLabel, thenBody, jump, elseLabel, elseBody, endLabel));
        } else {
            c = new CJumpStmt(cond, thenLabel.label, endLabel.label);
            stmts = new ArrayList<>(Arrays.asList(c, thenLabel, thenBody, endLabel));
        }
        SeqStmt seq = new SeqStmt(stmts);
        imcCode.store(seq, ifThenElse);
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

        IRExpr e;
        if (unary.operator.equals(Unary.Operator.NOT)) {
            e = new BinopExpr(
                    new ConstantExpr(1),
                    (IRExpr) imcCode.valueFor(unary.expr).get(),
                    BinopExpr.Operator.SUB
            );
            // 1 - 1 = 0
            // 1 - 0 = 1
        } else {
            e = new BinopExpr(
                    new ConstantExpr(0),
                    (IRExpr) imcCode.valueFor(unary.expr).get(),
                    unary.operator.equals(Unary.Operator.ADD) ? BinopExpr.Operator.ADD : BinopExpr.Operator.SUB
            );
        }
        imcCode.store(e, unary);
    }

    @Override
    public void visit(While whileLoop) {
        whileLoop.condition.accept(this);
        whileLoop.body.accept(this);

        if (imcCode.valueFor(whileLoop.condition).isEmpty())
            Report.error(whileLoop.position, "Manjka IMC za whileLoop.condition!");
        if (imcCode.valueFor(whileLoop.body).isEmpty())
            Report.error(whileLoop.position, "Manjka IMC za whileLoop.body!");

        IRExpr cond = null;
        IRStmt body;

        LabelStmt condLabel = new LabelStmt(Frame.Label.nextAnonymous());
        IRNode condNode = imcCode.valueFor(whileLoop.condition).get();
        if (condNode instanceof IRExpr) {
            cond = (IRExpr) condNode;
        } else {
            Report.error(whileLoop.condition.position, "Condition while loopa mora biti expression!");
        }


        LabelStmt thenLabel = new LabelStmt(Frame.Label.nextAnonymous());
        IRNode bodyNode = imcCode.valueFor(whileLoop.body).get();
        if (bodyNode instanceof IRStmt) {
            body = (IRStmt) bodyNode;
        } else {
            body = new ExpStmt((IRExpr) bodyNode);
        }
        JumpStmt endLabel = new JumpStmt(condLabel.label);
        LabelStmt elseLabel = new LabelStmt(Frame.Label.nextAnonymous());

        CJumpStmt c = new CJumpStmt(cond, thenLabel.label, elseLabel.label);
        List<IRStmt> stmts = new ArrayList<>(Arrays.asList(condLabel, c, thenLabel, body, endLabel, elseLabel));
        SeqStmt seq = new SeqStmt(stmts);

        imcCode.store(seq, whileLoop);
    }

    @Override
    public void visit(Where where) {
        where.defs.accept(this);
        where.expr.accept(this);

        if (this.imcCode.valueFor(where.expr).isEmpty())
            Report.error(where.position, "IMC za where.expr ni najden!");

        // generiranje fragmenta
        IRNode node = this.imcCode.valueFor(where.expr).get();
        this.imcCode.store(node, where);
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
        this.currentFrame = frame;

        NameExpr n = new NameExpr(frame.label);
        this.imcCode.store(n, funDef);

        // Pričakujemo expression
        IRNode node = this.imcCode.valueFor(funDef.body).get();
        IRExpr e = (IRExpr) node;
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
