import java.util.Vector;

/**
*	A variable set of data to write to disk, as a result of a parsing by Parser6502.
*
*	@author David Schager
*/
public class MachineCode6502 {
	private Symbol mOpcode;
	private int mAddrMode;
	private int mOperand;
	private boolean mFixed;
	/**
	*	Constructor
	*	@param opcode the opcode as defined in SymbolConstants6502
	*	@param addrMode the addressing mode of opcode as defined in SymbolConstants6502
	*	@param operand the 0-2 bytes following the opcode packed into an integer
	*	@param fixed true if operand is fixed, false if operand needs patching when relocating source to new address.
	*/
	public MachineCode6502 (Symbol opcode, int addrMode, int operand, boolean fixed)
	{
		mOpcode = opcode;
		mAddrMode = addrMode;
		mOperand = operand;
		mFixed = fixed;
	}
	
	/**
	*	Returns the opcode as Symbol object, 8 bits.
	*	A bitmask of valid addressing modes for this opcode can be gotten by calling the object's getValue method.
	*/
	public Symbol getOpcode ()
	{
		return mOpcode;
	}
	/**
	*	Returns operands (0, 1 or 2 bytes) that follows opcode, as packed into an integer.
	*	The low byte is bits 0-7, the high bytes bits 8-15.
	*	They must be written to disk in little endian for correct 6502 format.
	*	To know if the operands should be written, the assembler must constult the addrLen table in 
	*	SymbolConstant6502.
	*/
	public int getOperand ()
	{
		return mOperand;
	}
	/**
	*	Checks if operand is fixed or relocatable.
	*	If returning false, the Operand (returned by getOperand) points to an memory address
	*	contained by a label in the program, and must be patched to retain its meaning when relocating code
	*	when linking to another base address.
	*	If returning true, the operand points to a fixed memory address and does not need patching
	*	when linking.
	*	@note Operands to Branch instructions are always fixed and can be relocated without patching,
	*		because they are not an address, but a signed byte indicating the number of steps to jump
	*		forward or backward in code.)
	*/
	public boolean isFixed ()
	{
		return mFixed;
	}
	/**
	*	Gets the addressing mode used with opcode
	*/
	public int getAddrMode ()
	{
		return mAddrMode;
	}
}


