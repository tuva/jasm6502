import java.io.*;
import java.util.*;

/**
*	Assembler 6502 engine
*	Implements the interface @see AbstractAssembler
*
*	@todo: BRANCH TEST TO MAKE SURE ALL BRANCHES ARE CALCULATED CORRECTLY
*
*	@author David Schager 2006
*/
public class Assembler6502 implements SymbolConstant6502, AbstractAssembler {
	private AbstractLexer mLexer = null;
	private AbstractLexer mOrgLexer = null;
	private Parser6502 mParser = null;
	private SymbolTable mKeywords = null;
	private SymbolTable mIdentifiers = null;
	private String mCommentChars = "!;";
	private RandomAccessFile mFile = null;
	private int mPass = 1;
	private int mAsciiMode = 0;
	
	/*
	// variables for object files (Not supported yet)
	private int mObjectOffset = 0;
	private Vector mReloc = null;
	// Exported symbols table	(Not implemented)
	private Vector mExports = null;
	// Unresolved references table	(Not implemented)
	private Vector mUnresolved = null;
	// pointer to current Segment
	private Vector mSegment = null;
	*/

	/**
	*	Output format mode.
	*	Valid values are:
	*	0 = output assembly to screen for debugging
	*	1 = raw output to disk (no file format)
	*	2 = Commodore 64 .prg file (16bit header with start address)
	*	99 = Secret test mode for evaluator (Only Accept expressions on each line and output them on screen)
	*/
	private int mMode = MODE_SCREEN;

	/**
	*	Constructor
	*	Creates instances of the member objects Lexer6502 and Parser6502, 
	*	and setup the symboltables.
	*/
	public Assembler6502 ()
	{
		mKeywords = new SymbolTable ();
		mIdentifiers = new SymbolTable ();
		buildKeywordTable6502 (mKeywords);
		mOrgLexer = new Lexer6502 (mKeywords, mIdentifiers, mCommentChars);
		mLexer = mOrgLexer;
		mParser = new Parser6502 (mLexer);
		/*
		mReloc = new Vector ();
		mExports = new Vector ();
		mUnresolved = new Vector ();
		mSegment = new Vector ();
		*/
	}

