import java.util.Vector;

/**
*	Implements a parser for 6502 assembler.
*	The parsing is done by recursive descent, which is the easiest way to go about when coding a parser manualy
*	for a language. 
*	The parsing is done by defining one member function for each non-terminal in the language, which returns true
*	on successful parsing, and false if function found the current statement not being the non-terminal it tries
*	to identify. Here each such function has the prefix 'nt' meaning non-terminal. If a nonterminal function
*	identifies the start of the statement correctly, it gets more symbols from the lexer. Since the lexer
*	cannot backtrack, the function must throw an exception with an error message and parsing
*	is aborted for that line. Thus, the order of trying each nonterminal is important.
*	In other words: Each nonterminal must be constructed so that no other nonterminal starts with the same symbol.
*	If two nonterminals start with the same symbol, A new nonterminal function must be constructed that breaks up
*	the nonterminal in two new nonterminals and call sub 'nt' functions to identify them.
*	The result of this parsing constructs an "abstract syntax tree" where each node is a nonterminal to try,
*	and the leafs of the tree is a terminal.
*
*	The parsers doNonTerminal function tries calling each such function until one of those functions
*	return true meaning the correct non-terminal was found. 
*	It only needs to lookahead one symbol at most to be able to determine the correct non-terminal.
*	If all alternatives has been exhausted (calling all nt functions)
*	and none was true, un undefined statement error is triggered.
*
*	@see AbstractParser
*	@see SymbolConstant6502
*	@bugs There is an bug in the line number reporting on errors at certain points, because when an statement
*		in code ends prematurely. This could be remedied by ONLY using using mLexer.peekNext for each symbol,
*		before determine validity of statements.
*	@author David Schager 2006
*/
public class Parser6502 implements SymbolConstant6502, AbstractParser {
	AbstractLexer mLexer;
	private Symbol mSymbol = null;
	private int mConstant = 0;
	private boolean mFixed = true;
	private int mAddrMode = 0;
	private int mParseResult = PARSERESULT_UNDEF_STATEMENT;
	private Vector mSequence = null;
	private int mNewOrigin = 0x0000;
	private int mIp = 0;
	private int mPass = 1;
	private boolean mC64UpperCase = false;
	private boolean mC64LowerCase = false;
	private int mValReloc = ABSOLUTE;
	private Vector mTempLabels = new Vector ();
	
	/**
	*	Constructor
	*/
	public Parser6502 (AbstractLexer lexer)
	{
		mLexer = lexer;
		mSequence = new Vector ();
	}
	
	/**
	*	Returns true if parameter is within bits
	*/
	private boolean is8bits (int val)
	{
		return ((val & 0xFFFFFF00) == 0) ? true : false;
	}
	
	/**
	*	Returns true if parameter is within 16 bits	
	*/
	private boolean within16bits (int val)
	{
		return ((val & 0xFFFF0000) == 0) ? true : false;
	}
	
	/**
	*	Returns true if parameters has any of bits 8-15 set	
	*/
	private boolean hasHiByte (int val)
	{
		return ((val & 0x0000FF00) != 0) ? true : false;
	}
	
	/**
	*	Checks if Symbol if of a certain type	
	*/
	private boolean isTerminal (Symbol symbol, int type)
	{
		return symbol.getType () == type ? true : false;
	}
	
	
	/**
	*	
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private boolean expectTerminal (int type) throws LexerException
	{
		return isTerminal (mLexer.peekNext (), type);
	}

	/**
	*	Checks that an labels without colon postfix, is declared at column one, else throw expeption.
	*	@param symbol The identifier that acts as label
	*/
	private void enforceStrictLabels (Symbol symbol) throws ParserException, LexerException
	{
		String name = symbol.getName ();
		String line = mLexer.xtractLine (mLexer.getRememberLineNum ());
		name.toLowerCase ();
		line.toLowerCase ();
		line = line.substring (0, name.length ());
		if (0 != name.compareTo (line))
			throw new ParserException ("Label without colon must be on column 1", mLexer);
	}

