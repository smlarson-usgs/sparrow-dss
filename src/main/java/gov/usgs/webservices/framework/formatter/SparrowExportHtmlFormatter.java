package gov.usgs.webservices.framework.formatter;

import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.*;
import gov.usgs.webservices.framework.utils.XMLUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class SparrowExportHtmlFormatter extends AbstractFormatter {
	//Human readable data types
	public static final String STRING = "String";
	public static final String NUMBER = "Number";
	
	public static final Set<OutputType> acceptableTypes = new HashSet<OutputType>();
	static {
		// The DataFlatteningFormatter can flatten for the following output types
		//
		acceptableTypes.add(HTML);
		acceptableTypes.add(XHTML);
	}
	
	private Delimiters delims;
	private boolean isInItem;
	private boolean isInDescription;


	public SparrowExportHtmlFormatter(OutputType type) {
		
		super(type);
		
		switch (type) {
			case HTML:
			case XHTML:
				delims = Delimiters.HTML_DELIMITERS;
				break;
			default:
				throw new IllegalArgumentException("Only HTML or XHTML formats are accepted");
		}
		acceptableOutputTypes = acceptableTypes;
	}

	@Override
	public void dispatch(XMLStreamReader in, Writer out) throws IOException {
		try {
			
			out.write(delims.sheetStart);
			done: while (true) {
				int event = in.next();
				switch (event) {
					case XMLStreamConstants.START_DOCUMENT:
						break; // no start document handling needed
					case XMLStreamConstants.START_ELEMENT:

						String localName = in.getLocalName();						

						if ("group".equals(localName)) {

						} else if ("metadata".equals(localName)) {

						} else if ("description".equals(localName)) {
							
							isInDescription = true;
							out.write("<caption>");
							
						} else if ("columns".equals(localName)) {
							
							out.write(delims.headerStart);
							
							//Add an extra column for the ID
							out.write(delims.headerCellStart + "Reach ID" + delims.headerCellEnd);
							
						} else if ("col".equals(localName)) {
							
							String header = in.getAttributeValue(null, "name");
							out.write(delims.headerCellStart + formatSimple(header) + delims.headerCellEnd);
						
						} else if ("r".equals(localName)) {
								
							out.write(delims.bodyRowStart);
							out.write(delims.headerCellStart + formatSimple(in.getAttributeValue(null, "id")) + delims.headerCellEnd);
							
						} else if ("c".equals(localName)) {
							
							isInItem = true;
							out.write(delims.bodyCellStart);
							
						} 

						break;
					case XMLStreamConstants.CHARACTERS:
					case XMLStreamConstants.CDATA:	//fall through
						if (isInItem || isInDescription) {
							
							out.write(formatSimple(in.getText()));

						}
						break;
					case XMLStreamConstants.END_ELEMENT:
						localName = in.getLocalName();
						
						
						if ("group".equals(localName)) {

						} else if ("metadata".equals(localName)) {

						} else if ("description".equals(localName)) {
							
							out.write("</caption>");
							isInDescription = false;
							
						} else if ("columns".equals(localName)) {
							
							out.write(delims.headerEnd);
							
						} else if ("col".equals(localName)) {
							
							//col elements have no content
							
						} else if ("r".equals(localName)) {
								
							out.write(delims.bodyRowEnd);
							
						} else if ("c".equals(localName)) {
							
							if (isInItem) {
								isInItem = false;	//done w/ item
								out.write(delims.bodyCellEnd);
							} else {
								//Empty cell
								out.write("<td/>");
							}
							
						}

						break;
					case XMLStreamConstants.END_DOCUMENT:
						break done;
				}
			}
			out.write(delims.sheetEnd);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (EmptyStackException e) {
			e.printStackTrace();
		}
		out.flush();

	}

	public boolean isNeedsCompleteFirstRow() {
		return false;
	}

	/**
	 * TODO copied from DataFlatteningFormatter. Should decide where this ought to go.
	 * @param value
	 * @return
	 */
	private String formatSimple(String value) {
		if (value == null) {
			return "";
		}
		switch (this.outputType) {
			case CSV:
				value = StringEscapeUtils.escapeCsv(value);
				break;
			case XML: // same as excel
			case EXCEL:
				value = XMLUtils.quickTagContentEscape(value);
				break;
			case DATA:
			case TAB:
				//replace tabs, new line, form feed and carriage return w/ spaces
				value = StringUtils.replaceChars(value, "\t\n\f\r", "    ");
				break;
		}
		return value;
	}
	
	protected void setDelimiters(Delimiters delims) {
		this.delims = delims;
	}
}
