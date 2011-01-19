package gov.usgswim.sparrow.service.metadata;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import gov.usgswim.sparrow.SparrowServiceTest;
import gov.usgswim.sparrow.SparrowServiceTestWithCannedModel50;
import gov.usgswim.sparrow.action.PredefinedSessionsTest;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.request.PredefinedSessionRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Level;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.junit.Test;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class SavedSessionServiceTest extends SparrowServiceTestWithCannedModel50 {
	
	private static final String SESSION_SERVICE_URL = "http://localhost:8088/sp_session";
	
	// ============
	// TEST METHODS
	// ============
	@Test
	public void PUTandGETaSession() throws Exception {
		WebRequest req = new PutRequest(SESSION_SERVICE_URL);
		
		PredefinedSessionBuilder[] sessions = PredefinedSessionsTest.createUnsavedPredefinedSessions();
		assignRequestParams(req, sessions[0]);

		
		WebResponse response = client.sendRequest(req);
		String actualResponse = response.getText();
		//System.out.println("response: " + actualResponse);
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualResponse);
		
		String modelId = getAttributeValue(actualResponse, "model-id");
		String dbId = getAttributeValue(actualResponse, "db-id");
		String code = getAttributeValue(actualResponse, "predefinedSessionId");
		assertEquals("50", modelId);
		assertNotNull(code);
		
		req = new GetMethodWebRequest(SESSION_SERVICE_URL + "/" + code);
		response = client.sendRequest(req);
		actualResponse = response.getText();
		//System.out.println("response: " + actualResponse);
		
		
		PredefinedSessionBuilder deleteMe = new PredefinedSessionBuilder();
		deleteMe.setId(Long.parseLong(dbId));
		PredefinedSessionsTest.deleteSessions(deleteMe);
	}
	
	@Test
	public void FilterSessions() throws Exception {
		WebRequest req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		
		////////////////
		/// setup a whole bunch of sessions into groups, approved/not approved, etc.
		////////////////
		
		PredefinedSessionBuilder[] sessions = PredefinedSessionsTest.createUnsavedPredefinedSessions();
		IPredefinedSession[] savedSessions = PredefinedSessionsTest.saveSessions(sessions);
		
		//This set of tests is a modified copy of the filter test in PredefinedSessionsTest.
		PredefinedSessionBuilder set2ps1 = new PredefinedSessionBuilder(savedSessions[0]);
		PredefinedSessionBuilder set2ps2 = new PredefinedSessionBuilder(savedSessions[1]);
		PredefinedSessionBuilder set2ps3 = new PredefinedSessionBuilder(savedSessions[2]);
		PredefinedSessionBuilder set3ps1 = new PredefinedSessionBuilder(savedSessions[0]);
		PredefinedSessionBuilder set3ps2 = new PredefinedSessionBuilder(savedSessions[1]);
		PredefinedSessionBuilder set3ps3 = new PredefinedSessionBuilder(savedSessions[2]);
		
		PredefinedSessionBuilder[] newSessions = PredefinedSessionsTest.stripUniqueness(
			set2ps1, set2ps2, set2ps3, set3ps1, set3ps2, set3ps3);
		
		//Assign some group names
		newSessions[0].setGroupName("set2");
		newSessions[1].setGroupName("set2");
		newSessions[2].setGroupName("set2");
		newSessions[3].setGroupName("set3");
		newSessions[4].setGroupName("set3");
		newSessions[5].setGroupName("set3");
		
		//Our set2ps1 style references are now old
		newSessions = PredefinedSessionsTest.toBuilder(PredefinedSessionsTest.saveSessions(newSessions));
		
		//Set a few approved
		newSessions[0].setApproved(true);
		newSessions[1].setApproved(false);
		newSessions[2].setApproved(false);
		newSessions[3].setApproved(false);
		newSessions[4].setApproved(true);
		newSessions[5].setApproved(true);
		
		newSessions = PredefinedSessionsTest.toBuilder(PredefinedSessionsTest.saveSessions(newSessions));
		
		
		////////////////
		/// end of setup
		////////////////
		req.setParameter("modelId", "50");
		WebResponse response = client.sendRequest(req);
		String actualResponse = response.getText();
		//System.out.println("actual response: " + actualResponse);
		//Should be nine all together (no criteria)
		assertXpathEvaluatesTo("9", "count(//*[local-name()='session'])", actualResponse);

		
		//Should be 3 approved
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "50");
		req.setParameter("approved", "true");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("3", "count(//*[local-name()='session'])", actualResponse);

		//Should be 1 approved & FEATURED
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "50");
		req.setParameter("approved", "true");
		req.setParameter("type", "FEATURED");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("1", "count(//*[local-name()='session'])", actualResponse);
		
		//Should be 2 approved & in group 'set3'
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "50");
		req.setParameter("approved", "true");
		req.setParameter("groupName", "set3");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("2", "count(//*[local-name()='session'])", actualResponse);
		
		//Should be 2 NOT approved & in group 'set2'
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "50");
		req.setParameter("approved", "false");
		req.setParameter("groupName", "set2");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("2", "count(//*[local-name()='session'])", actualResponse);

		//Should be 1 approved, in group 'set3', and UNLISTED
		req = new GetMethodWebRequest(SESSION_SERVICE_URL);
		req.setParameter("modelId", "50");
		req.setParameter("approved", "true");
		req.setParameter("type", "UNLISTED");
		req.setParameter("groupName", "set3");
		response = client.sendRequest(req);
		actualResponse = response.getText();
		assertXpathEvaluatesTo("1", "count(//*[local-name()='session'])", actualResponse);

		PredefinedSessionsTest.deleteSessions(newSessions);
		
		
		PredefinedSessionsTest.deleteSessions(savedSessions);
	}
	
	
	public static void assignRequestParams(WebRequest req, IPredefinedSession ps) {
		req.setParameter("code", ps.getUniqueCode());
		req.setParameter("modelId", ps.getModelId().toString());
		req.setParameter("type", ps.getPredefinedSessionType().name());
		
		if (ps.getApproved() != null) {
			req.setParameter("approved", ps.getApproved()?"T":"F");
		}
		req.setParameter("name", ps.getName());
		req.setParameter("description", ps.getDescription());
		req.setParameter("sort_order", ps.getSortOrder().toString());
		req.setParameter("context_string", ps.getContextString());
		req.setParameter("add_by", ps.getAddBy());
		req.setParameter("add_note", ps.getAddNote());
		req.setParameter("add_contact_info", ps.getAddContactInfo());
		req.setParameter("group_name", ps.getGroupName());
	}
	
	
	private class PutRequest extends PostMethodWebRequest {
		public PutRequest(String url) {
			super(url);
		}
		
		@Override
		public String getMethod() {
			return "PUT";
		}
	}
}

