package gov.usgs.webservices.framework.formatter;

import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.*;
import gov.usgs.webservices.framework.utils.XMLUtils;
import gov.usgswim.datatable.RelationType;

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
		acceptableTypes.add(XHTML_TABLE);
	}
	

	
	//Configuration
	private String idHeader;	//header used for the row ID column
	private String jsFunctionForRowId;	//A javascript function to wrap around the row ID
	private String totalRowRowHeader;	//Text used in the ID column of row of type=total
	private String jsFileName;	//Name of a js file to include
	private String cssFileName;	//Name of a css file to include
	private String htmlTitle;	//content of the html title tag
	
	//State tracking
	private Delimiters delims;
	private boolean isInItem;
	private boolean isInDescription;
	private int currentColumnIndex = -1;
	private ValueFormatter baseFormat;
	private ArrayList<ValueFormatter> formatters = new ArrayList<ValueFormatter>();	//one per column


	public SparrowExportHtmlFormatter(OutputType type, String jsFunctionForRowId,
			String idHeader, String totalRowRowHeader, String jsFileName, String cssFileName, String htmlTitle) {
		
		super(type);
		
		switch (type) {
			case HTML:
			case XHTML:
			case XHTML_TABLE:
				delims = Delimiters.HTML_DELIMITERS;
				break;
			default:
				throw new IllegalArgumentException("Only HTML or XHTML formats are accepted");
		}
		acceptableOutputTypes = acceptableTypes;
		
		this.outputType = type;
		this.jsFunctionForRowId = jsFunctionForRowId;
		this.idHeader = idHeader;
		this.totalRowRowHeader = totalRowRowHeader;
		this.jsFileName = jsFileName;
		this.cssFileName = cssFileName;
		this.htmlTitle = htmlTitle;
		this.baseFormat = new SimpleValueFormatter(outputType);
		
	}

	@Override
	public void dispatch(XMLStreamReader in, Writer out) throws IOException {
		try {
			
			
			done: while (true) {
				int event = in.next();
				switch (event) {
					case XMLStreamConstants.START_DOCUMENT:
						break; // no start document handling needed
					case XMLStreamConstants.START_ELEMENT:

						String localName = in.getLocalName();						

						if ("group".equals(localName)) {

						} else if ("metadata".equals(localName)) {

							//Don't do this right at the start b/c we want to make
							//sure processing instructions are read first.
							if (XHTML.equals(outputType) || HTML.equals(outputType)) {
								writeHtmlHead(out);
							}
							
							out.write(delims.sheetStart);
							
						} else if ("description".equals(localName)) {
							
							isInDescription = true;
							out.write("<caption>");
							
						} else if ("columns".equals(localName)) {
							
							out.write(delims.headerStart);
							out.write(delims.headerRowStart);
							
							//Add an extra column for the ID
							if (jsFunctionForRowId != null) {
								if (idHeader != null) {
									out.write(delims.headerCellStart + idHeader + delims.headerCellEnd);
								} else {
									out.write(delims.headerCellStart + "Row ID" + delims.headerCellEnd);
								}
							}
							
						} else if ("col".equals(localName)) {
							
							String header = in.getAttributeValue(null, "name");
							String relation = in.getAttributeValue(null, RelationType.XML_ATTRIB_NAME);
							
							out.write(delims.headerCellStart + baseFormat.format(header) + delims.headerCellEnd);
							
							if (relation != null) {
								RelationType rt = RelationType.parse(relation);
								if (RelationType.rel_fraction.equals(rt)) {
									formatters.add(new HTMLRelativePercentValueFormatter(true));
								} else if (RelationType.rel_percent.equals(rt)) {
									formatters.add(new HTMLRelativePercentValueFormatter(false));
								} else {
									formatters.add(new HTMLTableCellFormatter(outputType));
								}
							} else {
								formatters.add(new HTMLTableCellFormatter(outputType));
							}
						
						} else if ("data".equals(localName)) {
							
							out.write(delims.bodyStart);
							
						} else if ("r".equals(localName)) {
							
							//reset the column index
							currentColumnIndex = -1;
							
							out.write(delims.bodyRowStart);
							
							//Add an extra column for the ID
							String type = in.getAttributeValue(null, "type");
							
							if (! "total".equals(type)) {
								//standard data row
								if (jsFunctionForRowId != null) {
									String id = baseFormat.format(in.getAttributeValue(null, "id"));
									
									out.write(delims.headerCellStart);
									out.write(
											"<a href=\"javascript:" + jsFunctionForRowId + "(" + id + ");\">" +
											id + "</a>");
									out.write(delims.headerCellEnd);
								} else {
									//don't write the id if not including the script
									//out.write(delims.headerCellStart + baseFormat.format(in.getAttributeValue(null, "id")) + delims.headerCellEnd);
								}
							} else {
								//Total row
								
								if (totalRowRowHeader != null) {
									out.write(delims.headerCellStart + baseFormat.format(totalRowRowHeader) + delims.headerCellEnd);
								} else {
									out.write(delims.headerCellStart + "Total for all rows:" + delims.headerCellEnd);
								}
							}

							
						} else if ("c".equals(localName)) {
							
							currentColumnIndex++;
							isInItem = true;
							//out.write(delims.bodyCellStart); (handled by cell formatters)
							
						} 

						break;
					case XMLStreamConstants.CHARACTERS:
					case XMLStreamConstants.CDATA:	//fall through
						if (isInItem) {
							out.write(formatters.get(currentColumnIndex).format(in.getText()));
						} else if (isInDescription) {
							out.write(baseFormat.format(in.getText()));

						}
						break;
					case XMLStreamConstants.PROCESSING_INSTRUCTION:
						//The current BasicTagEven and reader does not support PIs. :(

						break;
					case XMLStreamConstants.END_ELEMENT:
						localName = in.getLocalName();
						
						
						if ("group".equals(localName)) {

						} else if ("metadata".equals(localName)) {

						} else if ("description".equals(localName)) {
							
							out.write("</caption>");
							isInDescription = false;
							
						} else if ("columns".equals(localName)) {
							
							out.write(delims.headerRowEnd);
							out.write(delims.headerEnd);
							
						} else if ("col".equals(localName)) {
							
							//col elements have no content
							
						} else if ("data".equals(localName)) {
							
							out.write(delims.bodyEnd);
							
						} else if ("r".equals(localName)) {
								
							out.write(delims.bodyRowEnd);
							
						} else if ("c".equals(localName)) {
							
							if (isInItem) {
								isInItem = false;	//done w/ item
								//out.write(delims.bodyCellEnd); //handled by cell formatters
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
			
			if (XHTML.equals(outputType) || HTML.equals(outputType)) {
				writeHtmlFoot(out);
			}
			
			
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
	
	protected void writeHtmlHead(Writer out) throws IOException {
		
		if (XHTML.equals(outputType)) {
			out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
					"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
			
			out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		} else {
			out.write("<!DOCTYPE html>\n");
			out.write("<html>\n");
		}
		out.write("<head>\n");
		
		if (htmlTitle != null) {
			out.write("<title>" + htmlTitle + "</title>\n");
		} else {
			out.write("<title>SPARROW DSS Report</title>\n");
		}
		
		if (jsFileName != null) {
			out.write("<script src=\"" + jsFileName + "\">​</script>\n");
		}
		
		if (cssFileName != null) {
			out.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssFileName + "\" />\n");
		}

		out.write("</head>\n");
		out.write("<body>\n");
	}
	
	protected void writeHtmlFoot(Writer out) throws IOException {
		out.write("</body>\n");
		out.write("</html>");
	}

	
	protected void setDelimiters(Delimiters delims) {
		this.delims = delims;
	}
}
