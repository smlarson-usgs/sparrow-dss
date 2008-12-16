package gov.usgs.webservices.framework.formatter;

import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.CSV;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.DATA;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.EXCEL;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.EXCEL_FLAT;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.HTML;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.TAB;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.XHTML;
import static gov.usgs.webservices.framework.formatter.IFormatter.OutputType.XML;
import gov.usgs.webservices.framework.utils.URIUtils;
import gov.usgs.webservices.framework.utils.XMLUtils;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * DataFlatteningFormatter accepts an arbitrary XML Stream and renders it as 
 * a Resultset-like HTML table. Basically, it treats children of the root 
 * elements as rows, and each of the attributes and children as columns.
 * 
 * The Formatter can be configured to recognize either elements at a certain 
 * depth as rows, or recognize specified elements as rows. Use the methods
 * setRowElementName() for this. By default, the 
 * depth level is set to 3, e.g. the grandchildren elements of the root element.
 * 
 * CAVEAT: Assumes the first element found is complete.
 * 
 * @author ilinkuo
 * 
 */
public class DataFlatteningFormatter extends AbstractFormatter implements IFormatter {

	// CONSTANTS
	public static final int DEFAULT_ROW_DEPTH_LEVEL = 3;
	public static final Set<OutputType> acceptableTypes = new HashSet<OutputType>();
	private static final String EMPTY_STRING = "";
	
	
	static {
		// The DataFlatteningFormatter can flatten for the following output types
		acceptableTypes.add(EXCEL_FLAT);
		//
		acceptableTypes.add(EXCEL);
		acceptableTypes.add(HTML);
		acceptableTypes.add(XHTML);
		acceptableTypes.add(XML);
		acceptableTypes.add(CSV);
		acceptableTypes.add(DATA);
		acceptableTypes.add(TAB);
	}

	// Configuration fields
	private int depthLevel;
	private int ROW_DEPTH_LEVEL = DEFAULT_ROW_DEPTH_LEVEL;
	private String ROW_ELEMENT_IDENTIFIER = "!"; // nonsense value by default so nothing can be matched
	private Delimiters delims;
	private boolean isKeepElders; // true if elder element information is used to determine nestings
	private boolean isDoCopyDown = true;
	
	// ============
	// CONSTRUCTORS
	// ============
	public DataFlatteningFormatter() {
		this(OutputType.HTML);
		acceptableOutputTypes = acceptableTypes;
	}
	
