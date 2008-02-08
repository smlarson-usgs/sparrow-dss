package gov.usgs.webservices.framework.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class XMLUtils {
	public static String nonQnameSeparators = "[()/@]";
	public static String nonQnameChars = "[^-_.:0-9A-Za-z]";
	public static String allNonQnameChars = nonQnameSeparators + nonQnameChars;
	public static String nonQnameReplacements = "[()/@]" + "[^-_.:0-9A-Za-z]";
	
	public static Pattern replaceNonQnameSeparators = Pattern.compile(nonQnameSeparators);
	public static Pattern removeNonQname = Pattern.compile(nonQnameChars);
	public static Pattern removeLeftAngleBracket = Pattern.compile("<");
	public static Pattern removeRightAngleBracket = Pattern.compile(">");
	public static Pattern removeAmpersand = Pattern.compile("&");
	/**
	 * Performs a quick, possibly incomplete removal or replacement of illegal
	 * characters in an element name. ".", "-", and "_" are legal in a name, and
	 * ":" is part of a namespace
	 * 
	 * @param aName
	 * @return
	 */
	public static String xmlQuickSanitize(String aName) {
		if (aName.indexOf('/') >= 0 || aName.indexOf('@') >= 0) {
			Matcher matcher = replaceNonQnameSeparators.matcher(aName);
			aName = matcher.replaceAll("_");
		}
		return aName;
	}
	
	/**
	 * Removes or replaces illegal characters in an element name. ".", "-", and
	 * "_" are legal in a name, and ":" is part of a namespace
	 * 
	 * @param aName
	 * @return
	 */
	public static String xmlFullSanitize(String aName) {
		Matcher matcher = replaceNonQnameSeparators.matcher(aName);
		matcher = removeNonQname.matcher(matcher.replaceAll("_"));
		return matcher.replaceAll("");
	}
	
	public static String escapeAngleBrackets(String aString) {
		if (aString.indexOf('<') >= 0) {
			aString = removeLeftAngleBracket.matcher(aString).replaceAll("&lt;");
		}
		if (aString.indexOf('>') >= 0) {
			aString = removeRightAngleBracket.matcher(aString).replaceAll("&gt;");
		}
		return aString;
	}
	
	public static String quickTagContentEscape(String aString) {
		if (aString.indexOf('&') >= 0) {
			aString = removeAmpersand.matcher(aString).replaceAll("&amp;");
		}
		return escapeAngleBrackets(aString);
	}

}
