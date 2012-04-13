package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.service.ConfiguredCache.PredefinedSessions;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.request.PredefinedSessionRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

		List<IPredefinedSession> unfiltered = null;
		List<IPredefinedSession> filtered = null;
		
		if (request.getModelId() != null) {
			unfiltered =
				loadPredefinedSessions(request.getModelId());
			
			filtered = filter(unfiltered);
		} else {
			throw new Exception("No model ID is specified, but is required");
		}

		return filtered;
	}
	
	//PredefinedSessions Cache By Model
	@SuppressWarnings("unchecked")
	public List<IPredefinedSession> loadPredefinedSessions(Long modelId) {
		return (List<IPredefinedSession>) PredefinedSessions.get(modelId, false);
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
