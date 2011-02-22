package gov.usgswim.sparrow.domain;

import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.service.ServletResponseParser;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class FloatArrayXStreamConverterTest {

	@Test
	public void marshalUnmarshalAGeometry() {
		float[] ords = new float[] {0f, 0f, 1f, 1f};
		Geometry geom = new Geometry(ords, true);
		XStream xs = ServletResponseParser.getXMLXStream();
		
		String orgXml = xs.toXML(geom);
		//System.out.println(orgXml);
		
		Geometry rehydratedGeom = (Geometry) xs.fromXML(orgXml);
		
		String rehydratedXml = xs.toXML(rehydratedGeom);
		//System.out.println(rehydratedXml);
		
		assertEquals(orgXml, rehydratedXml);
	}
	

}
