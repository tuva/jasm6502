
/**
*	A error exception thrown by a Lexer when it encounters Lexical errors.
*	Theese are errors that concern the vocabulary of the programming language.
*
*	@author David Schager 2006
*/
public class LexerException extends Exception {
	/**
	*	Constructor
	*	@param error Error message describing what the lexical error was
	*	@param file The file name of current source file where error was encountered
	*	@param lineNum The line number where error was encountered
	*	@param colNum The column number where error was encountered
	*	@param line String containing the actual line containing the error
	*/
	public LexerException (String error, String file, int lineNum, String line) {
		super (error + " in file " + file + ", line " + lineNum + ":\n" + line);
	}
}
