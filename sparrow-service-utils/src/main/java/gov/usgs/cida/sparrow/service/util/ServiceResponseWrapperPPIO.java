package gov.usgs.cida.sparrow.service.util;

import javax.xml.namespace.QName;
import org.geoserver.wps.ppio.XStreamPPIO;

/**
 * An encoder/decoder marker class for WPS Process Parameter IO in GeoServer.
 * 
 * To use, instantiate in the appliationContext.xml of a GeoServer module.
 * This class will cause the type declared in the super() invocation to be
 * serialized and deserialized by XStream when it is passed into or out of a
 * GeoServer WPS process.
 * @author eeverman
 */
public class ServiceResponseWrapperPPIO  extends XStreamPPIO {
	
	public ServiceResponseWrapperPPIO() {
		super(ServiceResponseWrapper.class, new QName("http://water.usgs.gov/nawqa/sparrow/dss", "sparrow-wps"));
	}
	
}