	/**
	*	Evaluates expression, if there is one.
	*	Note that if ntExpression finds a identifier, it will return a symbol with type Constant,
	*	regardless of if the symbol is undefined or not.
	*	@return Symbol result of expression
	*	@warning If undefined label, returns a fake constant 16bits wide, if pass 2 returns
	*		new constant with labels value.
	*/
	private Symbol ntExpression () throws ParserException, LexerException
	{
		Symbol symbol = mLexer.peekNext ();
		int type = symbol.getType ();
		if (type == IDENTIFIER || type == LABEL || type == CONSTANT) {
			Evaluator evaluator = new Evaluator (mLexer);
			Symbol eval = evaluator.evaluate (mPass, mIp, LINEFEED);
			mValReloc = evaluator.isRelocatable ();
			return eval;
		}
		else if (type == OPERATOR) {
			int val = symbol.getValue ();
			if (val == '-' || val == '~' || val == '\\' || val == '<' || val == '>' || val == '*') {
				// unary operator or current address operator
				Evaluator evaluator = new Evaluator (mLexer);
				Symbol eval = evaluator.evaluate (mPass, mIp, LINEFEED);
				mValReloc = evaluator.isRelocatable ();
				return eval;
			}
			//else if (val == '*') {
				//
				// mLexer.getNext ();
				// mValReloc = 1;
				//return new Symbol ("", CONSTANT, mIp);
			//}
		}
		else if (type == FORWARDJUMP) {
			// eat next symbol
			mLexer.getNext ();
			// forward reference to temporary label, in pass one return dummy
			if (mPass == 1) return new Symbol ("", CONSTANT, 0x1000);
			else {
				// pass 2, scan through the temp labels that assembler
				// set for parser after pass 1, to find the closest one.
				Integer adjacent = null;
				for (int i = 0; i < mTempLabels.size (); ++i) {
					if (((Integer) mTempLabels.elementAt (i)).intValue () >= mIp + 1) {
						adjacent = (Integer) mTempLabels.elementAt (i);
						break;
					}
				}
				if (adjacent == null)
					throw new ParserException ("Cannot resolve forward reference to temporary label", mLexer);
				mValReloc = 1;
				return new Symbol ("", CONSTANT, adjacent.intValue ());
			}
		}
		else if (type == BACKWARDJUMP) {
			// eat next symbol
			mLexer.getNext ();
			// backward reference to temporary label, in pass one return dummy
			if (mPass == 1) return new Symbol ("", CONSTANT, 0x1000);
			else {
				// pass 2, scan through the temp labels that assembler
				// set for parser after pass 1, to find the closest one.
				Integer adjacent = null;
				for (int i = mTempLabels.size () - 1; i != -1; --i) {
					//System.out.println (((Integer) mTempLabels.elementAt (i)).intValue ());
					if (((Integer) mTempLabels.elementAt (i)).intValue () <= mIp) {
						adjacent = (Integer) mTempLabels.elementAt (i);
						break;
					}
				}

				if (adjacent == null) {
					if (mTempLabels.size () != 0)
						adjacent = (Integer) mTempLabels.elementAt (mTempLabels.size () - 1);
					else throw new ParserException ("Cannot resolve backward reference to temporary label", mLexer);
				}
				mValReloc = 1;
				return new Symbol ("", CONSTANT, adjacent.intValue ());
			}
		}
			
		return mLexer.getNext ();
	}

	/**
	*	Tries parse AccumulatorMode (opcode a) || (opcode).
	*	@note The accumulator has not to be specified:
	*	both forms
	*	rol a
	*	and
	*	rol 
	*	are accepted.
	*	@param symbol current symbol
	*	@return true if parsed OK
	*/
	private boolean ntAccumulatorMode (Symbol symbol) throws LexerException
	{
		boolean success = false;
		if (isTerminal (symbol, LINEFEED)) {
			int type = mSymbol.getType ();
			if (type == ASL || type == ROL || type == LSR || type == ROR) {
				success = true;
				mAddrMode = ACC;
			}
		}
		else if (isTerminal (symbol, A) && expectTerminal (LINEFEED)) {
			mLexer.getNext (); // eat linefeed
			mAddrMode = ACC;
			success = true;
		}
		return success;
	}
	
