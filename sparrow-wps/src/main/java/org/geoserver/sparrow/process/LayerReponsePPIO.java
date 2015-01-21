package org.geoserver.sparrow.process;

import javax.xml.namespace.QName;
import org.geoserver.wps.ppio.XStreamPPIO;


/**
 *
 * @author eeverman
 */
public class LayerReponsePPIO extends XStreamPPIO {
	
	public LayerReponsePPIO() {
        super(LayerResponse.class, new QName("http://www.sparrow.gov/layer", "layer-response"));
    }
}
