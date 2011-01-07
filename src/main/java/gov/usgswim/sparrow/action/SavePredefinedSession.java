package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;

public class SavePredefinedSession extends Action<IPredefinedSession> {

	PredefinedSessionBuilder session;

	public SavePredefinedSession(IPredefinedSession session) {
		this.session = new PredefinedSessionBuilder(session);
	}

	/**
	 * @return ImmutableList of immutable SparrowModels
	 */
	@Override
	public IPredefinedSession doAction() throws Exception {

		String actionName = null;
		IPredefinedSession newSession = null;
		
		//Empty params
		Map<String, Object> paramMap = new HashMap<String, Object>();

		int updateCount = 0;	//The number of rows updated/inserted
		PreparedStatement statement = null;
		ResultSet rset = null;
		List<PredefinedSession> sessions = new ArrayList<PredefinedSession>();
		
		//
		//assign values to parameters
		if (session.getId() != null) {
			//This is an update
			paramMap.put("PREDEFINED_SESSION_ID", session.getId());
			
			//Updates may set the approved value
			String strApproved = (session.getApproved()?"T":"F");
			paramMap.put("APPROVED", strApproved);
		} else {
			//This is a new record - autoset the add date
			//Construct a calendar date for today that does not include time.
			GregorianCalendar today = new GregorianCalendar();
			today = new GregorianCalendar(
					today.get(Calendar.YEAR),
					today.get(Calendar.MONTH),
					today.get(Calendar.DAY_OF_MONTH));
			paramMap.put("ADD_DATE", new Date(today.getTimeInMillis()));
			
			//New records are never approved
			paramMap.put("APPROVED", "F");
		}
		
		if (session.getUniqueCode() != null) {
			paramMap.put("UNIQUE_CODE", session.getUniqueCode());
		} else {
			String rndString = RandomStringUtils.random(5, "abcdefghjkmnpqrstuvwxyz23456789");
			session.setUniqueCode(rndString);	//new to record it so we can fetch back
			paramMap.put("UNIQUE_CODE", rndString);
		}
		
		
		
		
		paramMap.put("SPARROW_MODEL_ID", session.getModelId());
		paramMap.put("PREDEFINED_TYPE", session.getPredefinedSessionType().toString());

		paramMap.put("NAME", session.getName());
		paramMap.put("DESCRIPTION", session.getDescription());
		paramMap.put("SORT_ORDER", session.getSortOrder());
		paramMap.put("CONTEXT_STRING", session.getContextString());
		paramMap.put("GROUP_NAME", session.getGroupName());
		
		//Allow these fields to be edited, but not the add_date
		paramMap.put("ADD_BY", session.getAddBy());
		paramMap.put("ADD_NOTE", session.getAddNote());
		paramMap.put("ADD_CONTACT_INFO", session.getAddContactInfo());
		

		if (session.getId() != null) {
			statement = getRWPSFromPropertiesFile("UpdateSQL", null, paramMap);
			actionName = "update";
			
			//assume success
			setPostMessage("PredefinedSession id #" + session.getId() +
					" (" + session.getUniqueCode() + ") was successfully updated.");
		} else {
			statement = getRWPSFromPropertiesFile("InsertSQL", null, paramMap);
			actionName = "create";
			
			//assume success
			this.setPostMessage("PredefinedSession w/ unique code " +
					session.getUniqueCode() + " was successfully inserted.");
		}
		
		
		try {
			updateCount = statement.executeUpdate();
		} catch (Exception e) {
			setPostMessage("Could not run " + actionName + " statement.");
			throw e;
		}
		
		if (updateCount != 1) {
			throw new Exception("The updated should effect exactly one record. " +
				"Instead, " + updateCount + " records were affected.");
		}
		
		LoadPredefinedSessions lps = new LoadPredefinedSessions(session.getUniqueCode());
		List<IPredefinedSession> sessionList = lps.run();
		
		
		if (sessionList.size() != 1) {
			throw new Exception("Unable to retrieve the saved PredefinedSession back from the db.");
		}
		
		newSession = lps.run().get(0);
		
		if (newSession == null || ! session.getUniqueCode().equals(newSession.getUniqueCode())) {
			throw new Exception(
					"Failed to retrieve the newly created PredefinedSession, " +
					"UniqueCode: " + session.getUniqueCode());
		}

		return newSession;
	}

}
