package gov.usgs.webservices.framework.formatter;

import gov.usgs.webservices.framework.utils.UsgsStAXUtils;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.ctc.wstx.stax.WstxOutputFactory;

public class XMLPassThroughFormatter extends AbstractFormatter implements IFormatter {

	public XMLPassThroughFormatter() {
		super(OutputType.XML);
	}

	@Override
	public void dispatch(XMLStreamReader in, Writer out) throws IOException {
		try {
			XMLOutputFactory outputFactory = WstxOutputFactory.newInstance();
			XMLStreamWriter writer = outputFactory.createXMLStreamWriter(out);
			UsgsStAXUtils.copy(in, writer);
			writer.flush();
		} catch (XMLStreamException e) {
			e.printStackTrace();
			// stupid. I want to wrap this in an IOException but 
			// constructor(Throwable) doesn't exist until 1.6
//			throw new RuntimeException(e);
			// TODO The following is a really bad way of handling things. Redo this

			throw new RuntimeException(e);
		}	
	}
	
	/*
	 * Both static methods copy() and writeStartElement() are copied from STAXUtils. This is done in order to 
	 */

	public static final int SKIP_EVENTS = 2;
	
	public boolean isNeedsCompleteFirstRow() {
		return false;
	}
}
