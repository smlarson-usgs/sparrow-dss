package gov.usgs.webservices.framework.formatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

public abstract class AbstractFormatter implements IFormatter {
	
	public static final String UTF8 = "UTF-8";
	private static final char UNICODE_BOM = '\uFEFF';	//Unicode Byte Order Marker
	
	OutputType outputType;
	Set<OutputType> acceptableOutputTypes;
	private String fileName = "data"; // default file name is data
	
	
	protected AbstractFormatter(OutputType type) {
		this.outputType = type;
	}
	
	protected AbstractFormatter(String type) {
		this.outputType = OutputType.valueOf(type.toUpperCase());
	}

	public void dispatch(XMLStreamReader in, HttpServletResponse response) throws IOException {
		dispatch(in, response, true);
	}
	
	public void dispatch(XMLStreamReader in, HttpServletResponse response, boolean isAttachment) throws IOException {
		String mimeType = outputType.getMimeType();
		response.setContentType(mimeType);
		
		if (isAttachment) {
			response.addHeader(
			        "Content-Disposition","attachment; filename=" + fileName + "." + outputType.getFileSuffix() );
		}
		
		String encoding = in.getCharacterEncodingScheme();
		
		if (encoding == null) {
			encoding = UTF8;
		}
		
		response.setCharacterEncoding(encoding);
		
		switch (outputType) {
			case TAB:
			case CSV:
				
				/*
				 * These text files have no way to indicate encoding, so a BOM
				 * (Byte Order Marker) is added to help desktop applications figure
				 * out what the encoding of the file is after it has been saved
				 * to disk and the charset/encoding info passed in the header
				 * is no longer available.  This was specifically added to help
				 * Excel, which assumes default encoding for opening CSV files.
				 */
				response.getWriter().print(UNICODE_BOM);
				response.getWriter().flush();
				break;
			case TEXT:
			case EXCEL:
			case KML:
			case HTML:
			case JSON:
			case XML:
				
				break;
			default:
				// xml by default
				if (isAttachment) {
					response.addHeader(
					        "Content-Disposition","attachment; filename=" + fileName + "." + OutputType.XML.getFileSuffix() );
				}
		}
		dispatch(in, response.getWriter());
	}
	
	public void dispatch(XMLStreamReader in, OutputStream out) throws IOException {
		dispatch(in, new PrintWriter(out));
	}
	
	public abstract void dispatch(XMLStreamReader in, Writer out) throws IOException;
	
	public String getMimeType() {
		return outputType.getMimeType();
	}
	
	public String getFileSuffix() {
		return this.outputType.getFileSuffix();
	}
	
	public boolean accepts(OutputType type) {
		return (acceptableOutputTypes != null) && acceptableOutputTypes.contains(type);
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String name) {
		if (fileName != null && fileName.length() > 0) {
			this.fileName = name;
		}
	}

}
