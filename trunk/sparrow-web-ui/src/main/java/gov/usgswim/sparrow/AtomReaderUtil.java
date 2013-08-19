package gov.usgswim.sparrow;

import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.Response;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;

/**
 * A basic utility to pull in the content of the first entry in an Atom feed.
 * 
 * This was originally designed to pull the content of a single page from the
 * wiki so that it could be embedded in the application to provide editable
 * documentation, but it could easily be expanded for other uses.
 * 
 * @author eeverman
 */
public class AtomReaderUtil {

	public static final void main(String[] args) throws Exception {
		
		String feed = getAtomFeedContentOnlyAsString(args[0]);

		System.out.println("////////////");
		System.out.println(feed);
		System.out.println("////////////");
			
	}
	
	public static String getAtomFeedContentOnlyAsString(String atomUrl) throws Exception {
		String raw = getAtomFeedAsString(atomUrl);
		String content = getXPathValue("/html/body/div[1]/div[1]", raw);
		return content;
	}
	
	
	public static String getAtomFeedAsString(String atomUrl) throws Exception {
		StringWriter writer = new StringWriter();
				
		if (getAtomFeed(atomUrl, writer)) {
			return writer.toString();
		} else {
			return "<div><p>An error occured while loading the content/<p></div>";
		}
		
	}
	
	public static boolean getAtomFeed(String atomUrl, Writer output) throws Exception {

			// Default trust manager provider registered for port 443
			AbderaClient.registerTrustManager();
			
			Abdera abdera = new Abdera();

			AbderaClient client = new AbderaClient(abdera);
			ClientResponse resp = client.get(atomUrl);
			if (resp.getType() == Response.ResponseType.SUCCESS) {
				Document<Feed> doc = resp.getDocument();
				
				
				String summary = doc.getRoot().getEntries().get(0).getSummary();
				
				Tidy tidy = new Tidy();
				
				tidy.setXmlOut(true);
				//tidy.setXHTML(true);
				tidy.parse(new StringReader(summary), output);
				return true;
				
			} else {
				return false;
			}
			
	}
	
	/**
	 * Returns the string value of the XPath expression.
	 * This is namespace aware.
	 * @param xpathExpression
	 * @param xmlDocument
	 * @return
	 * @throws Exception
	 */
	public static String getXPathValue(String xpathExpression, String xmlDocument) throws Exception {
		org.w3c.dom.Document document = stringToDom(xmlDocument);
		XPath xPath = XPathFactory.newInstance().newXPath();
		Node node = (Node) xPath.evaluate(xpathExpression, document, XPathConstants.NODE);
		String value = nodeToString(node);
		return value;
	}


  public static org.w3c.dom.Document stringToDom( String str ) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource( new StringReader( str ) );
		org.w3c.dom.Document d = builder.parse(is);

		return d;
  }
	
	public static String nodeToString(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException te) {
			System.out.println("nodeToString Transformer Exception");
		}
		return sw.toString();
	}

}
