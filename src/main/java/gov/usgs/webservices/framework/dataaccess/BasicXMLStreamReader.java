package gov.usgs.webservices.framework.dataaccess;

import gov.usgs.webservices.framework.utils.UsgsStAXUtils;
import gov.usgs.webservices.framework.utils.XMLUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * Abstract template for an event-based implementation of XMLStreamReader (too
 * many methods) to easily create XMLStreamReaders. "Basic" means paying
 * attention only to elements and attributes -- ignoring PIs, comments, limited
 * namespace support etc. Namespaces are supported only for attributes, not
 * elements, and it is assumed that attribute local names are unique within a
 * single open tag event, and that all elements are either in no namespace or a
 * single namespace. Space is paid attention to in only a very limited fashion,
 * as a single carriage return in order that the resulting XML does not all
 * appear on a single line (to avoid violating line length constraints).
 *
 * As a consequence, the only StAX events returned by this reader are:
 * StartDocument, StartElement, EndElement, EndDocument, characters, space
 *
 * TODO make ignore namespaces an option??
 *
 * @author ilinkuo
 *
 */
public abstract class BasicXMLStreamReader implements XMLStreamReader {
	protected static final int NOT_YET_BEGUN_PARSING = 0;
	// For simplicity, we assume a 1-1 mapping between namespaces and prefixes
	protected Map<String, String> namespacePrefixes;
	// defaultNamespace really exists at the scope of the tag, but we're simplifying here.
	protected String defaultNamespace = XMLConstants.DEFAULT_NS_PREFIX;

	// current parse state variables
	protected Queue<BasicTagEvent> events = new LinkedList<BasicTagEvent>();
	protected BasicTagEvent currentEvent;
	protected ResultSet _rset;
	protected boolean isStarted;
	protected boolean isEnded;
	protected boolean isDataDone;
	protected int startElementCount;

	// OPTIMIZATION
	protected Map<String, String> attributeNameCache = new HashMap<String, String>();


	public static class Attribute{
		public String name;
		public String value;

		public Attribute(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public boolean isEmpty() {
			return (name == null && value == null);
		}
	}


	// ==========================
	// TEMPLATE METHOD (implements underlying event handling)
	// ==========================

	public boolean hasNext() throws XMLStreamException {
		if (currentEvent != null && currentEvent.hasNext()) {
			return true;
		} else if (events.isEmpty()) {
			readNext();
		}
		return !events.isEmpty();
	}

