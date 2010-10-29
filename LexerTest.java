import junit.framework.*;
import java.io.*;

/**
*	@test Runs a test case on Lexer6502.java.
*	<p>
*	Requiers that JUnit is installed.
*	It tries a small variety of symbols from the 6502 syntax, by loading the lexer with some test source files
*	and comparing the symbols the lexer returns with expected hardcoded ones.
*	<br /><br />
*	To run this test from shell, type from directory containing this source: 
*	java junit.textui.TestRunner LexerTest
*	</p>
*	@author David Schager 2006
*/
public class LexerTest extends TestCase implements SymbolConstant6502 {
	private AbstractAssembler mAsm = null;
	private AbstractLexer mLexer = null;
	private static Symbol mHash = new Symbol ("#", OPERATOR, '#');
	private static Symbol mLF = new Symbol ("", LINEFEED, NULL);
	private static Symbol mAssign = new Symbol ("=", ASSIGN, NULL);
	/**	
		Array of symbols expected back from Lexer when passing file testconstants.asm to Lexer
		The contents of that file is:
		#$9af0
		#$100
		#1000
		100
	*/
	private static Symbol[] mExpectedConstants = {
		mHash, new Symbol ("", CONSTANT, 0x9af0), mLF,
		mHash, new Symbol ("", CONSTANT, 0x100), mLF,
		mHash, new Symbol ("", CONSTANT, 1000), mLF,
		new Symbol ("", CONSTANT, 100), mLF,
		new Symbol ("", EOF, NULL)
	};
	
	/**
		Array of symbols expected back from Lexer when passing file testidentifiers.asm to Lexer
		The contents of that file is:
		hello
		label: hehh
		apa = hello
		*+-/($100)! a comment
		; more comments
	*/
	private static Symbol[] mExpectedIdentifiers = {
		new Symbol ("hello", IDENTIFIER, NULL), mLF,
		new Symbol ("label", LABEL, NULL), new Symbol ("hehh", IDENTIFIER, NULL), mLF,
		new Symbol ("apa", IDENTIFIER, NULL), mAssign,  new Symbol ("hello", IDENTIFIER, NULL), mLF,
		new Symbol ("*", OPERATOR, '*'), new Symbol ("+", OPERATOR, '+'), new Symbol ("-", OPERATOR, '-'),
		new Symbol ("/", OPERATOR, '/'), new Symbol ("(", LEFTPAREN, NULL), new Symbol ("", CONSTANT, 0x100),
		new Symbol (")", RIGHTPAREN, NULL), mLF, 
		new Symbol ("", EOF, NULL)
	};

	/**
		Array of symbols expected back from Lexer when passing file testprogram.asm to Lexer
		The contents of that file is:
		*=$1000
 		addr1=$02
 		ldx #$10
 		ldy #$00
		loop: lda (addr1),y ; use indirect indexed y addressing
 		sta ($fb,x)
 		dex
 		iny
 		bne loop
 		rts	
	*/
	private static Symbol[] mExpectedProgram = {
		new Symbol ("*", OPERATOR, '*'), mAssign, new Symbol ("", CONSTANT, 0x1000), mLF,
		new Symbol ("addr1", IDENTIFIER, NULL), mAssign, new Symbol ("", CONSTANT, 2), mLF,
		new Symbol ("ldx", LDX, NULL), new Symbol ("#", OPERATOR, '#'), new Symbol ("", CONSTANT, 0x10), mLF,
		new Symbol ("ldy", LDY, NULL), new Symbol ("#", OPERATOR, '#'), new Symbol ("", CONSTANT, 0), mLF,
		new Symbol ("loop", LABEL, NULL), new Symbol ("lda", LDA, NULL), new Symbol ("(", LEFTPAREN, NULL),
			new Symbol ("addr1", IDENTIFIER, NULL), new Symbol (")", RIGHTPAREN, NULL), new Symbol (",", DELIMITER, ','),
			new Symbol ("y", Y, NULL), mLF,
		new Symbol ("sta", STA, NULL), new Symbol ("(", LEFTPAREN, NULL), new Symbol ("", CONSTANT, 0xfb),
			new Symbol (",", DELIMITER, ','), new Symbol ("x", X, NULL), new Symbol (")", RIGHTPAREN, NULL), mLF,
		new Symbol ("dex", DEX, NULL), mLF,
		new Symbol ("iny", INY, NULL), mLF,
		new Symbol ("bne", BNE, NULL), new Symbol ("loop", LABEL, NULL), mLF,
		new Symbol ("rts", RTS, NULL), mLF,
		new Symbol ("", EOF, NULL)
	};

