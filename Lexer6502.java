import java.io.*;
import java.util.Vector;

/**
*	Analyses a source file, and returns atomic symbols from it.
*	Implements @see AbstractLexer
*	Uses constants from @see SymbolConstant6502
*
*	@todo This lexer works on Linux, but might totaly misfunction if other linefeed characters than linefeed (ascii 10) is present
*	@todo fix bug with linenumbers not updating as they should.
*	
*	@author David Schager 2006
*/
public class Lexer6502 implements SymbolConstant6502, AbstractLexer {
	private static final int STATE_READ_LINE = 0;
	private static final int STATE_SKIP_WHITE = 1;
	private int mLine = 1;
	private int mCol = 1;
	private int mLastCol = 1;
	private char mLastChar = '\0';
	private int mState = STATE_READ_LINE;
	private String mBuffer = "";
	private String mCommentChars;
	private boolean mEOF = false;
	private boolean mLinefeed = false;
	private Symbol mSymbol = null;
	private String mFilename = "";
	private String mCharSequence = "";
	private int mRadix = 10;
	private SymbolTable mKeywords = null;
	private SymbolTable mSymbolTable = null;
	private boolean mCaseInsensitive = true;
	private int mRembLine = 1;
	
	private RandomAccessFile mFile = null;

	/**
	*	Constructor
	*	@param keywords A symbol table filled with Reserved symbols for the language
	*	@param symbolTable An empty symbol table that the Lexer will insert new symbols into, when it finds them.
	*	@param commentChars a string describing characters that signals that what follows it are comments and should be skipped.
	*/
	public Lexer6502 (SymbolTable keywords, SymbolTable symbolTable, String commentChars) 
	{
		mSymbolTable = symbolTable;
		mKeywords = keywords;
		mCommentChars = commentChars;
	}
	
	/**
	*	Prints an error message and terminates program.
	*/
	private void errorExit (String str)
	{
		System.err.println (str);
		System.exit (1);
	}
	
	/**
	*	Gets current char, transforms to lower case, and advance file pointer
	*	@return next char
	*/
	private char getChar () 
	{
		int c = 0;
		try {
			c = mFile.read ();
			// try handle non-unix linefeeds..
			if (c == 0x0d) {
				// mac or windows linefeed, check next char and see (dont selfrecurse, will cause endless loop)
				long fp = mFile.getFilePointer ();
				int d = mFile.read ();
				if (d == 0x0a) {
					// windows linebreak
					c = d;
				}
				else {
					// mac linebreak, putback character, and transform \r into \n
					mFile.seek (fp);
					c = 0x0a;
				}
			}
		}
		catch (IOException e) {
			errorExit ("Lexer: " + e);
		}
		
		
		// check eof or linefeed..
		if (c == -1) {
			mEOF = true;
			// EOF
		}
		else if (c == 0x0a) {
			++mLine;
			mCol = mLastCol = 1;
		}
		else {
			mLastCol = ++mCol;
		}
		
		if (mCaseInsensitive) {
			// case insensitive mode is set at all times, EXCEPT when reading a character string
			// withint double quotes..
			if (c >= 65 && c <= 90) c += 32;	// to lower case
		}
		mLastChar = (char) c;

		return (char) c;
	}
	
	/**
	*	Retracts the filepointer one step backwards, and sets EOF to zero if it were signalled.
	*	Note, it is only supported to call putBack ONCE.
	*/
	private void putBack ()
	{
		try {
			long fp = mFile.getFilePointer ();
			mFile.seek (fp - 1);
		}
		catch (IOException e) {
			errorExit ("Lexer: " + e);
		}
		
		if (mLastChar == 0x0a) {
			--mLine;
			mCol = mLastCol;
		}
		else {
			--mCol;
			mLastCol = mCol;
		}
		mEOF = false;
	}
	
	/**
	*	Checks if char is a tab or space
	*	@param c character to check
	*	@return true for whitespace
	*/
	private boolean isWhite (char c) 
	{
		return (c == ' ' || c == '\t')
			? true : false;
	}
	
	/**
	*	Checks if a linefeed were encountered after the current symbol
	*	@return true for linefeed
	*/
	private boolean linefeedFollows ()
	{
		return mLinefeed;
	}
	
