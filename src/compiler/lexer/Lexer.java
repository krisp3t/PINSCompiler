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
    private String stanje;
    private int zacetekVrstica = 1;
    private int zacetekStolpec = 0;
    private int konecVrstica = 1;
    private int konecStolpec = 0;

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
    enum STANJA {
        KONST_INT, KONST_STR, IME, OPERATOR, KOMENTAR, INITIAL
    }

    /**
     * Inicializiraj možne operatorje.
     */
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
     * Izvedi leksikalno analizo.
     *
     * @return seznam leksikalnih simbolov.
     */
    public List<Symbol> scan() {
        var symbols = new ArrayList<Symbol>();
        var trenutniNiz = new StringBuilder();

        for (int i = 0; i < this.source.length(); i++) {
            var naslednjiZnak = this.source.charAt(i);
            this.konecVrstica++;
            this.konecStolpec++;
            System.out.printf("%c (%d:%d)\n", naslednjiZnak, this.konecVrstica, this.konecStolpec); // TODO: remove


            // Initial stanje
            if (trenutniNiz.length() == 0) {
                this.zacetekVrstica = this.konecVrstica;
                this.zacetekStolpec = this.konecStolpec;
                if (naslednjiZnak == '_' || Character.isLetter(naslednjiZnak)) {
                    this.stanje = String.valueOf(STANJA.IME);
                    trenutniNiz.append(naslednjiZnak);
                } else if (Character.isDigit(naslednjiZnak)) {
                    this.stanje = String.valueOf(STANJA.KONST_INT);
                    trenutniNiz.append(naslednjiZnak);
                } else if (naslednjiZnak == '\'') {
                    this.stanje = String.valueOf(STANJA.KONST_STR);
                    trenutniNiz.append(naslednjiZnak);
                } else if (naslednjiZnak == '#') {
                    this.stanje = String.valueOf(STANJA.KOMENTAR);
                } else if (OPERATORJI.contains(naslednjiZnak)) {
                    this.stanje = String.valueOf(STANJA.OPERATOR);
                }
            }
        }
        return symbols;
    }
}
