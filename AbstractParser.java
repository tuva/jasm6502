/**
*	A parser whose purpose is to syntacticaly parse a source file of some programming language.
*	The parser implements syntactic analysis of the language, and determines if each statement is
*	syntactically correct.
*	(Nothing implemented yet)
*	@author David Schager 2006
*/
public interface AbstractParser {
	/**
	*
	*/
	public abstract int doNonTerminal () throws ParserException, LexerException;
	/**
	*
	*/
	public abstract String toString ();
}
