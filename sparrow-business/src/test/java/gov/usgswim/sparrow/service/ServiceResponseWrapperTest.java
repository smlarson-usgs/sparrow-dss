package gov.usgswim.sparrow.service;

import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class ServiceResponseWrapperTest extends SparrowTestBase {
	
	
	@Test
	public void verifySimpleStringSerialization() throws Exception {
		
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(
				String.class, ServiceResponseOperation.REGISTER);
		
		wrap.addEntity("Result String");
		wrap.setStatus(ServiceResponseStatus.OK);
		
		XStream stream = SharedApplication.getInstance().getXmlXStream();
		
		String xml = stream.toXML(wrap);
		
		assertEquals(ServiceResponseStatus.OK.toString(), getXPathValue("//ServiceResponseWrapper/status", xml));
		assertEquals(ServiceResponseOperation.REGISTER.toString(), getXPathValue("//ServiceResponseWrapper/operation", xml));
		assertEquals("java.lang.String", getXPathValue("//ServiceResponseWrapper/entityClass", xml));
		assertEquals("1", getXPathValue("count(//ServiceResponseWrapper/entityList/entity)", xml));
		assertEquals("Result String", getXPathValue("//ServiceResponseWrapper/entityList/entity", xml));
	}
	
	@Test
	public void verifyPredefinedSessionSerialization() throws Exception {
		
		PredefinedSessionBuilder[] psbs = createUnsavedPredefinedSessions();
		PredefinedSession[] ps1 = convertToImmutable(psbs);

		
		ServiceResponseWrapper wrapper = new ServiceResponseWrapper(
				ps1[0], IPredefinedSession.class, 99L, ServiceResponseStatus.OK,
				ServiceResponseOperation.GET);
		
		XStream stream = SharedApplication.getInstance().getXmlXStream();
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
		
		PredefinedSessionBuilder[] psbs = createUnsavedPredefinedSessions();
		PredefinedSession[] ps1 = convertToImmutable(psbs);

			
		List<PredefinedSession> list = Arrays.asList(ps1);
		
		ServiceResponseWrapper wrapper = new ServiceResponseWrapper(
				IPredefinedSession.class, ServiceResponseOperation.GET);
		wrapper.setStatus(ServiceResponseStatus.OK);
		wrapper.addAllEntities(list);
		
		XStream stream = SharedApplication.getInstance().getXmlXStream();
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
	
	public static PredefinedSession[] convertToImmutable(PredefinedSessionBuilder[] PredefinedSessionBuilders) {
		PredefinedSession[] predifinedSessions = new PredefinedSession[PredefinedSessionBuilders.length];
		
		for (int i = 0; i<predifinedSessions.length; i++) {
			PredefinedSession ps = PredefinedSessionBuilders[i].toImmutable();
			predifinedSessions[i] = ps;
		}
		
		return predifinedSessions;
	}
	

	/**
	 * Creates three PredefinedSEssionBuilders.
	 * This may be used by other classes
	 * @return
	 */
	public static PredefinedSessionBuilder[] createUnsavedPredefinedSessions() {
		
		PredefinedSessionBuilder ps1;
		PredefinedSessionBuilder ps2;
		PredefinedSessionBuilder ps3;
		
		//Construct a calendar date for today that does not include time.
		GregorianCalendar today = new GregorianCalendar();
		
		today = new GregorianCalendar(
				today.get(Calendar.YEAR),
				today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH));
		GregorianCalendar yesterday = new GregorianCalendar(
				today.get(Calendar.YEAR),
				today.get(Calendar.MONTH),
				today.get(Calendar.DAY_OF_MONTH));
		yesterday.add(Calendar.DAY_OF_MONTH, -1);
		
		
		ps1 = new PredefinedSessionBuilder();
		ps1.setAddBy("Eric");
		ps1.setAddContactInfo("608.821.1111");
		//ps1.setAddDate(new Date(today.getTimeInMillis()));	//is autoset
		ps1.setAddNote("Please approve me");
		//ps1.setApproved(false);	//should default to false and not allow 'true' on new records
		ps1.setContextString("context");
		ps1.setDescription("[[TEST USAGE ONLY, DO NOT USE. DELETE ME]]desc");
		ps1.setGroupName("myGroup");
		ps1.setModelId(9999L);
		ps1.setName("Session 1");
		ps1.setPredefinedSessionType(PredefinedSessionType.FEATURED);
		ps1.setSortOrder(1);
		//ps1.setUniqueCode("veryUnique1");	//auto-create unique code
		
		//
		ps2 = new PredefinedSessionBuilder();
		ps2.setAddBy("I-Lin");
		ps2.setAddContactInfo("608.821.1112");
		ps2.setAddDate(new Date(yesterday.getTimeInMillis()));	//should be ignored - reset to today
		ps2.setAddNote("Please approve me");
		ps2.setApproved(true);	//ignored and set to false
		ps2.setContextString("context");
		ps2.setDescription("[[TEST USAGE ONLY, DO NOT USE. DELETE ME]]desc");
		ps2.setGroupName("myGroup");
		ps2.setModelId(9999L);
		ps2.setName("Session 2");
		ps2.setPredefinedSessionType(PredefinedSessionType.LISTED);
		ps2.setSortOrder(4);
		ps2.setUniqueCode("veryUnique2");	//auto-create unique code
		
		//
		ps3 = new PredefinedSessionBuilder();
		ps3.setAddBy("Lorraine");
		ps3.setAddContactInfo("608.821.1113");
		//ps3.setAddDate(new Date(yesterday.getTimeInMillis()));	//should be ignored - reset to today
		ps3.setAddNote("Please approve me");
		ps3.setApproved(false);
		ps3.setContextString("context");
		ps3.setDescription("[[TEST USAGE ONLY, DO NOT USE. DELETE ME]]desc");
		ps3.setGroupName("myGroup");
		ps3.setModelId(9999L);
		ps3.setName("Session 3");
		ps3.setPredefinedSessionType(PredefinedSessionType.UNLISTED);
		ps3.setSortOrder(6);
		ps3.setUniqueCode("veryUnique3");	//auto-create unique code
		
		return new PredefinedSessionBuilder[] {ps1, ps2, ps3};
	}
}
