package gov.usgs.webservices.framework.formatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;


public interface IFormatter {
	public static enum OutputType {
		EXCEL_FLAT("application/vnd.ms-excel", "xls"),
		TEXT("text/plain", "txt"), 
		DATA("text/plain", "txt"),
		SOAP1_1("application/xml", "xml"),
		SOAP1_2("application/xml", "xml"),
		ZIP("application/zip", ""),
		
		// carry-overs
		HTML("text/html", "html"), 
		EXCEL("application/vnd.ms-excel", "xls"), 
		XML("application/xml", "xml"),
		KML("application/vnd.google-earth.kml+xml", "kml"),
		KMZ("application/vnd.google-earth.kmz", "kmz"), // unverified
		JSON("text/javascript", "js"), 
		// new types
		XHTML("application/xhtml+xml", "xhtml"), // see http://www.w3.org/TR/xhtml-media-types/
		XHTML_TABLE("application/xhtml+xml", "xhtml"),	//table only
		CSV("text/csv", "csv"),
		TAB("text/tab-separated-values", "tsv");
		
		private final String _mimeType;
		private final String _fileSuffix;
		
		private OutputType(String mimeType, String fileSuffix) {
			this._mimeType = mimeType;
			this._fileSuffix = fileSuffix;
		}
		
		public String getMimeType() {
			return _mimeType;
		}
		public String getFileSuffix() {
			return _fileSuffix;
		}
		
		public static OutputType parse(String value) {
			OutputType result = null;
			int hits = 0;
			for (OutputType element: OutputType.values()) {
				if (element.name().equalsIgnoreCase(value)) {
					return element;
				}
				if (element._mimeType.equalsIgnoreCase(value)) {
					result = element;
					hits++;
				}
			}
			return (hits == 1)? result: null;
		}
	}

	public void dispatch(XMLStreamReader in, HttpServletResponse response, boolean isAttachment) throws IOException;
	public void dispatch(XMLStreamReader in, HttpServletResponse response) throws IOException;
	public void dispatch(XMLStreamReader in, OutputStream out) throws IOException;
	public void dispatch(XMLStreamReader in, Writer out) throws IOException;
	public String getMimeType();
	public String getFileSuffix();
	public void setFileName(String fileName);
	public boolean isNeedsCompleteFirstRow();
}
