package org.geoserver.sparrow.rest.resource;

import org.geoserver.test.GeoServerTestSupport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author isuftin
 */
@Ignore public class SLDResourceTest extends GeoServerTestSupport {

    public SLDResourceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }


    @Ignore 
    @Test
    public void testHandleGetReachSLDUnencoded() throws Exception {
        System.out.println("testHandleGetReachSLDUnencoded");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/reach.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
        assertTrue(dom.getFirstChild().getTextContent().contains("Sparrow Reach style"));
    }

@Ignore 
    @Test
    public void testHandleGetCatchmentSLDUnencodedUnBounded() throws Exception {
        System.out.println("testHandleGetCatchmentSLDUnencodedUnBounded");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/catch.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014&bounded=true");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
        assertTrue(dom.getFirstChild().getTextContent().contains("Sparrow Catchment style"));

        NodeList ftsList = dom.getElementsByTagName("FeatureTypeStyle");
        assertEquals(ftsList.getLength(), 5);
        Node node = ftsList.item(ftsList.getLength() - 1);
        assertNotNull(node);
    }
@Ignore 
    @Test
    public void testHandleGetCatchmentSLDUnencoded() throws Exception {
        System.out.println("testHandleGetCatchmentSLDUnencoded");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/catch.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
        assertTrue(dom.getFirstChild().getTextContent().contains("Sparrow Catchment style"));
    }
@Ignore 
    @Test
    public void testHandleGetFailover() throws Exception {
        System.out.println("testHandleGetFailover");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/not-a-real-thing.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
    }
@Ignore 
    @Test
    public void testWrongURI() throws Exception {
        System.out.println("testWrongURI");
       Document dom = getAsDOM("/rest/sld/workspace/layer/1629177216/not-a-real-thing.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
        assertEquals(dom.getDocumentElement().toString(), "[html: null]");
        assertEquals(dom.getDocumentElement().getFirstChild().getTextContent().trim(), "");
    }
@Ignore 
    @Test
    public void testHandleGetReachWithoutQueryString() throws Exception {
        System.out.println("testHandleGetReachWithoutQueryString");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/reach.sld");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
        assertTrue(dom.getFirstChild().getTextContent().contains("Sparrow Reach style"));
    }

}
