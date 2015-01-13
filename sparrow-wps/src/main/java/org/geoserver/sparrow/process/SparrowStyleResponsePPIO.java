package org.geoserver.sparrow.process;

import javax.xml.namespace.QName;
import org.geoserver.wps.ppio.XStreamPPIO;


/**
 *
 * @author eeverman
 */
public class SparrowStyleResponsePPIO extends XStreamPPIO {
	
	public SparrowStyleResponsePPIO() {
        super(SparrowStyleResponse.class, new QName("http://www.sparrow.gov/layer", "sparrow-wps-style-response"));
    }
}
