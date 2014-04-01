package org.geoserver.sparrow.process;

import javax.xml.namespace.QName;
import org.geoserver.wps.ppio.XStreamPPIO;


/**
 *
 * @author eeverman
 */
public class SparrowDataLayerPPIO extends XStreamPPIO {
	
	public SparrowDataLayerPPIO() {
        super(SparrowDataLayerResponse.class, new QName("http://www.sparrow.gov/layer", "sparrow-wps-response"));
    }
}
