package gov.usgswim.sparrow.monitor;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author eeverman
 */
public class MonitorHtmlUtil {
	
	DecimalFormat decimalFormat;
	DateFormat dateFormat;
	
	public MonitorHtmlUtil() {
		decimalFormat = new DecimalFormat("#,##0.0#");
		dateFormat = new SimpleDateFormat("K:mm:ss, z");
	}
	
	public CharSequence buildHtml(RequestMonitor[] monitors) {
		StringBuffer buff = new StringBuffer();
		
		startDoc(buff);
		
		for (RequestMonitor mon : monitors) {
			renderRequestMonitor(buff, mon);
		}
		
		endDoc(buff);
		
		return buff;
	}
	
	public void renderRequestMonitor(StringBuffer buff, RequestMonitor mon) {

		startInvocation(buff, mon);
		renderInvocationDetails(buff, mon);
		endInvocation(buff, mon);

	}
		
	public void renderInvocation(StringBuffer buff, Invocation invo, boolean isFirst) {

		startInvocation(buff, invo);
		renderInvocationDetails(buff, invo);
		endInvocation(buff, invo);

	}
	
	public void startInvocation(StringBuffer buff, Invocation invo) {
		
		String classes = mergeToString(
				"invocation",
				invo.getName(), 
				(invo.isFinished())?"finished":"not-finished",
				(invo.hasError())?"has-error":null);
		
		if (invo instanceof CacheInvocation) {
			CacheInvocation ci = (CacheInvocation)invo;
			
			if (true == ci.isCacheHit()) {
				classes = mergeToString(classes, "cache-hit");
			} else if (false == ci.isCacheHit()) {
				classes = mergeToString(classes, "cache-miss");
			}
		}

		buff.append("<div class=\"" + classes + "\">");
	}
	
	
	public void endInvocation(StringBuffer buff, Invocation mon) {
		buff.append("</div>");
	}
	
	public void renderInvocationDetails(StringBuffer buff, RequestMonitor invo) {
		
		buff.append("<div class=\"details\">");
		
		buff.append("<div class=\"basic\">");
		detail(buff, invo.getRequestUrl(), "[no url]", "Request url", "request-url");
		detail(buff, invo.getStartTime(), "[not started]", "Start time", dateFormat, "start-time");
		detail(buff, invo.getStartTime(), "[not started]", "Start time", "start-time-ms");
		detail(buff, invo.getRunTimeMs(), "[not started]", "Run time", "run-time-ms");
		buff.append("</div>");
		
		buff.append("<div class=\"extended\">");
		detail(buff, invo.getError(), "[no error]", "Error", true, "error");
		buff.append("</div>");
		
		buff.append("</div>");
		
		renderChildren(buff, invo);
	}
	
	public void renderInvocationDetails(StringBuffer buff, Invocation invo) {
		
		buff.append("<div class=\"details\">");
		
		if (invo.getBestGuessPercentComplete() != null) {
			buff.append("<div class=\"percent-complete\">");
				detail(buff, invo.getBestGuessPercentComplete(), "[unknown]", "Percent Complete", "percent-complete-value");
				
				String unit = invo.getChunksUnit();
				if (unit == null) unit = "";
				if (invo.getChunksDone() != null && invo.getChunksTotal() != null) {
					String v = invo.getChunksDone() + " of " + invo.getChunksTotal() + " " + unit;
					detail(buff, v, "[unknown]", "Stats", "percent-complete-stats");
				} else if (invo.getChunksDone() != null) {
					String v = invo.getChunksDone() + " of ??" + unit;
					detail(buff, v, "[unknown]", "Stats", "percent-complete-stats");
				}
				
			buff.append("</div>");
		}
		
		buff.append("<div class=\"basic\">");
		detail(buff, invo.getTarget(), "[no target]", "Request target", "request-target");
		detail(buff, invo.getStartTime(), "[not started]", "Start time", dateFormat, "start-time");
		detail(buff, invo.getStartTime(), "[not started]", "Start time", "start-time-ms");
		detail(buff, invo.getRunTimeMs(), "[not started]", "Run time", "run-time-ms");
		detail(buff, invo.isNonNullResponse(), "[no resp.]", "Non-null response", true, "non-null-response");
		buff.append("</div>");
		
		buff.append("<div class=\"extended\">");
		detail(buff, invo.getRequest(), "[no request]", "Request", true, "request-obj");
		detail(buff, invo.getRequestStr(), "[no req string]", "Request string", true, "request-str");
		detail(buff, invo.getError(), "[no error]", "Error", true, "error");
		buff.append("</div>");
		
		buff.append("</div>");
		
		renderChildren(buff, invo);
	}
	
	
	public void renderChildren(StringBuffer buff, Invocation invo) {
		if (invo.hasChildren()) {
			
			boolean isFirst = true;
			
			buff.append("<ul>");
			
			for (Invocation child : invo.getChildren()) {
				renderInvocation(buff, child, isFirst);
				isFirst = false;
			}
			
			buff.append("</ul>");
		}
	}
	
	public void detail(StringBuffer buff, Object value, String ifNullValue, String description, boolean skipIfNull, String cssClass) {
		if (value != null || ! skipIfNull) {
			detail(buff, value, ifNullValue, description, cssClass);
		}
	}
	
	public void detail(StringBuffer buff, Long value, String ifNullValue, String description, DateFormat dateFormat, String cssClass) {
		if (value != null && dateFormat != null) {
			String formatedDate = dateFormat.format(new Date(value));
			detail(buff, formatedDate, ifNullValue, description, cssClass);
		}
	}
		
	public void detail(StringBuffer buff, Object value, String ifNullValue, String description, String cssClass) {
		
		String valString;
		if (value != null && (value instanceof Double || value instanceof Float)) {
			valString = decimalFormat.format(value);
		} else if (value != null) {
			valString = value.toString();
		} else {
			valString = ifNullValue;
		}
		
		buff.append(
				"<p class=\"" + cssClass + "\">"
				+ "<span class=\"desc\">" + description + ": </span>"
				+ "<span class=\"val\">" + valString + "</span>"
				+ "</p>");
	}
	
	
	
	public void startDoc(StringBuffer buff) {
		buff.append("<div class=\"monitor-report\">");
	}
	
	public void endDoc(StringBuffer buff) {
		buff.append("</div>");
	}
	
	/**
	 * Merges an array of Strings, an array of an Array of Strings, or
	 * any type of object via toString(). 
	 * @param mergeMe
	 * @return 
	 */
	protected String mergeToString(Object... mergeMe) {
		StringBuffer sb = new StringBuffer();
		if (mergeMe.length == 0) return "";
		
		for (Object o: mergeMe) {
			if (o != null) {
				if (o instanceof Object[]) {
					if (((Object[])o).length == 1) {
						sb.append(mergeToString(((Object[])o)[0]) + " ");
					} else {
						sb.append(mergeToString(o) + " ");
					}
					
				} else {
					sb.append(o.toString() + " ");
				}
			}
		}
		
		if (sb.length() > 1) sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
	}
}
