package gov.usgswim.service;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ResponseFormat implements XMLStreamParserComponent {
	public static final String mainElementName = "response-format";

	public String formatName;
	public String name;
	protected String compressMethod;
	protected String mimeType;
	protected OutputType outputType;

	public ResponseFormat parse(XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (mainElementName.equals(localName) && eventCode == START_ELEMENT) : this
			.getClass().getSimpleName()
			+ " can only parse " + mainElementName + " elements.";
		boolean isStarted = false;

		while (in.hasNext()) {
			if (isStarted) {
				// Don't advance past the first element.
				eventCode = in.next();
			} else {
				isStarted = true;
			}

			// Main event loop -- parse until corresponding end tag encountered.
			switch (eventCode) {
				case START_ELEMENT:
					localName = in.getLocalName();
					if ("mime-type".equals(localName) || "mimeType".equals(localName)) {
						setMimeType(ParserHelper.parseSimpleElementValue(in));
					} else if (mainElementName.equals(localName)) {
						// pull out the relevant attributes
						compressMethod = in.getAttributeValue(null, "compress");
						name = in.getAttributeValue(null, "name");
					}
					break;

				case END_ELEMENT:
					localName = in.getLocalName();
					if (mainElementName.equals(localName)) {
						return this; // we're done
					}
					break;
			}
		}

		return this;
	}

	public String getParseTarget() {
		return mainElementName;
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public void setMimeType(String mimeType) {
		this.mimeType = (mimeType != null)? mimeType.toLowerCase(): null;
		// do stuff for output type
		if (mimeType != null) {
			outputType = Enum.valueOf(OutputType.class, mimeType.toUpperCase());
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

}
