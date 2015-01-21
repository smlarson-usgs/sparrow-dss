package org.geoserver.sparrow.process;

import javax.xml.namespace.QName;
import org.geoserver.wps.ppio.XStreamPPIO;


/**
 *
 * @author eeverman
 */
public class StyleResponsePPIO extends XStreamPPIO {
	
	public StyleResponsePPIO() {
        super(StyleResponse.class, new QName("http://www.sparrow.gov/style", "sparrow-wps-style-response"));
    }
}
