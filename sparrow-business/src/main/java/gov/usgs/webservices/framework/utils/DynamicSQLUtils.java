package gov.usgs.webservices.framework.utils;

import java.util.Collection;

public class DynamicSQLUtils {

//		public static String consolidateClauses(List<String> clauses, String clauseHeader) {
//			StringBuilder newWhereClause = new StringBuilder();
//			if (!clauses.isEmpty()) {
//				newWhereClause.append(clauseHeader);
//				for (String clause : clauses) {
//					newWhereClause.append("(").append(clause).append(") and ");
//				}
//				clauses = newWhereClause.substring(0,
//						newWhereClause.length() - 4);
//			}	
//		}
		
	public static StringBuilder join(Collection<String> strings, String delimiter, String pre, String post) {
		StringBuilder result = new StringBuilder();
		delimiter = (delimiter == null) ? "" : delimiter;
		pre = (pre == null) ? "" : pre;
		post = (post == null) ? "" : post;
		if (!strings.isEmpty()) {
			for (String string : strings) {
				result.append(pre).append(string).append(post).append(delimiter);
			}
			result.delete(result.length() - delimiter.length(), result.length());
		}
		return result;
	}

	public static String prepadClauseIfNecessary(String clause) {
		// for an empty clause, return the empty string
		if (clause == null || clause.length() == 0) {
			return "";
		}
		// for a non-empty clause beginning with whitespace, don't change anything
		if (clause.charAt(0) == ' ' || clause.charAt(0) == '\t') {
			return clause;
		}
		// pad the clause with a space
		return ' ' + clause;
	}

}
