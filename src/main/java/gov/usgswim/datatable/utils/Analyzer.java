package gov.usgswim.datatable.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import gov.usgswim.datatable.utils.DataFileDescriptor.DataType;
import static gov.usgswim.datatable.utils.DataFileDescriptor.DataType.*;

/**
 * Analyzes the structure of files to infer metadata to construct a DataTable.
 * This is available via a default static instance or a customizable constructor
 * instance
 *
 * @see DataFileDescriptor
 * @author ilinkuo
 *
 */
public class Analyzer {
	// =============
	// STATIC FIELDS
	// =============
	public static int DEFAULT_NUMBER_OF_LINES_TO_ANALYZE = 20;
	public static final int DEFAULT_FLOAT_PRECISION_DIGITS = 10; // 2^32 ~ 4.3 mill

	private final static Analyzer defaultAnalyzer = new Analyzer();

	// ==============
	// STATIC METHODS
	// ==============
	/**
	 * Analyze the metadata for a single file
	 *
	 * @param dataFile
	 * @return
	 * @throws IOException
	 */
	public static DataFileDescriptor analyzeFile(File dataFile) throws IOException {
		List<DataFileDescriptor> temp = defaultAnalyzer.analyze(dataFile);
		return (temp != null && !temp.isEmpty())? temp.get(0): null;

	}

	/**
	 * Analyze the metadata for all the files in the given directory
	 *
	 * @param dir
	 * @return
	 * @throws IOException
	 */
	public static List<DataFileDescriptor> analyzeDirectory(File dir) throws IOException {
		return defaultAnalyzer.analyze(dir);
	}

	/**
	 * Infers type of argument based on simple permissiveness hierarchy -
	 * Double, Float, Long, Int, Short, Byte, String
	 *
	 * @param typeCounts analysis of formats
	 * @return
	 */
	public static DataType[] inferTypes(List<Map<DataType, Integer>> typeCounts) {
		DataType[] result = new DataType[typeCounts.size()];
		for (int i=0; i<result.length; i++) {
			Map<DataType, Integer> counts = typeCounts.get(i);
			if (counts.get(StringType) != null) {
				result[i] = DataType.StringType;
			} else if (counts.get(DoubleType) != null) {
				result[i] = DoubleType;
			} else if (counts.get(FloatType) != null) {
				result[i] = FloatType;
			} else if (counts.get(LongType) != null) {
				result[i] = LongType;
			} else if (counts.get(IntType) != null) {
				result[i] = IntType;
			} else if (counts.get(ShortType) != null) {
				result[i] = ShortType;
			} else if (counts.get(ByteType) != null) {
				result[i] = ByteType;
			} else {
				// default is String
				result[i] = StringType;
			}
		}
		return result;
	}


	/**
	 * Analyzes a line of text to figure out whether it is using commas or tabs as delimiters
	 * @param line
	 * @return
	 */
	public static String analyzeDelimiter(String line) {
		if (line == null) return null;
		int commaCount = StringUtils.countMatches(line, ",");
		int tabCount = StringUtils.countMatches(line, "\t");
		int pipeCount = StringUtils.countMatches(line, "|");
		// By default, precedence order is comma, tab, pipe in case of ties
		if (pipeCount > tabCount && pipeCount > commaCount) return "|";
		return (tabCount > commaCount)? "\t": ",";
	}

	// ===============
	// INSTANCE FIELDS
	// ===============
	protected int numberOfLinesToAnalyze = DEFAULT_NUMBER_OF_LINES_TO_ANALYZE;
	protected int floatPrecision = DEFAULT_FLOAT_PRECISION_DIGITS;

	// ============
	// CONSTRUCTORS
	// ============
	public Analyzer() {
		this(DEFAULT_NUMBER_OF_LINES_TO_ANALYZE);
	}
	public Analyzer(int numberOfLinesToAnalyze) {
		this.numberOfLinesToAnalyze = numberOfLinesToAnalyze;
		assert(this.numberOfLinesToAnalyze > 0): "it doesn't make sense to analyze zero or a negative number of lines";
	}
	public Analyzer(int numberOfLinesToAnalyze, int precision) {
		this.numberOfLinesToAnalyze = numberOfLinesToAnalyze;
		this.floatPrecision = precision;
		assert(this.numberOfLinesToAnalyze > 0): "it doesn't make sense to analyze zero or a negative number of lines";
		assert(this.floatPrecision > 0): "floating point precision should be at least 1 digit";
	}

