import java.util.Vector;

/**
*	Translates a string with an expression from infix order, to postfix order.
*	Infix order is the default for expressions, i.e. 1 + 2 * 5 / 3 etc
*	In Postfix, the operator is put after the terms, i.e. 
*		1 + 2 becomes 1 2 +
*		1 * 2 becomes 1 2 *
*		1 - 5 * 2 becomes 1 5 2 * -
*	The translation is done via recursive descent, in the same way as Parser6502.java.
*	It is very hard to evaluate an expression in infix order, but after translating to postfix order
*	the expression can be evaluated easily using a stack (polish calculator method).
*	CPU's normaly use postfix order when calculating floating point expressions internaly.
*
*	The method for translation is simply "Delay each operator until its right-hand operand
*	has been translated. Pass each operand through without delay."
*	
*	@author David Schager
*/
public class InfixToPostfix implements SymbolConstant6502 {
	private AbstractLexer mLexer;
	private Vector mPostfix = null;
	private int mPass;
	private int mIp;
	private int mValReloc = ABSOLUTE;

	/**
	*	Constructor
	*	@param lexer Lexer that may have encountered an expression
	*/
	public InfixToPostfix (AbstractLexer lexer)
	{
		mLexer = lexer;
		mPostfix = new Vector ();
	}
	
	/**
	*	Returns true if parameter symbol matches parameter type
	*/
	private boolean isTerminal (Symbol symbol, int type)
	{
		return symbol.getType () == type ? true : false;
	}
	
	/**
	*	Returns true if the Lexers look-ahead symbol matches parameter type.
	*/
	private boolean expectTerminal (int type) throws ParserException, LexerException
	{
		return isTerminal (mLexer.peekNext (), type);
	}
	
	/**
	*	Returns true if the Lexers look-ahead symbol matches parameter type and parameter value.
	*/
	private boolean expectTerminal (int type, int value) throws ParserException, LexerException
	{
		Symbol next = mLexer.peekNext ();
		return isTerminal (next, type) && next.getValue () == value;
	}
	
	/**
	* 	Tries check below lexlevels if symbol is present there and not undefined.
	*	If present inserts it in the postfix vector
	*	@return true if present
	*/
	private boolean probeBelowLexLevels (Symbol symbol)
	{
		boolean success = true;
		SymbolTable symbolTable = mLexer.getSymbolTable ();
		Symbol lower = symbolTable.probeToRootNotNULL (symbol.getName (), IDENTIFIER);
		if (lower == null) {
			lower = symbolTable.probeToRootNotNULL (symbol.getName (), LABEL);
			if (lower == null) success = false;
		}
		if (success) {
			if (lower.getType () == LABEL) mValReloc = RELOC;
			mPostfix.addElement (new Symbol ("", CONSTANT, lower.getValue ()));
		}
		
		return success;
	}
	
	/**
	*	Tries extract a factor or a new subexpression.
	*/
	private void factor () throws ParserException, LexerException
	{
		if (expectTerminal (CONSTANT)) {
			mPostfix.addElement (mLexer.getNext ());
		}
		else if (expectTerminal (OPERATOR, '*')) {
			// In this case, the operator * acts as current address operator.
			mPostfix.addElement (new Symbol ("", CONSTANT, mIp));
			mLexer.getNext ();
			mValReloc = RELOC;
		}
		else if (expectTerminal (IDENTIFIER)) {
			if (expectTerminal (IDENTIFIER, NULL)) {
				if (mPass == 2) {
					Symbol symbol = mLexer.getNext ();
					if (!probeBelowLexLevels (symbol)) {
						throw new ParserException ("Undefined identifier '" + symbol.getName () + "'", mLexer);
					}
				}
				else if (mPass == 1) {
				// We're in pass one, return a fake constant,
				// it won't be written to disk..
					mPostfix.addElement (new Symbol ("", CONSTANT, 0x1000));
					// eat symbol
					mLexer.getNext ();
				}
				else throw new ParserException ("Program logic error pass !={1,2}", mLexer);
			}
			else mPostfix.addElement (new Symbol ("", CONSTANT, mLexer.getNext ().getValue ()));
		}
		else if (expectTerminal (LABEL)) {
			mValReloc = RELOC;
			if (expectTerminal (LABEL, NULL)) {
				// We're in pass one, return a fake constant,
				// it won't be written to disk..
				mPostfix.addElement (new Symbol ("", CONSTANT, 0x1000));
				// eat symbol
				mLexer.getNext ();
			}
			else mPostfix.addElement (new Symbol ("", CONSTANT, mLexer.getNext ().getValue ()));
		}
		else if (expectTerminal (LEFTPAREN)) {
			// eat parentheses
			mLexer.getNext ();
			expression ();
			if (!expectTerminal (RIGHTPAREN)) 
				throw new ParserException ("Expected Right parentheses", mLexer);
			// eat parentheses
			mLexer.getNext ();
		}
		else throw new ParserException ("Expected Expression", mLexer);
	}
	