	/**
	*	Checks if char is a linefeed
	*	@param c character to check
	*	@return true for linefeed (\n)
	*/
	private boolean isLinefeed (char c) 
	{
		return (c == '\n') ? true : false;
	}
	
	
	/**
	*	Scans to next line
	*/
	public void nextLine ()
	{
		char c = '\0';
		while (!eof () && c != '\n') c = getChar ();
		if (!eof ()) putBack ();
	}
	
	/**
	*	Checks if current char is a comment
	*	@param c character to check
	*	@return true if comment found
	*/
	private boolean isComment (char c)
	{
		boolean wasComment = false;
		for (int i = 0; i < mCommentChars.length (); ++i) {
			if (c == mCommentChars.charAt (i)) {
				wasComment = true;
				break;
			}
		}
		return wasComment;
	}
	
	/**
	*	Checks if current char is a comment, and if it is, scans to next line
	*	@param c character to check
	*	@return true if comment found
	*/
	private boolean skipComment (char c)
	{
		boolean wasComment = isComment (c);
		if (wasComment) nextLine ();
		return wasComment;
	}
	
	/**
	*	Advance filepointer until a character is found that is neither whitespace or newline
	*/
	private void skipWhiteSpace () 
	{
		while (!eof ()) {
			char c = getChar ();
			if (isLinefeed (c)) {
				setLinefeed (true);
			}
			if (skipComment (c)) continue;
			if (!isWhite (c) && !isLinefeed (c) && !eof ()) {
				putBack ();
				break;
			}
		}
	}
	
	/**
	*	Sets linefeed signal to false or true
	*	@param s true or false
	*/
	private void setLinefeed (boolean s)
	{
		mLinefeed = s;
	}
	
	/**
	*	Skip whitespace and change state (called by statemachine in @see Lexer6502.getNext)
	*/
	private boolean stateSkipWhite () 
	{
		skipWhiteSpace ();
		mState = STATE_READ_LINE;
		return false;
	}
	
	/**
	*	Reads next symbol from current line
	*	@return process status. true if statemachine should go on to skip whitespace, or false if EOF been found.
	*/
	private boolean stateReadLine () throws LexerException
	{
		char c = '\0';
		while (!eof ()) {
			c = getChar ();
			if (isWhite (c)) {
				skipWhiteSpace ();
				break;
			}
			else if (isLinefeed (c)) {
				setLinefeed (true);
				break;
			}
			else {
				mBuffer += c;
				boolean success = getSymbol (mBuffer);
				if (success) break;
			}
		}
		mState = STATE_SKIP_WHITE;
		return !eof ();
	}
	
	/**
	*	Checks if character is a-z or A-Z or underscore
	*	@param c character to check
	*	@return true if alpha
	*/
	private boolean isAlpha (char c) {
		return ((c <= 'Z' && c >= 'A') || (c <= 'z' && c >= 'a') || c == '_') ? true : false;
	}
	
	/**
	*	Checks if character is a digit
	*	@param c character to check
	*	@param radix the number base of the number system currently in
	*/
	private boolean isDigit (char c, int radix) {
		boolean success = true;
		if (c < '0') {
			success = false;
		}
		else if (radix <= 10) {
			if (c > '0' + radix - 1) {
				success = false;
			}
		}
		else if (radix >= 11) {
			if (c > '9') {
				if (c >= 'a' + (radix - 10)) {
					if (c >= 'A' + (radix - 10)) {
						success = false;
					}
				}
			}
		}
		return success;
	}
	
	/**
	*	Tries to analyze a numeric constant from current position in stream
	*	@param buffer the char buffer to insert chars into
	*	@param next the lookahead character in stream
	*	@return true if a numeric constant was found, and installed to member mSymbol
	*	@throws LexerException on syntax errors in the sourcefile
	*/
	private boolean scanConstant (String buffer, char next) throws LexerException {
		boolean success = false;
		if (!isDigit (next, mRadix)) {
			int value = 0;
			try {
				value = Integer.parseInt (buffer, mRadix);
			}
			catch (NumberFormatException e) {
				throw new LexerException ("Integer error", mFilename, mLine, xtractLine (mLine));
			}
			mSymbol = new Symbol ("", CONSTANT, value);
			success = true;
		}
		return success;
	}
	
