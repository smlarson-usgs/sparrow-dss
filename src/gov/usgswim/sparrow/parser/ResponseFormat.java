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
	public static ResponseFormat parseStream(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
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
	public ResponseFormat parse(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : this
			.getClass().getSimpleName()
		+ " can only parse " + MAIN_ELEMENT_NAME + " elements.";
		boolean isStarted = false;

		try {
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
						if (MAIN_ELEMENT_NAME.equals(localName)) {
							// pull out the relevant attributes in the target start tag
							compressMethod = in.getAttributeValue(null, "compress");
							name = in.getAttributeValue(null, "name");
						} else if ("mime-type".equals(localName) || "mimeType".equals(localName)) {
							setMimeType(ParserHelper.parseSimpleElementValue(in)); // performs simple checking
						}
						break;
					case END_ELEMENT:
						localName = in.getLocalName();
						if (MAIN_ELEMENT_NAME.equals(localName)) {
							// DO POST PROCESSING HERE IF NECESSARY
							
							checkValidity();
							return this; // we're done
						}
						break;
				}
			}
		} catch (Exception e) {
			throw new XMLParseValidationException(e);
		}


		return this;
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}

	public void checkValidity() throws XMLParseValidationException {
		if (!isValid()) {
			// throw a custom error message depending on the error
			throw new XMLParseValidationException(MAIN_ELEMENT_NAME + " is not valid");
		}
	}

	public boolean isValid() {
		return true;
	}
	// =================
	// GETTERS & SETTERS
	// =================
	public void setMimeType(String mimeType) {
		if (mimeType != null) {
			this.mimeType = mimeType.toLowerCase();
			// set the associated output type
			outputType = OutputType.parse(mimeType);
			if (outputType == null) {
				throw new RuntimeException("The mime-type '" + mimeType + "' is unrecognized.");
			}
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
