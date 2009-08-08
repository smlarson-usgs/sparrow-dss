package gov.usgswim.sparrow.parser;

import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.ReachGroup;
import gov.usgswim.sparrow.test.TestHelper;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class AdjustmentGroupsTest extends TestCase {
    
    /** Valid xml string represention of the adjustment groups. */
    public static final String VALID_FRAGMENT = ""
        + "<adjustment-groups conflicts=\"supersede\">"
        + ReachGroupTest.VALID_FRAGMENT
        + DefaultGroupTest.VALID_FRAGMENT
        + IndividualGroupTest.VALID_FRAGMENT
        + "</adjustment-groups>"
        ;
    
    /** Used to create XMLStreamReaders from XML strings. */
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	// ============
	// TEST METHODS
	// ============
	public void testParseMainUseCase() throws Exception {

		AdjustmentGroups adjGroups = buildAdjGroups(1L);
		
		List<ReachGroup> reachGroups = adjGroups.getReachGroups();
		assertEquals("supersede", adjGroups.getConflicts());
		assertEquals(new Long(1L), adjGroups.getModelID());
		assertEquals(1, reachGroups.size());
		assertNotNull(adjGroups.getDefaultGroup());
		assertNotNull(adjGroups.getIndividualGroup());
	}
	
	
	public void testHashcode() throws Exception {	
		AdjustmentGroups adjGroups1 = buildAdjGroups(1L);
		AdjustmentGroups adjGroups2 = buildAdjGroups(1L);
		TestHelper.testHashCode(adjGroups1, adjGroups2, adjGroups1.clone());
	
		// test IDs
		assertEquals(adjGroups1.hashCode(), adjGroups1.getId().intValue());
		assertEquals(adjGroups2.hashCode(), adjGroups2.getId().intValue());
	}
	
	public AdjustmentGroups buildAdjGroups(long modelId) throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(VALID_FRAGMENT));
		AdjustmentGroups adjGroups = new AdjustmentGroups(modelId);
		reader.next();
		adjGroups = adjGroups.parse(reader);
		
		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(AdjustmentGroups.MAIN_ELEMENT_NAME, reader.getLocalName());
		
		return adjGroups;
	}
}
