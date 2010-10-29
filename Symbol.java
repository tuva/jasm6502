/**
*	A symbol part of the vocabulary of the programming language that the assembler implements.
*
*	@author David Schager 2006
*/
public class Symbol {
	private String mName;
	private int mType;
	private int mValue;
	private int mExtra = 0;

	/**
	*	Constructor
	*	@param name Name of symbol as key in a symbol table, or set to empty string ""
	*	@param type A defined integer identifying the symbol, always set for all symbols.
	*	@param value A value of the symbol, or set to NULL
	*	@see SymbolConstant6502
	*/
	public Symbol (String name, int type, int value) {
		mName = name;
		mType = type;
		mValue = value;
	}

	/**
	*	Returns name of symbol, which is also the key in a symbol table.
	*	Not all symbols will have this field set, but those from symbol tables will.
	*/
	public String getName () {
		return mName;
	}

	/**
	*	Returns type of symbol, will be a defined value from SymbolConstants6502.
	*	@see SymbolConstants6502.
	*/
	public int getType () {
		return mType;
	}
	
	/**
	*	Returns value of symbol.
	*	For Symbols of type CONSTANT, will be a integer expressing the constants value.
	*	For Symbols of type OPCODE, will be a bit mask expressing the valid addressing modes as defined in SymbolConstants6502.
	* 	For Symbols of type OPERATOR, will be a char value determining the operator type: +/-*#$ etc
	*	For some Symbols, for instance LINEFEED, EOF, and others, this field is set to NULL (0xFFFFFFFF)
	*	@see SymbolConstants6502
	*	@see Assembler6502
	*/
	public int getValue () 
	{
		return mValue;
	}
	
	
	/**
	*	Sets the value of symbol
	*	@param val Value to set
	*/
	public void setValue (int val)
	{
		mValue = val;
	}
	
	/**
	*	Returns extra value, which may have different meaning in context of different Symbol types.
	*	(Was added as an afterthought)
	*	@return integer value of extra field.
	*/
	public int getExtra ()
	{
		return mExtra;
	}
	
	/**
	*	Sets extra value.
	*/
	public void setExtra (int val)
	{
		mExtra = val;
	}
	
	public String toString ()
	{
		return  ("name: " + mName + "\ntype: 0x" + Integer.toHexString (mType) + "\nvalue: " + 
			Integer.toHexString (mType));
	}
}
