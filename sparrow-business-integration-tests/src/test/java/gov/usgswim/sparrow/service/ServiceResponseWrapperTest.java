package gov.usgswim.sparrow.service;

import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.action.PredefinedSessionsLongRunTest;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class ServiceResponseWrapperTest extends SparrowTestBase {
	
	@Test
	public void verifyPredefinedSessionSerialization() throws Exception {
		
		PredefinedSessionBuilder[] ps1 = PredefinedSessionsLongRunTest.createUnsavedPredefinedSessions();

		
		
		ServiceResponseWrapper wrapper = new ServiceResponseWrapper(
				ps1[0], IPredefinedSession.class, 99L, ServiceResponseStatus.OK,
				ServiceResponseOperation.GET);
		
		XStream stream = new XStream();
		stream.processAnnotations(ServiceResponseWrapper.class);
		
		String xml = stream.toXML(wrapper);
		//System.out.println(xml);
		
		assertEquals("OK", getXPathValue("//ServiceResponseWrapper/status", xml));
		assertEquals("GET", getXPathValue("//ServiceResponseWrapper/operation", xml));
		assertEquals("gov.usgswim.sparrow.domain.IPredefinedSession", getXPathValue("//ServiceResponseWrapper/entityClass", xml));
		assertEquals("99", getXPathValue("//ServiceResponseWrapper/entityId", xml));
		assertEquals("1", getXPathValue("count(//ServiceResponseWrapper/entityList/entity)", xml));
		assertEquals("PredefinedSession", getXPathValue("//ServiceResponseWrapper/entityList/entity/@class", xml));
	}
	
	@Test
	public void verifyPredefinedSessionListSerialization() {
		
		PredefinedSessionBuilder[] ps1 = PredefinedSessionsLongRunTest.createUnsavedPredefinedSessions();
		List<?> list = Arrays.asList(ps1);
		
		
		
		ServiceResponseWrapper wrapper = new ServiceResponseWrapper(
				IPredefinedSession.class, ServiceResponseOperation.GET);
		wrapper.setStatus(ServiceResponseStatus.OK);
		wrapper.addAllEntities(list);
		
		XStream stream = new XStream(new StaxDriver());
		stream.processAnnotations(ServiceResponseWrapper.class);
		String resultStr = stream.toXML(wrapper);
		
		//System.out.println(resultStr);
		ServiceResponseWrapper recreatedWrap = (ServiceResponseWrapper) stream.fromXML(resultStr);
		
		assertEquals(wrapper.getEntityClass(), recreatedWrap.getEntityClass());
		assertEquals(wrapper.getEntityId(), recreatedWrap.getEntityId());
		assertEquals(wrapper.getMessage(), recreatedWrap.getMessage());
		assertEquals(wrapper.getOperation(), recreatedWrap.getOperation());
		assertEquals(wrapper.getStatus(), recreatedWrap.getStatus());
		
		List<Object> orgEnts = wrapper.getEntityList();
		List<Object> recEnts = recreatedWrap.getEntityList();
		
		for (int i = 0; i< orgEnts.size(); i++) {
			assertEquals(
					((PredefinedSession)(orgEnts.get(i))).getContextString(),
					((PredefinedSession)(recEnts.get(i))).getContextString()
			);
		}
	}
}
