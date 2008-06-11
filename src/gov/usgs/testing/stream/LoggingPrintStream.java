package gov.usgs.testing.stream;

import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * This class prints to System.out, but also keeps a record of what has been
 * printed. Useful for testing
 * 
 * @author ilinkuo
 * 
 */
public class LoggingPrintStream extends PrintStream {
	private StringBuilder printRecord = new StringBuilder();
	private boolean isSuppressOutput;
	
	// ===========
	// CONSTRUCTOR
	// ===========
	public LoggingPrintStream() throws FileNotFoundException {
		super(System.out);
		// TODO Auto-generated constructor stub
	}
	// =====================
	// CONFIGURATION METHODS
	// =====================
	public void disable() {
		isSuppressOutput = true;
	}
	
	public void enable() {
		isSuppressOutput = false;
	}
	
	
	// ===============
	// SERVICE METHODS
	// ===============
	/**
	 * clears the print record
	 */
	public void clear() {
		printRecord = new StringBuilder();
	}
	
	/**
	 * @return a copy of the print record
	 */
	public String getRecord() {
		return printRecord.toString();
	}
	
	
	// ==================
	// OVERRIDDEN METHODS
	// ==================
	
	@Override
	public void print(boolean b) {
		if (!isSuppressOutput) {
			super.print(b);
		}
	}

	@Override
	public void print(char c) {
		printRecord.append(c);
		if (!isSuppressOutput) {
			super.print(c);
		}
	}

	@Override
	public void print(char[] s) {
		printRecord.append(s);
		if (!isSuppressOutput) {
			super.print(s);
		}
	}

	@Override
	public void print(double d) {
		printRecord.append(d);
		if (!isSuppressOutput) {
			super.print(d);
		}
	}

	@Override
	public void print(float f) {
		printRecord.append(f);
		if (!isSuppressOutput) {
			super.print(f);
		}
	}

	@Override
	public void print(int i) {
		printRecord.append(i);
		if (!isSuppressOutput) {
			super.print(i);
		}
	}

	@Override
	public void print(long l) {
		printRecord.append(l);
		if (!isSuppressOutput) {
			super.print(l);
		}
	}

	@Override
	public void print(Object obj) {
		printRecord.append(obj);
		if (!isSuppressOutput) {
			super.print(obj);
		}
	}

	@Override
	public void print(String s) {
		printRecord.append(s);
		if (!isSuppressOutput) {
			super.print(s);
		}
	}

	@Override
	public void println() {
		printRecord.append('\n');
		if (!isSuppressOutput) {
			super.println();
		}
	}

	@Override
	public void println(boolean x) {
		printRecord.append(x).append('\n');
		if (!isSuppressOutput) {
			super.println(x);
		}
	}

	@Override
	public void println(char x) {
		printRecord.append(x).append('\n');
		if (!isSuppressOutput) {
			super.println(x);
		}
	}

	@Override
	public void println(char[] x) {
		printRecord.append(x).append('\n');
		if (!isSuppressOutput) {
			super.println(x);
		}
	}

	@Override
	public void println(double x) {
		printRecord.append(x).append('\n');
		if (!isSuppressOutput) {
			super.println(x);
		}
	}

	@Override
	public void println(float x) {
		printRecord.append(x).append('\n');
		if (!isSuppressOutput) {
			super.println(x);
		}
	}

	@Override
	public void println(int x) {
		printRecord.append(x).append('\n');
		if (!isSuppressOutput) {
			super.println(x);
		}
	}

	@Override
	public void println(long x) {
		printRecord.append(x).append('\n');
		if (!isSuppressOutput) {
			super.println(x);
		}
	}

	@Override
	public void println(Object x) {
		printRecord.append(x).append('\n');
		if (!isSuppressOutput) {
			super.println(x);
		}
	}

	@Override
	public void println(String x) {
		printRecord.append(x).append('\n');
		if (!isSuppressOutput) {
			super.println(x);
		}
	}

	@Override
	public PrintStream append(char c) {
		printRecord.append(c);
		if (!isSuppressOutput) {
			super.append(c);
		}
		return this;
	}

	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		throw new UnsupportedOperationException("Must override and check this before calling");
//		super.append(csq, start, end);
//		return super.append(csq, start, end);
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		for (int i = 0; i < len; i++) {
			printRecord.append((char)buf[i + off]);
		}
		if (!isSuppressOutput) {
			super.write(buf, off, len);
		}
	}

	@Override
	public void write(int b) {
		printRecord.append(b);
		if (!isSuppressOutput) {
			super.write(b);
		}
	}
	

}
