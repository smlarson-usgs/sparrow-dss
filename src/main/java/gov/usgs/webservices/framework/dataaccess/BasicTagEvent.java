package gov.usgs.webservices.framework.dataaccess;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;

/**
	 * This is a basic event for those documents where we don't care about
	 * non-tag items such as comments, PIs, etc. This is capable of representing
	 * a start tag, a close tag, or a combination (start tag, character content,
	 * close tag). It is also necessary to represent a whitespace event in
	 * order that the resulting xml does not run afoul of linelength limitations.
	 *
	 * @author ilinkuo
	 *
	 */
	public class BasicTagEvent {
		public int type;
		public String eventName;
		List<String> attributeValues;
		List<String> attributeNamespaces;
		List<String> attributePrefixes;
		List<String> attributeNames;

		// consider using a case-insensitive lookup
		Map<String, String> attributesByName;

		// ComboEvent-related attributes
		protected int state; // this should be set by ComboEvents
		protected int MAX_STATE; // this should be set by Combo Events
		protected boolean isComboTagEvent;
		String tagContent;

		// ------------
		// CONSTRUCTORS
		// ------------
		/**
		 * Creates an unnamed BasicTagEvent
		 * @param type
		 */
		public BasicTagEvent(int type) {
		this.type = type;
		if (type != SPACE) {
			initializeAttributes();
		} else {
			tagContent = "\n";
		}
	}

		/**
		 * Creates a named BasicTagEvent for START_ELEMENT or END_ELEMENT
		 * SPACE elements are created using single argument version.
		 * CHARACTERS are created as part of combo tag;
		 * @param type
		 * @param name
		 */
		public BasicTagEvent(int type, String name){
			assert(type == START_ELEMENT || type == END_ELEMENT );
			this.eventName = name;
			this.type = type;
			initializeAttributes();
		}

		/**
		 * Creates a combo event (start tag, char content, end tag)
		 * @param name
		 * @param content
		 */
		public BasicTagEvent(String name, String content){

			this(START_ELEMENT ,name);
			// This creates a combination tag: start tag, String tag content, end tag
			// combination tag
			isComboTagEvent = true;
			tagContent = content;
			if (tagContent == null) {
				tagContent = ""; // null String for CHARACTER event causes problems.
			}
			// 1 for start, 2 for char content, 3 for end
			state = 1;
			MAX_STATE = 3;
		}

		/**
		 * addAttribute() adds a non-null attribute-value pair
		 * @param name cannot be null
		 * @param value
		 */
		public BasicTagEvent addAttribute(String name, String value) {
			assert(name != null && (isComboTagEvent || type == START_ELEMENT));
			value = StringUtils.trimToNull(value);
			return addAttribute(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.DEFAULT_NS_PREFIX, name, value);
		}

		private void initializeAttributes() {
			this.attributeValues = new ArrayList<String>();
			// consider postponing the building of maps and etc. until necessary.
			this.attributeNamespaces = new ArrayList<String>();
			this.attributePrefixes = new ArrayList<String>();
			this.attributeNames = new ArrayList<String>();
			this.attributesByName = new HashMap<String, String>();
		}

		/**
		 * addAttribute() adds a non-null attribute-value pair, with namespace
		 * @param namespace cannot be null, but may be XMLConstants.NULL_NS_URI
		 * @param name cannot be null
		 * @param value
		 */
		public BasicTagEvent addAttribute(String prefix, String namespace, String name, String value) {
			assert(namespace != null && name != null);
			if (value != null) {
				if (attributeValues == null) {
					initializeAttributes();
				}
				attributeNamespaces.add(namespace);
				attributePrefixes.add(prefix);
				attributeValues.add(value);
				//
				attributeNames.add(name);
				attributesByName.put(name, value);
			}
			return this;
		}

		// -----------------------

		/**
		 * ComboTag events should override next()
		 * @return
		 */
		public int next() {
			switch (state) {
				case 1:
					state++;
					type = CHARACTERS;
					return type;
				case 2:
					state++;
					type = END_ELEMENT;
					return type;
				default:
					// covers 0 = not a combo tag, and 3 = end
					// nothing happens. already in end state
			}
			throw new IllegalStateException("cannot call next on ComboEvent if already in end state");

		}

		/**
		 * Combo events should override hasNext()
		 * @return
		 */
		public boolean hasNext() {
			return state > 0 && state < MAX_STATE; // the end tag does not have a next event
		}

		public boolean isEmpty() {
			if (!isComboTagEvent) {
				throw new UnsupportedOperationException("isEmpty() method may not be called on non combo tag ");
			}
			return (tagContent == null || tagContent.length() == 0);
		}

		/*
		 * Note that Combo Events allow access to attributes even if CHAR or END tag.
		 */
		public String getAttributeValues(int i) {
			return attributeValues.get(i);
		}

		public String getAttribute(String name) {
			return attributesByName.get(name);
		}

		public String getAttributeLocalName(int i) {
			return attributeNames.get(i);
		}

		public String getAttributeNamespace(int i) {
			return attributeNamespaces.get(i);
		}

		public String getAttributePrefix(int i) {
			return attributePrefixes.get(i);
		}

		public int getAttributeCount() {
			return attributeValues.size();
		}


	}