	public DataFlatteningFormatter(OutputType type) {
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
	
	// ====================================
	// PRIVATE CLASSES: Element, ParseState
	// ====================================
	private static class Element{
		public String fullName;
		public String qualifiedname;
		public String localName;
		public boolean hasChildren;
		
		public Element(String full, String local) {
			fullName = full;
			localName = local;
		}
		
		@Override 
		public int hashCode() {
			return fullName.hashCode();
		};
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof Element)) {
				return false;
			}
			Element that = (Element) obj;
			return (fullName.equals(that.fullName));
		}
	}
	
	private class ParseState{
		// global state fields
		boolean hasEncounteredTargetContent;
		int row;
		
		// row state fields
		boolean isInTarget;
		boolean isProcessingHeaders;

		// context tracks the parsing depth within the document
		public Stack<String> context = new Stack<String>();
		public String targetElementContext = EMPTY_STRING;
		/**
		 * Tracks the currently open target element until a child of that element is encountered
		 */
		Element currentTargetColumn;
		/**
		 * Tracks the currently open elder element until a child of that element
		 * is encountered. An elder is an ancestor, uncle, or elder sibling of a
		 * target element.
		 */
		Element currentElder; 
		
		// ouput fields
		public Set<Element> targetColumnList = new LinkedHashSet<Element>();
		public Set<Element> elderColumnList = new LinkedHashSet<Element>();
		public Map<String, String> targetColumnValues = new HashMap<String, String>();
		public Map<String, String> elderColumnValues = new HashMap<String, String>();

		// --------------------
		// STATE UPDATE METHODS
		// --------------------
		/**
		 * Updates the state at the beginning of a StAX StartElement event
		 * @param localName
		 */
		public void startElementBeginUpdate(String localName) {
			// Bookkeeping: the top of the context stack always points to the current element
			String contextName = makeFullName(context.peek(), localName);
			context.push(contextName);
			if (isOnTargetRowStartOrEnd(localName)) {
				// "Correct" the context because we want to use shorter contexts
				// while within the target. This makes the within target context
				// a *relative* one, which allows us to properly handle the target
				// if it occurs at different places or different levels within
				// the document. Using the absolute context fails to handle
				// uneven hierarchies correctly.
				context.pop();
				context.push(localName);
				
				// Nonetheless, we want to remember the target element. In this
				// case, we use the full context rather than the abbreviated
				// one.
				targetElementContext = contextName;
				row++;
				
				// Reset the row parsing status
				isInTarget = true;
				isProcessingHeaders = (row == 1);
				targetColumnValues.clear();
			}
		}
		
		/**
		 * Updates the state at the end of a StAX EndElement event
		 * @param onTargetEnd
		 */
		public void finishEndElement(boolean onTargetEnd) {
			String current = context.pop();
			boolean isElderElement = !isInTarget;
			if (isElderElement && isKeepElders) {
				if (isDoCopyDown) {
					clearAncestralDescendants(current);
				}
				// now backtracking. currentElder only tracks going down.
				currentElder = null;
			} else { // not a elder, in the row
				currentTargetColumn = null;
			}
			if (onTargetEnd) {
				isInTarget = false; // exiting target
			}
		}
		
		// ----------------
		// STATE INDICATORS
		// ----------------
		public boolean isOnTargetRowStartOrEnd(String localName) {
			return context.size() == ROW_DEPTH_LEVEL || ROW_ELEMENT_IDENTIFIER.equals(localName);
		}
		
		/**
		 * Returns true if at least one target has been found. The difference
		 * between isTargetFound() and hasEncounteredContent is that the the
		 * first is true immediately upon encountering hte start tag and that
		 * the latter becomes true only when nonempty tag content or attribute
		 * content within the target is encountered.
		 * 
		 * @return
		 */
		public boolean isTargetFound() {
			return (row > 0);
		}
		
		// ---------------
		// SERVICE METHODS
		// ---------------
		/**
		 * Convenience method for creating an element.
		 * @param localName
		 * @return
		 */
		public Element makeElement(String localName) {
			return new Element(context.peek(), localName);
		}
		
		/**
		 * Adds a target header or column. Should only be called within a target
		 * @param localName
		 */
		public void addHeaderOrColumn(String localName) {
			if (isInTarget) {
				Element e = makeElement(localName);
				boolean isNew = targetColumnList.add(e);
				// Note that the previous column element has child tags so shouldn't
				// be output as data flattening isn't set to deal with document
				// style xml. 
				if (isNew) {
					if (currentTargetColumn != null) {
						currentTargetColumn.hasChildren = true;
					}
					if (currentElder != null) {
						currentElder.hasChildren = true;
					}
					currentTargetColumn = e; // update
				}
			}
		}
		
		/**
		 * Adds an elder target header or column. Should only be called before a
		 * target is ever found. We can't deal with nontarget columns whose
		 * first appearance is after a target.
		 * 
		 * @param localName
		 */
		public void addElderHeaderOrColumn(String localName) {
			if (!isTargetFound()) {
				if (isKeepElders) {
					Element e = makeElement(localName);
					boolean isNew = elderColumnList.add(e);
					// Note that the previous column element has child tags so shouldn't
					// be output as data flattening isn't set to deal with document
					// style xml. 
					if (isNew) {
						if (currentElder != null) {
							currentElder.hasChildren = true;
						}
						currentElder = e; // update
					}
				}
			}
		}
		
		public boolean hasTargetContent() {
			if (hasEncounteredTargetContent) {
				return true;
			}
			for (String value: targetColumnValues.values()) {
				if (value != null && value.length() > 0) {
					hasEncounteredTargetContent = true;
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Clears all descendant values of an ancestor of the target, excluding
		 * the target itself.
		 * 
		 * @param fullName
		 */
		public void clearAncestralDescendants(String fullName) {
			if (!URIUtils.isAncestorOf(fullName, targetElementContext)) {
				// it's not a direct ancestor of the target, so don't do anything
				return; 
			}
			boolean isAncestorFound = false;
			for (Element elderColumn: elderColumnList) {
				String elementName = elderColumn.fullName;
				// Make use of the fact that elderColumnList is an ordered set
				// ordered by document order to know that once you've found an
				// ancestor, you can delete everything after it.
				if (isAncestorFound || URIUtils.isAncestorOf(fullName, elementName)) {
					isAncestorFound = true;
					elderColumnValues.remove(elementName);
				}
			}
		}

		/**
		 * Store the character text using the current element name as key
		 * @param value
		 */
		public void putChars(String value) {
			if (isInTarget) {
				targetColumnValues.put(context.peek(), value);
			} else if (isKeepElders) {
				elderColumnValues.put(context.peek(), value);
			}
		}

	}
	
	// =====================
	// CONFIGURATION METHODS
	// =====================
	/**
	 * 
	 * @param level
	 */
	public void setDepthLevel(int level) {
		if (level <= 0) {
			throw new IllegalArgumentException("level must be > 0");
		}
		depthLevel = level;
		ROW_DEPTH_LEVEL = depthLevel + 1;
		ROW_ELEMENT_IDENTIFIER = "!"; //set the ROW_ELEMENT_NAME to an illegal name so that it never triggers
	}
	
	public void setRowElementName(String name) {
		if (depthLevel > 0) {
			throw new IllegalStateException("Can only set depthLevel or rowElementName, not both");
		}
		ROW_DEPTH_LEVEL = 1000; // set the ROW_DEPTH_LEVEL so deep that it never triggers
		ROW_ELEMENT_IDENTIFIER = name;
	}
	
	/**
	 * SEt true to keep the information from elder elements when flattening
	 * @param isKeepElder
	 */
	public void setKeepElderInfo(boolean isKeepElder) {
		this.isKeepElders = isKeepElder;
	}
	
	/**
	 * Set true to copy down all of elders information when flattening
	 * @param copyDown
	 */
	public void setCopyDown(boolean copyDown) {
		// You can't copy down elders info without keeping it, can you?
		this.isDoCopyDown = copyDown;
		if (copyDown) {
			this.isKeepElders = true;
		}
		
	}
	
	// ==============
	// SERVICE METHOD
	// ==============
	/* (non-Javadoc)
	 * @see gov.usgs.webservices.framework.formatter.AbstractFormatter#dispatch(javax.xml.stream.XMLStreamReader, java.io.Writer)
	 * 
	 * Note that namespaces are ignored.
	 */
	@SuppressWarnings("unchecked")
	public void dispatch(XMLStreamReader in, Writer out) throws IOException {
		ParseState state = new ParseState();
		
		try {
			// initialize the context stack to avoid empty stack errors
			state.context.push("");
			
			out.write(delims.sheetStart);
			done: while (true) {
				int event = in.next();
				switch (event) {
					case XMLStreamConstants.START_DOCUMENT:
						break; // no start document handling needed
					case XMLStreamConstants.START_ELEMENT:

						String localName = in.getLocalName();						

						state.startElementBeginUpdate(localName);				
						
						if (state.isTargetFound() && state.isInTarget){
							// PROCESS THE ELEMENT HEADERS
							// Read and record the column headers from the first row's elements.
							// Add columns for later rows, but they don't get headers because
							// we're streaming and can't go back to the column headers.
							state.addHeaderOrColumn(localName);

							// PROCESS/STORE ATTRIBUTE HEADERS AND NAME/VALUES
							int attCount = in.getAttributeCount();
							for (int i=0; i< attCount; i++) {
								String attLocalName = in.getAttributeLocalName(i);
								if (!"schemaLocation".equals(attLocalName)) {
									String fullName = makeFullName(state.context.peek(), attLocalName);
									Element att = new Element(fullName, attLocalName);
									state.targetColumnList.add(att);
									state.targetColumnValues.put(fullName, in.getAttributeValue(i).trim());
								}
							}
						} else if (isKeepElders && !state.isInTarget) {
							state.addElderHeaderOrColumn(localName);
							
							// PROCESS/STORE ATTRIBUTE HEADERS AND NAME/VALUES
							int attCount = in.getAttributeCount();
							for (int i=0; i< attCount; i++) {
								String attLocalName = in.getAttributeLocalName(i);
								if (!"schemaLocation".equals(attLocalName)) {
									String fullName = makeFullName(state.context.peek(), attLocalName);
									Element att = new Element(fullName, attLocalName);
									state.elderColumnList.add(att);
									state.elderColumnValues.put(fullName, in.getAttributeValue(i).trim());	
								}
							}
						}

						break;
					case XMLStreamConstants.CHARACTERS:

						state.putChars(in.getText().trim());

						break;
					// case XMLStreamConstants.ATTRIBUTE:
						// TODO may need to handle this later
					case XMLStreamConstants.END_ELEMENT:
						localName = in.getLocalName();
						
						// Write tag content
						boolean onTargetEnd = state.isOnTargetRowStartOrEnd(localName);
						if (onTargetEnd) {
							
							// OUTPUT HEADER row first, if not already done
							if (state.isProcessingHeaders) {
								// write out the columns headers first
								out.write(delims.headerRowStart);
								
								// preprocess to disambiguate common column headers
								if (isKeepElders) {
									updateQualifiedNames(state.elderColumnList, state.targetColumnList);
									//output the elder headers
									for (Element element: state.elderColumnList) {
										if (!element.hasChildren){// don't output elements with child elements
											out.write(delims.headerCellStart + element.qualifiedname + delims.headerCellEnd);
										}
									}
								} else {
									updateQualifiedNames(state.targetColumnList);
								}
								
								for (Element element: state.targetColumnList) {
									if (!element.hasChildren){// don't output elements with child elements
										out.write(delims.headerCellStart + element.qualifiedname + delims.headerCellEnd);
									}
								}
								out.write(delims.headerRowEnd);
								// bookkeeping
								state.isProcessingHeaders = false;
							}
							
							// OUTPUT DATA row only if there is content
							if (state.hasTargetContent()) {
								out.write(delims.bodyRowStart);
								if (isKeepElders) {
									for (Element element: state.elderColumnList) {
										if (!element.hasChildren){
											// don't output elements with child elements
											String value = state.elderColumnValues.get(element.fullName);
											value = (value != null)? value: "";
											out.write(delims.bodyCellStart + formatSimple(value) +  delims.bodyCellEnd);
										}
									}

									if (!isDoCopyDown) {
										// clear ALL the elder values
										state.elderColumnValues.clear();
									}
								}
								
								for (Element element: state.targetColumnList) {
									if (!element.hasChildren){
										// don't output elements with child elements
										String value = state.targetColumnValues.get(element.fullName);
										value = (value != null)? value: "";
										out.write(delims.bodyCellStart + formatSimple(value) +  delims.bodyCellEnd);
									}
								}
								out.write(delims.bodyRowEnd);
							}
						}
						state.finishEndElement(onTargetEnd);
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
	
	private void updateQualifiedNames(Set<Element>... columnLists) {
		Set<String> uniqueLocalNames = new HashSet<String>();
		Set<String> duplicates = new HashSet<String>();
		// First create a collection of the duplicates. We only care about the
		// childless ones, however.
		for (Set<Element> columnList: columnLists) {
			for (Element element: columnList) {
				if (!element.hasChildren) {
					boolean isUnique = uniqueLocalNames.add(element.localName);
					if (!isUnique) {
						duplicates.add(element.localName);
					}
				}
			}
		}

		// now go through and update the qualified name
		for (Set<Element> columnList: columnLists) {
			for (Element element: columnList) {
				if (!element.hasChildren) {
					if (duplicates.contains(element.localName)) {
						element.qualifiedname = URIUtils.parseQualifiedName(element.fullName);
					} else { // not a duplicate
						element.qualifiedname = element.localName;
					}
				}
			}
		}

	}

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

	private String makeFullName(String context, String name) {
		return (context.length() > 0)? context + URIUtils.SEPARATOR + name: name;
	}

	public boolean isNeedsCompleteFirstRow() {
		return true;
	}

}
