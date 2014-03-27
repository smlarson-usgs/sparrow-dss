package org.geoserver.sparrow.process;

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
	public SparrowWpsFactory() { 
			super("Custom Sparrow WPS Processes", "spdss", SparrowWps.class); 
	} 
}