	/**
	*	Scan for all other symbols than numeric constants. They can be reserved keywords from the 
	*	mKeywords symbol table, or undefined symbols which will be installed into mSymbolTable, or
	*	previously installed symbols from symbolTable.
	*	@param buffer The character buffer to insert scanned characters into
	*	@param first The first character in the buffer
	*	@param next The lookahead character in stream
	*	@throws LexerException on syntax errors in the sourcefile
	*/
	private boolean scanMixed (String buffer, char first, char next) throws LexerException
	{

		boolean success = false;
		if (mKeywords.contains (buffer)) {
			// it is a keyword
			Symbol s = mKeywords.getSymbol (buffer);
			if (s.getType () == OPERATOR) {
				if (s.getValue () == '<' || s.getValue () == '>') {
					// see if its >> or <<, the shift operators
					char c = getChar ();
					if (c == s.getValue ()) {
						if (s.getValue () == '<') {
							s = new Symbol ("<<", OPERATOR, ('<' << 8) | '<');
						}
						else {
							s = new Symbol (">>", OPERATOR, ('>' << 8) | '>');
						}
					}
					else {
						putBack ();
					}
				}
				success = true;
			}
			else if (s.getType () == ASSIGN ||
				s.getType () == LEFTPAREN || 
				s.getType () == RIGHTPAREN ||
				s.getType () == DELIMITER ||
				s.getType () == DOUBLEQUOTES) {
				success = true;
			}
			else if (s.getType () == TEMPLABEL) {
				char c = getChar ();
				if (c == 'f' || c == '+') {
					s = new Symbol ("@f", FORWARDJUMP, NULL);
				}
				else if (c == 'b' || c == '-') {
					s = new Symbol ("@b", BACKWARDJUMP, NULL);
				}
				else if (c == ':') {
					// temp label too, so don't putback char
				}
				else putBack ();
				success = true;
			}
			else if (!isAlpha (next) && !isDigit (next, mRadix)) {
				success = true;
			}
			
			if (success) {
				mSymbol = s;
				return success;
			}
		}
		
		if (!isAlpha (next) && !isDigit (next, mRadix)) {	/* was && !isDigit (next, mRadix) */
			// it must be an identifier or label
			if (mSymbolTable.contains (buffer)) {
				// its already in symbol table
				mSymbol = mSymbolTable.getSymbol (buffer);
				if (next == ':') {
					// its a label definition
					if (mSymbol.getType () == IDENTIFIER && mSymbol.getValue () == NULL) {
						// Found undefined identifier previously installed in symbol table
						// The source is this label, so remove undefined identifier from table
						// and install and return it as as a label instead.
						mSymbolTable.remove (buffer);
						mSymbol = new Symbol (buffer, LABEL, NULL);
						mSymbolTable.install (mSymbol);
					}
					// eat colon
					getChar ();
				}
				//System.out.println (mSymbol.getName () + " " + mSymbol.getType () + " " + mSymbol.getValue ());
				success = true;
			}
			else {
				if (!isAlpha (first)) {
					throw new LexerException ("Illegal char '" + first + "' at start of identifier ",
						mFilename, mLine, xtractLine (mLine));
				}
				else if (next == ':') {
					mSymbol = new Symbol (buffer, LABEL, NULL);
					success = true;
					// eat colon
					getChar ();
				}
				else {
					mSymbol = new Symbol (buffer, IDENTIFIER, NULL);
				}
				mSymbolTable.install (mSymbol);
				success = true;
			}
		}
		return success;
	}

	/**
	*	Gets a symbol from stream, either numeric constant or other symbols (Identifiers / keywords)
	*	@param buffer Character buffer to work on
	*	@return true if symbol was found
	*	@throws LexerException on syntax errors in the sourcefile
	*/
	private boolean getSymbol (String buffer) throws LexerException
	{
		boolean success = false;
		char n = getChar ();
		putBack ();
		char f = buffer.charAt (0);
		if (isDigit (f, mRadix)) {
			success = scanConstant (mBuffer, n);
		}
		else {
			success = scanMixed (mBuffer, f, n);
		}
		return success;
	}
	
	/**
	*	Initialize reading (called by getNext prior to entering stateMachine in @Lexer6502.getNext)
	*/
	private void initRead () 
	{
		mState = STATE_READ_LINE;
		mBuffer = "";
		mSymbol = null;
	}
	
