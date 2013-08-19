package gov.usgs.webservices.framework.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public abstract class StringUtils {
	private static final Pattern whiteSpaces = Pattern.compile("\\s+");
	
	public static String removeWhiteSpace(String aString) {
		return whiteSpaces.matcher(aString).replaceAll("");
	}
	
	public static String normalize(String aString) {
		aString = aString.toLowerCase();
		return whiteSpaces.matcher(aString).replaceAll("");
	}

	// =======================
	// PUBLIC UTILITY METHODS
	// =======================
	public static StringBuilder appendQuotedList(String[] codes,
			StringBuilder builder) {
		// if no codes, just return original
		if (codes == null || codes.length == 0) {
			return builder;
		}
		assert (codes.length > 0);
		//
		List<String> codeList = Arrays.asList(codes);
		StringBuilder result = DynamicSQLUtils.join(codeList, ",", "'", "'");
		builder.append(" (").append(result).append(") ");
		return builder;
	}

	public static StringBuilder appendUnQuotedList(String[] codes,
			StringBuilder builder) {
		// if no codes, just return original
		if (codes == null || codes.length == 0) {
			return builder;
		}
		assert (codes.length > 0);
		//
		List<String> codeList = Arrays.asList(codes);
		StringBuilder result = DynamicSQLUtils.join(codeList, ",", null, null);
		builder.append(" (").append(result).append(") ");
		return builder;
	}

	public static String escapeForCSV(String value) {
		if (value.indexOf('"') >= 0) {
			return value.replaceAll("\"", "\"\"");
		}
		return value;
	}
	
	public static String join(String[] strings) {
		StringBuilder result = new StringBuilder();
		if (strings != null) {
			for (String string: strings) {
				result.append(string);
			}
		}
		return result.toString();
	}
	
	public static String join(List<String> strings) {
		StringBuilder result = new StringBuilder();
		if (strings != null) {
			for (String string: strings) {
				result.append(string);
			}
		}
		return result.toString();
	}
}
