package gov.usgswim.sparrow.parser;

import gov.usgswim.sparrow.parser.Adjustment;
import gov.usgswim.sparrow.parser.IndividualGroup;
import gov.usgswim.sparrow.parser.ReachElement;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * Unit tests for the {@code IndividualGroup} class.
 * 
 * @see gov.usgswim.sparrow.parser.IndividualGroup
 * @author mtruiz
 */
public class IndividualGroupTest extends TestCase {
    
    /** Valid xml string represention of the individual group. */
    public static final String VALID_FRAGMENT = ""
        + "<individual-group enabled=\"true\">"
        + "  <reach id=\"12345\">"
        + "    <adjustment src=\"1\" abs=\"123\" />"
        + "  </reach>"
        + "</individual-group>"
        ;

    /** Used to create XMLStreamReaders from XML strings. */
    protected XMLInputFactory inFact = XMLInputFactory.newInstance();
    
    /**
     * Ensures that the {@code enabled} attribute is correctly parsed.
     */
    public void testEnabled() {
        try {
            // An enabled individual group
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(VALID_FRAGMENT));
            reader.next();

            // Test enabled group
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            assertTrue(group.isEnabled());

            // A disabled individual group
            String disabledFragment = ""
                + "<individual-group enabled=\"false\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"123\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            reader = inFact.createXMLStreamReader(new StringReader(disabledFragment));
            reader.next();
            
            // Test disabled group
            group = new IndividualGroup(1);
            group.parse(reader);
            assertFalse(group.isEnabled());

        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            fail(pve.toString());
        }
    }
    
    /**
     * Ensures the individual group parses correctly with zero reaches specified.
     */
    public void testZeroReaches() {
        try {
            // Group with no reaches
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();

            // Parse
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);

        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            fail(pve.toString());
        }
    }
    
    /**
     * Ensures the individual group parses correctly with only one reach specified.
     */
    public void testOneReach() {
        try {
            // Group with one reach
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"123\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();

            // Test single reach
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            
            List<ReachElement> reaches = group.getExplicitReaches();
            assertEquals(reaches.size(), 1);
            assertEquals(reaches.get(0).getId(), Long.valueOf(12345L));
            
        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            fail(pve.toString());
        }
    }
    
    /**
     * Ensures the individual group parses correctly with multiple reaches specified.
     */
    public void testReaches() {
        try {
            // Group with multiple reaches
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"123\" />"
                + "  </reach>"
                + "  <reach id=\"54321\">"
                + "    <adjustment src=\"2\" abs=\"321\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();

            // Test reach definitions
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            
            List<ReachElement> reaches = group.getExplicitReaches();
            assertEquals(reaches.size(), 2);
            assertEquals(reaches.get(0).getId(), Long.valueOf(12345L));
            assertEquals(reaches.get(1).getId(), Long.valueOf(54321L));
            
        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            fail(pve.toString());
        }
    }
    
    /**
     * Main success scenario - enabled individual group with multiple reaches,
     * each of which have adjustments.
     */
    public void testReachAdjustments() {
        try {
            // Group with multiple adjusted reaches
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "    <adjustment src=\"2\" abs=\"222\" />"
                + "  </reach>"
                + "  <reach id=\"54321\">"
                + "    <adjustment src=\"3\" abs=\"333\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();

            // Test adjustment definitions
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            
            // Adjustment tests
            List<ReachElement> reaches = group.getExplicitReaches();
            List<Adjustment> adjustments1 = reaches.get(0).getAdjustments(); // 12345 adjs
            List<Adjustment> adjustments2 = reaches.get(1).getAdjustments(); // 54321 adjs
            
            // First reach (ID: 12345)
            assertEquals(adjustments1.get(0).getSource(), Integer.valueOf(1));
            assertEquals(adjustments1.get(0).getAbsolute(), Double.valueOf(111D));
            assertEquals(adjustments1.get(1).getSource(), Integer.valueOf(2));
            assertEquals(adjustments1.get(1).getAbsolute(), Double.valueOf(222D));
            
            // Second reach (ID: 54321)
            assertEquals(adjustments2.get(0).getSource(), Integer.valueOf(3));
            assertEquals(adjustments2.get(0).getAbsolute(), Double.valueOf(333D));

        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            fail(pve.toString());
        }
    }
    
    /**
     * Ensures that the individual group is hashed correctly. 
     */
    public void testHashCode() {
        try {
            String fragment1 = ""
                + "<individual-group enabled=\"true\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            String fragment2 = "" // Equivalent to fragment 1
                + "<individual-group enabled=\"true\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            String fragment3 = "" // Differs from fragments 1 and 2
                + "<individual-group enabled=\"false\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "    <adjustment src=\"2\" abs=\"222\" />"
                + "  </reach>"
                + "  <reach id=\"54321\">"
                + "    <adjustment src=\"3\" abs=\"333\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            // Technically we should vary each piece of the group on an
            // individual basis for this test (i.e., enabled, reach ids,
            // adjustment sources adjustment abs, number of reaches, number of
            // adjustments).

            // Parse group 1
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment1));
            reader.next();
            IndividualGroup group1 = new IndividualGroup(1);
            group1.parse(reader);
            
            // Parse group 2
            reader = inFact.createXMLStreamReader(new StringReader(fragment2));
            reader.next();
            IndividualGroup group2 = new IndividualGroup(1);
            group2.parse(reader);
            
            // Parse group 3
            reader = inFact.createXMLStreamReader(new StringReader(fragment3));
            reader.next();
            IndividualGroup group3 = new IndividualGroup(1);
            group3.parse(reader);
            
            assertEquals(group1.getStateHash(), group2.getStateHash());
            assertFalse(group2.getStateHash() == group3.getStateHash());

        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
    
    /**
     * Ensures that an exception is thrown if an improper reach adjustment is
     * specified.
     */
    public void testInvalidReachAdjustments() {
        try {
            // Group with multiple adjusted reaches
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "    <adjustment src=\"2\" coef=\"0.9\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();

            // Parse expecting an exception
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            fail("Coefficient on a reach adjustment should throw an exception.");

        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
    
    /**
     * Ensures that an exception is thrown when specifying an invalid root.
     */
    public void testInvalidRootElement() {
        try {
            String fragment = ""
                + "<individual-group-too enabled=\"true\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                + "</individual-group-too>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();
    
            // Parse expecting an exception
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            fail("Invalid root element should throw an exception");
    
        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
    
    /**
     * Ensures that an exception is thrown when a name is specified.
     */
    public void testFailOnName() {
        try {
            String fragment = ""
                + "<individual-group enabled=\"true\" name=\"Yo\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();
    
            // Parse expecting an exception
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            fail("Specifying a name should throw an exception, yo");
    
        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
    
    /**
     * Ensures that an exception is thrown when a description is specified.
     */
    public void testFailOnDescription() {
        try {
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <desc>description</desc>"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();
    
            // Parse expecting an exception
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            fail("Specifying a description should throw an exception");
    
        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
    
    /**
     * Ensures that an exception is thrown when notes are specified.
     */
    public void testFailOnNotes() {
        try {
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <notes>notes</notes>"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();
    
            // Parse expecting an exception
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            fail("Specifying a notes element should throw an exception");
    
        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
    
    /**
     * Ensures that an exception is thrown when a group adjustment is specified.
     */
    public void testFailOnAdjustment() {
        try {
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <adjustment src=\"3\" coef=\"0.9\" />"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();
    
            // Parse expecting an exception
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            fail("Specifying a group-wide adjustment should throw an exception");
    
        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
    
    /**
     * Ensures that an exception is thrown when a logical set is specified.
     */
    public void testFailOnLogicalSet() {
        try {
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <logical-set>"
                + "    <criteria attrib=\"huc8\">01746286</criteria>"
                + "  </logical-set>"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                + "</individual-group>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();
    
            // Parse expecting an exception
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            fail("Specifying a logical set should throw an exception");
    
        } catch (XMLStreamException se) {
            fail(se.toString());
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
    
    /**
     * Ensures that an exception is thrown when the end element is missing.
     */
    public void testMissingEndElement() {
        try {
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();
    
            // Parse expecting an exception
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            fail("Missing end element should throw an exception");
    
        } catch (XMLStreamException se) {
            // Success
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
    
    /**
     * Ensures that an exception is thrown when the end element is invalid.
     */
    public void testInvalidEndElement() {
        try {
            String fragment = ""
                + "<individual-group enabled=\"true\">"
                + "  <reach id=\"12345\">"
                + "    <adjustment src=\"1\" abs=\"111\" />"
                + "  </reach>"
                + "</individual-group-too>"
                ;
            XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(fragment));
            reader.next();
    
            // Parse expecting an exception
            IndividualGroup group = new IndividualGroup(1);
            group.parse(reader);
            fail("Invalid end element should throw an exception");
    
        } catch (XMLStreamException se) {
            // Success
        } catch (XMLParseValidationException pve) {
            // Success
        }
    }
}
