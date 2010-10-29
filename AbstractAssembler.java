import java.io.*;
/**
*	An interface describing an assembler
*	@author David Schager 2006
*/
public interface AbstractAssembler {
	/**
	*	Returns the Lexer object that this assembler is using
	*	@return The @see AbstractLexer
	*/
	public abstract AbstractLexer getLexer ();
	/**
	*	Returns the parser object that this assembler is using
	*	@return The @see AbstractParser
	*/
	public abstract AbstractParser getParser ();
	/**
	*	Assembles a source file
	*	@param filename	File name of source file to assemble
	*	@return Error code indicating level of success of assembly. 
	*	A 1 indicates error, and a 0 indicates success.
	*	@throws IOException on error while reading file
	*/
	public abstract int assemble (String sourceFile, String outFileName) throws IOException;
	/**
	*	Sets a mode
	*	0 = output to screen
	*	1 = output to disk in raw binary format
	*	2 = output to .prg file
	*	1 is default
	*/
	public abstract void setMode (int mode);
	/**
	*	Sets an ascii translation mode for the target architecture
	*/
	public abstract void setAsciiTranslation (int mode);
}

