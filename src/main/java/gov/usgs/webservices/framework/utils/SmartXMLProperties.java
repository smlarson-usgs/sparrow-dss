package gov.usgs.webservices.framework.utils;

import static gov.usgswim.sparrow.util.ParserHelper.parseToStartTag;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
	protected Map<String, String> props = new HashMap<String, String>();
	protected Map<String, String> values = new HashMap<String, String>();
	protected Set<String> listNames = new HashSet<String>();

	public String get(String simpleOrCompoundKey) {
		if (simpleOrCompoundKey == null) return null;
		if (isCompoundKey(simpleOrCompoundKey)) {
			return props.get(simpleOrCompoundKey);
		}
		return null;
	}

//	public String getAsXMLFragment() {
//
//	}
//	public String getAsFullXMLNode() {
//
//	}
//
//	public Object getAsObject(Class<?> itemClass) {
//
//	}


	public boolean isCompoundKey(String key) {
		return key.indexOf('.') > 0;
	}

//	public boolean isListKey(String key) {
//
//	}

	public void parse(String xml) throws XMLStreamException, XMLParseValidationException {
		// TODO replace this by SourceToStreamConverter calls
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(xml));
		parse(reader);
	}

	public void parse(XMLStreamReader in)
	throws XMLStreamException, XMLParseValidationException {
		parseToStartTag(in); // setup to parse

		ParseState state = new ParseState(in);
		// We assume now that we are at the root

		while(in.hasNext()) {
			if (state.isOnRoot()) {
				System.out.println("ROOT: " + in.getLocalName());
				state.parseToNextRootChildStart();
			} else if (state.isOnRootChildStart()){
				System.out.println("  CHILD START: " + in.getLocalName());
				state.setAsRootChild(in.getLocalName());
				state.parseToRootChildEndOrListElementStart();
			} else if (state.isOnRootChildEnd()) {
				System.out.println("    CHILD CONTENT: " + state.content);
				System.out.println("    CHILD NODE: " + state.getContentAsNode());
				System.out.println("  CHILD END: " + in.getLocalName());

				props.put(in.getLocalName(), state.content.toString());

				state.parseToNextRootChildStart();
			} else if (state.isOnListElementStart()) {
				state.setAsListElement();
				state.parseToListElementEnd();
				//
//			} else if (state.isOnRootGrandChildStart()) {
//				if (state.isOnListElementStart()) {
//					System.out.println("    LIST ELMT START: " + in.getLocalName());
//					state.setAsListElement();
//					// get all the grandchildren
//					state.parseToListElementEnd();
//				} else {
//					// continue parsing to end
//					state.parseToRootChildEnd();
////					System.out.println("    CHILD CONTENT: " + state.content);
////					System.out.println("    CHILD NODE: " + state.getContentAsNode());
//				}
			} else if (state.isOnListElementEnd()) {
				System.out.println("      LIST ELMT CONTENT: " + state.content);
				System.out.println("      LIST ELMT NODE: " + state.getContentAsNode());
				props.put(state.getRootChildName() + "." + state.getId(), state.content.toString());
				props.put(state.getListElementName(), state.content.toString());
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
	public void clear() { props.clear();}

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
