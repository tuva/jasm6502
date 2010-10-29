/**
*	jasm6502
*	Contains a number of constants used by jasm6502
*	@author David Schager 2006
*/
public interface SymbolConstant6502 {
	// opcodes (reserved numbers 0-127 (0x00-0x7F)
	public static final int ADC = 0x00;
	public static final int AND = 0x01;
	public static final int ASL = 0x02;
	public static final int BCC = 0x03;
	public static final int BCS = 0x04;
	public static final int BEQ = 0x05;
	public static final int BIT = 0x06;
	public static final int BMI = 0x07;
	public static final int BNE = 0x08;
	public static final int BPL = 0x09;
	public static final int BRK = 0x0a;
	public static final int BVC = 0x0b;
	public static final int BVS = 0x0c;
	public static final int CLC = 0x0d;
	public static final int CLD = 0x0e;
	public static final int CLI = 0x0f;
	
	public static final int CLV = 0x10;
	public static final int CMP = 0x11;
	public static final int CPX = 0x12;
	public static final int CPY = 0x13;
	public static final int DEC = 0x14;
	public static final int DEX = 0x15;
	public static final int DEY = 0x16;
	public static final int EOR = 0x17;
	public static final int INC = 0x18;
	public static final int INX = 0x19;
	public static final int INY = 0x1a;
	public static final int JMP = 0x1b;
	public static final int JSR = 0x1c;
	public static final int LDA = 0x1d;
	public static final int LDX = 0x1e;
	public static final int LDY = 0x1f;
	
	public static final int LSR = 0x20;
	public static final int NOP = 0x21;
	public static final int ORA = 0x22;
	public static final int PHA = 0x23;
	public static final int PHP = 0x24;
	public static final int PLA = 0x25;
	public static final int PLP = 0x26;
	public static final int ROL = 0x27;
	public static final int ROR = 0x28;
	public static final int RTI = 0x29;
	public static final int RTS = 0x2a;
	public static final int SBC = 0x2b;
	public static final int SEC = 0x2c;
	public static final int SED = 0x2d;
	public static final int SEI = 0x2e;
	public static final int STA = 0x2f;
	
	public static final int STX = 0x30;
	public static final int STY = 0x31;
	public static final int TAX = 0x32;
	public static final int TAY = 0x33;
	public static final int TSX = 0x34;
	public static final int TXA = 0x35;
	public static final int TXS = 0x36;
	public static final int TYA = 0x37;
	
	// assembler directives
	
	public static final int ORG = 0x80;
	public static final int LABEL = 0x81;
	public static final int BYTE = 0x82;
	public static final int WORD = 0x83;
	public static final int INCLUDE = 0x84;
	public static final int PROC = 0x85;
	public static final int ENDPROC = 0x86;
	
	// other keywords
	
	public static final int EOF	= 0x100;
	public static final int LINEFEED = 0x101;
	public static final int OPERATOR = 0x102;
	public static final int ASSIGN = 0x103;
	public static final int LEFTPAREN = 0x104;
	public static final int RIGHTPAREN = 0x105;
	public static final int DELIMITER = 0x106;
	public static final int X = 0x107;
	public static final int Y = 0x108;
	public static final int A = 0x109;
	public static final int CONSTANT = 0x10a;
	public static final int IDENTIFIER = 0x10b;
	public static final int DOUBLEQUOTES = 0x10c;
	public static final int CHARSEQUENCE = 0x10d;
	public static final int TEMPLABEL = 0x10e;
	public static final int FORWARDJUMP = 0x10f;
	public static final int BACKWARDJUMP = 0x110;
	
	// values 
	public static final int NULL = 0xFFFFFFFF;
	
	// addressing modes
	public static final int ACC =  1;		// accumulator
	public static final int IMM =  1 << 1;		// immediate
	public static final int ZP =   1 << 2;	// zero page
	public static final int ZPX =  1 << 3;	// zero page, x
	public static final int ZPY =  1 << 4;	// zero page, y
	public static final int ABS =  1 << 5;	// absolute
	public static final int ABSX = 1 << 6;	// absolute, x
	public static final int ABSY = 1 << 7;	// absolute, y
	public static final int IMPL = 1 << 8;	// implied
	public static final int REL  = 1 << 9;	// relative
	public static final int INDX = 1 << 10;	// indirect, x
	public static final int INDY = 1 << 11;	// indirect, y
	public static final int IND =  1 << 12;	// indirect
	public static final int MODE_A = IMM|ZP|ZPX|ABS|ABSX|ABSY|INDX|INDY;
	public static final int MODE_B = ZP|ZPX|ABS|ABSX;



	
	/**
	* machinecode length's for addressing modes (in bytes)
	*/
	public static final int addrModeLen[] = {
		1, // accumulator
		2, // immediate
		2, // zero page
		2, // zero page, x
		2, // zero page, y
		3, // absolute
		3, // absolute, x
		3, // absolute, y
		1, // implied
		2, // relative
		2, // indirect, x
		2, // indirect, y
		3 // indirect
	};