	/**
	*	Builds a symboltable containing the Vocabulary of the language.
	*	The symbol table that is passed to function will on completion of the method
	*	contain the symbols that make up the vocabulary. Theese symbols are called keywords.
	*	The constants are defined in SymbolConstant6502
	*	@param symbolTable The symbol table to insert the reserved keywords into
	*	@see SymbolConstant6502
	*/
	protected void buildKeywordTable6502 (SymbolTable symbolTable)
	{
		// Define the keywords
		Symbol[] symbols6502 = {
			new Symbol ("adc", ADC, MODE_A),
			new Symbol ("and", AND, MODE_A),
			new Symbol ("asl", ASL, ACC|MODE_B),
			new Symbol ("bcc", BCC, REL),
			new Symbol ("bcs", BCS, REL),
			new Symbol ("beq", BEQ, REL),
			new Symbol ("bit", BIT, ZP|ABS),
			new Symbol ("bmi", BMI, REL),
			new Symbol ("bne", BNE, REL),
			new Symbol ("bpl", BPL, REL),
			new Symbol ("brk", BRK, IMPL),
			new Symbol ("bvc", BVC, REL),
			new Symbol ("bvs", BVS, REL),
			new Symbol ("clc", CLC, IMPL),
			new Symbol ("cld", CLD, IMPL),
			new Symbol ("cli", CLI, IMPL),
			new Symbol ("clv", CLV, IMPL),
			new Symbol ("cmp", CMP, MODE_A),
			new Symbol ("cpx", CPX, IMM|ZP|ABS),
			new Symbol ("cpy", CPY, IMM|ZP|ABS),
			new Symbol ("dec", DEC, MODE_B),
			new Symbol ("dex", DEX, IMPL),
			new Symbol ("dey", DEY, IMPL),
			new Symbol ("eor", EOR, MODE_A),
			new Symbol ("inc", INC, MODE_B),
			new Symbol ("inx", INX, IMPL),
			new Symbol ("iny", INY, IMPL),
			new Symbol ("jmp", JMP, ABS|IND),
			new Symbol ("jsr", JSR, ABS),
			new Symbol ("lda", LDA, MODE_A),
			new Symbol ("ldx", LDX, IMM|ZP|ZPY|ABS|ABSY),
			new Symbol ("ldy", LDY, IMM|ZP|ZPX|ABS|ABSX),
			new Symbol ("lsr", LSR, ACC|MODE_B),
			new Symbol ("nop", NOP, IMPL),
			new Symbol ("ora", ORA, MODE_A),
			new Symbol ("pha", PHA, IMPL),
			new Symbol ("php", PHP, IMPL),
			new Symbol ("pla", PLA, IMPL),
			new Symbol ("plp", PLP, IMPL),
			new Symbol ("rol", ROL, ACC|MODE_B),
			new Symbol ("ror", ROR, ACC|MODE_B),
			new Symbol ("rti", RTI, IMPL),
			new Symbol ("rts", RTS, IMPL),
			new Symbol ("sbc", SBC, MODE_A),
			new Symbol ("sec", SEC, IMPL),
			new Symbol ("sed", SED, IMPL),
			new Symbol ("sei", SEI, IMPL),
			new Symbol ("sta", STA, ZP|ZPX|ABS|ABSX|ABSY|INDX|INDY),
			new Symbol ("stx", STX, ZP|ZPY|ABS),
			new Symbol ("sty", STY, ZP|ZPX|ABS),
			new Symbol ("tax", TAX, IMPL),
			new Symbol ("tay", TAY, IMPL),
			new Symbol ("tsx", TSX, IMPL),
			new Symbol ("txa", TXA, IMPL),
			new Symbol ("txs", TXS, IMPL),
			new Symbol ("tya", TYA, IMPL),
			/* hardware register keywords */
			new Symbol ("x", X, 'x'),
			new Symbol ("y", Y, 'y'),
			new Symbol ("a", A, 'a'),
			/* assembler directives. 
		 	install several directives with multiple names for
		 	compability with different assembler formats */
			new Symbol (".org", ORG, NULL),
			new Symbol ("org", ORG, NULL),
			new Symbol (".byte", BYTE, NULL),
			new Symbol ("byte", BYTE, NULL),
			new Symbol (".byt", BYTE, NULL),
			new Symbol ("byt", BYTE, NULL),
			new Symbol (".asc", BYTE, NULL),
			new Symbol ("asc", BYTE, NULL),
			new Symbol (".word", WORD, NULL),
			new Symbol ("word", WORD, NULL),
			new Symbol (".db", BYTE, NULL),
			new Symbol ("db", BYTE, NULL),
			new Symbol (".dw", WORD, NULL),
			new Symbol ("dw", WORD, NULL),
			new Symbol ("include", INCLUDE, NULL),
			new Symbol (".include", INCLUDE, NULL),
			new Symbol ("#include", INCLUDE, NULL),
			new Symbol (".proc", PROC, NULL),
			new Symbol (".scope", PROC, NULL),
			new Symbol (".endproc", ENDPROC, NULL),
			new Symbol (".endscope", ENDPROC, NULL),
			new Symbol ("@", TEMPLABEL, NULL),
			new Symbol ("@:", TEMPLABEL, NULL),
			new Symbol ("@f", FORWARDJUMP, NULL),
			new Symbol ("@+", FORWARDJUMP, NULL),
			new Symbol ("@b", BACKWARDJUMP, NULL),
			new Symbol ("@-", BACKWARDJUMP, NULL),
			/* the operators and other terminals */
			new Symbol ("+", OPERATOR, '+'),
			new Symbol ("-", OPERATOR, '-'),
			new Symbol ("/", OPERATOR, '/'),
			new Symbol ("*", OPERATOR, '*'),
			new Symbol ("$", OPERATOR, '$'),
			new Symbol ("%", OPERATOR, '%'),
			new Symbol ("#", OPERATOR, '#'),
			new Symbol ("<", OPERATOR, '<'),
			new Symbol (">", OPERATOR, '>'),
			new Symbol ("&", OPERATOR, '&'),
			new Symbol ("|", OPERATOR, '|'),
			new Symbol ("^", OPERATOR, '^'),
			new Symbol ("\\", OPERATOR, '\\'),	/* unary minus operator (negation) */
			new Symbol ("<<", OPERATOR, ('<' << 8) | '<'),
			new Symbol (">>", OPERATOR, ('>' << 8) | '>'),
			new Symbol ("~", OPERATOR, '~'),
			new Symbol ("=", ASSIGN, NULL),
			new Symbol ("(", LEFTPAREN, NULL),
			new Symbol (")", RIGHTPAREN, NULL),
			new Symbol (",", DELIMITER, ','),
			new Symbol ("\"", DOUBLEQUOTES, NULL)
		};
		// Install the opcode keywords into the keywords symboltable
		for (int i = 0; i < symbols6502.length; ++i) {
			symbolTable.install (symbols6502[i]);
		}
	}
	
