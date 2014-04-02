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
    }
    @Test
    public void testHandleGetCatchmentSLDUnencoded() throws Exception {
        System.out.println("testHandleGetReachSLDUnencoded");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-catchment/layer/1629177216/catch.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
    }

}
