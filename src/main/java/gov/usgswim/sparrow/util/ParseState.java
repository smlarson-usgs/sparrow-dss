/**
 *
 */
package gov.usgswim.sparrow.util;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ParseState{
	int depth;
	XMLStreamReader stream;
	StringBuilder content;

	ParseState(XMLStreamReader in){this.stream = in;}

	int next() throws XMLStreamException {
		int result = stream.next();
		switch(result) {
			case START_ELEMENT:
				depth++;
				break;
			case END_ELEMENT:
				depth--;
				break;
		}
		return result;
	}

	boolean isOnRootChildStart() {
		return (depth == 1) && (stream.getEventType() == START_ELEMENT);
	}

	public boolean isOnRoot() {
		return depth==0 && stream.getEventType() == START_ELEMENT;
	}

	public boolean isOnRootChildEnd() {
		return (depth == 1) && (stream.getEventType() == END_ELEMENT);
	}

	public void parseToNextRootChildStart() throws XMLStreamException {
		while (stream.hasNext()) {
			int currentEvent = next();
			if (depth == 1 && currentEvent == START_ELEMENT) {
				return;
			}
		}
	}

	public void parseToRootChildEnd() throws XMLStreamException {
		assert(isOnRootChildStart()): "Parsing to the end should only be called when on a start element of a root child";
		content = new StringBuilder();
		writeCurentEvent(stream, content);
		while (stream.hasNext()) {
			int currentEvent = next();
			writeCurentEvent(stream, content);
			if (depth == 1 && currentEvent == END_ELEMENT) {
				return;
			}
		}
	}

	public static void writeCurentEvent(XMLStreamReader in, StringBuilder record) {

	}

}