	/**
	*	Formats and prints an integer in range 0-0xFF as two digit number
	*/
	protected void printHexByte (int b)
	{
		String out = Integer.toHexString (b);
		if (b < 10 && b >= 0) System.out.print ("0" + out);
		else System.out.print (out);
	}
	
	/**
	*	Formats and prints an integer in range 0-0xFFFF as four digit number
	*/
	protected void printHexWord (int w)
	{
		if (w < 0x100) printHexByte (w);
		else {
			String out = Integer.toHexString (w);
			if (w < 0x1000) System.out.print ("0" + out);
			else System.out.print (out);
		}
	}
	
	/**
	*	Creates the machine code from opcode number, and addressing mode constant.
	*	By indexing into machinCodeTab (SymbolConstant6502) with opcode number as first index, and
	*	addressing mode constant bit number as second index, the machine code is found.
	*	But the addressing index has to be transformed first from number to bit-number.
	*	The relationship is like so:
	*	addrMode	bit-number
	*	1			0
	*	2			1
	*	4			2
	*	8			3
	*	16			4
	*	etc..
	*	It is transformed by rightshifting until zero, the number of shifts - 1 = addressing index into 
	*	machinCodeTab.
	*	The length of the machine code is 1-3 bytes, and is looked up in addrModeLen table in SymbolConstant6502.
	*	The same addressing mode index can be used for this table too.
	*	There is 3 formats
	*	1 byte machine codes: 	code
	*	2 byte machine codes:	code, low-byte
	*	3 byte machine codes:	code, low-byte, high-byte
	*	Where low-byte is a 8bit number, and low/high byte is a 16bit little endian number.
	*
	*	The resulting bytes are packed into an integer with this format:
	*	bits 31-24	Machine code Length in bytes
	*	bits 23-16	Machine opcode
	*	bits 15-8	low-byte
	*	bits 7-0	high-byte
	*
	*	@param mc MachineCode6502 object returned by Parser6502, containing machine code data
	*	@param ip Current instruction pointer in assembly
	*
	*	@see SymbolConstant6502
	*	@return true for success
	*/
	private int constructMachineCode (MachineCode6502 mc6502, int ip) throws ParserException, RuntimeException
	{
		int addrMode = mc6502.getAddrMode ();
		Symbol opcode = mc6502.getOpcode ();
		int operand = mc6502.getOperand ();
		
		if ((addrMode & ~(ACC|IMM|ZP|ZPX|ZPY|ABS|ABSX|ABSY|IMPL|REL|INDX|INDY|IND)) != 0) {
			String reason = "Unexpected value of addrMode variable " + addrMode;
			throw new RuntimeException (reason);
		}
		// Is addressing mode OK for this opcode?
		if ((opcode.getValue () & addrMode) == 0) {
			throw new ParserException ("Illegal addressing mode", mLexer);
		}

		// Calculate the addressing index
		int index = -1;
		int a = addrMode;
		while (true) {
			a >>= 1;
			++index;
			if (a == 0) {
				break;
			}
		}
		// get machine opcode
		int op = machineCodeMatrix[opcode.getType ()][index];
		// Errors in table?
		if (op < 0) throw new RuntimeException ("Unexpected -1 in machineCodeTab");
		
		// get machine code length
		int mcLen = addrModeLen [index];
		if (mcLen < 0 || mcLen >= 4) throw new RuntimeException ("Unexpected mc len in constructMachineCode");
		
		if (mPass == 2) {
			int lo = operand & 0xFF;
			int hi = (operand >> 8) & 0xFF;
			int outLen = mcLen;
			if (mc6502.getAddrMode () == REL) {
				// patch output for screen when relative addressing, that's why outLen is 3
				outLen = 3;
				// must be within 126 bytes before the branch or 128 bytes after the brach instruction
				// (A branch to itself is in fact a branch -2 because the branch is counted from the address of
				// the following opcode, and not from the address of the branch-opcode)
				int nip = ip + mcLen;
				if (operand < nip) {
					if (ip - operand >= 127) {
						/*
						System.out.println ("addr mode: " + addrToString [index]);
						System.out.println ("opcode: " +  typeToString [opcode.getType ()]);
						System.out.println ("operand: " + Integer.toHexString (operand));
						System.out.println ("");
						*/
						throw new ParserException ("Branch too far (max 126 bytes backwards)", mLexer);
					}
					// create negative byte with 2's complement manually (to be sure)
					operand = (nip - operand) & 0xFF;
					operand = (operand ^ 0xFF) + 1;
				}
				else {
					if (operand - ip >= 129) throw new ParserException ("Branch too far (max 128 bytes forward)", mLexer);
					operand -= nip;
				}
			}

			if (mMode == MODE_SCREEN) {
				// output to screen
				printHexWord (ip);
				System.out.print (" ");
				printHexByte (op);
				System.out.print (" ");
				if (mcLen >= 2) printHexByte (operand & 0xFF);
				System.out.print (" ");
				if (mcLen == 3) printHexByte ((operand >> 8) & 0xFF);
				System.out.print ("\n");
			}
			/*
			else if (mMode == MODE_OBJECT) {
				// output to object file
				toObjectFile (op, mcLen, operand, mParser.getRelocatable ());
			}
			*/
			else {
				try {
					// output to executable binary
					mFile.writeByte (op);
					if (mcLen == 2) {
						mFile.writeByte (operand & 0xFF);
					}
					else if (mcLen == 3) {
						// little endian
						mFile.writeByte (operand & 0xFF);
						mFile.writeByte ((operand >> 8) & 0xFF);
					}
				}
				catch (IOException e) {
					System.out.println (e);
				}
			}
		}
		
		/*
		System.out.println ("addr mode: " + addrToString [index]);
		System.out.println ("opcode: " +  typeToString [opcode.getType ()]);
		System.out.println ("operand: " + Integer.toHexString (operand));
		System.out.println ("len: " + mcLen);
		*/
		/*
					int reloc = mParser.getRelocatable ();
			if (reloc == 0) System.out.println ("Abs");
			else if (reloc == 1) System.out.println ("Rel");
			else if (reloc == 2) System.out.println ("Rel lo");
			else if (reloc == 3) System.out.println ("Rel hi");
		System.out.println ("");
		*/
		
		return mcLen;
	}
	
	
	/*
	// Not implemented
	private void toObjectFile (int op, int mcLen, int operand, int reloc)
	{
		mSegment.addElement (new Integer (op));
		if (mcLen == 2) {
			mSegment.addElement (new Integer (operand & 0xFF));
		}
		else if (mcLen == 3) {
			// little endian
			mSegment.addElement (new Integer (operand & 0xFF));
			mSegment.addElement (new Integer ((operand >> 8) & 0xFF));
		}
		
		if (reloc == RELOC) {
			mReloc.addElement (new Integer (mObjectOffset + 1));
			mReloc.addElement (new Integer (RELOC));
		}
		else if (reloc == RELOC_LOBYTE) {
			mReloc.addElement (new Integer (mObjectOffset + 1));
			mReloc.addElement (new Integer (RELOC_LOBYTE));
		}
		else if (reloc == RELOC_HIBYTE) {
			mReloc.addElement (new Integer (mObjectOffset + 1));
			mReloc.addElement (new Integer (RELOC_HIBYTE));
		}

		mObjectOffset += mcLen;
	}
	*/
	