	/**
	*	Constructor, calls TestCase Parent class from JUnit
	*/
	public LexerTest (String testName)
	{
		super (testName);
	}
	
	/**
	*	Sets up Test
	*	Instantiates an Assembler6502 object, and gets the lexer from it.
	*/
	public void setUp ()
	{
		mAsm = new Assembler6502 ();
		mLexer = mAsm.getLexer ();
	}

	/**
	*	Test the Lexers parsing of constants.
	*	The file testconstants.asm (which must be in same directory as test) is passed by filename to Lexer.
	*	For each passed symbol from Lexer, method compares it with expected Symbol in member array 
	*	mExpectedConstants.
	*	The constants are both checked by symbol type, and that the lexer could interpret the constants
	*	values, both decimal and hexadecimal, correctly.
	*/
	public void testConstants ()
	{
		try {
			mLexer.attachInput ("test/testconstants.asm");
			int i = 0;
			while (i < mExpectedConstants.length) {
				Symbol symbol = mLexer.getNext ();
				assertEquals (mExpectedConstants[i].getName (), symbol.getName ());
				assertEquals (mExpectedConstants[i].getType (), symbol.getType ());
				assertEquals (mExpectedConstants[i].getValue (), symbol.getValue ());
				++i;
			}
		}
		catch (Exception e) {
			System.err.println (e);
			e.printStackTrace ();
		}
	}

	/**
	*	Test the Lexers parsing of Identifiers.
	*	The file testidentifiers.asm (which must be in same directory as test) is passed by filename to Lexer.
	*	For each passed symbol from Lexer, method compares it with expected Symbol in member array 
	*	mExpectedIdentifiers. Apart from identifiers, operators (*-+) and are checked, and proper ignoring of
	*	comments.
	*/
	public void testIdentifiers ()
	{
		try {
			mLexer.attachInput ("test/testidentifiers.asm");
			int i = 0;
			while (i < mExpectedIdentifiers.length) {
				Symbol symbol = mLexer.getNext ();
				//System.err.print (symbol.getName () + " ");
				//System.err.println ("0x" + Integer.toHexString (symbol.getType ()));
				assertEquals (mExpectedIdentifiers[i].getName (), symbol.getName ());
				assertEquals (mExpectedIdentifiers[i].getType (), symbol.getType ());
				assertEquals (mExpectedIdentifiers[i].getValue (), symbol.getValue ());
				++i;
			}
		}
		catch (Exception e) {
			System.err.println (e);
			e.printStackTrace ();
		}
	}
	
	/**
	*	Test the Lexers parsing of small assembler program.
	*	The file testprogram.asm (which must be in same directory as test) is passed by filename to Lexer.
	*	For each passed symbol from Lexer, method compares it with expected Symbol in member array 
	*	mExpectedProgram. This is a more realistic test, as it is a small source file.
	*	All types of symbols are tested here.
	*/
	public void testProgram ()
	{
		try {
			mLexer.attachInput ("test/testprogram.asm");
			int i = 0;
			while (i < mExpectedProgram.length) {
				Symbol symbol = mLexer.getNext ();
				//System.err.print (symbol.getName () + " ");
				//System.err.print ("0x" + Integer.toHexString (symbol.getType ()));
				//System.err.println (" 0x" + Integer.toHexString (symbol.getValue ()));
				assertEquals (mExpectedProgram[i].getName (), symbol.getName ());
				assertEquals (mExpectedProgram[i].getType (), symbol.getType ());
				// Don't check values.. opcodes contains addressing constants, no need to check
				++i;
			}
		}
		catch (Exception e) {
			System.err.println (e);
			e.printStackTrace ();
		}
	}
}

/*
	ett fel, när ingen linefeed innan eof
*/
