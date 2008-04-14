package gov.usgs.webservices.framework.formatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

public abstract class AbstractFormatter implements IFormatter {
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
		String mimeType = outputType.getMimeType();
		response.setContentType(mimeType);
		switch (outputType) {
			case TEXT:
			case EXCEL:
			case KML:
			case CSV:
			case TAB:
			case HTML:
			case JSON:
			case XML:
				response.addHeader(
				        "Content-Disposition","attachment; filename=" + fileName + "." + outputType.getFileSuffix() );
				break;
				default:
					// xml by default
					response.addHeader(
					        "Content-Disposition","attachment; filename=" + fileName + "." + OutputType.XML.getFileSuffix() );
			
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
