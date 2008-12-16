package gov.usgs.webservices.framework.formatter;

import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.CSV;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.DATA;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.EXCEL;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.HTML;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.TAB;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.XHTML;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.XML;
import gov.usgs.webservices.framework.utils.XMLUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SparrowFlatteningFormatter extends AbstractFormatter {
	//
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
//	private String itemValue;
	private Queue<String> headers = new LinkedList<String>();
	private Queue<String> groups = new LinkedList<String>();
	private boolean isStarted;
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

						if ("c".equals(localName)) {
							isInItem = true;
						} else if ("r".equals(localName)) {
//							rowCounter++;
//							if (rowCounter > 100) {
//								out.write(delims.sheetEnd);
//								out.flush();
//								return;
//							}
							if (!isStarted) {
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
								isStarted = true;
							}
							// write body row head
							out.write(delims.bodyRowStart);
							rowCellValues.add(in.getAttributeValue(null, "id"));
						} else if ("col".equals(localName)) {
							String header = in.getAttributeValue(null, "name");
							headers.add(header);
							groupCount++;
						} else if ("group".equals(localName)) {
							String groupName = in.getAttributeValue(null, "name");
							groups.add(groupName);
							groupCount = 0;
						} else if ("metadata".equals(localName)) {
							// add the id attribute to the beginning of the headers queue, as it's not explicitly specified
							headers.add("id");
							// the id attribute will have its own header, with a
							// group of size = 1;
							groups.add("");
							groupCounts.add(1);
						}

						break;
					case XMLStreamConstants.CHARACTERS:
						if (isInItem) {
							rowCellValues.add(in.getText());
						}
						break;
					case XMLStreamConstants.END_ELEMENT:
						localName = in.getLocalName();
						
						if ("c".equals(localName)) {
							isInItem = false;

						} else 	if ("r".equals(localName)) {
							// output the row's cells
							StringBuilder cells = new StringBuilder();
							for (String cellValue: rowCellValues) {
								cells.append(delims.bodyCellStart).append(cellValue).append(delims.bodyCellEnd);
							}
							out.write(cells.toString());
							
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
		// TODO Auto-generated method stub
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
				// Currently handles commas and quotes. May need to handle carriage
				// returns and tabs later?
				boolean hasQuotes = value.indexOf('"') >= 0;
				boolean isDoEncloseInQuotes = (value.indexOf(',')>=0) || hasQuotes; 
				if (hasQuotes) {
					value = value.replaceAll("\"", "\"\""); // escape quotes by doubling them
				}
				if (isDoEncloseInQuotes) {
					return '"' + value + '"';
				}
				break;
			case XML: // same as excel
			case EXCEL:
				value = XMLUtils.quickTagContentEscape(value);
				break;
		}
		return value;
	}
}
