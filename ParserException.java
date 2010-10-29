
/**
*	A error exception thrown by a Parser when it encounters Syntactic errors.
*	Theese are errors that concern the grammar of the programming language.
*
*	@author David Schager 2006
*/
public class ParserException extends Exception {
	/**
	*	Constructor
	*	@param error Error message describing what the lexical error was
	*	@param Lexer Lexer object
	*	@see AbstractLexer
	*/
	public ParserException (String error, AbstractLexer lexer) {
		super (error + " in file " + 
			lexer.getFilename () + ", line " + 
			lexer.getRememberLineNum () + ":\n" +
			lexer.xtractLine (lexer.getRememberLineNum ()));
	}
}
