package gov.usgswim.sparrow.domain;

import static org.junit.Assert.*;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import gov.usgs.cida.sparrow.service.util.ServletResponseParser;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class GeometryTest {

	@Test
	public void marshalUnmarshalAGeometry() {
		float[] ords = new float[] {0f, -1f, 1f, 3f};
		Segment s = new Segment(ords, false);
		Segment[] ss = new Segment[] {s};
		Geometry geom = new Geometry(s);
		
		assertEquals(0f, geom.getMinLong(), .0001f);
		assertEquals(1f, geom.getMaxLong(), .0001f);
		
		assertEquals(-1f, geom.getMinLat(), .0001f);
		assertEquals(3f, geom.getMaxLat(), .0001f);
		
		assertEquals(.5f, geom.getCenterLong(), .0001f);
		assertEquals(1f, geom.getCenterLat(), .0001f);
	}
	

}