	// ================
	// INSTANCE METHODS
	// ================
	/**
	 * Analyze a single file or a directory
	 *
	 * @param dataFileOrDir
	 * @return a List of DataFileDescriptor
	 * @throws IOException
	 */
	public List<DataFileDescriptor> analyze(File dataFileOrDir) throws IOException {
		assert(dataFileOrDir.exists()) : "the directory or file " + dataFileOrDir.getPath() + " does not exist";

		List<DataFileDescriptor> result = new ArrayList<DataFileDescriptor>();

		if (dataFileOrDir.isFile()) {
			// analyze a file
			DataFileDescriptor fileResult = new DataFileDescriptor(dataFileOrDir.getName());
			List<Map<DataType, Integer>> typeCountsAnalysis = gatherStats(dataFileOrDir, fileResult);
			fileResult.columnCount = typeCountsAnalysis.size();

			if (fileResult.hasColumnHeaders() && typeCountsAnalysis.size() != fileResult.getHeaders().length) {
				System.err.println(dataFileOrDir.getName() + ": # of headers: " + fileResult.getHeaders().length + " != # of data columns: " + typeCountsAnalysis.size());
				System.err.println();
			}
			fileResult.dataTypes = inferTypes(typeCountsAnalysis);
			result.add(fileResult);
		} else if (dataFileOrDir.isDirectory()) {
			// analyze a directory by collecting the analysis for each of its files
			File dir = dataFileOrDir;
			for (File file: dir.listFiles()) {
				if (file.isFile()) result.addAll(analyze(file));
			}
		}
		return result;
	}


	/**
	 * Analyzes a number of lines from a dataFile, given a preliminary fileDescription
	 *
	 * @param dataFile
	 * @param fileDescription
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public List<Map<DataType, Integer>> gatherStats(File dataFile, DataFileDescriptor fileDescription)
			throws FileNotFoundException, IOException {
		List<Map<DataType, Integer>> typeCounts = new ArrayList<Map<DataType, Integer>>();
		fileDescription.delimiter = null;
		int lineCount = 0;
		String line;
		int width = 0;
		BufferedReader in = new BufferedReader(new FileReader(dataFile));
		try {
			// Gather Statistics: read the first ~20 lines of the file, if possible.
			while (( line = in.readLine()) != null && lineCount < numberOfLinesToAnalyze) {
				lineCount++;
				if (fileDescription.delimiter == null) {
					fileDescription.delimiter = analyzeDelimiter(line);
				}

				if (lineCount == 1) {
					// assume that the headers may be on the first or second line.
					fileDescription.setHeaders(inferHeaders(line, fileDescription.delimiter));
					if (fileDescription != null) continue;
				}
				String[] cells = line.split(fileDescription.delimiter);
				if (cells.length == 0) continue;
				width = Math.max(width, cells.length);
				// extend types if necessary
				for (int i=0; i< cells.length; i++) {
					String value = cells[i];
					DataType type = inferType(value);
					Map<DataType, Integer> counts = getCount(typeCounts, i);

					if (type != null) {
						Integer count = counts.get(type);
						count = (count == null)? 0: count;
						count++;
						counts.put(type, count);
					}
				}
			}
			// keep reading to count the number of lines
			while (( line = in.readLine()) != null) {
				if (line.length() > 0) lineCount++;
			}
		} finally {
			in.close();
		}

		fileDescription.lines = (fileDescription.hasColumnHeaders())? lineCount - 1: lineCount;
		return typeCounts;
	}

	/**
	 * Returns the tally/histogram of types for the given column
	 *
	 * @param typeCounts
	 * @param col
	 * @return
	 */
	private Map<DataType, Integer> getCount(List<Map<DataType, Integer>> typeCounts, int col) {
		if (col >= typeCounts.size()) {
			if (col > typeCounts.size() + numberOfLinesToAnalyze) {
				System.err.println("Something is seriously wrong with the looping index");
			}
			while (col>= typeCounts.size()) {
				typeCounts.add(new HashMap<DataType, Integer>());
			}
		}
		return typeCounts.get(col);
	}


	/**
	 * Best effort inference of the type of a cell, using the most restrictive type possible.
	 * @param value
	 * @return
	 */
	public DataType inferType(String value) {
		// return null if null or empty
		if (value == null || (value = value.trim()) == null) return null;

		try {
			long longValue = Long.parseLong(value);
			if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
				return DataType.LongType;
			} else if (longValue > Short.MAX_VALUE || longValue < Short.MIN_VALUE) {
				return DataType.IntType;
			} else if (longValue > Byte.MAX_VALUE || longValue < Byte.MIN_VALUE) {
				return DataType.ShortType;
			} else {
				return DataType.ByteType;
			}
		} catch (Exception e) {/* ignore, try next */}

		try {
			double doubleValue = Double.parseDouble(value);
			if (doubleValue > Float.MAX_VALUE || doubleValue < Float.MIN_VALUE
					|| value.length() > DEFAULT_FLOAT_PRECISION_DIGITS + 1) { // add 1 for decimal or -. Not completely accurate
				return DataType.DoubleType;
			}
			return DataType.FloatType;
		} catch (Exception e) {/* ignore */}

		return DataType.StringType;
	}

	/**
	 * Returns headers if it thinks the line is a header line, null otherwise.
	 * <br/><b><i>Assumption:</i></b> No pure numbers are allowed for headers.
	 *
	 * @param line
	 * @param delimiter
	 * @return
	 */
	private String[] inferHeaders(String line, String delimiter) {
		if (line == null || line.length() == 0) return null;
		String[] possibleHeaders = line.split(delimiter);
		for (String possibleHeader: possibleHeaders) {
			DataType guessedType = inferType(possibleHeader);
			if (guessedType.isFloat || guessedType.isInt) return null;
		}
		return possibleHeaders;
	}

}
