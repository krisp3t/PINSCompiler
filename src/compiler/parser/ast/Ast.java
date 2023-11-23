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
<<<<<<< HEAD
<<<<<<< HEAD
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
=======
>>>>>>> repo4/main
=======
>>>>>>> repo5/main
=======
>>>>>>> repo6/main
    public final Position position;

    /**
     * Ustvari novo vozlišče.
     *
     * @param position Lokacija.
     */
    public Ast(Position position) {
        this.position = position;
    }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> repo3/main
=======
>>>>>>> repo4/main
=======
>>>>>>> repo5/main
=======
>>>>>>> repo6/main

    /**
     * 'Sprejmi' obiskovalca.
     */
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
	public abstract void accept(Visitor visitor);
=======
    public abstract void accept(Visitor visitor);
>>>>>>> repo3/main
=======
    public abstract void accept(Visitor visitor);
>>>>>>> repo4/main
=======
    public abstract void accept(Visitor visitor);
>>>>>>> repo5/main
=======
    public abstract void accept(Visitor visitor);
>>>>>>> repo6/main
}