	/**
	*	A table used when calculating the machine codes for an opcode.
	*	Two indeces are used. The first is the opcode define, see 'opcodes' defines at top of this file.
	*	The second is addressing mode, see two blocks up this file.
	*	By indexing into this table the machine code for the opcode in the addressing mode specified is aquired.
	*	If a -1 is aquired it means the opcode is illegal in that addressing mode.
	*	Usage:
	*	int opcode = machineCodeTab [opcode][addressing mode]
	*	@note The aquired machine code should be written to disk as an 8-bit unsigned byte, not an integer.
	*	(The integer format is for signalling illegal opcode with a negative one.)
	*/
	public static final int[][] machineCodeMatrix = {
	//	  ACC   IMM   ZP    ZPX   ZPY   ABS   ABSX  ABSY  IMPL  REL   INDX  INDY  IND
		{ -1  , 0x69, 0x65, 0x75, -1  , 0x6d, 0x7d, 0x79, -1  , -1  , 0x61, 0x71, -1  }, // ADC
		{ -1  , 0x29, 0x25, 0x35, -1  , 0x2d, 0x3d, 0x39, -1  , -1  , 0x21, 0x31, -1  }, // AND
		{ 0x0a, -1  , 0x06, 0x16, -1  , 0x0e, 0x1e, -1  , -1  , -1  , -1  , -1  , -1  }, // ASL
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0x90, -1  , -1  , -1  }, // BCC
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0xb0, -1  , -1  , -1  }, // BCS
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0xf0, -1  , -1  , -1  }, // BEQ
		{ -1  , -1  , 0x24, -1  , -1  , 0x2c, -1  , -1  , -1  , -1  , -1  , -1  , -1  }, // BIT
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0x30, -1  , -1  , -1  }, // BMI
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0xd0, -1  , -1  , -1  }, // BNE
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0x10, -1  , -1  , -1  }, // BPL
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0x00, -1  , -1  , -1  , -1  }, // BRK
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0x50, -1  , -1  , -1  }, // BVC
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0x70, -1  , -1  , -1  }, // BVS
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0x18, -1  , -1  , -1  , -1  }, // CLC
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0xd8, -1  , -1  , -1  , -1  }, // CLD
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0x58, -1  , -1  , -1  , -1  }, // CLI
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0xb8, -1  , -1  , -1  , -1  }, // CLV
		{ -1  , 0xc9, 0xc5, 0xd5, -1  , 0xcd, 0xdd, 0xd9, -1  , -1  , 0xc1, 0xd1, -1  }, // CMP
		{ -1  , 0xe0, 0xe4, -1  , -1  , 0xec, -1  , -1  , -1  , -1  , -1  , -1  , -1  }, // CPX
		{ -1  , 0xc0, 0xc4, -1  , -1  , 0xcc, -1  , -1  , -1  , -1  , -1  , -1  , -1  }, // CPY
		{ -1  , -1  , 0xc6, 0xd6, -1  , 0xce, 0xde, -1  , -1  , -1  , -1  , -1  , -1  }, // DEC
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0xca, -1  , -1  , -1  , -1  }, // DEX
		{ -1  , -1  , -1  , -1  , -1  , -1  , -1  , -1  , 0x88, -1  , -1  , -1  , -1  }, // DEY
		{ -1  , 0x49, 0x45, 0x55, -1  , 0x4d, 0x5d, 0x59, -1  , -1  , 0x41, 0x51, -1  }, // EOR
		{ -1  , -1  , 0xe6, 0xf6, -1  , 0xee, 0xfe, -1  , -1  , -1  , -1  , -1  , -1  }, // INC
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0xe8, -1  , -1  , -1  , -1  }, // INX
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0xc8, -1  , -1  , -1  , -1  }, // INY
		{ -1  , -1  , -1  ,  -1 , -1  , 0x4c, -1  , -1  , -1  , -1  , -1  , -1  , 0x6c}, // JMP
		{ -1  , -1  , -1  ,  -1 , -1  , 0x20, -1  , -1  , -1  , -1  , -1  , -1  , -1  }, // JSR
		{ -1  , 0xa9, 0xa5, 0xb5, -1  , 0xad, 0xbd, 0xb9, -1  , -1  , 0xa1, 0xb1, -1  }, // LDA
		{ -1  , 0xa2, 0xa6,  -1 , 0xb6, 0xae, -1  , 0xbe, -1  , -1  , -1  , -1  , -1  }, // LDX
		{ -1  , 0xa0, 0xa4, 0xb4, -1  , 0xac, 0xbc, -1  , -1  , -1  , -1  , -1  , -1  }, // LDY
		{ 0x4a, -1  , 0x46, 0x56, -1  , 0x4e, 0x5e, -1  , -1  , -1  , -1  , -1  , -1  }, // LSR
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0xea, -1  , -1  , -1  , -1  }, // NOP
		{ -1  , 0x09, 0x05, 0x15, -1  , 0x0d, 0x1d, 0x19, -1  , -1  , 0x01, 0x11, -1  }, // ORA
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x48, -1  , -1  , -1  , -1  }, // PHA
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x08, -1  , -1  , -1  , -1  }, // PHP
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x68, -1  , -1  , -1  , -1  }, // PLA
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x28, -1  , -1  , -1  , -1  }, // PLP
		{ 0x2a, -1  , 0x26, 0x36, -1  , 0x2e, 0x3e, -1  , -1  , -1  , -1  , -1  , -1  }, // ROL
		{ 0x6a, -1  , 0x66, 0x76, -1  , 0x6e, 0x7e, -1  , -1  , -1  , -1  , -1  , -1  }, // ROR
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x40, -1  , -1  , -1  , -1  }, // RTI
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x60, -1  , -1  , -1  , -1  }, // RTS
		{ -1  , 0xe9, 0xe5, 0xf5, -1  , 0xed, 0xfd, 0xf9, -1  , -1  , 0xe1, 0xf1, -1  }, // SBC
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x38, -1  , -1  , -1  , -1  }, // SEC
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0xf8, -1  , -1  , -1  , -1  }, // SED
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x78, -1  , -1  , -1  , -1  }, // SEI
		{ -1  , -1  , 0x85, 0x95, -1  , 0x8d, 0x9d, 0x99, -1  , -1  , 0x81, 0x91, -1  }, // STA
		{ -1  , -1  , 0x86,  -1 , 0x96, 0x8e, -1  , -1  , -1  , -1  , -1  , -1  , -1  }, // STX
		{ -1  , -1  , 0x84, 0x94, -1  , 0x8c, -1  , -1  , -1  , -1  , -1  , -1  , -1  }, // STY
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0xaa, -1  , -1  , -1  , -1  }, // TAX
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0xa8, -1  , -1  , -1  , -1  }, // TAY
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0xba, -1  , -1  , -1  , -1  }, // TSX
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x8a, -1  , -1  , -1  , -1  }, // TXA
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x9a, -1  , -1  , -1  , -1  }, // TXS
		{ -1  , -1  , -1  ,  -1 , -1  , -1  , -1  , -1  , 0x98, -1  , -1  , -1  , -1  }  // TYA
	//	  ACC   IMM   ZP    ZPX   ZPY   ABS   ABSX  ABSY  IMPL  REL   INDX  INDY  ABSI
	};
	
	/**
	*	Translates addrModeLen array to a string (Only used for debugging)
	*/
	public static final String addrToString [] = {
		"ACC",
		"IMM",
		"ZP",
		"ZPX",
		"ZPY",
		"ABS",
		"ABSX",
		"ABSY",
		"IMPL",
		"REL",
		"INDX",
		"INDY",
		"IND"
	};


	public static final int ABSOLUTE = 0;
	public static final int RELOC = 1;
	public static final int RELOC_LOBYTE = 2;
	public static final int RELOC_HIBYTE = 3;
	
	public static final int MODE_SCREEN = 0;
	public static final int MODE_RAW = 1;
	public static final int MODE_C64 = 2;
	public static final int MODE_SECRET = 99;
	
	public static final int PARSERESULT_UNDEF_STATEMENT = 0;
	public static final int PARSERESULT_OPCODE = 1;
	public static final int PARSERESULT_TEMPORARY_LABEL = 2;
	public static final int PARSERESULT_BYTE_SEQ = 3;
	public static final int PARSERESULT_WORD_SEQ = 4;
	public static final int PARSERESULT_LABEL = 5;
	public static final int PARSERESULT_ASSIGN = 6;
	public static final int PARSERESULT_NEW_ORIGIN = 7;
	public static final int PARSERESULT_EOF = 8;
	public static final int PARSERESULT_INCLUDE = 9;
	public static final int PARSERESULT_DATA = 10;
	public static final int PARSERESULT_PROC = 11;
	public static final int PARSERESULT_ENDPROC = 12;

}