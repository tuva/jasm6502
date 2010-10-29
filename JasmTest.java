import junit.framework.*;
import java.io.*;
import java.lang.Runtime;
import java.lang.Process;

/**
*	@test Runs a test case where a list of assembler source files are assembled first with xa assembler, then with
*	jasm6502 assembler and compare the output files, if they are equal, the test are successful.
*	To run this test from shell, type from directory containing this source: 
*	java junit.textui.TestRunner JasmTest
*	@author David Schager 2006
*/
public class JasmTest extends TestCase {
	public JasmTest (String testName)
	{
		super (testName);
	}
	
	/**
	*	Invokes The xa assembler and the jasm6502 assembler, on a source file residing in 
	*	folder test, and outputs a plain binary	of the file in the folder test/bin.
	*	@param asmFile filename of source file, without the .asm extension.
	*/
	private boolean invokeAsm (String asmFile)
	{
		boolean success = true;
		try {
			// delete files if exists
			File file1 = new File ("test/out/" + asmFile + "_xa.bin");
			File file2 = new File ("test/out/" + asmFile + ".bin");
			file1.delete ();
			file2.delete ();
			// invoke xa assembler and jasm6502 on source file
			String execXa = "xa -o test/out/" + asmFile + "_xa.bin test/" + asmFile + ".asm";
			String execJasm = "java jasm6502 -o test/out/" + asmFile + ".bin test/" + asmFile + ".asm";
			Runtime runtime = Runtime.getRuntime ();
			Process xa = runtime.exec (execXa);
			Process p_jasm6502 = runtime.exec (execJasm);
		}
		catch (IOException e) {
			System.out.println (e.getMessage ());
			e.printStackTrace ();
			success = false;
		}
		return success;
	}
	
	/**
	*	Compares two assembler plain binaries byte by byte.
	*	@param asmFile the filename of binary excluding the .bin extension.
	*	@return true if both binaries are equal.
	*/
	private boolean compareFiles (String asmFile)
	{
		boolean success = true;
		try {
			File file1 = new File ("test/out/" + asmFile + "_xa.bin");
			File file2 = new File ("test/out/" + asmFile + ".bin");
			int tries = 4;
			while (!file1.canRead ()) {
				System.err.print (".");
				try {
					Thread.sleep (1000);
				}
				catch (InterruptedException e) {
					System.err.println (e.getMessage ());
				}
				if (0 == --tries) {
					success = false;
					System.err.println ("Cannot open file" + "test/out/" + asmFile + "_xa.bin");
					System.exit (1);
				}
			}
			tries = 4;
			while (!file2.canRead ()) {
				System.err.print (".");
				try {
					Thread.sleep (1000);
				}
				catch (InterruptedException e) {
					System.err.println (e.getMessage ());
				}
				if (0 == --tries) {
					success = false;
					System.err.println ("Cannot open file" + "test/out/" + asmFile + ".bin");
					System.exit (1);
				}
			}
			tries = 4;
			// try to match file lengths at least 4 times, and pause 1 second between each try, because
			// the files might not be completely flushed to disk yet.
			while (file1.length () != file2.length ()) {
				System.err.print (".");
				try {
					Thread.sleep (1000);
				}
				catch (InterruptedException e) {
					System.err.println (e.getMessage ());
				}
				if (0 == --tries) {
					success = false;
					System.err.println ("");
					break;
				}
			}
			
			String fileName1 = "test/out/" + asmFile + "_xa.bin";
			String fileName2 = "test/out/" + asmFile + ".bin";
			
			if (!success) {
				System.err.println ("Warning: " + fileName1 + " and " + fileName2 + " differ in length.");
				System.err.println (fileName1 + " len is " + file1.length ());
				System.err.println (fileName2 + " len is " + file2.length ());
			}

			RandomAccessFile f1 = new RandomAccessFile (file1, "r");
			RandomAccessFile f2 = new RandomAccessFile (file2, "r");
			long l1 = f1.length ();
			long l2 = f2.length ();
			long min = (l1 < l2) ? l1 : l2;
			
			for (int i = 0; i < min; ++i) {
				if (f1.readByte () != f2.readByte ()) {
					System.err.println ("diff offset: " + i);
					success = false;
				}
			}
			if (!success) {
				System.err.println ("In files " + fileName1 + " & " + fileName2);
			}
		}
		catch (IOException e) {
			System.err.println (e.getMessage ());
			e.printStackTrace ();
			success = false;
		}

		return success;
	}
	
