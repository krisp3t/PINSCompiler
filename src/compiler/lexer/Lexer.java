/**
 * @Author: turk
 * @Description: Leksikalni analizator.
 */

package compiler.lexer;

import common.Report;

import static common.RequireNonNull.requireNonNull;
import static java.util.Map.entry;

import java.util.*;

public class Lexer {
    /**
     * Izvorna koda.
     */
    private final String source;
    private lexStanja stanje = lexStanja.INITIAL;
    private Position pozicija = new Position(1, 1, 1, 1);
    private StringBuilder trenutniNiz = new StringBuilder();
    private int stNarekovajev = 0;
    private int vrstica = 1;
    private int stolpec = 0;
    private boolean zakljucenNiz = true;


    /**
     * Preslikava iz ključnih besed v vrste simbolov.
     */
    private final static Map<String, TokenType> keywordMapping;

    static {
        keywordMapping = new HashMap<>();
        for (var token : TokenType.values()) {
            var str = token.toString();
            if (str.startsWith("KW_")) {
                keywordMapping.put(str.substring("KW_".length()).toLowerCase(), token);
            }
            if (str.startsWith("AT_")) {
                keywordMapping.put(str.substring("AT_".length()).toLowerCase(), token);
            }
        }
    }

    /**
     * Tipi možnih stanj, v katerem je leksikalni analizator.
     */
    enum lexStanja {
        KONST_INT, KONST_STR, IME, OPERATOR, KOMENTAR, INITIAL
    }

    /**
     * Inicializiraj možne operatorje.
     */
    static final HashSet<Character> BELO_BESEDILO = new HashSet<>(Arrays.asList(' ', '\t', '\n', '\r'));
    static final HashSet<Character> OPERATORJI = new HashSet<>(Arrays.asList('+', '-', '*', '/', '%', '&', '|', '!', '<', '>', '(', ')', '[', ']', '{', '}', ':', ';', '.', ',', '='));
    static final HashSet<String> LOGICNI = new HashSet<>(Arrays.asList("true", "false"));
    static final Map<String, TokenType> operatorMapping = Map.ofEntries(
            entry("+", TokenType.OP_ADD),
            entry("-", TokenType.OP_SUB),
            entry("*", TokenType.OP_MUL),
            entry("/", TokenType.OP_DIV),
            entry("%", TokenType.OP_MOD),
            entry("&", TokenType.OP_AND),
            entry("|", TokenType.OP_OR),
            entry("!", TokenType.OP_NOT),
            entry("==", TokenType.OP_EQ),
            entry("!=", TokenType.OP_NEQ),
            entry("<", TokenType.OP_LT),
            entry(">", TokenType.OP_GT),
            entry("<=", TokenType.OP_LEQ),
            entry(">=", TokenType.OP_GEQ),
            entry("(", TokenType.OP_LPARENT),
            entry(")", TokenType.OP_RPARENT),
            entry("[", TokenType.OP_LBRACKET),
            entry("]", TokenType.OP_RBRACKET),
            entry("{", TokenType.OP_LBRACE),
            entry("}", TokenType.OP_RBRACE),
            entry(":", TokenType.OP_COLON),
            entry(";", TokenType.OP_SEMICOLON),
            entry(".", TokenType.OP_DOT),
            entry(",", TokenType.OP_COMMA),
            entry("=", TokenType.OP_ASSIGN)
    );


    /**
     * Ustvari nov analizator.
     *
     * @param source Izvorna koda programa.
     */
    public Lexer(String source) {
        requireNonNull(source);
        this.source = source;
    }

    /**
     * Določi stanje leksikalnega analizatorja iz začetnega stanja.
     */
    private lexStanja dolociZacetnoStanje(char naslednjiZnak) {
        if (naslednjiZnak == '_' || Character.isLetter(naslednjiZnak)) {
            return lexStanja.IME;
        } else if (Character.isDigit(naslednjiZnak)) {
            return lexStanja.KONST_INT;
        } else if (naslednjiZnak == '\'') {
            this.zakljucenNiz = false;
            return lexStanja.KONST_STR;
        } else if (naslednjiZnak == '#') {
            return lexStanja.KOMENTAR;
        } else if (OPERATORJI.contains(naslednjiZnak)) {
            return lexStanja.OPERATOR;
        } else {
            return lexStanja.INITIAL;
        }
    }