	/**
	*	Tries to parse Immediate (opcode)
	*	(for instance lda #10)
	*	@param symbol current symbol
	*	@throws ParserException on syntax errors
	*	@return true if parsed OK
	*/
	private boolean ntImmediateMode (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntImmediateMode");
		boolean success = false;
		if (symbol.getType () == OPERATOR && symbol.getValue () == '#') {
			// Now must follow constant or expression
			// try evaluate next symbol

			Symbol e = ntExpression ();
			if (!isTerminal (e, CONSTANT))
				throw new ParserException ("Expected expression", mLexer);

			if (mPass != 1 && (!within16bits (e.getValue ()) || !is8bits (e.getValue ())))
				throw new ParserException ("Number too large", mLexer);
			mConstant = e.getValue ();	// not needed
			if (!expectTerminal (LINEFEED))
				throw new ParserException ("Unexpected end of statement", mLexer);
			// eat linefeed
			mLexer.getNext ();
			mAddrMode = IMM;
			success = true;
		}
		return success;
	}
	
	
	/**
	*	Tries to parse variety of addressing modes
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private boolean ntMixedAddressing (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntMixedAddressing");
		boolean success = false;
		if (symbol.getType () == CONSTANT) {
			mConstant = symbol.getValue ();
			symbol = mLexer.getNext ();
			if (!ntDirect (symbol) && !ntIndexed (symbol))
				throw new ParserException ("Unexpected symbol", mLexer);
			success = true;
		}
		return success;
	}
	
	
	/**
	*	Tries to parse direct (opcode $xxxx)
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private boolean ntDirect (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntDirect");
		boolean success = false;
		int op = mSymbol.getType ();
		if (isTerminal (symbol, LINEFEED)) {
			// is absolute or zero page
			if (mPass != 1 && !within16bits (mConstant)) 
				throw new ParserException ("Number too large", mLexer);
			else // Check for Branch instructions (relative addressing)
			if (op == BCC || op == BCS || op == BEQ || op == BMI || op == BNE || op == BPL || op == BVC || op == BVS) {
				// is branching with relative addressing
				mAddrMode = REL;
				mValReloc = 0;
			}
			else if (hasHiByte (mConstant)) {
				// is absolute (jsr or jmp)
				mAddrMode = ABS;
			}
			else {
				// is zero page
				mAddrMode = ZP;
			}
			success = true;
		}
		return success;
	}
	
	
	/**
	*	Tries to parse indexed
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private boolean ntIndexed (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntIndexed");
		boolean success = false;
		if (isTerminal (symbol, DELIMITER)) {
			symbol = mLexer.getNext ();
			if (!ntIndexedX (symbol) && !ntIndexedY (symbol))
				throw new ParserException ("Unexpected Symbol", mLexer);
			// eat linefeed
			mLexer.getNext ();
			success = true;
		}
		return success;
	}
	
	
	/**
	*	Tries to parse indexed x (opcode ($xxxx), x)
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private boolean ntIndexedX (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntIndexedX");
		boolean success = false;
		if (isTerminal (symbol, X)) {
			if (isTerminal (mLexer.peekNext (), LINEFEED)) {
				// is indexed x addressing
				if (mPass != 1 && !within16bits (mConstant)) 
					throw new ParserException ("Number too large", mLexer);
				else if (hasHiByte (mConstant)) {
					// is absolute indexed x
					mAddrMode = ABSX;
				}
				else {
					// is zero page indexed x
					mAddrMode = ZPX;
				}
				success = true;
			}
		}
		return success;
	}
	
	
	/**
	*	Tries to parse indexed y (opcode ($xxxx),y)
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private boolean ntIndexedY (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntIndexedY");
		boolean success = false;
		if (isTerminal (symbol, Y)) {
			if (isTerminal (mLexer.peekNext (), LINEFEED)) {
				// is indexed x addressing
				if (mPass != 1 && !within16bits (mConstant)) 
					throw new ParserException ("Number too large", mLexer);
				// Note: In pass 1 ABSY and ZPY is ambiguous and might turn out wrong
				// when ntExpression returns its dummy constant, with the result
				// that labels might be miscalculated since ABSY and ZPY has different opcode byte lengths (3 & 2)
				else if (hasHiByte (mConstant)) {
					// is absolute indexed y
					mAddrMode = ABSY;
				}
				else {
					// is zero page indexed y
					mAddrMode = ZPY;
				}
				success = true;
			}
		}
		return success;
	}

	/**
	*	Tries to parse indirect modes
	*	@param symbol current symbol
	*	@throws ParserException on syntax errors
	*	@return true if parsed OK
	*/
	private boolean ntMixedIndirect (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntMixedIndirect");
		boolean success = false;
		if (symbol.getType () == LEFTPAREN) {
			symbol = ntExpression ();
			if (!isTerminal (symbol, CONSTANT)) throw new ParserException ("Expected address", mLexer);
			if (mPass != 1 && !within16bits (symbol.getValue ())) throw new ParserException ("Address out of range", mLexer);
			mConstant = symbol.getValue ();
			
			symbol = mLexer.getNext ();
			if (!ntAbsoluteIndirectMode (symbol) && !ntIndirectXMode (symbol) && !ntIndirectYMode (symbol))
				throw new ParserException ("Unexpected indirect mode", mLexer);
			success = true;
			
		}
		return success;
	}
	
	
	/**
	*	Try parse absolute indirect (opcode (x))
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private  boolean ntAbsoluteIndirectMode (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntAbsoluteIndirectMode");
		boolean success = false;
		if (isTerminal (symbol, RIGHTPAREN) && expectTerminal (LINEFEED)) {
			// eat linefeed
			mLexer.getNext ();
			mAddrMode = IND;
			success = true;
		}
		return success;
	}
	
	
	/**
	*	Try parse indirect x (opcode ($xx, x))
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private  boolean ntIndirectXMode (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntIndirectXMode");
		boolean success = false;
		if (isTerminal (symbol, DELIMITER)) {
			if (!isTerminal (mLexer.getNext (), X))
				throw new ParserException ("Expected x", mLexer);
			else if (!isTerminal (mLexer.getNext (), RIGHTPAREN))
				throw new ParserException ("Expected )", mLexer);
			else if (!isTerminal (mLexer.getNext (), LINEFEED)) 
				throw new ParserException ("Unexpected end of statement", mLexer);
			else if (mPass != 1 && !is8bits (mConstant)) 
				throw new ParserException ("Need 8 bits for zero page addressing", mLexer);
			else {
				mAddrMode = INDX;
				success = true;
			}
		}
		return success;
	}

	
	/**
	*	Try parse Indirect Y addressing (opcode ($xx), y)
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private  boolean ntIndirectYMode (Symbol symbol) throws ParserException, LexerException
	{
	//System.err.println ("ntIndirectYMode");
		boolean success = false;
		if (isTerminal (symbol, RIGHTPAREN)) {
			if (!isTerminal (mLexer.getNext (), DELIMITER))
				throw new ParserException ("Expected separator", mLexer);
			else if (!isTerminal (mLexer.getNext (), Y))
				throw new ParserException ("Expected y", mLexer);
			else if (!expectTerminal (LINEFEED)) 
				throw new ParserException ("Unexpected end of statement", mLexer);
			
			mLexer.getNext ();
			if (mPass != 1 && !is8bits (mConstant))
				throw new ParserException ("Need 8 bits for zero page addressing", mLexer);
			else {
				mAddrMode = INDY;
				success = true;
			}
		}
		return success;
	}

	/**
	*	Try parse opcode.
	*	Tries a number of non-terminals, one or more for each addressing mode.
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private  boolean ntOpcode (Symbol e) throws ParserException, LexerException
	{
	//System.err.println ("ntOpcode");
		boolean success = false;
		if (ntAccumulatorMode (e)) 			success = true;
		else if (ntImmediateMode (e))		success = true;
		else if (ntMixedAddressing (e))		success = true;
		else if (ntMixedIndirect (e))		success = true;
		else if (isTerminal (e, LINEFEED)) {
			// is implied mode
			//System.out.println ("Implied");
			mAddrMode = IMPL;
			success = true;
		}
		if (success) return true;
		else {
			System.out.println ("op === " + mSymbol.getType ());
			throw new ParserException ("Internal assembler error", mLexer);
		}
	}
	
	/**
	*	Try parse origin directive (.org $xxxx)
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private  boolean ntOrigin (Symbol symbol) throws ParserException, LexerException
	{
		boolean success = false;
		if (symbol.getType () != CONSTANT) 
			throw new ParserException ("Expected origin address", mLexer);
		int org = symbol.getValue ();
		if (mPass != 1 && !within16bits (org))
			throw new ParserException ("Number too large", mLexer);
		if (!expectTerminal (LINEFEED))
			throw new ParserException ("Unexpected end of statement", mLexer);
		mLexer.getNext ();
		mNewOrigin = org;
		success = true;

		return success;
	}
	
	/**
	*	Try parse include directive (.include "somefile")
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private boolean ntInclude (Symbol symbol) throws ParserException, LexerException
	{
		if (symbol.getType () == CHARSEQUENCE) {
			mSymbol = new Symbol (mLexer.getCharSequence (), CHARSEQUENCE, NULL);
		}
		else throw new ParserException ("Expected name of include file, enclosed in doublequotes", mLexer);
		
		if (!expectTerminal (LINEFEED))
			throw new ParserException ("Unexpected end of statement", mLexer);
		mLexer.getNext ();
		return true;
	}
	
	/**
	*	Try parse byte sequence (.byt x,x,x,"string",...)
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*	@todo check addition mLexer.nextLine, if necessary, it was added 2006-05-27
	*/	
	private boolean ntByteSequence (Symbol symbol) throws ParserException, LexerException
	{
		boolean success = false;

		if (symbol.getType () == CONSTANT) {
			if (mPass != 1 && !is8bits (symbol.getValue ()))
				throw new ParserException ("Number too large", mLexer);
			mSequence.addElement (new Integer (symbol.getValue ()));
		}
		else if (symbol.getType () == CHARSEQUENCE) {
			String str = mLexer.getCharSequence ();
			AsciiTranslator translator = null;

			if (mC64UpperCase) {
				translator = new C64UpperCaseTranslator ();
			}
			else if (mC64LowerCase) {
				translator = new C64LowerCaseTranslator ();
			}
			for (int i = 0; i < str.length (); ++i) {
				int c = (int) str.charAt (i);
				if (translator != null) {
					c = translator.translate (c);
				}
				mSequence.addElement (new Integer (c));
			}
		}
		else throw new ParserException ("Expected constant", mLexer);
		
		Symbol next = mLexer.peekNext ();
		if (next.getType () == LINEFEED) {
			// eat linefeed
			mLexer.getNext ();
			success = true;
		}
		else if (next.getType () == DELIMITER) {
			// eat delim
			mLexer.getNext ();
			// self recursion for next byte
			success = ntByteSequence (ntExpression ());
		}
		else {
			// below is addition 2006-05-27 to patch a bug
			mLexer.nextLine ();
			throw new ParserException ("Unexpected end of statement", mLexer);
		}
		
		return success;
	}
	