	/**
	*	compile and compare allops.asm (tests all possible opcodes in all possible addressing modes)
	*/
	public void testCompileAllops ()
	{
		assertTrue (invokeAsm ("allops"));
		assertTrue (compareFiles ("allops"));
		System.out.println ("test/allops.asm is ok");
	}
	
	/**
	*	compile and compare testlabel.asm (tests labels are correct)
	*/
	public void testCompileTestlabel ()
	{
		assertTrue (invokeAsm ("testlabel"));
		assertTrue (compareFiles ("testlabel"));
		System.out.println ("test/testlabel.asm is ok");
	}
	
	/**
	*	compile and compare testprogram.asm
	*/
	public void testCompileTestProgram ()
	{
		assertTrue (invokeAsm ("testprogram"));
		assertTrue (compareFiles ("testprogram"));
		System.out.println ("test/testlabel.asm is ok");
	}
	
	/**
	*	compile and compare lohibyte.asm (tests lo and hi byte operator)
	*/
	public void testCompileLoHiByte ()
	{
		assertTrue (invokeAsm ("lohibyte"));
		assertTrue (compareFiles ("lohibyte"));
		System.out.println ("test/lohibyte.asm is ok");
	}

	/**
	*	Compile and test  testbyte.asm (tests byte strings)
	*/
	public void testCompileTestByte ()
	{
		assertTrue (invokeAsm ("testbyte"));
		assertTrue (compareFiles ("testbyte"));
		System.out.println ("test/testbyte.asm is ok");
	}
	
	/*
	/**
	*	@bug removed this test, because xa assembler cannot handle multiple origins when assembling to plain output.
	*	Thus comparison fails even though it should not.
	*/
	/*
	public void testCompileOrgTest ()
	{
		assertTrue (invokeAsm ("orgtest"));
		assertTrue (compareFiles ("orgtest"));
		System.out.println ("test/orgtest.asm is ok");
	}
	*/
	
	/**
	*	Compile and test  assign.asm (tests assign)
	*/
	public void testCompileAssign ()
	{
		assertTrue (invokeAsm ("assign"));
		assertTrue (compareFiles ("assign"));
		System.out.println ("test/assign.asm is ok");
	}
	
	/**
	*	Compile and compare curaddress.asm (tests current address operator)
	*/	
	public void testCompileCurAddress ()
	{
		assertTrue (invokeAsm ("curaddress"));
		assertTrue (compareFiles ("curaddress"));
		System.out.println ("test/curaddress.asm is ok");
	}
	
	/**
	*	Compile and compare compatible.asm (various tests)
	*/
	public void testCompileCompatible ()
	{
		assertTrue (invokeAsm ("compatible"));
		assertTrue (compareFiles ("compatible"));
		System.out.println ("test/compatible.asm is ok");
	}
	
	/**
	*	Compile and test helloworld.asm (various tests)
	*/
	public void testCompileHelloWorld ()
	{
		assertTrue (invokeAsm ("helloworld"));
		assertTrue (compareFiles ("helloworld"));
		System.out.println ("test/helloworld.asm is ok");
	}
	
	/**
	*	Compile and test eval.asm (expression tests)
	*/
	public void testCompileEval ()
	{
		assertTrue (invokeAsm ("eval"));
		assertTrue (compareFiles ("eval"));
		System.out.println ("test/eval.asm is ok");
	}
	
	/**
	*	Compile and test eval.asm (expression tests)
	*/
	public void testCompileEval2 ()
	{
		assertTrue (invokeAsm ("eval2"));
		assertTrue (compareFiles ("eval2"));
		System.out.println ("test/eval2.asm is ok");
	}
	
	/**
	*	Compile and test maclf.asm (mac linefeeds \r).
	*	@bug: Removed test, because XA-assembler cannot handle mac linefeeds
	*	However manual test, proved mac linefeeds were parsed okay by jasm6502.
	*
	public void testCompileMacLF ()
	{
		assertTrue (invokeAsm ("maclf"));
		assertTrue (compareFiles ("maclf"));
		System.out.println ("test/maclf.asm is ok");
	}
	*/
	
	/**
	*	Compile and test winlf.asm (win linefeeds \r\n)
	*/
	public void testCompileWinLF ()
	{
		assertTrue (invokeAsm ("winlf"));
		assertTrue (compareFiles ("winlf"));
		System.out.println ("test/winlf.asm is ok");
	}
	/*
	public void testCompile ()
	{
		assertTrue (invokeAsm (""));
		assertTrue (compareFiles (""));
		System.out.println ("test/.asm is ok");
	}
	*/
	
}