	/**
	*	@see AbstractLexer.getNext
	*	@todo check the new addition.. It is not really thought through, just to patch a bug
	*		when errounumous symbols in middle of bytesequence cause parser to freak out with millions of error msgs
	*/
	public Symbol getNext () throws LexerException
	{
		//storePutbackPos ();
		if (mFile == null) errorExit ("Lexer: trying to call getNext with null mFile");
		if (linefeedFollows ()) {
			setLinefeed (false);
			return new Symbol ("", LINEFEED, NULL);
		}
		else if (eof ()) {
			return new Symbol ("", EOF, NULL);
		}

		initRead ();
		boolean process = true;
		while (process) {
			switch (mState) {
				case STATE_READ_LINE:
					process = stateReadLine ();
					// begin new addition 2006-05-27
					if (mSymbol == null) {
						mState = STATE_READ_LINE;
						process = true;
						continue;
					}
					// end new addition 2006-05-27
					if (mSymbol.getType () == DOUBLEQUOTES) {
						process = false;
					}
					break;
				case STATE_SKIP_WHITE:
					process = stateSkipWhite ();
					break;
				default:
					errorExit ("Unexpected mState in Lexer::getNext");
			}
		}
		// is radix symbol?
		if (mSymbol.getType () == OPERATOR && mSymbol.getValue () == '$') {
			// set new radix, ignore radix symbol and selfrecurse next symbol
			mRadix = 16;
			getNext ();
			// is it a constant?
			if (mSymbol.getType () != CONSTANT) {
				throw new LexerException ("Constant must follow hexadecimal radix ",
					mFilename, mLine, xtractLine (mLine));
			}
		}
		else if (mSymbol.getType () == OPERATOR && mSymbol.getValue () == '%') {
			// set new radix, ignore radix symbol and selfrecurse next symbol
			mRadix = 2;
			getNext ();
			// is it a constant?
			if (mSymbol.getType () != CONSTANT) {
				throw new LexerException ("Constant must follow binary radix ",
					mFilename, mLine, xtractLine (mLine));
			}
		}
		else if (mSymbol.getType () == DOUBLEQUOTES) {
			// special case, double quotes means a string of characters, try read it into mCharSequence
			int startLine = mLine;
			mCharSequence = "";
			mCaseInsensitive = false;
			char c = getChar ();
			while (c != '"' && !eof ()) {
				mCharSequence += c;
				c = getChar ();
			}
			mCaseInsensitive = true;
			if (eof () && c != '"') {
				System.err.println ("char : " + c);
				throw new LexerException ("Unterminated character sequence", 
					mFilename, startLine, xtractLine (startLine));
			}
			else if (mCharSequence.length () == 0) 
				throw new LexerException ("Empty character sequence", 
					mFilename, startLine, xtractLine (startLine));
					
			mSymbol = new Symbol ("", CHARSEQUENCE, NULL);
			stateSkipWhite ();
		}
		
		mRadix = 10;
		return new Symbol (mSymbol.getName (), mSymbol.getType (), mSymbol.getValue ());
	}
	
/*
		// begin remove
		System.err.println ("symbol name: " + mSymbol.getName () + " type: " + mSymbol.getType () + " line: " +
			mLine + " reml: " + mRembLine);
		// end remove
*/
	
	/**
	*	@see AbstractLexer.peekNext
	*/
	public Symbol peekNext () throws LexerException
	{
		if (mFile == null) errorExit ("Lexer: trying to call peekNext with null mFile");
		// save all dynamic member vars (except Symbol tables)
		String tempBuffer = mBuffer;
		boolean tempEOF = mEOF;
		int tempRadix = mRadix;
		Symbol tempSymbol = mSymbol;
		int tempLine = mLine;
		int tempCol = mCol;
		boolean tempLinefeed = mLinefeed;
		long fp = 0;
		Symbol peekSymbol = null;
		int tempState = mState;
		String tempCharSequence = mCharSequence;
		char tempLastChar = mLastChar;
		int tempLastCol = mLastCol;
		try {
			fp = mFile.getFilePointer ();
			peekSymbol = getNext ();
			mFile.seek (fp);
					
			// restore member vars
			mLinefeed = tempLinefeed;
			mLine = tempLine;
			mCol = tempCol;
			mEOF = tempEOF;
			mBuffer = tempBuffer;
			mRadix = tempRadix;
			mSymbol = tempSymbol;
			mState = tempState;
			mCharSequence = tempCharSequence;
			mLastCol = tempLastCol;
			mLastChar = tempLastChar;
		}
		catch (IOException e) {
			errorExit ("Lexer: " + e);
		}
		return peekSymbol;
	}
		
