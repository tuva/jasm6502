import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Iterator;



/**
*	Symbol table with support for nested namespaces.
*	mNamespace is a hashmap holding Namespace objects, forming a tree of namespaces.
*	The root node has the key "global" and is the global namespace.
*	Each node in tree from root is a namespace belonging to a procedure, and the key
*	is the name of the procedure. Thus nested procedure can exist with the same names
*	and same variable names in scope.
*	mStack is a stack of Namespace objects, the stacktop is current namespace.
*	Each time the assembler goes into a new procedure that procedure's Namespace object it fetched from mNamespace
*	and pushed on mStack, or if not existing - created and inserted in mNamespace and pushed on mStack.
*
*	It is used by a Lexer and Assembler
*	@todo Check the bug when a identifier constant outside scope is undef and label with same name inside scope 
*	@author David Schager 2006
*/
public class SymbolTable implements SymbolConstant6502 {
	private HashMap mNamespace;			// hashmap of hashmaps (each hashmap a namespace)
	private Stack mStack;
	private int mLexLevel;
	private final static int LEVEL_LIMIT = 16;
	
	/**
	*	Constructor.
	*/
	public SymbolTable () {
		mLexLevel = 0;
		mStack = new Stack ();
		// create hashmap for global namespace
		mNamespace = new HashMap ();
		// create global namespace
		Namespace global = new Namespace ();
		// set current namespace reference to global
		mStack.push (global);
		// install namespace
		mNamespace.put ("global", global);
	}
	
	/**
	*	Check if symboltable contains Symbol, at current namespace and downwards to global namespace.
	*	@param name Name of symbol to check
	*/
	public boolean contains (String name) {
		boolean success = false;
		Stack temp = (Stack) mStack.clone ();
		while (!temp.empty ()) {
			Namespace namespace = (Namespace) temp.pop ();
			HashMap tab = namespace.getSymbolTable (); 
			if (tab.containsKey (name)) {
				success = true;
				break;
			}
		}
		return success;
	}

	
	/**
	*	Returns a symbol if it exists from current namespace and downwards to global namespace, else null.
	*	@param name name of Symbol
	*/
	public Symbol getSymbol (String name) {
		Symbol s = null;
		Stack temp = (Stack) mStack.clone ();
		while (!temp.empty ()) {
			Namespace namespace = (Namespace) temp.pop ();
			HashMap tab = namespace.getSymbolTable (); 
			if (null != (s = (Symbol) tab.get (name))) break;
		}
		return (s == null) ? null : new Symbol (s.getName (), s.getType (), s.getValue ());
	}
	
	/**
	*	Returns Symbol if exist in current namespace only
	*	@param name name of symbol
	*	@return Symbol or null on failure.
	*/
	public Symbol getSymbolCurrentLevel (String name)
	{
		Namespace namespace = (Namespace) mStack.peek ();
		HashMap tab = namespace.getSymbolTable (); 
		Symbol s = (Symbol) tab.get (name);
		return (s == null) ? null : new Symbol (s.getName (), s.getType (), s.getValue ());
	}
	
	/**
	*	Installs symbol in current namespace
	*/
	public boolean install (Symbol symbol) 
	{
		Namespace namespace = (Namespace) mStack.peek ();
		HashMap tab = namespace.getSymbolTable ();
		tab.put (symbol.getName (), symbol);
		return true;
	}
	
	/**
	*	Removes a symbol from current namespace
	*/
	public void remove (String key)
	{
		Namespace namespace = (Namespace) mStack.peek ();
		HashMap tab = namespace.getSymbolTable ();
		tab.remove (key);
	}
	
	/**
	*	Traverse namespace tree by recursion and remove all symbols with name key.
	*	@param namespace node to traverse
	*	@param key name of symbol to remove
	*/
	private void traverseAndRemove (Namespace namespace, String key)
	{
		HashMap childNamespaces = namespace.getNamespace ();
		HashMap tab = namespace.getSymbolTable ();
		tab.remove (key);
		for (Iterator it = childNamespaces.entrySet ().iterator (); it.hasNext (); ) {
			Map.Entry entry = (Map.Entry) it.next ();
			Namespace nextChild = (Namespace) entry.getValue ();
			traverseAndRemove (nextChild, key);
		}
	}
	
	/**
	*	Removes a symbol from all namespaces.
	*	@param key name of symbol to remove
	*/
	public void removeFromAllNamespaces (String key)
	{
   		for (Iterator it = mNamespace.entrySet ().iterator (); it.hasNext (); ) {
			Map.Entry entry = (Map.Entry) it.next ();
			//String key = (String) entry.getKey ();
			Namespace namespace = (Namespace) entry.getValue ();
			traverseAndRemove (namespace, key);
		}
	}
	
	/**
	*	Sets hashmap tree
	*/
	protected void setVars (HashMap namespaceTree, Stack stack, int lexLevel)
	{
		mNamespace = namespaceTree;
		mStack = stack;
		mLexLevel = lexLevel;
	}
	
	/**
	*	copy symboltable
	*/
	public SymbolTable copy ()
	{
		SymbolTable stab = new SymbolTable ();
		//for (int i = 0; i < LEVEL_LIMIT; ++i) stab.setHashMaps ((HashMap[]) mSymbols.clone ());
			//stab.setHashMap ((HashMap) mSymbols[i].clone (), i);
		stab.setVars ((HashMap) mNamespace.clone (), (Stack) mStack.clone (), mLexLevel);
		return stab;
	}
	
	/**
	*	Steps into a namespace, creating it if not existing.
	*	@param name name of new namespace (procedure name)
	*/
	public boolean stepIntoNamespace (String name)
	{
		Namespace namespace = (Namespace) mStack.peek ();
		// check if contains namespace with param name
		HashMap namespaceMap = (HashMap) namespace.getNamespace ();
		if (namespaceMap.containsKey (name)) {
			Namespace stepInto = (Namespace) namespaceMap.get (name);
			mStack.push (stepInto);
		}
		else {
			Namespace newNamespace = new Namespace ();
			namespaceMap.put (name, newNamespace);
			mStack.push (newNamespace);
		}
		++mLexLevel;
		return true;
	}
	
	/**
	*	Search for symbol of a specific type thats not set to value NULL, from current namespace down to global namespace.
	*	@param name name of symbol
	*	@param type type of symbol
	*	@return The found symbol or null on failure.
	*/
	public Symbol probeToRootNotNULL (String name, int type)
	{
		Symbol s = null;
		Stack temp = (Stack) mStack.clone ();
		while (!temp.empty ()) {
			Namespace namespace = (Namespace) temp.pop ();
			HashMap tab = (HashMap) namespace.getSymbolTable ();
			if (null != (s = (Symbol) tab.get (name))) {
				if (s.getType () == type && s.getValue () != NULL) break;
			}
		}

		return (s == null) ? null : new Symbol (s.getName (), s.getType (), s.getValue ());
	}
	
	/**
	*	Steps out of current namespace
	*/
	public boolean stepOut ()
	{
		boolean success = false;
		if (mLexLevel >= 1) {
			--mLexLevel;
			mStack.pop ();
			success = true;
		}
		return success;
	}
	
	/**
	*	Returns current lex level. (0-15)
	*/
	public int getLexLevel ()
	{
		return mLexLevel;
	}
	
	/**
	*	Returns the lexical level limit.
	*/
	public int getLevelLimit ()
	{
		return LEVEL_LIMIT;
	}
}
