package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.PredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

public class LoadPredefinedSessions extends Action<List<PredefinedSession>> {

	/**
	 * A unique, user specified code that IDs a session.
	 */
	String uniqueCode;

	public LoadPredefinedSessions() {
		//default to loading all sessions
	}
	
	public LoadPredefinedSessions(String uniqueCode) {
		this.uniqueCode = uniqueCode;
	}

	/**
	 * @return ImmutableList of immutable SparrowModels
	 */
	@Override
	public List<PredefinedSession> doAction() throws Exception {


		Map<String, Object> paramMap = new HashMap<String, Object>();
		String queryName = null;	//The name of the query to run
		
		if (uniqueCode != null) {
			//Select a single record based on the code
			paramMap.put("UNIQUE_CODE", uniqueCode);
			queryName = "LoadByUniqueCodeSQL";
		} else{
			//Select all records
			queryName = "LoadAllSQL";
		}

		PreparedStatement selectSessions = null;
		ResultSet rset = null;
		List<PredefinedSession> sessions = new ArrayList<PredefinedSession>();

		selectSessions = getROPSFromPropertiesFile(queryName, null, paramMap);

		try {
			rset = selectSessions.executeQuery();
			sessions = hydrate(rset);
		} finally {
			// rset can be null if there is an sql error. This has happened with the renaming of a field
			if (rset != null) rset.close();
		}


		//Returns an ImmutableList of immutable SparrowModels.
		return ImmutableList.copyOf(sessions);
	}
	
	public List<PredefinedSession> hydrate(ResultSet rset) throws Exception {

		List<PredefinedSession> sessions = new ArrayList<PredefinedSession>();
		
		while (rset.next()) {
			PredefinedSession s = new PredefinedSession();
			s.setId(rset.getLong("PREDEFINED_SESSION_ID"));
			s.setUniqueCode(rset.getString("UNIQUE_CODE"));
			s.setModelId(rset.getLong("SPARROW_MODEL_ID"));
			
			String strType = rset.getString("PREDEFINED_TYPE");
			PredefinedSessionType sType = PredefinedSessionType.valueOf(strType);
			s.setPredefinedSessionType(sType);
			
			String strApproved = rset.getString("APPROVED");
			boolean approved = "T".equals(strApproved);
			s.setApproved(approved);
			
			s.setName(rset.getString("NAME"));
			s.setDescription(rset.getString("DESCRIPTION"));
			s.setSortOrder(rset.getInt("SORT_ORDER"));
			s.setContextString(rset.getString("CONTEXT_STRING"));
			s.setAddDate(rset.getDate("ADD_DATE"));
			s.setAddBy(rset.getString("ADD_BY"));
			s.setAddNote(rset.getString("ADD_NOTE"));
			s.setAddContactInfo(rset.getString("ADD_CONTACT_INFO"));
			s.setGroupName(rset.getString("GROUP_NAME"));
			
			
			sessions.add(s);
		}


		//Returns an ImmutableList of immutable SparrowModels.
		return ImmutableList.copyOf(sessions);
	}

}