	/**
	*	@see AbstractLexer.eof
	*/
	public boolean eof ()
	{
		return mEOF;
	}
	
	/**
	*	@see AbstractLexer.attachInput
	*/
	public void attachInput (String filename)
	{
		mFilename = filename;
		try {
			if (mFile != null) mFile.close ();
			mFile = new RandomAccessFile (filename, "r");
		}
		catch (FileNotFoundException e) {
			errorExit ("Cannot open file " + filename);
		}
		catch (IOException e) {
			errorExit ("Lexer: " + e);
		}
		reset ();
	}
	
	/**
	*	@see AbstractLexer.reset
	*/
	public void reset ()
	{
		mLine = mRembLine = 1;
		mCol = mLastCol = 0;
		mLastChar = '\0';
		mRadix = 10;
		mBuffer = "";
		mEOF = false;
		mSymbol = null;
		try {
			mFile.seek (0);
		}
		catch (IOException e) {
			errorExit ("Lexer reset " + e);
		}
		skipWhiteSpace ();
		setLinefeed (false);
	}

	/**
	*	@see AbstractLexer.xtractLine
	*/
	public String xtractLine (int lineNum) {
		
		int p = 0;
		int line = 1;
		String str = "";
		long fp = 0;
		int tempLastCol = mLastCol;
		char tempLastChar = mLastChar;
		int tempLine = mLine;
		int tempCol = mCol;
		boolean tempLinefeed = mLinefeed;
		boolean tempEOF = mEOF;
		try {
			mLine = 1;
			mEOF = false;
			// save current filepointer and then reset to beginning of file
			fp = mFile.getFilePointer ();
			mFile.seek (0);
			// find line
			char c;
			while (!eof () && line != lineNum) {
				c = '\0';
				while (!eof () && '\n' != (c = getChar ()));
				if (eof ()) return "";
				++line;
			}

			// extract the line
			if (line == lineNum) {
				c = '\0';
				while (!eof () && '\n' != (c = getChar ())) str += c;
			}
			// restore filepointer again
			mFile.seek (fp);
		}
		catch (IOException e) {
			errorExit ("Lexer: " + e);
		}
		mLastCol = tempLastCol;
		mLastChar = tempLastChar;
		mLine = tempLine;
		mCol = tempCol;
		mLinefeed = tempLinefeed;
		mEOF = tempEOF;
		// if no line found return empty string
		return str;
	}

	/**
	*	@see AbstractLexer.getLineNum
	*/
	public int getLineNum ()
	{
		return mLine;
	}
	
	/**
	*	@see AbstractLexer.getColNum
	*/
	public int getColNum ()
	{
		return mCol;
	}
	
	/**
	*	@see AbstractLexer.getFilename
	*/
	public String getFilename ()
	{
		return mFilename;
	}
	
	/**
	*	@see AbstractLexer.toString
	*/
	public String toString ()
	{
		return "Lexer6502 file " + mFilename + ", line " + mLine + ", column " + mCol;
	}
	
	/**
	*	Returns a character sequence
	*/
	public String getCharSequence ()
	{
		return mCharSequence;
	}
	
	/**
	*	Returns a copy of lexer
	*/
	public AbstractLexer copy ()
	{
		return new Lexer6502 (mSymbolTable.copy (), (SymbolTable) mKeywords.copy (), mCommentChars);
	}
	
	/**
	*	Tells lexer to keep a mark on current line number
	*/
	public void rememberLineNum ()
	{
		mRembLine = mLine;
	}
	
	/**
	*	Tells lexer to return the last mark on current line number
	*/
	public int getRememberLineNum ()
	{
		return mRembLine;
	}
	
	/**
	*	Returns the symbol table with identifiers
	*/
	public SymbolTable getSymbolTable ()
	{
		return mSymbolTable;
	}
}