	/**
	 * This is the most important method to override. It should populate the
	 * event queue but not touch currentEvent.
	 *
	 * @throws XMLStreamException
	 */
	public void readNext() throws XMLStreamException {
		try {
			if (_rset != null) {
				if (_rset.next()) {
					if (_rset.isFirst()) {
						documentStartAction();
					}
					rowStartAction();

					readRow(_rset);

					rowEndAction();
				} else {
					// document has started and there is no more next row, so it must be the end of the document
					if (isStarted) {
						documentEndAction();
					}


					ResultSet resultSet = _rset;
					_rset = null;
					resultSet.close();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new XMLStreamException(e);
		}
	}

	// These may be overridden as necessary
	protected BasicTagEvent documentStartAction() {
		isStarted = true;
		return null;
	}
	protected BasicTagEvent rowStartAction() throws SQLException { return null;}
	protected void readRow(@SuppressWarnings("unused") ResultSet rset) throws SQLException {}
	protected void rowEndAction() {}
	protected void documentEndAction() {
		isEnded = true;
	}

	public int nextTag() throws XMLStreamException {
		int currentState = NOT_YET_BEGUN_PARSING;
		do {
			if (currentEvent != null && currentEvent.hasNext()) {
				currentEvent.next();
			} else {
				if (events.isEmpty()) {
					readNext();
				}
				currentEvent = events.poll();
			}
			if (currentEvent == null) {break;}
			currentState = currentEvent.type;
		} while (currentState != NOT_YET_BEGUN_PARSING
				&& currentState!= XMLStreamConstants.START_ELEMENT
				&& currentState != XMLStreamConstants.END_ELEMENT);

		if (currentState == NOT_YET_BEGUN_PARSING) {
			assert(false);	// this shouldn't happen, or rather, it happens
			// only when there were never any events in the queue.
			// IK: Throwing an exception here seems extremely unfriendly,
			// but that's what the StAX documentation says I should do.
			throw new XMLStreamException("expected start or end tag");
		} else if (currentState == END_DOCUMENT){
			// all done. throw an exception according to XMLStreamReader javadocs
			throw new XMLStreamException("no more tags.");
		}
		return currentState;
	}

	public int next() throws XMLStreamException{
		// check combo events first before checking queue.
		if (currentEvent != null && currentEvent.hasNext()) {
			return trackReturnState(currentEvent.next());
		}
		if (hasNext()) {
			currentEvent = events.poll();
			return trackReturnState(currentEvent.type);
		}
		throw new IllegalStateException("next() cannot be called when stream is empty");
	}

	protected int trackReturnState(int i) {
		if (i == XMLStreamConstants.START_ELEMENT) {
			startElementCount++;
		}
		return i;
	}

	// ================
	// UTILITY METHODS
	// ================

	/**
	 * Returns a combo open tag, content, closed tag event if tagContent is not
	 * null. Note that all makeXXX() methods do not add to the events queue.
	 *
	 * @param tagName
	 * @param tagContent
	 */
	public BasicTagEvent makeNonNullBasicTag(String tagName, String tagContent) {
		return (tagContent == null)? null : new BasicTagEvent(tagName, tagContent);
	}

	/**
	 * Returns a combo tag if either tagContent is not null or attributes not
	 * empty. Note that all makeXXX() methods do not add to the events queue.
	 *
	 * @param tagName
	 * @param tagContent
	 * @param attributes
	 * @return
	 */
	public BasicTagEvent makeNonNullBasicTag(String tagName, String tagContent, Attribute... attributes ) {
		BasicTagEvent result = null;
		if (hasContent(tagContent, attributes)) {
			result = new BasicTagEvent(tagName, tagContent);
			for (Attribute att: attributes) {
				if (att != null && !att.isEmpty()) {
					result.addAttribute(att.name, att.value);
				}
			}
		}
		return result;
	}

	/**
	 * Returns a combo tag if tagContent is not null. The existence or
	 * nonexistence of attributes does not affect whether a null is returned,
	 * only the form of the returned tag. It's similar to
	 * makeNonNullBasicTag(String tagName, String tagContent) except that
	 * attributes are allowed. Note that all makeXXX() methods do not add to the
	 * events queue.
	 *
	 * @param tagName
	 * @param tagContent
	 * @param attributes
	 * @return
	 */
	public BasicTagEvent makeNonNullBasicTagIgnoreAttributes(String tagName, String tagContent, Attribute... attributes ) {
		return (tagContent != null)? makeNonNullBasicTag(tagName, tagContent, attributes): null;
	}

	/**
	 * Adds an open tag, content, closed tag event to the event stream if
	 * tagContent is not null. Note that all addXXX() methods add to the events
	 * queue immediately hence return void
	 *
	 * @param tagName
	 * @param tagContent
	 */
	public void addNonNullBasicTag(String tagName, String tagContent) {
		if (tagContent != null) {
			events.add(new BasicTagEvent(tagName, tagContent));
		}
	}

	/**
	 * Adds an open tag, content, closed tag event to the event stream, even
	 * if the tagConent is null. Note that all addXXX() methods add to the events
	 * queue immediately hence return void
	 *
	 * @param tagName
	 * @param tagContent
	 */
	public void addBasicTag(String tagName, String tagContent) {
		events.add(new BasicTagEvent(tagName, tagContent));
	}
	
	/**
	 * Adds an open tag, content, closed tag event to the event stream if
	 * tagContent or attributes are not null. Note that all addXXX() methods add
	 * to the events queue immediately hence return void
	 *
	 * @param tagName
	 * @param tagContent
	 */
	public void addNonNullBasicTag(String tagName, String tagContent, Attribute... attributes ) {
		if (hasContent(tagContent, attributes)) {
			BasicTagEvent result = new BasicTagEvent(tagName, tagContent);
			for (Attribute att: attributes) {
				if (att != null && !att.isEmpty()) {
					result.addAttribute(att.name, att.value);
				}
			}
			events.add(result);
		}
	}

	/**
	 * Adds an open tag, content, closed tag event to the event stream if
	 * tagContent is not null. The existence or nonexistence of attributes does
	 * not affect whether a null is returned, only the form of the returned tag.
	 * It is similar to makeNonNullBasicTagIgnoreAttributes(). Note that all
	 * addXXX() methods add to the events queue immediately hence return void
	 *
	 * @param tagName
	 * @param tagContent
	 */
	public void addNonNullBasicTagIgnoreAttributes(String tagName, String tagContent, Attribute... attributes ) {
		if (tagContent != null) {
			addNonNullBasicTag(tagName, tagContent, attributes);
		}
	}

	protected boolean hasContent(String tagContent, Attribute... attributes) {
		boolean hasContent = tagContent != null;
		if (!hasContent) {
			for (Attribute att: attributes) {
				if (att != null && !att.isEmpty()) return true;
			}
		}
		return hasContent;
	}

	/**
	 * Adds a tag to the events queue if all the child elements are non-null.
	 * Note that all addXXX() methods add to the events queue immediately hence
	 * return void
	 *
	 * @param tagName
	 * @param basicTagEvents
	 */
	public void addNonNullTag(String tagName, BasicTagEvent...basicTagEvents ) {
		boolean hasContent = false;
		// check that at least one non-null event exists
		for (BasicTagEvent event: basicTagEvents) {
			if (event != null) {
				hasContent = true;
				break;
			}
		}
		if (!hasContent) { return;}
		// now we can write the tag and its contents
		addOpenTag(tagName);
		for (BasicTagEvent event: basicTagEvents) {
			if (event != null) {
				events.add(event);
			}
		}
		addCloseTag(tagName);
	}

	/**
	 * Adds a close tag (END_ELEMENT) to the events queue. Note that all
	 * addXXX() methods add to the events queue immediately hence return void
	 *
	 * @param tagName
	 */
	public void addCloseTag(String tagName) {
		events.add(new BasicTagEvent(END_ELEMENT, tagName));
	}

	/**
	 * Adds a open tag (START_ELEMENT) to the events queue. Note that all addXXX() methods add to the events queue immediately
	 * @param tagName
	 */
	public void addOpenTag(String tagName) {
		events.add(new BasicTagEvent(START_ELEMENT, tagName));
	}

	// ================
	// UTILITY METHODS (trimmed versions)
	// ================
	/**
	 * Adds an open tag, content, closed tag event to the event stream if trimmed tagContent is not null
	 * @param tagName
	 * @param tagContent
	 */
	public void addTrimmedNonNullBasicTag(String tagName, String tagContent) {
		if (tagContent != null) {
			tagContent = tagContent.trim();
			if (tagContent.length() != 0) {
				events.add(new BasicTagEvent(tagName, tagContent));
			}
		}
	}

	/**
	 * Returns a combo open tag, content, closed tag event to the event stream if trimmed tagContent is not null
	 * @param tagName
	 * @param tagContent
	 */
	public BasicTagEvent makeTrimmedNonNullBasicTag(String tagName, String tagContent) {
		if (tagContent != null) {
			tagContent = tagContent.trim();
			if (tagContent.length() != 0) {
				return new BasicTagEvent(tagName, tagContent);
			}
		}
		return null;
	}

	// =======================================
	// (must override) XMLStreamReader METHODS
	// =======================================
	/*
	 * Note that while the abstract declarations below are syntactically
	 * unnecessary (methods are already required by the XMLStreamReader
	 * interface), they are placed here to provide a guide for what probably
	 * needs to be overridden
	 *
	 */
	public abstract void close() throws XMLStreamException;
	public String getAttributeLocalName(int index) {
		return currentEvent.getAttributeLocalName(index);
	}
	public String getAttributeValue(int index) {
		return currentEvent.getAttributeValues(index);
	}
	public String getAttributeValue(String namespaceURI, String localName) {
		return currentEvent.getAttribute(localName);
	}
	public String getAttributeNamespace(int index) {
		return currentEvent.getAttributeNamespace(index);
	}

	public String getAttributePrefix(int index) {
		return currentEvent.getAttributePrefix(index);
	}
	// =================================================
	// (maybe override) XMLStreamReader METHODS
	// =================================================
	public int getEventType() {
		if (currentEvent != null) {
			return currentEvent.type;
		}
		try {
			return next();
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String getLocalName() {
		XMLStreamReaderMethod.getLocalName.check(currentEvent.type);
		return currentEvent.eventName;
	}

	public Location getLocation() {
		return UsgsStAXUtils.defaultLocation;	// location ignored by Basic reader
	}
	public boolean isAttributeSpecified(int index) {
		return false; // assume all attributes created explicitly, not be default as this is not schema aware
	}
	public String getAttributeType(int index) {
		return XMLConstants.DEFAULT_NS_PREFIX; // attribute type ignored by BasicXMLStreamReader as it is not schema/type aware
	}

	public QName getAttributeName(int index) {
		// namespaces ignored by Basic reader, so this is just local name
		return new QName(XMLConstants.DEFAULT_NS_PREFIX, getAttributeLocalName(index), XMLConstants.DEFAULT_NS_PREFIX);
	}

	public boolean isStandalone() {
		return true; // assume no external DTDs or schemas referenced
	}
	public boolean standaloneSet() {
		return true; // assume no external DTDs or schemas referenced
	}
	public QName getName() {
		// namespaces ignored by Basic reader, so this is just local name
		return new QName(XMLConstants.DEFAULT_NS_PREFIX, currentEvent.eventName, XMLConstants.DEFAULT_NS_PREFIX);
	}




	// ========================================
	// FULLY IMPLEMENTED XMLStreamReader METHODS (no need to implement)
	// ========================================
	public final boolean isEndElement() {
		return currentEvent.type == XMLStreamConstants.END_ELEMENT;
	}
	public final boolean isStartElement() {
		return currentEvent.type == XMLStreamConstants.START_ELEMENT;
	}

	public final boolean hasName() {
		return currentEvent.type == XMLStreamConstants.START_ELEMENT
				|| currentEvent.type == XMLStreamConstants.END_ELEMENT;
	}

	public final int getAttributeCount() {
		XMLStreamReaderMethod.getAttributeCount.check(currentEvent.type);
		return currentEvent.getAttributeCount();
	}
	
	/**
	 * UTF-8 is a good assumption - implementations are free to override.
	 */
	public String getCharacterEncodingScheme() {
		return "UTF-8";
	}

	public String getEncoding() {
		// Should I return the default java encoding? nah.
		return null;
	}


	// ============================
	// XMLStreamReader TEXT METHODS
	// ============================
	public String getText() {
		if (currentEvent != null) {
			return currentEvent.tagContent;
		}
		return null;
	}

	public char[] getTextCharacters() {
		if (currentEvent != null) {
			return currentEvent.tagContent.toCharArray();
		}
		return null;
	}

	public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
		throw new UnsupportedOperationException("not implemented");
	}

	public int getTextLength() {
		if (currentEvent != null) {
			return currentEvent.tagContent.length();
		}
		return 0;
	}

	public int getTextStart() {
		XMLStreamReaderMethod.getTextStart.check(currentEvent.type);
		throw new UnsupportedOperationException("not implemented");
	}

	public boolean hasText() {
		int currentState = currentEvent.type;
		return currentState == XMLStreamConstants.CHARACTERS
		|| currentState == XMLStreamConstants.DTD
		|| currentState == XMLStreamConstants.ENTITY_REFERENCE
		|| currentState == XMLStreamConstants.COMMENT
		|| currentState == XMLStreamConstants.SPACE;
	}
	public boolean isCharacters() {
		if (currentEvent != null) {
			return currentEvent.type == CHARACTERS;
		}
		return false;
	}
	public boolean isWhiteSpace() {
		// not keeping track of whitespace now
		return false;
	}

	public String getElementText() throws XMLStreamException {
		if (!XMLStreamReaderMethod.getElementText.isAllowed(currentEvent.type)) {
			throw new XMLStreamException();
		}
		throw new UnsupportedOperationException("not implemented");
	}
	// =======================
	// PRIVATE UTILITY METHODS
	// =======================
	protected String makeErrorMessage(String methodName) {
		return makeErrorMessage(methodName, UsgsStAXUtils.eventNames.get(currentEvent.type));
	}

	protected String makeErrorMessage(String methodName, String stateName) {
		StringBuffer sb = new StringBuffer(methodName);
		sb.append("() may not be called when current state is ")
			.append(stateName);
		return sb.toString();
	}

	// =================
	// NAMESPACE METHODS
	// =================
	public void setDefaultNamespace(String namespace) {
		if (startElementCount > 0) {
			throw new IllegalStateException("Cannot set defaultNamespace after first element has been returned.");
		}
		this.defaultNamespace = namespace;
	}

	public void addNamespace(String namespace, String prefix) {
		if (prefix.indexOf(':') >= 0) {
			throw new IllegalArgumentException("illegal prefix: " + prefix);
		}
		if (startElementCount > 0) {
			throw new IllegalStateException("Cannot add namespaces after first element has been returned.");
		}
		if (namespacePrefixes == null) {
			namespacePrefixes = new LinkedHashMap<String, String>();
		}

		namespacePrefixes.put(namespace, prefix);
	}

	/*
	 * @see javax.xml.stream.XMLStreamReader#getNamespaceCount()
	 */
	public int getNamespaceCount() {
		// Note that it's not clear from the docs what this should return when
		// the default namespace is also one of the prefixed namespaces. From
		// examining the output of a STAXUtils.createXMLStreamReader(new
		// StringReader(ATTRIBUTE_NAMESPACE_OUTPUT)) call, it's determined that
		// this is a simple default namespace + # prefixed namespaces without
		// determining if there is duplication
		if (startElementCount > 1) {
			// As a simplification, namespaces are ignored after the first start
			// element in the stream is returned.
			return 0;
		}
		int namespacePrefixCount = (namespacePrefixes == null)? 0: namespacePrefixes.size();
		return (hasDefaultNamespace())? namespacePrefixCount + 1: namespacePrefixCount;
//		return 0;
	}

	public boolean hasDefaultNamespace() {
		return !(defaultNamespace == null || defaultNamespace == XMLConstants.DEFAULT_NS_PREFIX);
	}

	/* (non-Javadoc)
	 * @see javax.xml.stream.XMLStreamReader#getNamespaceURI()
	 */
	public String getNamespaceURI() {
		return defaultNamespace;
	}

	public String getNamespacePrefix(int index) {
		if (hasDefaultNamespace()) {
			// the defaultNamespace is assigned to index 0, if it exists.
			index--;
		}
		if (index < 0) {
			return XMLConstants.DEFAULT_NS_PREFIX; // for the default namespace
		}
		if (namespacePrefixes != null) {
			// iterate throught the namespaces. Not the most efficient, but it shouldn't matter much.
			int i=0;
			for (Map.Entry<String, String> entry: namespacePrefixes.entrySet()) {
				if (index == i) {
					return entry.getValue();
				}
				i++;
			}
		}
		return XMLConstants.DEFAULT_NS_PREFIX; // namespaces ignored by Basic reader
	}
	public NamespaceContext getNamespaceContext() {
		throw new UnsupportedOperationException("getNamespaceContext() needs to be implemented");
//		return UsgsStAXUtils.defaultNSContext;	// namespaces ignored by Basic reader
	}

	public String getNamespaceURI(String prefix) {
		return XMLConstants.DEFAULT_NS_PREFIX; // namespaces ignored by Basic reader
	}

	public String getNamespaceURI(int index) {
		if (hasDefaultNamespace()) {
			// the defaultNamespace is assigned to index 0, if it exists.
			index--;
		}
		if (index < 0) {
			return defaultNamespace; // for the default namespace
		}
		if (namespacePrefixes != null) {
			// iterate throught the namespaces. Not the most efficient, but it shouldn't matter much.
			int i=0;
			for (Map.Entry<String, String> entry: namespacePrefixes.entrySet()) {
				if (index == i) {
					return entry.getKey();
				}
				i++;
			}
		}
		return XMLConstants.DEFAULT_NS_PREFIX; // namespace not found
	}

	public String getPrefix() {
		return null; // prefixes ignored by Basic reader
	}

	public void require(int type, String namespaceURI, String localName)
			throws XMLStreamException {
		// namespaces ignored by Basic reader
	}


	// ========================================
	// (Unimplemented)XMLStreamReader METHODS
	// ========================================
	public Object getProperty(String name) throws IllegalArgumentException {
		if (name == null) {
			throw new IllegalArgumentException("Property name may not be null");
		}
		// no properties defined for this basic implementation, but has future potential
		return null;
	}


	// ==================================================
	// (Unused/ignored) XMLStreamReader MISCELLANEOUS METHODS
	// ==================================================
	public String getPIData() {
		return null; // PIs ignored
	}

	public String getPITarget() {
		return null; // PIs ignored
	}

	public String getVersion() {
		return null; // version ignored
	}

	// =======================
	// INNER CLASS
	// =======================
	public class BasicEvent extends BasicTagEvent{
		List<String> attributeAliases;
		Map<String, String> attributesByAlias;

		public BasicEvent(int type){
			super(type);
			this.attributeAliases = new ArrayList<String>();
			this.attributesByAlias = new HashMap<String, String>();
		}

		public BasicEvent(int type, String name){
			super(type, name);
			this.attributeAliases = new ArrayList<String>();
			this.attributesByAlias = new HashMap<String, String>();
		}

		public BasicEvent(String name, String content){
			super(name, content);
			this.attributeAliases = new ArrayList<String>();
			this.attributesByAlias = new HashMap<String, String>();
		}

		@Override
		public BasicEvent addAttribute(String name, String value) {
			super.addAttribute(name, value);
			// --------------------------------
			// optimization code for attributes
			// --------------------------------
			String key = attributeNameCache.get(name);
			if (key == null) {
				// TODO this may not belong here, specific to BasicResultsetReader
				key = XMLUtils.xmlFullSanitize(name);
				attributeNameCache.put(name, key);
			}
			attributeAliases.add(key);
			attributesByAlias.put(key, value);
			return this;
		}

		@Override
		public String getAttribute(String name) {
			String result = attributesByAlias.get(name);
			if (result == null) {
				result = attributesByName.get(name);
			}
			return result;
		}

		@Override
		public String getAttributeLocalName(int i) {
			return attributeAliases.get(i);
		}
	}

}
