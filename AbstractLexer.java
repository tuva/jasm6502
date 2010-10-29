import java.io.*;
import java.util.Vector;

/**
*	Interface for a Lexer, whose purpose is to analyse a source file, 
*	and return atomic symbols from it, one at a time.
*	@author David Schager 2006
*/
public interface AbstractLexer {
	/**
	*	Attachs a source file to Lexer.
	*	@param filename of the source file to attach
	*/
	public abstract void attachInput (String filename);
	/**
	*	Restarts the lexicalical analysis from beginning of source file.
	*/
	public abstract void reset ();
	/**
	*	Extracts and returns a line from file.
	*	@param lineNum Line number of the line to return
	*	@return String from lineNum
	*/
	public abstract String xtractLine (int lineNum);
	/**
	*	Returns the current line number in source file.
	*/
	public abstract int getLineNum ();
	/**
	*	Returns the current column number in source file.
	*/
	public abstract int getColNum ();
	/**
	*	Advance pointer to the next line in file
	*/
	public abstract void nextLine ();
	/**
	*	Returns the file name of current source file.
	*	@return The file name
	*/
	public abstract String getFilename ();
	/**
	*	Checks if End Of File were encountered.
	*	@return true for condition met.
	*/
	public abstract boolean eof ();
	/**
	*	Outputs object
	*	@return object statistics
	*/
	public abstract String toString ();
	/**
	*	Returns the next atomic symbol, and advance position to next symbol.
	*	@return The next @see Symbol
	*	@throws LexerException on lexicalical errors
	*/
	public abstract Symbol getNext () throws LexerException;
	/**
	*	Returns the next atomic symbol, without advancing.
	*	@return The next @see Symbol
	*	@throws LexerException on lexicalical errors
	*/
	public abstract Symbol peekNext () throws LexerException;
	/**
	*	Returns Character sequence
	*	@return String containing a character sequence
	*/
	public abstract String getCharSequence ();
	/**
	*	Returns a new quasi clone of this lexer, only the symboltables are the same.
	*/
	public abstract AbstractLexer copy ();
	/**
	*	Tells lexer to keep a mark on current line number
	*/
	public abstract void rememberLineNum ();
	/**
	*	Tells lexer to return the last mark on current line number
	*/
	public abstract int getRememberLineNum ();
	/**
	*	Returns symbol table with identifiers
	*/
	public abstract SymbolTable getSymbolTable ();
}
