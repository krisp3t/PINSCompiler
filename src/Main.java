/**
 * @Author: turk
 * @Description: Vhodna točka prevajalnika.
 */

import java.io.IOException;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import cli.PINS;
import cli.PINS.Phase;
<<<<<<< HEAD
<<<<<<< HEAD
import compiler.common.PrettyPrintVisitor4;
import compiler.frm.Access;
import compiler.frm.Frame;
import compiler.frm.FrameEvaluator;
import compiler.gen.LinCodeGenerator;
import compiler.gen.Memory;
import compiler.interpret.Interpreter;
import compiler.ir.IRCodeGenerator;
import compiler.ir.IRPrettyPrint;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
import compiler.parser.ast.def.Def;
import compiler.seman.common.NodeDescription;
import compiler.seman.name.NameChecker;
import compiler.seman.name.env.FastSymbolTable;
import compiler.seman.name.env.SymbolTable;
import compiler.seman.type.TypeChecker;
import compiler.seman.type.type.Type;
=======
import java.nio.file.Files;
import java.nio.file.Paths;

import cli.PINS;
import cli.PINS.Phase;
import compiler.lexer.Lexer;
>>>>>>> repo1/main
=======
import compiler.lexer.Lexer;
import compiler.parser.Parser;
>>>>>>> repo2/main
=======
import compiler.common.PrettyPrintVisitor1;
import compiler.lexer.Lexer;
import compiler.parser.Parser;
>>>>>>> repo3/main

public class Main {
    /**
     * Metoda, ki izvede celotni proces prevajanja.
     * 
     * @param args parametri ukazne vrstice.
     */
    public static void main(String[] args) throws Exception {
        var cli = PINS.parse(args);
        run(cli);
    }


    // -------------------------------------------------------------------


    private static void run(PINS cli) throws IOException {
        var sourceCode = Files.readString(Paths.get(cli.sourceFile));
        run(cli, sourceCode);
    }

    private static void run(PINS cli, String sourceCode) {
        /**
         * Izvedi leksikalno analizo.
         */
        var symbols = new Lexer(sourceCode).scan();
        if (cli.dumpPhases.contains(Phase.LEX)) {
            for (var symbol : symbols) {
                System.out.println(symbol.toString());
            }
        }
        if (cli.execPhase == Phase.LEX) {
            return;
        }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
        /**
         * Izvedi sintaksno analizo.
         */
        Optional<PrintStream> out = cli.dumpPhases.contains(Phase.SYN) 
                ? Optional.of(System.out)
                : Optional.empty();
        var parser = new Parser(symbols, out);
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> repo3/main
        var ast = parser.parse();
        if (cli.execPhase == Phase.SYN) {
            return;
        }
        /**
         * Abstraktna sintaksa.
         */
<<<<<<< HEAD
        var prettyPrint = new PrettyPrintVisitor4(2, System.out);
=======
        var prettyPrint = new PrettyPrintVisitor1(2, System.out);
>>>>>>> repo3/main
        if (cli.dumpPhases.contains(Phase.AST)) {
            ast.accept(prettyPrint);
        }
        if (cli.execPhase == Phase.AST) {
            return;
        }
<<<<<<< HEAD
        /**
         * Izvedi razreševanje imen.
         */
        SymbolTable symbolTable = new FastSymbolTable();
        var definitions = new NodeDescription<Def>();
        var nameChecker = new NameChecker(definitions, symbolTable);
        ast.accept(nameChecker);
        if (cli.dumpPhases.contains(Phase.NAME)) {
            prettyPrint.definitions = Optional.of(definitions);
            ast.accept(prettyPrint);
        }
        if (cli.execPhase == Phase.NAME) {
            return;
        }
        /**
         * Izvedi preverjanje tipov.
         */
        var types = new NodeDescription<Type>();
        var typeChecker = new TypeChecker(definitions, types);
        ast.accept(typeChecker);
        if (cli.dumpPhases.contains(Phase.TYP)) {
            prettyPrint.definitions = Optional.of(definitions);
            prettyPrint.types = Optional.of(types);
            ast.accept(prettyPrint);
        }
        if (cli.execPhase == Phase.TYP) {
            return;
        }
        /**
         * Izvedi analizo klicnih zapisov in dostopov.
         */
        var frames = new NodeDescription<Frame>();
        var accesses = new NodeDescription<Access>();
        var frameEvaluator = new FrameEvaluator(frames, accesses, definitions, types);
        ast.accept(frameEvaluator);
        if (cli.dumpPhases.contains(Phase.FRM)) {
            prettyPrint.definitions = Optional.of(definitions);
            prettyPrint.types = Optional.of(types);
            prettyPrint.frames = Optional.of(frames);
            prettyPrint.accesses = Optional.of(accesses);
            ast.accept(prettyPrint);
        }
        if (cli.execPhase == Phase.FRM) {
            return;
        }
        /**
         * Generiranje vmesne kode.
         */
        var generator = new IRCodeGenerator(new NodeDescription<>(), frames, accesses, definitions, types);
        ast.accept(generator);
        if (cli.dumpPhases.contains(Phase.IMC)) {
            new IRPrettyPrint(System.out, 2).print(generator.chunks);
        }
        if (cli.execPhase == Phase.IMC) {
            return;
        }
        /**
         * Linearizacija vmesne kode.
         */
        var memory = new Memory(cli.memory);
        var mainCodeChunk = new LinCodeGenerator(memory).generateCode(generator.chunks);
        if (!cli.dumpPhases.contains(Phase.INT)) {
            return;
        }
        /**
         * Izvajanje vmesne kode.
         */
        if (mainCodeChunk.isPresent()) {
            Optional<PrintStream> outputStream = cli.dumpPhases.contains(Phase.INT) ? Optional.of(System.out) : Optional.empty();
            var interpreter = new Interpreter(memory, outputStream);
            interpreter.interpret(mainCodeChunk.get());
        }
=======
>>>>>>> repo1/main
=======
        parser.parse();
        if (cli.execPhase == Phase.SYN) {
            return;
        }
>>>>>>> repo2/main
=======
>>>>>>> repo3/main
    }
}