	/**
	*	Try parse word sequence (.word x,x,x...)
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*	@todo check addition mLexer.nextLine, if necessary, it was added 2006-05-27
	*/	
	private boolean ntWordSequence (Symbol e) throws ParserException, LexerException
	{
		boolean success = false;
		if (e.getType () == CONSTANT) {
			if (mPass != 1 && !within16bits (e.getValue ()))
				throw new ParserException ("Number too large", mLexer);
			mSequence.addElement (new Integer (e.getValue ()));
		}
		else throw new ParserException ("Expected constant", mLexer);
		
		Symbol next = mLexer.peekNext ();
		if (next.getType () == LINEFEED) {
			// eat linefeed
			mLexer.getNext ();
			success = true;
		}
		else if (next.getType () == DELIMITER) {
			// eat delim
			mLexer.getNext ();
			// self recursion for next byte
			success = ntWordSequence (ntExpression ());
		}
		else {
			// below is addition 2006-05-27 to patch a bug
			mLexer.nextLine ();
			throw new ParserException ("Unexpected end of statement", mLexer);
		}
		
		return success;
	}
	
	/**
	*	Try parse Asterisk assign (*=$xxxx)
	*	@param symbol The current symbol from lexer
	*	@return true if succesful
	*/	
	private boolean ntAsteriskAssign () throws ParserException, LexerException
	{
		boolean success = false;
		Symbol symbol = mLexer.getNext ();
		if (symbol.getType () != ASSIGN)
			throw new ParserException ("Expected assign", mLexer);
		Symbol org = ntExpression ();
		if (org.getType () != CONSTANT)
			throw new ParserException ("Expected new origin address", mLexer);
		int newOrg = org.getValue ();
		if (mPass != 1 && !within16bits (newOrg))
			throw new ParserException ("Number too large", mLexer);
		if (!expectTerminal (LINEFEED))
			throw new ParserException ("Unexpected end of statement ", mLexer);
		// eat linefeed	
		mLexer.getNext ();

		mNewOrigin = newOrg;
		success = true;		
		return success;
	}

	
	/**
	*	Try parse Procedure directive (.proc "procname")
	*/
	private boolean ntProc (Symbol symbol) throws ParserException, LexerException
	{
		boolean success = false;
		
		if (symbol.getType () == IDENTIFIER) {
			// in pass 1, and lexer returns undefined identifier as proc name
			Symbol next = mLexer.getNext ();
			if (next.getType () != LINEFEED)
				throw new ParserException ("Expected procedure name", mLexer);
			mSymbol = new Symbol (symbol.getName (), PROC, NULL);
			success = true;
		}
		else if (symbol.getType () == LABEL) {
			// ok, probably in pass 2 and proc name is already installed.
			mSymbol = symbol;
			Symbol next = mLexer.getNext ();
			if (next.getType () != LINEFEED)
				throw new ParserException ("Expected procedure name", mLexer);
			success = true;
		}
		else {
			throw new ParserException ("Unexpected end of statement", mLexer);
		}
		
		return success;
	}
	
