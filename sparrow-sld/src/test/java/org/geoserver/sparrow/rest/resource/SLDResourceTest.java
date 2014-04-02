package org.geoserver.sparrow.rest.resource;

import org.geoserver.test.GeoServerTestSupport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 *
 * @author isuftin
 */
public class SLDResourceTest extends GeoServerTestSupport {

    public SLDResourceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testHandleGetReachSLDUnencoded() throws Exception {
        System.out.println("testHandleGetReachSLDUnencoded");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/reach.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
        assertTrue(dom.getFirstChild().getTextContent().contains("Sparrow Reach style"));
    }
    
    @Test
    public void testHandleGetCatchmentSLDUnencodedBounded() throws Exception {
        System.out.println("testHandleGetCatchmentSLDUnencodedBounded");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/catch.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014&bounded=false");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
        assertTrue(dom.getFirstChild().getTextContent().contains("Sparrow Catchment style"));
    }

    @Test
    public void testHandleGetCatchmentSLDUnencoded() throws Exception {
        System.out.println("testHandleGetCatchmentSLDUnencoded");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/catch.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
        assertTrue(dom.getFirstChild().getTextContent().contains("Sparrow Catchment style"));
    }

    @Test
    public void testHandleGetFailover() throws Exception {
        System.out.println("testHandleGetFailover");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/not-a-real-thing.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
    }

    @Test
    public void testWrongURI() throws Exception {
        System.out.println("testWrongURI");
        Document dom = getAsDOM("/rest/sld/workspace/layer/1629177216/not-a-real-thing.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
        assertEquals(dom.getDocumentElement().toString(), "[html: null]");
        assertEquals(dom.getDocumentElement().getFirstChild().getTextContent().trim(), "");
    }

    @Test
    public void testHandleGetReachWithoutQueryString() throws Exception {
        System.out.println("testHandleGetReachWithoutQueryString");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/reach.sld");
        assertNotNull(dom);
        assertEquals(dom.getFirstChild().getNodeName(), "StyledLayerDescriptor");
        assertTrue(dom.getFirstChild().getTextContent().contains("Sparrow Reach style"));
    }

}