    private void handleStanje(char naslednjiZnak, ArrayList<Symbol> symbols) {
        if (naslednjiZnak == '\n') {
            this.vrstica++;
            this.stolpec = 0;
        } else if (naslednjiZnak == 9) { // tabulator
            this.stolpec += 3;
        } else if ((naslednjiZnak == '#') && (this.stanje != lexStanja.KONST_STR)) {
            this.stanje = lexStanja.KOMENTAR;
            return;
        }

        switch (this.stanje) {
            case KOMENTAR:
                // Komentar se razteza do konca vrstice.
                if (naslednjiZnak == '\n')
                    this.stanje = lexStanja.INITIAL;
                break;
            case IME:
                if (naslednjiZnak == '\'') { // Začetek string literala
                    preveriIme(symbols);
                    this.stanje = lexStanja.INITIAL;
                } else if (BELO_BESEDILO.contains(naslednjiZnak)) { // Belo besedilo - konec imena
                    preveriIme(symbols);
                    this.stanje = lexStanja.INITIAL;
                } else if (OPERATORJI.contains(naslednjiZnak)) { // Operator - konec imena
                    preveriIme(symbols);
                    this.trenutniNiz = new StringBuilder();
                    this.trenutniNiz.append(naslednjiZnak);
                    this.pozicija = Position.fromLocation(new Position.Location(this.vrstica, this.stolpec));
                    this.stanje = lexStanja.OPERATOR;
                } else {
                    this.trenutniNiz.append(naslednjiZnak);
                }
                break;
            case KONST_INT:
                if (Character.isDigit(naslednjiZnak))
                    this.trenutniNiz.append(naslednjiZnak);
                else {
                    symbols.add(new Symbol(new Position(this.pozicija.start.line, this.pozicija.start.column, this.vrstica, this.stolpec - 1), TokenType.C_INTEGER, trenutniNiz.toString()));
                    this.stanje = lexStanja.INITIAL;
                    handleStanje(naslednjiZnak, symbols);
                }
                break;
            case KONST_STR:
                if ((this.stNarekovajev % 2 == 1) && (this.trenutniNiz.length() > 1)) { // Ne gledamo na začetku niza, ko samo '
                    if (naslednjiZnak == '\'') { // Dva narekovaja escape char
                        this.trenutniNiz.append(naslednjiZnak);
                        this.stNarekovajev++;
                    } else { // Konec string literala
                        String leksem = trenutniNiz.toString().replaceAll("'{2}", "'"); // '' -> '
                        if (leksem.length() > 1)
                            leksem = leksem.substring(1, leksem.length() - 1);
                        symbols.add(new Symbol(new Position(this.pozicija.start.line, this.pozicija.start.column, this.vrstica, this.stolpec - 1), TokenType.C_STRING, leksem)); // leksem brez narekovajev
                        zakljucenNiz = true;
                        this.stanje = lexStanja.INITIAL;
                        handleStanje(naslednjiZnak, symbols);
                    }
                } else {
                    this.trenutniNiz.append(naslednjiZnak);
                    this.stNarekovajev = (naslednjiZnak == '\'') ? this.stNarekovajev + 1 : 0;
                }
                break;
            case OPERATOR:
                String kandidat = trenutniNiz.toString() + naslednjiZnak;
                if (operatorMapping.containsKey(kandidat)) { // Če bi z naslednjim znakom dobili operator dolžine 2
                    symbols.add(new Symbol(new Position(this.pozicija.start.line, this.pozicija.start.column, this.vrstica, this.stolpec), operatorMapping.get(kandidat), kandidat));
                    this.stanje = lexStanja.INITIAL;
                } else { // npr. "+b"
                    symbols.add(new Symbol(new Position(this.pozicija.start.line, this.pozicija.start.column, this.vrstica, this.stolpec - 1), operatorMapping.get(trenutniNiz.toString()), trenutniNiz.toString()));
                    this.stanje = lexStanja.INITIAL;
                    handleStanje(naslednjiZnak, symbols);
                }
                break;
            case INITIAL:
                this.pozicija = Position.fromLocation(new Position.Location(this.vrstica, this.stolpec));
                this.trenutniNiz = new StringBuilder();
                this.stanje = dolociZacetnoStanje(naslednjiZnak);
                if (this.stanje == lexStanja.IME || this.stanje == lexStanja.KONST_INT || this.stanje == lexStanja.KONST_STR || this.stanje == lexStanja.OPERATOR)
                    this.trenutniNiz.append(naslednjiZnak);
                break;
        }

    }

    private void preveriIme(ArrayList<Symbol> symbols) {
        int startVrstica = this.pozicija.start.line;
        int startStolpec = this.pozicija.start.column;
        if (keywordMapping.containsKey(trenutniNiz.toString().toLowerCase()))
            symbols.add(new Symbol(new Position(startVrstica, startStolpec, this.vrstica, this.stolpec - 1), keywordMapping.get(trenutniNiz.toString().toLowerCase()), trenutniNiz.toString()));
        else if (LOGICNI.contains(trenutniNiz.toString().toLowerCase()))
            symbols.add(new Symbol(new Position(startVrstica, startStolpec, this.vrstica, this.stolpec - 1), TokenType.C_LOGICAL, trenutniNiz.toString().toLowerCase()));
        else
            symbols.add(new Symbol(new Position(startVrstica, startStolpec, this.vrstica, this.stolpec - 1), TokenType.IDENTIFIER, trenutniNiz.toString()));
    }

    /**
     * Izvedi leksikalno analizo.
     *
     * @return seznam leksikalnih simbolov.
     */
    public List<Symbol> scan() {
        var symbols = new ArrayList<Symbol>();
        for (int i = 0; i < this.source.length(); i++) {
            var naslednjiZnak = this.source.charAt(i);
            this.stolpec++;
            handleStanje(naslednjiZnak, symbols);

        }
        if (this.source.length() > 0) {
            this.stolpec++;
            handleStanje(' ', symbols); // Pohendlaj še zadnji char
        }
        if (!this.zakljucenNiz) {
            Report.error(new Position(this.vrstica, this.stolpec, this.vrstica, this.stolpec), "NAPAKA: Konstanta string ni zaključena!");
        }

        symbols.add(new Symbol(new Position(this.vrstica, this.stolpec + 1, this.vrstica, this.stolpec), TokenType.EOF, "$"));

        return symbols;
    }
}