	/**
	*	Try parse End procedure directive (.endproc)
	*/
	private boolean ntEndProc (Symbol symbol) throws ParserException, LexerException
	{
		boolean success = false;
		if (symbol.getType () != LINEFEED) 
			throw new ParserException ("Unexpected end of statement", mLexer);
		success = true;
		return success;		
	}

	/**
	*	Try parse identifier
	*	@return true if succesful
	*/
	private boolean ntIdentifier (Symbol symbol) throws ParserException, LexerException
	{
		boolean success = false;
		int type = symbol.getType ();
		if (type == LABEL || type == IDENTIFIER) {
			if (expectTerminal (LINEFEED)) {
				// eat linebreak
				mLexer.getNext ();
				// label on single line, but if not with colon, must be of first column
				if (type == IDENTIFIER) {
					enforceStrictLabels (symbol);
				}
				mParseResult = PARSERESULT_LABEL;
				mSymbol = new Symbol (symbol.getName (), LABEL, NULL);
				success = true;
			}
			else if (type == IDENTIFIER && expectTerminal (ASSIGN)) {
				mLexer.getNext ();	// eat assign
				Symbol operand = ntExpression ();
				if (operand.getType () != CONSTANT)
					throw new ParserException ("Expected constant or identifier", mLexer);
				if (mPass != 1 && !within16bits (operand.getValue ()))
					throw new ParserException ("Number too large", mLexer);
				if (!expectTerminal (LINEFEED))
					throw new ParserException ("Unexpected end of statement", mLexer);
				// eat linefeed	
				mLexer.getNext ();
				// assigning label to identifier, transforms the identifier to a label.
				type = (mValReloc != 0) ? LABEL : IDENTIFIER;
				mSymbol = new Symbol (symbol.getName (), type, operand.getValue ());
				mParseResult = PARSERESULT_ASSIGN;
				success = true;
			}
			else {
				if (type == IDENTIFIER) {
					enforceStrictLabels (symbol);
				}
				mParseResult = PARSERESULT_LABEL;
				mSymbol = new Symbol (symbol.getName (), LABEL, NULL);
				success = true;
			}
		}
		
		return success;
	}

