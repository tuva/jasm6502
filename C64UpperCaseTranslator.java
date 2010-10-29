public class C64UpperCaseTranslator implements AsciiTranslator {
	/**
	*	Translates to c64 upper case.
	*	@warning not complete translation, but translates most of the normal set we use.
	*/
	public int translate (int c)
	{
		if (c >= 65 && c <= 90) c -= 64;
		else if (c >= 97 && c <= 122) c -= 96;
		else if (c == '@') c = 0;
		else if (c == '[') c = 27;
		else if (c == ']') c = 29;
		return c;
	}
}