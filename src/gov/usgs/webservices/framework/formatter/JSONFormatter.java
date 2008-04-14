package gov.usgs.webservices.framework.formatter;


import java.io.IOException;
import java.io.Writer;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Convert an XMLStream to JSON. Following rules at
 * http://www.xml.com/pub/a/2006/05/31/converting-between-xml-and-json.html One
 * caveat is that no mixed content XML is allowed -- an element either has inner
 * text or element children, not both.
 * 
 * @author ilinkuo
 * 
 */
public class JSONFormatter extends AbstractFormatter implements IFormatter {

	protected Set<String> repeatedTags = new HashSet<String>();
	
	// =============
	// INNER CLASSES
	// =============
	public class XMLElement{
		String tagName;
		String parentName;
		String key;
		boolean isArray;
		boolean isArrayMemberOpen; // true -> false when corresponding end tag encountered
		boolean hasChildOrAttribs;
		boolean hasContent; // hasContent = hasChildOrAttribs || has text content
		
		// isStarted is applicable only for arrays. Indicates whether the first
		// member has been printed to stream yet.
		boolean isArrayStarted = false;

		public XMLElement(String parent, String tagName) {
			this.tagName = tagName;
			this.parentName = parent;
			this.key = key(parent, tagName);
			isArray = repeatedTags.contains(this.key);
			isArrayMemberOpen = isArray;
		}
		
		@Override public String toString() {
			return parentName + "|" + tagName;
		}
		
		public void closeElement() {
			hasChildOrAttribs = false;
			hasContent = false;
			isArrayMemberOpen = false;
		}

	}

	public class ParseState{
		Stack<XMLElement> parsed = new Stack<XMLElement>();
		boolean isDocumentStarted;
		boolean hasContent;
		{	// initializer
			parsed.add(new XMLElement("", ""));
		}

		public StringBuilder startElementBeginUpdate(String localName) {
			StringBuilder result = new StringBuilder();
			// make sure parsed is not empty
			XMLElement previous = parsed.peek();
			/* START ELEMENT CASES:
				 1) prev = plain element, new = plain element // prev =  parent
				 	, tagName : { push, 
				 2) prev = plain element, new = array element // prev = parent
				 	, tagName : [ { push
				 3) prev = array element open, new = array member // prev = parent
				 	, tagName : [ push
				 4) prev = array element closed, new = plain element // prev = sibling
				 	pop ] , tagName : { push
				 5) prev = array element closed, new = different array element // prev = sibling
				 	pop ] , tagName : [ { push
				 6) prev = array element open, new = nested array element // prev = parent
				 	, tagName : [ { push
			 */
			if (previous.isArray) {
				if (localName.equals(previous.tagName)) {
					// CASE 3: is member of array					
					// Close the previous array member.
					previous.closeElement();
					result.append(", ");
					// Open the new array member
					previous.isArrayMemberOpen = true;
				} else { // CASE 4, 5 & 6: previous element is an array
					String parentName = null;
					if (previous.isArrayMemberOpen) {
						// CASE 6
						parentName = previous.tagName;
					} else {
						// CASE 4 & 5: Close the previous array
						// Previous is array closed, prev = sibling
						parsed.pop();
						result.append("]");
						parentName = previous.parentName;
					}
					assert(parsed.peek().tagName.equals(parentName)): "top of stack is parent of current";
					result.append(pushElement(localName, parentName));
				}
			} else {
				// CASE 1 & 2
				String parentName = previous.tagName;
				assert(parsed.peek().tagName.equals(parentName)): "top of stack is parent of current";
				result.append(pushElement(localName, parentName));
			}
			isDocumentStarted = true;
			return result;
		}
		
		public StringBuilder endElementUpdate(String localName) {
			StringBuilder result = new StringBuilder();
			// make sure parsed is not empty
			XMLElement previous = parsed.peek();
			/*
			 * END ELEMENT CASES: (Note, only array previous can be unequal!)
			 * 	1) plain prev = plain current	// plain element closed
			 * 		} pop
			 * 	2) array prev = array current	// array member closed but not necessarily array
			 * 		} don't pop
			 * 	3) plain prev != plain current
			 * 		ERROR: unclosed prev tag
			 * 	4) plain prev != array current
			 * 		error: unclosed prev tag
			 * 	5) array prev != plain current // current = parent of prev
			 * 		] pop } pop 
			 * 	6) array prev != array current	// nested array, current = parent of prev, prev.isArrayMemberOpen = false, current.isArrayMemberOpen = true
			 * 		] pop } don't pop
			 */
			XMLElement current = null;
			if (localName.equals(previous.tagName)) {
				// CASE 1 & 2
				current = previous;
				
				// Close, outputting closing bracket if there is non-text content, js null
				// if no content, no closing char if text content only
				result.append((previous.hasChildOrAttribs)? "}": (previous.hasContent)? "": "null");
				
			} else { // CASE 5 & 6: prev array != current
				// Close and pop the unclosed child array
				result.append("] }");
				parsed.pop(); 
				current = parsed.peek();
			}
			assert(current.tagName.equals(localName)) : "The top of the stack is the current end tag.";
			
			if (current.isArray) {
				// CASE 6 nested array or CASE 2: array member
				assert(current.isArrayMemberOpen): "The current element/array should be open at this point.";
				current.closeElement();
				// don't pop as array may not be closed
			} else {
				// CASE 5 or CASE 1: current member is not an array, so just close object
				parsed.pop();
			}
			return result;
		}

