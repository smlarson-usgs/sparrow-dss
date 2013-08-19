package gov.usgs.webservices.framework.utils;

import static gov.usgswim.sparrow.util.ParserHelper.parseToStartTag;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Ingests XML and can return child/grandchildren as Strings or Objects or Lists of Strings or Objects
 * @author ilinkuo
 *
 */
public class SmartXMLProperties implements Map<String, String>{


	// ==============
	// STATIC METHODS
	// ==============
	public static boolean isCompoundKey(String key) {
		return key.indexOf('.') > 0;
	}

	public static String parseListKey(String compoundKey) {
		if (isCompoundKey(compoundKey)) {
			 return compoundKey.split("\\.")[0];
		}
		return null;
	}

	public static String parseItemKey(String simpleOrCompoundKey) {
		if (isCompoundKey(simpleOrCompoundKey)) {
			 return simpleOrCompoundKey.split("\\.")[1];
		}
		return simpleOrCompoundKey;
	}

	// ======================
	// INSTANCE MEMBER FIELDS
	// ======================
	protected Map<String, String> props = new LinkedHashMap<String, String>();
	protected Map<String, String> nodes = new LinkedHashMap<String, String>();
	protected Map<String, Map<String, String>> maps = new HashMap<String, Map<String, String>>();

	// ====================
	// INSTANCE GET METHODS
	// ====================
	public String get(String key) {
		if (key == null) return null;
		if (isListKey(key)) {
			return getListAsFullXMLNode(key);
		}
		return props.get(key);
//		String listKey = parseListKey(simpleOrCompoundKey);
//		return getListAsFullXMLNode(listKey);
	}
	public String getAsFullXMLNode(String key) {
		if (key == null) return null;
		if (isListKey(key)) {
			return getListAsFullXMLNode(key);
		}
		String content = props.get(key);
		if (content == null) return null;
		if (isCompoundKey(key)) {
			String listName = parseListKey(key);
			Map<String, String> map = maps.get(listName);
			if (map != null && !map.isEmpty()) {
				String itemKey = parseItemKey(key);
				return map.get(itemKey);
			}
		}
		return "<" + key + ">" + content + "</" + key + ">";

	}

	private String getListAsFullXMLNode(String listKey) {
		Map<String, String> map = maps.get(listKey);
		StringBuilder result = new StringBuilder();
		if (map != null && !map.isEmpty()) {
			for (Entry<String, String> entry: map.entrySet()) {
				result.append(entry.getValue());
			}
		}
		if (result.length() > 0) {
			result.insert(0, "<" + listKey + ">");
			result.append("</" + listKey + ">");
		}
		return result.toString();
	}

	public Set<String> listKeySet() {
		return maps.keySet();
	}

	public Map<String, String> getListAsMap(String listKey){
		return maps.get(listKey);
	}
//
//	public Object getAsObject(Class<?> itemClass) {
//
//	}


	public boolean isListKey(String key) {
		return maps.containsKey(key);
	}

	// =============
	// PARSE METHODS
	// =============
	public void parse(String xml) throws XMLStreamException, XMLParseValidationException {
		// TODO replace this by SourceToStreamConverter calls
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(xml));
		parse(reader);
	}

	public void parse(Reader reader) throws XMLStreamException, XMLParseValidationException {
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader xReader = inFact.createXMLStreamReader(reader);
		parse(xReader);
	}

	public void parse(XMLStreamReader in)
	throws XMLStreamException, XMLParseValidationException {
		parseToStartTag(in); // setup to parse

		ParseState state = new ParseState(in);
		// We assume now that we are at the root

		while(in.hasNext()) {
			if (state.isOnRoot()) {
//				System.out.println("ROOT: " + in.getLocalName());
				state.parseToNextRootChildStart();
			} else if (state.isOnRootChildStart()){
//				System.out.println("  CHILD START: " + in.getLocalName());
				state.setAsRootChild(in.getLocalName());
				state.parseToRootChildEndOrListElementStart();
			} else if (state.isOnRootChildEnd()) {
//				System.out.println("    CHILD CONTENT: " + state.content);
//				System.out.println("    CHILD NODE: " + state.getContentAsNode());
//				System.out.println("  CHILD END: " + in.getLocalName());

				if (!state.isInList()) {
					props.put(in.getLocalName(), state.content.toString());
				}

				state.clearRootChild();
				state.parseToNextRootChildStart();
			} else if (state.isOnListElementStart()) {
				state.setAsListElement();
				state.parseToListElementEnd();
			} else if (state.isOnListElementEnd()) {
//				System.out.println("      LIST ELMT CONTENT: " + state.content);
//				System.out.println("      LIST ELMT NODE: " + state.getContentAsNode());
				props.put(state.getRootChildName() + "." + state.getId(), state.content.toString());
				Map<String, String> map = maps.get(state.getRootChildName());
				if (map == null) {
					map = new LinkedHashMap<String, String>();
					maps.put(state.getRootChildName(), map);
				}
				map.put(state.getId(), state.getContentAsNode().toString());
				//props.put(state.getListElementName(), state.content.toString());
				state.parseToNextListElementOrRootChildEnd();
			} else {
				throw new IllegalStateException("the above should be the only legal states");
			}
		}
	}

	// =====================
	// Map interface methods
	// =====================
	@Override
	public void clear() {
		props.clear();
		nodes.clear();
		maps.clear();
	}

	@Override
	public boolean containsKey(Object key) {return props.containsKey(key);}

	@Override
	public boolean containsValue(Object value) { return props.containsValue(value);}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() { return props.entrySet();}

	@Override
	public String get(Object key) {
		return (key == null)? null: get(key.toString());
	}

	@Override
	public boolean isEmpty() {return props.isEmpty();}

	@Override
	public Set<String> keySet() {return props.keySet();}

	@Override
	public String put(String key, String value) {return props.put(key, value);}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {props.putAll(m);}

	@Override
	public String remove(Object key) {return props.remove(key);}

	@Override
	public int size() {return props.size();}

	@Override
	public Collection<String> values() {return props.values();}

}
