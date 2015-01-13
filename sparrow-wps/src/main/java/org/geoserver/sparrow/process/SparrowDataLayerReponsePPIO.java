package org.geoserver.sparrow.process;

import javax.xml.namespace.QName;
import org.geoserver.wps.ppio.XStreamPPIO;


/**
 *
 * @author eeverman
 */
public class SparrowDataLayerReponsePPIO extends XStreamPPIO {
	
	public SparrowDataLayerReponsePPIO() {
        super(SparrowDataLayerResponse.class, new QName("http://www.sparrow.gov/layer", "sparrow-wps-datalayer-response"));
    }
}