	/**
	*	Look for unary operators.
	*	Theese have highest precedence.
	*/
	private boolean unary () throws ParserException, LexerException
	{
		if (expectTerminal (OPERATOR, '\\') || 
			expectTerminal (OPERATOR, '-') || 
			expectTerminal (OPERATOR, '~') ||
			expectTerminal (OPERATOR, '<') ||
			expectTerminal (OPERATOR, '>')) {
			Symbol operator = mLexer.getNext ();
			if (operator.getValue () == '-') operator = new Symbol ("", OPERATOR, '\\');
			factor ();
			mPostfix.addElement (operator);
			return true;
		}
		return false;
	}
	
	/**
	*	First looks for a unary operator, then factor, then loop on operators
	*	with precedence 2.
	*/
	private void term () throws ParserException, LexerException
	{
		if (!unary ()) factor ();
		while (expectTerminal (OPERATOR, '*') || 
			expectTerminal (OPERATOR, '/') ||
			expectTerminal (OPERATOR, '&') ||
			expectTerminal (OPERATOR, '^') ||
			expectTerminal (OPERATOR, ('<' << 8) | '<') ||
			expectTerminal (OPERATOR, ('>' << 8) | '>')) {
			Symbol mulOrDiv = mLexer.getNext ();
			if (!unary ()) factor ();
			mPostfix.addElement (mulOrDiv);
		}
	}
	
	/**
	*	Transform expression or subexpression.
	*	Tries a term, then loop on binary operators with precedence 1 (lowest).
	*/
	private void expression () throws ParserException, LexerException
	{
		term ();
		while (expectTerminal (OPERATOR, '+') || 
			expectTerminal (OPERATOR, '-') ||
			expectTerminal (OPERATOR, '|')) {
			Symbol plusOrMinus = mLexer.getNext ();
			term ();
			mPostfix.addElement (plusOrMinus);
		}
	}
	
	/**
	*	Transforms the expression output from lexer to postfix order, and returns it in a string.
	*	@param pass The current pass of assembler
	*	@param ip The current instruction pointer of assembler
	*	@param exprEndTerminal The terminal symbol that should mark the end of the expression.
	*	@return String containing postfix expression.
	*/
	public Vector xform (int pass, int ip, int exprEndTerminalType) throws ParserException, LexerException
	{
		if (mPostfix == null) {
			System.err.println ("Unexpected null mPostfix in InfixToPostfix. fatal.");
			System.exit (1);
		}
		mPostfix.clear ();
		mPass = pass;
		mIp = ip;
		expression ();	
		//if (!expectTerminal (exprEndTerminalType))
			//throw new ParserException ("Invalid expression", mLexer);
		return mPostfix;
	}
	
	/**
	*	Returns relocatable status.
	*	If a label is present inside the expression, the expression should be relocatable.
	*/
	public int isRelocatable ()
	{	
		return mValReloc;
	}
}
