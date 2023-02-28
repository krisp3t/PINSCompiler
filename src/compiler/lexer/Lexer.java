/**
 * @Author: turk
 * @Description: Leksikalni analizator.
 */

package compiler.lexer;

import static common.RequireNonNull.requireNonNull;
import static java.util.Map.entry;

import java.util.*;

public class Lexer {
    /**
     * Izvorna koda.
     */
    private final String source;
    private lexStanja stanje = lexStanja.INITIAL;
    private Position pozicija = new Position(Position.Location.zero(), Position.Location.zero());
    private StringBuilder trenutniNiz = new StringBuilder();
    private int stNarekovajev = 0;


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
    static final HashSet<Character> BELO_BESEDILO = new HashSet<Character>(Arrays.asList(' ', '\t', '\n', '\r'));
    static final HashSet<Character> OPERATORJI = new HashSet<Character>(Arrays.asList('+', '-', '*', '/', '%', '&', '|', '!', '<', '>', '(', ')', '[', ']', '{', '}', ':', ';', '.', ',', '='));
    static final HashSet<String> LOGICNI = new HashSet<String>(Arrays.asList("true", "false"));
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
        if (naslednjiZnak == '#') {
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
                    this.stanje = lexStanja.OPERATOR;
                } else {
                    this.trenutniNiz.append(naslednjiZnak);
                }
                break;
            case KONST_INT:
                if (Character.isDigit(naslednjiZnak))
                    this.trenutniNiz.append(naslednjiZnak);
                else {
                    symbols.add(new Symbol(pozicija, TokenType.C_INTEGER, trenutniNiz.toString()));
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
                        symbols.add(new Symbol(pozicija, TokenType.C_STRING, leksem));
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
                    symbols.add(new Symbol(pozicija, operatorMapping.get(kandidat), kandidat));
                    this.stanje = lexStanja.INITIAL;
                } else { // npr. "+b"
                    symbols.add(new Symbol(pozicija, operatorMapping.get(trenutniNiz.toString()), trenutniNiz.toString()));
                    this.stanje = lexStanja.INITIAL;
                    handleStanje(naslednjiZnak, symbols);
                }
                break;
            case INITIAL:
                this.trenutniNiz = new StringBuilder();
                this.stanje = dolociZacetnoStanje(naslednjiZnak);
                if (this.stanje == lexStanja.IME || this.stanje == lexStanja.KONST_INT || this.stanje == lexStanja.KONST_STR || this.stanje == lexStanja.OPERATOR)
                    this.trenutniNiz.append(naslednjiZnak);
                break;
        }

    }

    private void preveriIme(ArrayList<Symbol> symbols) {
        if (keywordMapping.containsKey(trenutniNiz.toString().toLowerCase()))
            symbols.add(new Symbol(this.pozicija, keywordMapping.get(trenutniNiz.toString().toLowerCase()), trenutniNiz.toString()));
        else if (LOGICNI.contains(trenutniNiz.toString().toLowerCase()))
            symbols.add(new Symbol(this.pozicija, TokenType.C_LOGICAL, trenutniNiz.toString().toLowerCase()));
        else
            symbols.add(new Symbol(this.pozicija, TokenType.IDENTIFIER, trenutniNiz.toString()));
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
            // this.konecVrstica++;
            // this.konecStolpec++;

            handleStanje(naslednjiZnak, symbols);

        }
        handleStanje(this.source.charAt(this.source.length() - 1), symbols); // Pohendlaj še zadnji char
        symbols.add(new Symbol(this.pozicija, TokenType.EOF, ""));

        for (Symbol s : symbols) {
            System.out.println(s);
        }
        return symbols;
    }
}
