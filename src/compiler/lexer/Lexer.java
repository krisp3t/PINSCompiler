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

    /**
     * Izvedi leksikalno analizo.
     *
     * @return seznam leksikalnih simbolov.
     */
    public List<Symbol> scan() {
        var symbols = new ArrayList<Symbol>();
        var trenutniNiz = new StringBuilder();

        for (int i = 0; i < this.source.length(); i++) {
            var naslednjiZnak = this.source.charAt(i);
            // this.konecVrstica++;
            // this.konecStolpec++;
            System.out.printf("%c %s\n", naslednjiZnak, this.stanje); // TODO: remove


            // Initial stanje
            if (trenutniNiz.length() == 0) {
                //this.zacetekVrstica = this.konecVrstica;
                //this.zacetekStolpec = this.konecStolpec;
                this.stanje = dolociZacetnoStanje(naslednjiZnak);
                if (this.stanje == lexStanja.IME || this.stanje == lexStanja.KONST_INT || this.stanje == lexStanja.KONST_STR)
                    trenutniNiz.append(naslednjiZnak);
                continue;
            }

            switch (this.stanje) {
                case KOMENTAR:
                    if (naslednjiZnak == '\n')
                        this.stanje = lexStanja.INITIAL;
                    break;
                case IME:
                    if (BELO_BESEDILO.contains(naslednjiZnak)) {
                        if (keywordMapping.containsKey(trenutniNiz.toString().toLowerCase()))
                            symbols.add(new Symbol(pozicija, keywordMapping.get(trenutniNiz.toString().toLowerCase()), trenutniNiz.toString()));
                        else
                            symbols.add(new Symbol(pozicija, TokenType.IDENTIFIER, trenutniNiz.toString()));
                        trenutniNiz = new StringBuilder();
                        this.stanje = lexStanja.INITIAL;
                    } else if (OPERATORJI.contains(naslednjiZnak)) {
                        if (keywordMapping.containsKey(trenutniNiz.toString().toLowerCase())) {
                            symbols.add(new Symbol(pozicija, keywordMapping.get(trenutniNiz.toString().toLowerCase()), trenutniNiz.toString()));
                        } else {
                            symbols.add(new Symbol(pozicija, TokenType.IDENTIFIER, trenutniNiz.toString()));
                        }
                        trenutniNiz = new StringBuilder();
                        trenutniNiz.append(naslednjiZnak);
                        this.stanje = lexStanja.OPERATOR;
                    } else {
                        trenutniNiz.append(naslednjiZnak);
                    }
                    break;
                case KONST_INT:
                    break;
                case KONST_STR:
                    break;
                case OPERATOR:
                    String kandidat = trenutniNiz.toString() + naslednjiZnak;
                    if (operatorMapping.containsKey(kandidat)) {
                        symbols.add(new Symbol(pozicija, operatorMapping.get(kandidat), kandidat));
                    } else {
                        symbols.add(new Symbol(pozicija, operatorMapping.get(trenutniNiz.toString()), trenutniNiz.toString()));
                    }
                    trenutniNiz = new StringBuilder();
                    this.stanje = dolociZacetnoStanje(naslednjiZnak);
                    i--;
                    break;
            }
        }
        return symbols;
    }
}
