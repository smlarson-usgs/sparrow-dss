package gov.usgswim.sparrow.parser;

import javax.xml.stream.XMLInputFactory;

import junit.framework.TestCase;

import org.junit.Ignore;

@Ignore
public class AreaOfInterestTest extends TestCase {
    
    /** Valid xml string represention of the area of interest section. */
    public static final String VALID_FRAGMENT = ""
        + "<area-of-interest>"
        + "  <logical-set />"
        + "</area-of-interest>";
        ;

    /** Used to create XMLStreamReaders from XML strings. */
    protected XMLInputFactory inFact = XMLInputFactory.newInstance();
    
	@Ignore
	public void testDummy() {
		// do nothing. This is here only because Infinitest complains if a test class has no test methods.
	}
}
