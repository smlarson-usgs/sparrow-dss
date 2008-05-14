package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ResponseFormat implements XMLStreamParserComponent {
	public static final String MAIN_ELEMENT_NAME = "response-format";


	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	public static ResponseFormat parseStream(XMLStreamReader in) throws XMLStreamException {
		ResponseFormat rf = new ResponseFormat();
		return rf.parse(in);
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	public String formatName;
	public String name;
	public String fileName;
	protected String compressMethod;
	protected String mimeType;
	protected OutputType outputType;
	protected boolean isAttachment = true;
	
	// ================
	// INSTANCE METHODS
	// ================
	public ResponseFormat parse(XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : this
			.getClass().getSimpleName()
			+ " can only parse " + MAIN_ELEMENT_NAME + " elements.";
		boolean isStarted = false;

		while (in.hasNext()) {
			if (isStarted) {
				// Don't advance past the first element.
				eventCode = in.next();
			} else {
				isStarted = true;
			}

			// Main event loop -- parse until corresponding target end tag encountered.
			switch (eventCode) {
				case START_ELEMENT:
					localName = in.getLocalName();
					if ("mime-type".equals(localName) || "mimeType".equals(localName)) {
						setMimeType(ParserHelper.parseSimpleElementValue(in));
					} else if (MAIN_ELEMENT_NAME.equals(localName)) {
						// pull out the relevant attributes in the target start tag
						compressMethod = in.getAttributeValue(null, "compress");
						name = in.getAttributeValue(null, "name");
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						return this; // we're done
					}
					break;
			}
		}

		return this;
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public void setMimeType(String mimeType) {
		if (mimeType != null) {
			this.mimeType = mimeType.toLowerCase();
			// set the associated output type
			outputType = OutputType.parse(mimeType);
		}
	}

	public String getMimeType() {
		return mimeType;
	}

	public OutputType getOutputType() {
		return outputType;
	}

	public void setCompression(String compressMethod) {
		if ("zip".equalsIgnoreCase(compressMethod) || "gzip".equalsIgnoreCase(compressMethod)) {
			this.compressMethod = compressMethod.toLowerCase();
		}
	}

	public String getCompression() {
		return compressMethod;
	}
	
	public boolean isAttachement() {
		return isAttachment;
	}

	public void setAttachment(boolean attach) {
		this.isAttachment = attach;
	}

}
