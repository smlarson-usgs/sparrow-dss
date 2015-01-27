package org.geoserver.sparrow.util;

import javax.xml.namespace.QName;
import org.geoserver.wps.ppio.XStreamPPIO;


/**
 *
 * @author eeverman
 */
public class SweepResponsePPIO extends XStreamPPIO {
	
	public SweepResponsePPIO() {
        super(SweepResponse.class, new QName("http://www.sparrow.gov/sweep", "sparrow-wps-sweep-response"));
    }
}
