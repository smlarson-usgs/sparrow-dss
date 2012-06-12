package gov.usgswim.sparrow.monitor;

import java.text.DecimalFormat;
import java.util.Iterator;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author eeverman
 */
public class MonitorHtmlUtil {
	
	DecimalFormat decimalFormat;
	
	public MonitorHtmlUtil() {
		decimalFormat = new DecimalFormat("#,##0.0#");
	}
	
	public CharSequence buildHtml(Iterator<RequestMonitor> monitors) {
		StringBuffer buff = new StringBuffer();
		
		startDoc(buff);
		
		while (monitors.hasNext()) {
			RequestMonitor mon = monitors.next();
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
		
		String classes = "";
		
		if (invo instanceof RequestMonitor) {
			classes = "request";
		} else if (invo instanceof ActionInvocation) {
			classes = "invocation action";
		} else if (invo instanceof CacheInvocation) {
			classes = "invocation cache";
			
			if (((CacheInvocation)invo).isCacheHit() == null) {
				//do nothing
			} else if (((CacheInvocation)invo).isCacheHit()) {
				classes += " cache-hit";
			} else {
				classes += " cache-miss";
			}
		} else {
			classes = "invocation";
		}
		
		
		classes += (invo.isFinished())?" finished":" not-finished";
		classes += (invo.hasError())?" has-error":"";
		
		buff.append("<div class=\"" + classes + "\">");
	}
	
	
	public void endInvocation(StringBuffer buff, Invocation mon) {
		buff.append("</div>");
	}
	
	public void renderInvocationDetails(StringBuffer buff, Invocation invo) {
		
		buff.append("<div class=\"details\">");
		
		buff.append("<div class=\"basic\">");
		detail(buff, invo.getTarget(), "[no target]", "Request target", "request-target");
		detail(buff, invo.getStartTime(), "[not started]", "Start time", "start-time");
		detail(buff, (double)invo.getRunTimeMs() / 1000d, "[not started]", "Run time", "run-time");
		detail(buff, invo.isNonNullResponse(), "[no resp.]", "Non-null response", "non-null-response", true);
		buff.append("</div>");
		
		buff.append("<div class=\"extended\">");
		detail(buff, invo.getRequest(), "[no request]", "Request", "request-obj", true);
		detail(buff, invo.getRequestStr(), "[no req string]", "Request string", "request-str", true);
		detail(buff, invo.getError(), "[no error]", "Error", "error", true);
		buff.append("</div>");
		
		buff.append("</div>");
		
		renderChildren(buff, invo);
	}
	
	public void renderInvocationDetails(StringBuffer buff, RequestMonitor invo) {
		
		buff.append("<div class=\"details\">");
		
		buff.append("<div class=\"basic\">");
		detail(buff, invo.getRequestUrl(), "[no url]", "Request url", "request-url");
		detail(buff, invo.getStartTime(), "[not started]", "Start time", "start-time");
		detail(buff, (double)invo.getRunTimeMs() / 1000d, "[not started]", "Run time", "run-time");
		buff.append("</div>");
		
		buff.append("<div class=\"extended\">");
		detail(buff, invo.getError(), "[no error]", "Error", "error", true);
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
	
	public void detail(StringBuffer buff, Object value, String ifNullValue, String description, String cssClass, boolean skipIfNull) {
		if (value != null || ! skipIfNull) {
			detail(buff, value, ifNullValue, description, cssClass);
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
}
