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

public class SparrowFlatteningFormatter extends AbstractFormatter {
	//Human readable data types
	public static final String STRING = "String";
	public static final String NUMBER = "Number";
	
	public static final Set<OutputType> acceptableTypes = new HashSet<OutputType>();
	static {
		// The DataFlatteningFormatter can flatten for the following output types
		//
		acceptableTypes.add(EXCEL);
		acceptableTypes.add(HTML);
		acceptableTypes.add(XHTML);
		acceptableTypes.add(XML);
		acceptableTypes.add(CSV);
		acceptableTypes.add(DATA);
		acceptableTypes.add(TAB);
	}
	
	private Delimiters delims;
	private boolean isInItem;
	private boolean isInDescription;
	private Queue<String> headers = new LinkedList<String>();
	private Queue<String> groups = new LinkedList<String>();
	private int groupCount;
	private Queue<Integer> groupCounts = new LinkedList<Integer>();
	private Queue<String> rowCellValues = new LinkedList<String>();


	public SparrowFlatteningFormatter(OutputType type) {
		
		super(type);
		
		switch (type) {
			case EXCEL_FLAT: // TODO eliminate
			case EXCEL:
				// TODO use configuration parameter
				delims = Delimiters.makeExcelDelimiter("USGS", new Date().toString());
				break;
			case HTML:
			case XHTML:
			case XML:
				delims = Delimiters.HTML_DELIMITERS;
				break;
			case CSV:
				delims = Delimiters.CSV_DELIMITERS;
				break;
			case DATA:
			case TAB:
				delims = Delimiters.TAB_DELIMITERS;
				break;
			default:
				throw new IllegalArgumentException("JSON and KML/KMZ not accepted");
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

						if ("description".equals(localName)) {
							isInDescription = true;
						} else if ("c".equals(localName)) {
							isInItem = true;
						} else if ("r".equals(localName)) {
							// write body row head
							out.write(delims.bodyRowStart);
							rowCellValues.add(in.getAttributeValue(null, "id"));
						} else if ("col".equals(localName)) {
							String header = StringUtils.trimToEmpty(in.getAttributeValue(null, "name"));
							headers.add(header);
							groupCount++;
							//String type = in.getAttributeValue(null, "type");
						} else if ("group".equals(localName)) {
							String groupName = StringUtils.trimToEmpty(in.getAttributeValue(null, "name"));
							groups.add(groupName);
							groupCount = 0;
						} else if ("metadata".equals(localName)) {
							// add the id attribute to the beginning of the headers queue, as it's not explicitly specified
							headers.add("id");
							// the id attribute will have its own header, with a
							// group of size = 1;
							groups.add("");
							groupCounts.add(1);
						} else if ("data".equals(localName)) {
							// output the header and column names
							StringBuilder groupHeaderOutput =  new StringBuilder();
							StringBuilder colHeaderOutput = new StringBuilder(delims.headerRowStart);
							while (groups.peek() != null) {
								String groupName = groups.poll();
								int gCount = groupCounts.poll();

								{// output the cell for the group name row
									groupHeaderOutput.append(delims.makeWideHeaderCell(formatSimple(groupName), gCount));
								}
								// output the cells for the column header row
								for (int i = 0; i < gCount; i++) {
									colHeaderOutput.append(delims.headerCellStart).append(formatSimple(headers.poll())).append(delims.headerCellEnd);
								}
							}

							// only output the group header row if there is stuff
							if (groupHeaderOutput.length() > 0) {
								groupHeaderOutput.insert(0, delims.headerRowStart);
								groupHeaderOutput.append(delims.headerRowEnd);
								out.write(groupHeaderOutput.toString());
							}

							// write the colum header row
							colHeaderOutput.append(delims.headerRowEnd);
							out.write(colHeaderOutput.toString());
						}

						break;
					case XMLStreamConstants.CHARACTERS:
						if (isInItem) {
							String s = formatSimple(in.getText());
							rowCellValues.add(s);
							isInItem = false;	//done w/ item
						}
						break;
					case XMLStreamConstants.CDATA:
						if (isInDescription) {
							
							String s = in.getText();
							
							switch (outputType) {
							case EXCEL:
								//All stuffed into one cell w/ multiple lines
								String EXCEL_LINE_BREAK = "&#10;";	//Empirically, this seems to be it.
								s = StringEscapeUtils.escapeXml(s);	//Excel seems to have issues w/ CDATA, so escape all xml.
								s = "<Row><Cell ss:StyleID=\"s22\"><Data ss:Type=\"String\">" + s.replaceAll("[\n\r]+", EXCEL_LINE_BREAK) + "</Data></Cell></Row>";
								break;
							case CSV:
								s = StringUtils.replace(s, "\"", "\"\"");
								s = "#,\"" + s.replaceAll("[[\n\r][\r\n]\n\r][\t ]*", "\"\n#,\"") + "\"\n";
								break;
							case DATA:
							case TAB:
								s = "#,\"" + s.replaceAll("[[\n\r][\r\n]\n\r][\t ]*", "\"\n#,\"") + "\"\n";
								break;
							default:
								break;
							}
							rowCellValues.add(s);
							isInDescription = false;
						}
						break;
					case XMLStreamConstants.END_ELEMENT:
						localName = in.getLocalName();
						
						if ("description".equals(localName)) {
							// output the row's cells
							StringBuilder cells = new StringBuilder();
							
							//Should only be one
							for (String cellValue: rowCellValues) {
								cells.append(cellValue);
							}
							
							if (cells.length() > 0) {
								out.write(cells.toString());
							}
							
							// clear the row
							rowCellValues.clear();
						} else if ("c".equals(localName)) {
							if (isInItem) {
								//There was no content for this 'c', so add an empty cell value
								rowCellValues.add("");
							}
						} else 	if ("r".equals(localName)) {
							// output the row's cells
							StringBuilder cells = new StringBuilder();
							for (String cellValue: rowCellValues) {
								cells.append(delims.bodyCellStart).append(cellValue).append(delims.bodyCellEnd);
							}
							
							String cellsString = "";
							if (cells.length() > 0 && (CSV.equals(outputType) || TAB.equals(outputType))) {
								//Drop the last cell body delimiter
								cellsString = cells.substring(0, cells.length() - delims.bodyCellEnd.length());
							} else {
								cellsString = cells.toString();
							}
							
							out.write(cellsString);
							
							// clear the row and output its end
							rowCellValues.clear();
							out.write(delims.bodyRowEnd);
							
						} else if ("group".equals(localName)) {
							groupCounts.add(groupCount);
							groupCount = 0;
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