	/**
	*	Write sequence of bytes to machine code
	*/
	private void byteSequenceToMachineCode (int ip, Vector seq)
	{
		try {
			int i = seq.size ();
			int j = 0;
			do {
				int val = ((Integer) seq.elementAt (j++)).intValue ();
				if (mMode == MODE_SCREEN) {
					printHexWord (ip++);
					System.out.print (" ");
					printHexByte (val & 0xFF);
					System.out.println ("");
				}
				/*
				else if (mMode == MODE_OBJECT) {
					mSegment.addElement (new Integer (val & 0xFF));
					// add getting a vector of reloc values from parser and scan it here
				}
				*/
				else mFile.writeByte (val);
			} while (--i != 0);
		}
		catch (IOException e) {
			System.err.println (e);
			System.exit (1);
		}
	}
	
	/**
	*	Write a sequence of words to machine code
	*/
	private void wordSequenceToMachineCode (int ip, Vector seq)
	{
		try {
			int i = seq.size ();
			int j = 0;
			do {
				int val = ((Integer) seq.elementAt (j++)).intValue ();
				if (mMode == MODE_SCREEN) {
					printHexWord (ip++);
					System.out.print (" ");
					printHexByte (val & 0xFF);
					System.out.println ("");
					printHexWord (ip++);
					System.out.print (" ");
					printHexByte ((val >> 8) & 0xFF);
				}
				/*
				else if (mMode == MODE_OBJECT) {
					mSegment.addElement (new Integer (val & 0xFF));
					mSegment.addElement (new Integer ((val >> 8) & 0xFF));
					// add getting a vector of reloc values from parser and scan it here
				}
				*/
				else {
					// little endian
					mFile.writeByte (val & 0xFF);
					mFile.writeByte ((val >> 8) & 0xFF);
				}

			} while (--i != 0);
		}
		catch (IOException e) {
			System.err.println (e);
			System.exit (1);
		}
	}
	
