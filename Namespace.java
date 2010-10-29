import java.util.HashMap;
import java.util.Map;

/**
*	Namespace object.
*	Holds one hashmap for Namespace objects.
*	and another hashmap for Symbol objects.
*
*	This class is used internally by SymbolTable class.
*/
public class Namespace {
	
	/**
	*	Hashmap that holds Namespace objects.
	*/
	private HashMap mNamespace;
	
	/**
	*	Hashmap that holds Symbol Objects.
	*/
	private HashMap mSymbolTable;

	public Namespace ()
	{
		mNamespace = new HashMap ();
		mSymbolTable = new HashMap ();
	}

	/**
	*	Returns the hashmap with child namespaces (contains namespaces within this namespace)
	*/
	public HashMap getNamespace ()
	{
		return mNamespace;
	}
	
	/**
	*	Returns the hashmap with Symbols for this namespace.
	*/
	public HashMap getSymbolTable ()
	{
		return mSymbolTable;
	}
}
