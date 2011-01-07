package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.request.PredefinedSessionRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.google.common.collect.ImmutableList;

public class FilterPredefinedSessions extends Action<List<IPredefinedSession>> {


	PredefinedSessionRequest request;

	public FilterPredefinedSessions(PredefinedSessionRequest request) {
		this.request = request;
	}
	


	/**
	 * @return A list of PredefinedSessions, filtered as requested
	 */
	@Override
	public List<IPredefinedSession> doAction() throws Exception {

		
		List<IPredefinedSession> unfiltered =
			SharedApplication.getInstance().loadPredefinedSessions(request.getModelId());
		
		List<IPredefinedSession> filtered = null;
		
		
		//if a uniqueCode is spec'ed, there is no other filtering to do,
		//just find the one record
		if (request.getUniqueCode() != null) {
			
			filtered = new ArrayList<IPredefinedSession>(1);
			
			for (IPredefinedSession s : unfiltered) {
				if (request.getUniqueCode().equalsIgnoreCase(s.getUniqueCode())) {
					filtered.add(s);
				}
			}
			
			return filtered;
		} else {
			//Otherwise apply the other filters
			filtered = filter(unfiltered);
		}

		return filtered;
	}
	
	public List<IPredefinedSession> filter(List<IPredefinedSession> unfiltered) throws Exception {

		List<IPredefinedSession> filtered = new ArrayList<IPredefinedSession>(unfiltered);
		ListIterator<IPredefinedSession> it = null;
		
		//filter based on Approved
		if (request.getApproved() != null) {
			it = filtered.listIterator();
			while (it.hasNext()) {
				IPredefinedSession s = it.next();
				if (! request.getApproved().equals(s.getApproved())) {
					it.remove();
				}
			}
		}
		
		//filter based on predefinedSessionType
		if (request.getPredefinedSessionType() != null) {
			it = filtered.listIterator();
			while (it.hasNext()) {
				IPredefinedSession s = it.next();
				if (! request.getPredefinedSessionType().equals(s.getPredefinedSessionType())) {
					it.remove();
				}
			}
		}
		
		//filter based on groupName
		if (request.getGroupName() != null) {
			it = filtered.listIterator();
			while (it.hasNext()) {
				IPredefinedSession s = it.next();
				if (! request.getGroupName().equals(s.getGroupName())) {
					it.remove();
				}
			}
		}

		return filtered;

	}
	

}
