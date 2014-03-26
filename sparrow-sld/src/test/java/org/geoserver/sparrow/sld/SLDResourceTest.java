package org.geoserver.sparrow.sld;

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
    public void testHandleGet() throws Exception {
        System.out.println("testHandleGet");
        assertEquals("It works!", getAsString("/rest/test.txt").trim());
    }
    
}