	/**
	*	Parse another statement in file.
	*	@throws ParserException on syntactic errors and terminates
	*	@throws LexerException on lexicalic errors and terminates
	*	@return A integer define describing the result of the parsing.
	*/
	public int doNonTerminal () throws ParserException, LexerException
	{
		// Tell lexer to remember line number at start of statement
		// or error reporting will report wrong line numbers.
		mLexer.rememberLineNum ();
		
		mConstant = mAddrMode = 0;
		mValReloc = ABSOLUTE;
		mFixed = true;
		mSymbol = null;
		mParseResult = PARSERESULT_UNDEF_STATEMENT;

			Symbol symbol = mLexer.getNext ();
			int type = symbol.getType ();
			
			if (type >= 0x00 && type <= 0x37) {
				// Symbol is opcode
				mSymbol = symbol;
				if (ntOpcode (ntExpression ())) {
					mParseResult = PARSERESULT_OPCODE;
				}
				else throw new ParserException ("Parser6502.doNonTerminal error 1", mLexer);
			}
			else if (type == EOF) {
				// Symbol is EOF
				mParseResult = PARSERESULT_EOF;
			}
			else if (type == ORG) {
				if (ntOrigin (ntExpression ())) {
					mParseResult = PARSERESULT_NEW_ORIGIN;
				}
				else throw new ParserException ("Parser6502.doNonTerminal error 2", mLexer);
			}
			else if (type == INCLUDE) {
				ntInclude (mLexer.getNext ());
				mParseResult = PARSERESULT_INCLUDE;
			}
			else if (type == OPERATOR && symbol.getValue () == '*') {
				// synonymous with org if assign follows (for many assemblers)
				if (ntAsteriskAssign ()) {
					mParseResult = PARSERESULT_NEW_ORIGIN;
				}
				else throw new ParserException ("Parser6502.doNonTerminal error 2a", mLexer);
			}
			else if (type == TEMPLABEL) {
				mParseResult = PARSERESULT_TEMPORARY_LABEL;
				// eat linefeed if nothing follows..
				if (expectTerminal (LINEFEED)) mLexer.getNext ();
			}
			else if (type == BYTE) {
				mSequence.clear ();
				if (ntByteSequence (ntExpression ())) {
					mParseResult = PARSERESULT_BYTE_SEQ;
				}
				else throw new ParserException ("Parser6502.doNonTerminal error 3", mLexer);
			}
			else if (type == WORD) {
				mSequence.clear ();
				if (ntWordSequence (ntExpression ())) {
					mParseResult = PARSERESULT_WORD_SEQ;
				}
				else throw new ParserException ("Parser6502.doNonTerminal error 4", mLexer);
			}
			else if (type == PROC) {
				if (ntProc (mLexer.getNext ())) {
					mParseResult = PARSERESULT_PROC;
				}
				else throw new ParserException ("Parser6502.doNonTerminal error 5", mLexer);
			}
			else if (type == ENDPROC) {
				if (ntEndProc (mLexer.getNext ())) {
					mParseResult = PARSERESULT_ENDPROC;
				}
				else throw new ParserException ("Parser6502.doNonTerminal error 6", mLexer);
			}
			else if (!ntIdentifier (symbol)) {
				mParseResult = PARSERESULT_UNDEF_STATEMENT;
			}
		
		return mParseResult;
	}
	