	/**
	*	Zero fill memory
	*	@param oldIp instruction pointer to begin fill zeros from.
	*	@param newIp instruction pointer where we end fill zeros.
	*/
	private void zeroFill (int oldIp, int newIp) throws ParserException, RuntimeException
	{
		try {
			if (newIp >= oldIp + 1) {
				for (int i = oldIp; i < newIp; ++i) {
					mFile.writeByte (0);
				}
			}
			else if (newIp < oldIp)
				throw new ParserException ("Cannot set new origin backwards in memory", mLexer);
		}
		catch (IOException e) {
			System.out.println (e);
			System.exit (1);
		}
	}
	
	/**
	*	@see AbstractAssembler.getLexer
	*/
	public AbstractLexer getLexer ()
	{
		return mLexer;
	}
	
	/**
	*	@see AbstractAssembler.getParser
	*/
	public AbstractParser getParser ()
	{
		return mParser;
	}
	
	
	/**
	*	Includes a source file.
	*	The contents of the included file is injected directly into the source containing the include directive.
	*	@param fileName file name of file to include
	*	@param stack stack that holds lexers.
	*/
	public void includeFile (String fileName, Stack stack) throws ParserException, LexerException
	{
		File file = new File (fileName);
		if (!file.exists ()) 
			throw new ParserException ("Cannot open file " + fileName, mLexer);
		AbstractLexer newLexer = new Lexer6502 (mKeywords, mIdentifiers, mCommentChars);
		newLexer.attachInput (fileName);
		mParser.setLexer (newLexer);
		mLexer = newLexer;
		stack.push (newLexer);
	}
	
