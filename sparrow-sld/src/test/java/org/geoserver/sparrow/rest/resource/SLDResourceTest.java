package org.geoserver.sparrow.rest.resource;

import java.util.List;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.test.GeoServerTestSupport;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Request;
import org.restlet.data.Response;
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
    public void testHandleGetReachSLDEnncoded() throws Exception {
        System.out.println("testHandleGetReachSLDEnncoded");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-flowline/layer/1373524916/reach.sld?binLowList=-1000%2C1000%2C2000%2C5000%2C24000&binHighList=1000%2C2000%2C5000%2C24000%2C70000000&binColorList=FFFFD4%2CFEE391%2CFEC44F%2CFE9929%2CEC7014");
        assertNotNull(dom);
    }

    @Test
    public void testHandleGetReachSLDUnencoded() throws Exception {
        System.out.println("testHandleGetReachSLDUnencoded");
        Document dom = getAsDOM("/rest/sld/workspace/sparrow-flowline/layer/1373524916/reach.sld?binLowList=-1000,1000,2000,5000,24000&binHighList=1000,2000,5000,24000,70000000&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014");
        assertNotNull(dom);
    }

//
//    @Test
//    public void testHandleGetCatchmentSLD() throws Exception {
//        System.out.println("testHandleGetCatchmentSLD");
//        Document dom = getAsDOM("/rest/sld/testWorkspace/datastore/testDatastore/testLayer/catchment.sld");
//        assertNotNull(dom);
//    }
}
