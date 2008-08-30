package gov.usgswim.sparrow.loader;

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
import gov.usgswim.sparrow.loader.DataFileDescriptor.DataType;
import static gov.usgswim.sparrow.loader.DataFileDescriptor.DataType.*;

public class Analyzer {
	public static int NUMBER_OF_LINES_TO_ANALYZE = 20;
	
	public static List<DataFileDescriptor> analyzeDirectory(File dir) throws IOException {
		assert(dir.exists());
		assert(dir.isDirectory());
		List<DataFileDescriptor> result = new ArrayList<DataFileDescriptor>();
		for (File file: dir.listFiles()) {
			if (file.isFile()) result.add(Analyzer.analyzeFile(file));
		}
		return result;
	}

	public static DataFileDescriptor analyzeFile(File dataFile) throws IOException {
		assert(dataFile.isFile());
		DataFileDescriptor result = new DataFileDescriptor(dataFile.getName());
		List<Map<DataType, Integer>> typeCountsAnalysis = gatherStats(dataFile, result);
		
		if (result.hasColumnHeaders() && typeCountsAnalysis.size() != result.getHeaders().length) {
			System.err.println(dataFile.getName() + ": # of headers: " + result.getHeaders().length + " != # of data columns: " + typeCountsAnalysis.size());
			System.err.println();
		}
		result.dataTypes = inferTypes(typeCountsAnalysis);

		return result;
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

	public static List<Map<DataType, Integer>> gatherStats(File dataFile, DataFileDescriptor result)
			throws FileNotFoundException, IOException {
		List<Map<DataType, Integer>> typeCounts = new ArrayList<Map<DataType, Integer>>();
		result.delimiter = null;
		int lineCount = 0;
		String line;
		String[] headers = null;
		int width = 0;
		BufferedReader in = new BufferedReader(new FileReader(dataFile));
		try {
			// Gather Statistics: read the first 20 lines of the file, if possible.
			while (( line = in.readLine()) != null && lineCount < NUMBER_OF_LINES_TO_ANALYZE) {
				lineCount++;
				if (result.delimiter == null) {
					result.delimiter = analyzeDelimiter(line);
				}

				if (lineCount == 1) {
					// assume that the headers may be on the first or second line.
					result.setHeaders(inferHeaders(line, result.delimiter));
					if (result != null) continue;
				}
				String[] cells = line.split(result.delimiter);
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

		result.lines = (result.hasColumnHeaders())? lineCount - 1: lineCount;
		return typeCounts;
	}

	private static Map<DataType, Integer> getCount(List<Map<DataType, Integer>> typeCounts, int i) {
		if (i >= typeCounts.size()) {
			if (i > typeCounts.size() + 20) {
				System.err.println("Something is seriously wrong with the looping index");
			}
			while (i>= typeCounts.size()) {
				typeCounts.add(new HashMap<DataType, Integer>());
			}
		}
		return typeCounts.get(i);
	}

	public static final int FLOAT_PRECISION_DIGITS = 10; // 2^32 ~ 4.3 mill
	public static DataType inferType(String value) {
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
		} catch (Exception e) {} // ignore, try next
		
		try {
			double doubleValue = Double.parseDouble(value);
			if (doubleValue > Float.MAX_VALUE || doubleValue < Float.MIN_VALUE 
					|| value.length() > FLOAT_PRECISION_DIGITS + 1) { // add 1 for decimal or -. Not completely accurate
				return DataType.DoubleType;
			}
			return DataType.FloatType;
		} catch (Exception e) {} // ignore
		
		return DataType.StringType;
	}

	/**
	 * Returns headers if it thinks the line is a header line, null otherwise.
	 * No pure numbers are allowed for headers.
	 * 
	 * @param line
	 * @param delimiter
	 * @return
	 */
	private static String[] inferHeaders(String line, String delimiter) {
		if (line == null || line.length() == 0) return null;
		String[] possibleHeaders = line.split(delimiter);
		for (String possibleHeader: possibleHeaders) {
			DataType guessedType = inferType(possibleHeader);
			if (guessedType.isFloat || guessedType.isInt) return null;
		}
		return possibleHeaders;
	}

	public static String analyzeDelimiter(String line) {
		if (line == null) return null;
		int commaCount = StringUtils.countMatches(line, ",");
		int tabCount = StringUtils.countMatches(line, "\t");
		return (tabCount > commaCount)? "\t": ","; // assume comma if tied or by default
	}
	
	
}
