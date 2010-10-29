import java.io.IOException;

/**
*	jasm6502
*	An assembler for the 6502 family of CPU's.
*	Given a source file it can assemble it to a binary format and outputs to disk.
*	This program was made as part of my participation in the course 
*	Application Development at Gotland University.
*	@author David Schager 2006
*/
public class jasm6502 implements SymbolConstant6502 {
	static public final String usageMsg = 
		"jasm6502, Portable 6502 cross assembler v0.6.1 (c) David Schager 2006\n" +
		"usage:\n" +
		"\tjava jasm6502 <options> <inputfile>\n\n" +
		"Options:\n" + 
		"-f <format>\n\t<format> can be:\n" +
		"\tscreen\t- Output assembly to screen\n" +
		"\traw\t- Output to raw binary file (default)\n" +
		"\tc64\t- Output to Commodore 64 .prg file\n" +
		"-o <output file name>\n" +
		"-t <translate ascii> (Translates to commodore 64 screen codes)\n" +
		"\t<translate ascii> can be:\n" +
		"\tupper\t- Translate to upper case\n" +
		"\tlower\t- Translate to lower case\n" +
		"\tnone\t- Supress translation (Default)\n";
		
	/**
	*	Returns the value of an option declared on the command line.
	*	@param args The commandline arguments
	*	@param option The name of the option
	*	@return String value of option
	*/
	private String getOptionValue (String[] args, String option)
	{
		String value = "";
		for (int i = 0; i < args.length; ++i) {
			if (args[i].compareTo (option) == 0) {
				if (i >= args.length) {
					System.err.println ("Error: Option " + args[i] + " needs a value.\n\n" + usageMsg);
					System.exit (1);
				}
				else value = args[i + 1];
			}
		}
		return value;
	}
	/**
	*	Returns true if the requested option is present on commandline.
	*	@param args The commandline arguments
	*	@param option Name of option
	*	@return true or false.
	*/
	private boolean hasOption (String[] args, String option)
	{
		boolean success = false;
		for (int i = 0; i < args.length; ++i) {
			if (args[i].compareTo (option) == 0) {
				success = true;
				break;
			}
		}
		return success;
	}
	
	/** 
	*	Scans commandline for options and invokes the assembler.
	*
	*	@param args Commandline arguments
	*	Uses exit system call with exit code 1 for errors, 0 for successful assembly.
	*/
	public void run (String[] args)
	{
		if (args.length < 1) {
			System.err.println (usageMsg);
			System.exit (1);
		}
		
		// default extension for output
		String extension = ".bin";

		// Get the output format if any, else default to raw output (plain binary without header)
		int mode = MODE_RAW;
		if (hasOption (args, "-f")) {
			String format = getOptionValue (args, "-f");
			if (format.compareTo ("screen") == 0) {
				// output to screen instead of disk
				mode = MODE_SCREEN;
			}
			else if (format.compareTo ("raw") == 0) {
				// output to raw
				mode = MODE_RAW;
			}
			else if (format.compareTo ("c64") == 0) {
				// output to commodore 64 prg file
				mode = MODE_C64;
				extension = ".prg";
			}
			/*
			else if (format.compareTo ("obj") == 0) {
				mode = MODE_OBJECT;
				extension = ".o";
			}
			*/
			else {
				System.err.println ("Error: Unknown output format.\n\n" + usageMsg);
				System.exit (1);
			}
		}
		
		int translateAscii = 0;
		if (hasOption (args, "-t")) {
			String asciiMode = getOptionValue (args, "-t");
			if (asciiMode.compareTo ("upper") == 0) {
				translateAscii = 1;
			}
			else if (asciiMode.compareTo ("lower") == 0) {
				translateAscii = 2;
			}
			else if (asciiMode.compareTo ("none") == 0) {
				translateAscii = 0;
			}
			else {
				System.err.println ("Error: Unknown ascii translation format.\n\n" + usageMsg);
				System.exit (1);
			}
		}
		
		// Get the input file name, which is the last argument on command line
		String inFileName = args[args.length - 1];
		
		// Construct the output file name, which is option -o, or create a default
		String outFileName = "";
		if (mode != 0) {
			if (hasOption (args, "-o")) {
				outFileName = getOptionValue (args, "-o"); 
			}
			else {
				try {
					// no output filename specified, create default by appending .bin to input file name
					outFileName = inFileName;
					int n = outFileName.lastIndexOf (".");
					if (n >= 0) outFileName = outFileName.substring (0, n);
					outFileName += extension;
				}
				catch (Exception e) {
					System.err.println ("Error: Malformed command line.\n\n" + usageMsg);
					System.exit (1);
				}
			}
		}
			
		if (inFileName.compareTo (outFileName) == 0) {
			System.err.println ("Error: The input and output file names cannot be the same.\n\n" + usageMsg);
			System.exit (1);
		}
		
		if (hasOption (args, "-eee")) {
			// secret test evaluator mode!
			mode = MODE_SECRET;
		}
		
		/*
		System.out.println ("Out file name: " + outFileName);
		System.out.println ("In file name: " + inFileName);
		System.out.println ("Format: " + mode);
		System.exit (0);
		*/
		// Invoke the 6502 assembler
		try {
			AbstractAssembler jasm = new Assembler6502 ();
			jasm.setMode (mode);
			jasm.setAsciiTranslation (translateAscii);
			System.exit (jasm.assemble (inFileName, outFileName));
		}
		catch (IOException e) {
			System.err.println (e.getMessage ());
		}
		System.exit (1);
	}
	
	/**
	*	Program entry point, creates jasm6502 object and calls run method.
	*/
	public static void main (String[] args) 
	{
		jasm6502 j = new jasm6502 ();
		j.run (args);
	}
}
