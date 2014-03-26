package gov.usgswim.sparrow.domain;

import static org.junit.Assert.assertEquals;
import gov.usgs.cida.sparrow.service.util.ServletResponseParser;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class FloatArrayXStreamConverterTest {

	@Test
	public void marshalUnmarshalAGeometry() {
		float[] ords = new float[] {0f, -1f, 1f, 3f};
		Segment s = new Segment(ords, false);
		Segment[] ss = new Segment[] {s};
		Geometry geom = new Geometry(ss);
		XStream xs = ServletResponseParser.getXMLXStream();
		
		String orgXml = xs.toXML(geom);
		//System.out.println(orgXml);
		
		Geometry rehydratedGeom = (Geometry) xs.fromXML(orgXml);
		
		String rehydratedXml = xs.toXML(rehydratedGeom);
		//System.out.println(rehydratedXml);
		
		assertEquals(orgXml, rehydratedXml);
		
		//What does this look like as JSON?
//		xs = ServletResponseParser.getJSONXStream();
//		System.out.println(xs.toXML(geom));
	}
	

}
