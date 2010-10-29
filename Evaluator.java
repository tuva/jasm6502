/**
*	A evaluator for expressions.
*	It first transforms the expression from infix to postfix form, then 
*	use a stack to evaluate the expression. (Reverse polish calculator)
*	@author David Schager
*/
import java.util.Stack;
import java.util.Vector;

public class Evaluator implements SymbolConstant6502 {
	private Stack mStack = null;
	private AbstractLexer mLexer = null;
	private InfixToPostfix mInfixToPostfix = null;
	private int mValReloc = ABSOLUTE;
	
	/**
	*	Constructor
	*	@param lexer The Lexer object that has encountered expression to evaluate
	*/
	public Evaluator (AbstractLexer lexer)
	{
		mLexer = lexer;
		mInfixToPostfix = new InfixToPostfix (lexer);
		mStack = new Stack ();
	}
	
	/**
	*	Do stack calculation.
	*	Pops two operands from the stack, a and b, and performs binary operation on them.
	*	The result is pushed back on the stack.
	*	@bug Possible bug: check the division, if in pass1 dividing by a label thats undefined and defaulting to a constant $1000
	*		and the result.. possibly might cause 8 bit result instead of 16 bits, and labels gets misaligned.
	*		Don't know if even possible but.. (too late at night :P)
	*	@param binaryOp Symbol containing the binary operation (-,+,/ or *)
	*	@return Integer object containing the result.
	*/
	private Integer calculate (int pass, Symbol binaryOp) throws ParserException, LexerException
	{
		int binVal = binaryOp.getValue ();
		int a = ((Integer) mStack.pop ()).intValue ();
		int b = 0;
		
		// don't pop two operands if unary operator
		if (binVal != '\\' && binVal != '<' && binVal != '>' && binVal != '~')
			b = ((Integer) mStack.pop ()).intValue ();
			
		int result = 0;
		switch (binaryOp.getValue ()) {
			// +++++++ priority three, highest precedence. (unary operators) +++++++++++++
			// unary minus (using backslash symbol to discern it from binary minus)
			case '\\': 
				result = -(a); 
				break;
			// 16bit low byte operator
			case '<': 
				result = a & 0xFF;
				if (mValReloc != ABSOLUTE) {
					if (mValReloc == RELOC_HIBYTE) throw new ParserException ("Low byte of high byte is ambiguous", mLexer);
					mValReloc = RELOC_LOBYTE;
				}
				break;
			// 16bit high byte operator
			case '>': 
				result = (a >> 8) & 0xFF;
				if (mValReloc != ABSOLUTE) {
					if (mValReloc == RELOC_LOBYTE) throw new ParserException ("High byte of low byte is ambiguous", mLexer);
					mValReloc = RELOC_HIBYTE;
				}
				break;
			// unary bitwise not
			case '~': 
				result = ~a; 
				break;
				
			// ++++++++ priority two, next highest precedence. +++++++++++
			// multiplication
			case '*': 
				result = b * a; 
				break;
			// division
			case '/': 
				if (a == 0 && pass != 1) throw new ParserException ("Division by zero", mLexer);
				else if (a == 0) throw new ParserException ("Internal assembler error div by zero", mLexer);
				result = b / a; 
				break;
			// bitwise and
			case '&': 
				result = b & a; 
				break;
			// bitwise exlusive or
			case '^': 
				result = b ^ a; 
				break;
			// arithmetic shift left
			case ('<' << 8) | '<': 
				result = b << a; 
				break;
			// arithmetic shift right
			case ('>' << 8) | '>': 
				result = b >> a; 
				break;

			// +++++ priority one, lowest precedence. +++++++++
			// binary plus
			case '+': 
				result = b + a; 
				break;
			// binary minus
			case '-': 
				result = b - a; 
				break;
			// bitwise or
			case '|': 
				result = b | a; 
				break;

			default:
				throw new ParserException ("Evaluate.calculate: fatal, unexpected binaryOp", mLexer);
		}
		return new Integer (result);
	}
	
	/**
	*	Evaluates an expression.
	*	@param exprEndTerminal The symbol type that is expected to terminate expression (LINEFEED for instance)
	*	@return A Symbol of type CONSTANT containing the result of the evaluation
	*/
	public Symbol evaluate (int pass, int ip, int exprEndTerminalType) throws ParserException, LexerException
	{
		mStack.clear ();
		Vector postfix = mInfixToPostfix.xform (pass, ip, exprEndTerminalType);
		mValReloc = mInfixToPostfix.isRelocatable ();
		int len = postfix.size ();

		int i = 0;
		// If the next symbol is operand, push it on stack.
		// If the next symbol is operator, call caluculate and push back result on stack.
		// When postfix expression vector is scanned through, the operand on top of stack is the result of expression.
		while (0 != len--) {
			Symbol m = (Symbol) postfix.elementAt (i++);
			if (m.getType () == CONSTANT) {
				mStack.push (new Integer (m.getValue ()));
			}
			else if (m.getType () == OPERATOR) {
				mStack.push (calculate (pass, m));
			}
		}
		
		// Stack should now have exactly one element.
		if (mStack.size () != 1) { 
			// die
			int a = 0;
			throw new ParserException ("Evaluator: Fatal program logic error in pass " + pass, mLexer);
			//System.err.println ("Fatal program logic error in Evaluator.");
			//System.exit (1);
		}
		
		// The final top of stack is the result.
		int result = ((Integer) mStack.pop ()).intValue ();

		// Return result
		return new Symbol ("", CONSTANT, result);
	}
	
	/**
	*	Returns relocatable status.
	*	@return 0 = not relocatable.
	*		1 == 16bit relocatable
	*		2 == 8bit relocatable low byte
	*		3 == 8bit relocatable high byte
	*/
	public int isRelocatable ()
	{
		return mValReloc;
	}
}
