package gov.usgs.webservices.framework.formatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

public class ZipFormatter implements IFormatter {
	IFormatter wrappedFormatter;
	private String fileName = "data"; // the default name of the zip package is "data"
	
	public ZipFormatter(IFormatter formatter) {
		wrappedFormatter = formatter;
	}

	public void dispatch(XMLStreamReader in, HttpServletResponse response, boolean isAttachment)
	throws IOException {
		ZipOutputStream zOutStream = new ZipOutputStream(response.getOutputStream());
		response.setContentType("application/zip");
		response.addHeader("Content-transfer-encoding", "binary");
		if (isAttachment) {
			response.setHeader("Content-disposition", "attachment;filename=\"" + fileName + ".zip\"");
		}
		String suffix = wrappedFormatter.getFileSuffix();
		suffix = (suffix != null)? suffix: "txt";
		String fileName = "data." + suffix;
		zOutStream.putNextEntry(new ZipEntry(fileName));
		wrappedFormatter.dispatch(in, zOutStream);
		zOutStream.flush();
		zOutStream.close();
	}

	
	public void dispatch(XMLStreamReader in, HttpServletResponse response)
			throws IOException {
		dispatch(in, response, true);
	}

	public void dispatch(XMLStreamReader in, OutputStream out)
			throws IOException {
		// TODO Auto-generated method stub

	}

	public void dispatch(XMLStreamReader in, Writer out) throws IOException {
		// TODO Auto-generated method stub

	}

	public boolean isNeedsCompleteFirstRow() {
		// for the ZipFormatter, this is irrelevant, but may be needed for others in the pipeline
		return false;
	}
	public String getMimeType() {
		return OutputType.ZIP.getMimeType();
	}
	public String getFileSuffix() {
		return OutputType.ZIP.getFileSuffix();
	}

	public void setFileName(String name) {
		if (fileName  != null && fileName.length() > 0) {
		this.fileName = name;
		}
	}

}
