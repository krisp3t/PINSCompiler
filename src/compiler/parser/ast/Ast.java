/**
 * @ Author: turk
 * @ Description: Vozlišče v abstraktnem sintaksnem drevesu.
 */

package compiler.parser.ast;

import compiler.common.Visitor;
import compiler.lexer.Position;

public abstract class Ast {
    /**
     * Lokacija vozlišča v izvorni kodi.
     */
<<<<<<< HEAD
	public final Position position;

	/**
	 * Ustvari novo vozlišče.
	 * 
	 * @param position Lokacija.
	 */
	public Ast(Position position) {
		this.position = position;
	}
=======
    public final Position position;

    /**
     * Ustvari novo vozlišče.
     *
     * @param position Lokacija.
     */
    public Ast(Position position) {
        this.position = position;
    }
>>>>>>> repo3/main

    /**
     * 'Sprejmi' obiskovalca.
     */
<<<<<<< HEAD
	public abstract void accept(Visitor visitor);
=======
    public abstract void accept(Visitor visitor);
>>>>>>> repo3/main
}