	/**
	*	Returns new origin address
	*/
	public int getNewOrigin ()
	{
		return mNewOrigin;
	}
	/**
	*	Returns machine code
	*/
	public MachineCode6502 getMachineCode ()
	{
		return new MachineCode6502 (mSymbol, mAddrMode, mConstant, mFixed);
	}
	/**
	*	Returns byte/word sequence
	*/
	public Vector getSequence ()
	{
		return mSequence;
	}
	
	/**
	*	Returns the latest encounetered symbol
	*/
	public Symbol getSymbol ()
	{
		return mSymbol;
	}
	
	/**
	*	Output object
	*/
	public String toString ()
	{
		return "Parser6502";
	}
	
	/**
	*	Sets the current pass, 1 or 2.
	*/
	public void setPass (int pass)
	{
		mPass = pass;
	}
	
	/**
	*	Sets the current instruction pointer, must be set prior to each call to doNonTerminal.
	*/
	public void setInstructionPointer (int ip)
	{
		mIp = ip;
	}
	
	/**
	*	Sets a vector of Symbols, containing all the temporary labels, this should
	*	be set after first pass.
	*/
	public void setTempLabels (Vector tempLabels)
	{
		mTempLabels = tempLabels;
	}
	
	/**
	*	Tells the parser to translate text strings in source to Commodore 64 ascii upper case.
	*/
	public void setC64UpperCaseMode (boolean mode)
	{
		mC64UpperCase = mode;
	}
	
	/**
	*	Tells parser to translate text strings in source to Commodore 64 ascii lower case.
	*/
	public void setC64LowerCaseMode (boolean mode)
	{
		mC64LowerCase = mode;
	}
	
	/**
	*	Sets the lexer
	*/
	public void setLexer (AbstractLexer lexer)
	{
		mLexer = lexer;
	}
	
	/**
	* 	Returns the relocatable state of last parse result.
	*	In other words, if an opcode had a local label instead of an 
	*	absolute address as operand, the machine code could be relocated.
	*	This information can be used if there is need to produce relocatable object files.
	*/
	public int getRelocatable ()
	{
		return mValReloc;
	}

}