		private StringBuilder pushElement(String localName, String parentName) {
			// assert precondition: top of stack = parent of current.
			assert(parsed.peek().tagName.equals(parentName)): "parent is at top of stack";
			
			StringBuilder result = new StringBuilder();
			XMLElement current = new XMLElement(parentName, localName);
			XMLElement parent = parsed.peek();
			
			if (isDocumentStarted) {
				result.append((parent.hasChildOrAttribs)? ",": "{");
				// Mark the parent as nonempty
				parent.hasChildOrAttribs = true;
				parent.hasContent = true;
			}
			
			result.append(" \"").append(localName).append("\": ");
			if (current.isArray) {
				// CASE 2 & 5
				result.append("[");
			}			
			parsed.push(current);
			return result;
		}
	}

	// ===========
	// CONSTRUCTOR
	// ===========
//	protected JSONFormatter(XMLStreamReader reader) {
//		super(OutputType.JSON);
//		this.xsReader = reader;
//	}

	public JSONFormatter() {
		super(OutputType.JSON);
	}
	
	// =======================
	// PUBLIC STATIC METHODS
	// =======================
	public static String key(String tagParent, String tagName) {
		return tagParent + "|" + tagName;
	}
	
	public static final Pattern quotesEscape= Pattern.compile("\"");
	public static final Pattern forwardSlashEscape= Pattern.compile("\\");
	public static String escapeForJSString(String value) {
		String result = forwardSlashEscape.matcher(value).replaceAll("\\\\");
		result = quotesEscape.matcher(value).replaceAll("\\\"");
		return result;
	}

	// =======================
	// PUBLIC INSTANCE METHODS
	// =======================
	/**
	 * Tags which may be repeated must be identified to the JSONFormatter so
	 * that it knows to serialize it as an array object rather than a name-value
	 * hash entry.
	 * 
	 * @param tagName
	 * @param tagParent
	 *            [optional]
	 */
	public void identifyRepeatedTagElement(String tagName, String tagParent) {
		assert(tagName != null): "tagName is required";
		tagParent = (tagParent == null)? "": tagParent;
		repeatedTags.add(key(tagParent, tagName));
	}

	@Override
	public void dispatch(XMLStreamReader in, Writer out) throws IOException {
		ParseState state = new ParseState();
		try {
			done: while (true) {
				int event = in.next();
				switch (event) {
					case XMLStreamConstants.START_DOCUMENT:
						break; // no start document handling needed
					case XMLStreamConstants.START_ELEMENT:
					{
						String localName = in.getLocalName();						

						StringBuilder output = state.startElementBeginUpdate(localName);
						XMLElement current = state.parsed.peek();
						int attCount = in.getAttributeCount();
						for (int i = 0; i< attCount; i++) {
							output.append((i == 0)? "{ ": ", ");
							output.append("\"@").append(in.getAttributeLocalName(i)).append("\": \"");
							output.append(escapeForJSString(in.getAttributeValue(i))).append('"');
							current.hasChildOrAttribs = true;
							current.hasContent = true;
						}
						out.write(output.toString());
					}
					break;
					case XMLStreamConstants.CHARACTERS:
						String text = in.getText().trim();
						if (text.length() > 0) {
							XMLElement current = state.parsed.peek();
							if (current.hasChildOrAttribs) {
								out.write(", \"#text\": \"" + escapeForJSString(text) + '"');
							} else {
								// assume that no children follow -- no mixed content xml
								out.write("\"" + escapeForJSString(text) + '"');
								current.hasContent = true;
							}
						}
						break;
					case XMLStreamConstants.END_ELEMENT:
					{
						String localName = in.getLocalName();						
						StringBuilder output = state.endElementUpdate(localName);
						out.write(output.toString());
					}
					break;
					case XMLStreamConstants.END_DOCUMENT:
						break done;
				}
			}

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

}