	/**
	*	Installs a label into symbol table
	*/
	private void installLabel (Symbol label, int ip) throws ParserException, LexerException
	{
		Symbol prev = mIdentifiers.getSymbolCurrentLevel (label.getName ());
		if (prev != null) {	
			if (prev.getType () == LABEL && prev.getValue () != NULL) 
				throw new ParserException ("Label redefinition: " + prev.getName (), mLexer);
			else if (prev.getType () == IDENTIFIER && prev.getValue () != NULL)
				throw new ParserException ("Symbol redefinition: " + prev.getName (), mLexer);
		}
		Symbol installLabel = new Symbol (label.getName (), LABEL, ip);
		mIdentifiers.install (installLabel);
	}

	/**
	*	@see AbstractAssembler.assemble
	*/
	public int assemble (String filename, String outFileName) throws IOException
	{
		int error = 0;
		int numOpcodes = 0;
		
		Vector tempIdentifiers = new Vector ();
		Vector[] tempLabels = new Vector [mIdentifiers.getLevelLimit ()];
		for (int i = 0; i < mIdentifiers.getLevelLimit (); ++i) tempLabels[i] = new Vector ();


		// pass reader to lexer
		mLexer.attachInput (filename);
		
		if (mMode == MODE_SECRET) {
			// secret test mode for evaluator
			try {
				int ip = 0;	// fake instruction pointer, just add one per line
				Evaluator eval = new Evaluator (mLexer);
				while (!mLexer.eof ()) {
					Symbol result = eval.evaluate (2, ip++, LINEFEED);
					System.out.println ("line " + (mLexer.getLineNum () - 1) + ": " + result.getValue ());
					// eat linefeed
					mLexer.getNext ();
				}
			}
			catch (Exception e) {
				System.err.println (e.getMessage ());
				System.exit (0);
			}
			System.exit (0);
		}
		
		// Use 0x1000 as default start address when assembling
		int ip = 0x1000;
		int startAddress = ip;
		
		// PASS 1 - parse labels & parse assigns 
		mPass = 1;
		mParser.setPass (mPass);
		Stack lexerStack = new Stack ();
		lexerStack.push (mLexer);
		while (true) {
			try {
				mParser.setInstructionPointer (ip);
				int result = mParser.doNonTerminal ();
				if (result == PARSERESULT_EOF) {
					lexerStack.pop ();
					if (lexerStack.size () == 0)
						break;
					mLexer = (AbstractLexer) lexerStack.peek ();
					mParser.setLexer (mLexer);
				}
				else if (result == PARSERESULT_OPCODE) {
					ip += constructMachineCode (mParser.getMachineCode (), ip);
					++numOpcodes;
				}
				else if (result == PARSERESULT_NEW_ORIGIN) {
					ip = mParser.getNewOrigin ();
					if (ip < 0x100) {
						System.err.println ("Assembly to zero page not supported.");
						System.exit (1);
					}
					if (numOpcodes == 0) startAddress = ip;
				}
				else if (result == PARSERESULT_INCLUDE) {
					includeFile (mParser.getSymbol ().getName (), lexerStack);
				}
				else if (result == PARSERESULT_UNDEF_STATEMENT) {
					throw new ParserException ("Undefined statement", mLexer);
				}
				else if (result == PARSERESULT_TEMPORARY_LABEL) {
					tempLabels[mIdentifiers.getLexLevel ()].addElement (new Integer (ip));
				}
				else if (result == PARSERESULT_BYTE_SEQ) {
					Vector v = mParser.getSequence ();
					ip += v.size ();
					++numOpcodes;
				}
				else if (result == PARSERESULT_WORD_SEQ) {
					Vector v = mParser.getSequence ();
					ip += (v.size () << 1);
					++numOpcodes;
				}
				else if (result == PARSERESULT_LABEL) {
					Symbol label = mParser.getSymbol ();
					if (label.getType () != LABEL) {
						System.err.println ("Program logic error: label is not of type label");
						System.exit (1);
					}
					else if (label.getValue () != NULL) {
						System.err.println ("Program logic error: label is not null in pass 1");
						System.err.println (label.getName () + " " + label.getType () + " " + label.getValue ());
						System.exit (1);
					}
					installLabel (label, ip);
					//System.out.print ("installed Label " + label.getName () + " with address 0x");
					//printHexWord (label.getValue ());
					//System.out.println ("");
				}
				else if (result == PARSERESULT_ASSIGN) {
					// install into symbol table
					Symbol identifier = mParser.getSymbol ();
					mIdentifiers.remove (identifier.getName ());
					mIdentifiers.install (identifier);
					tempIdentifiers.addElement (identifier);
				}
				else if (result == PARSERESULT_PROC) {
					Symbol procLabel = mParser.getSymbol ();
					installLabel (procLabel, ip);
					if (!mIdentifiers.stepIntoNamespace (procLabel.getName ())) 
						throw new ParserException ("Lexical max level is reached", mLexer);
				}
				else if (result == PARSERESULT_ENDPROC) {
					if (!mIdentifiers.stepOut ())
						throw new ParserException ("Found unmatched end of procedure", mLexer);
				}
				else {
					System.err.println ("Unexpected return from mParser in Assembler6502.assemble");
					System.exit (1);
				}
			}
			catch (LexerException e) {
				System.err.println (e.getMessage ());
				error = 1;
			}
			catch (ParserException e) {
				System.err.println (e.getMessage ());
				error = 1;
			}
			catch (Exception e) {
				System.err.println (e);
				e.printStackTrace ();
				error = 1;
			}
		}
		
		if (mIdentifiers.getLexLevel () != 0) {
			System.err.println ("Scope is not zero after pass 1, did you forget end a procedure?");
			error = 1;
		}
		
		if (error != 0) {
			System.exit (error);
		}
		
		// Now remove all identifiers from symbol table
		for (int i = 0; i < tempIdentifiers.size (); ++i) {
			Symbol identifier = (Symbol) tempIdentifiers.elementAt (i);
			mIdentifiers.removeFromAllNamespaces (identifier.getName ());
		}
		
		
		// PASS 2
		
			// pass reader to lexer
		mLexer = mOrgLexer;
		mLexer.attachInput (filename);
		lexerStack.push (mLexer);
		mParser.setLexer (mLexer);
		mPass = 2;
		mParser.setPass (mPass);
		
		if (mAsciiMode == 1) mParser.setC64UpperCaseMode (true);
		else if (mAsciiMode == 2) mParser.setC64LowerCaseMode (true);
		
		ip = 0x1000;
		numOpcodes = 0;
		
		
		if (mFile != null) mFile.close ();
		File remove = new File (outFileName);
		if (remove.exists ()) remove.delete ();
		remove = null;

		mFile = new RandomAccessFile (outFileName, "rw");
		if (mMode == MODE_C64) {
			// write start address header, for .prg (commodore 64)
			mFile.writeByte (startAddress & 0xFF);
			mFile.writeByte ((startAddress >> 8) & 0xFF);
		}
		
		while (true) {
			try {
				mParser.setInstructionPointer (ip);
				mParser.setTempLabels (tempLabels[mIdentifiers.getLexLevel ()]);
				int result = mParser.doNonTerminal ();
				if (result == PARSERESULT_EOF) {
					lexerStack.pop ();
					if (lexerStack.size () == 0)
						break;
					mLexer = (AbstractLexer) lexerStack.peek ();
					mParser.setLexer (mLexer);
				}
				else if (result == PARSERESULT_OPCODE) {
					ip += constructMachineCode (mParser.getMachineCode (), ip);
					++numOpcodes;
				}
				else if (result == PARSERESULT_NEW_ORIGIN) {
					int newIp = mParser.getNewOrigin ();
					// zero fill here
					if (numOpcodes != 0) {
						zeroFill (ip, newIp);
					}
					ip = newIp;
				}
				else if (result == PARSERESULT_INCLUDE) {
					includeFile (mParser.getSymbol ().getName (), lexerStack);
				}
				else if (result == PARSERESULT_UNDEF_STATEMENT) {
					throw new ParserException ("Undefined statement", mLexer);
				}
				else if (result == PARSERESULT_TEMPORARY_LABEL) {
					// do nothing in pass 2
				}
				else if (result == PARSERESULT_BYTE_SEQ) {
					Vector v = mParser.getSequence ();
					int newIp = ip + v.size ();
					++numOpcodes;
					byteSequenceToMachineCode (ip, v);
					ip = newIp;
				}
				else if (result == PARSERESULT_WORD_SEQ) {
					Vector v = mParser.getSequence ();
					int newIp = ip + (v.size () << 1);
					++numOpcodes;
					wordSequenceToMachineCode (ip, v);
					ip = newIp;
				}
				else if (result == PARSERESULT_LABEL) {
					// Skip labels in pass 2
				}
				else if (result == PARSERESULT_ASSIGN) {
					// install into symbol table
					Symbol identifier = mParser.getSymbol ();
					mIdentifiers.remove (identifier.getName ());
					mIdentifiers.install (identifier);
				}
				else if (result == PARSERESULT_PROC) {
					Symbol procLabel = mParser.getSymbol ();
					if (!mIdentifiers.stepIntoNamespace (procLabel.getName ())) 
						throw new ParserException ("Lexical max level is reached", mLexer);
				}
				else if (result == PARSERESULT_ENDPROC) {
					if (!mIdentifiers.stepOut ())
						throw new ParserException ("Found unmatched end of procedure", mLexer);
				}			
				else {
					System.err.println ("Unexpected return from mParser in Assembler6502.assemble");
					System.exit (1);
				}
			}
			catch (LexerException e) {
				System.err.println (e.getMessage ());
				error = 1;
			}
			catch (ParserException e) {
				System.err.println (e.getMessage ());
				error = 1;
			}
			catch (Exception e) {
				System.err.println (e);
				e.printStackTrace ();
				error = 1;
			}
		}

		mFile.close ();
		mFile = null;
		if (error != 0) {
			// delete output file if errors were encountered.
			File file = new File (outFileName);
			file.delete ();
		}

		return error;
	}
	/**
	*	Sets output mode.
	*	@param mode to set, can be one of following:
	*		0 = output to screen
	*		1 = output to raw binary to disk
	*/
	public void setMode (int mode)
	{
		mMode = mode;
	}
	
	/**
	*	Sets ascii translation mode when assembling for Commodore 64.
	*	@param asciiMode Determines the mode, can be one of the following:
	*	0 = no translation
	*	1 = translate to upper case
	*	2 = translate to lower case
	*/
	public void setAsciiTranslation (int asciiMode)
	{
		mAsciiMode = asciiMode;
	}
}
