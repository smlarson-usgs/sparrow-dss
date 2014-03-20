package org.geoserver.sparrow.process;

import org.geoserver.sparrow.process.SparrowWps;
import org.geoserver.wps.jts.SpringBeanProcessFactory;

/**
 * A GeoServer Process Factory that works w/ Spring to find Beans implementing
 * the SparrowWpsinterface and register them as WPS processes.
 * 
 * A custom factory is required to give them a custom namespace.
 * 
 * @author eeverman
 */
public class SparrowWpsFactory extends SpringBeanProcessFactory {
	public SparrowWpsFactory(String title, String namespace){ 
			super(title, namespace, SparrowWps.class); 
	} 
}
