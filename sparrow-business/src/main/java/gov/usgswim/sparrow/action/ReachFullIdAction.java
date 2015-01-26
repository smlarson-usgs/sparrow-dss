package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.ReachFullId;
import gov.usgswim.sparrow.request.ReachClientId;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple wrapper to handle the case where there is only a single Full reach ID
 * requested.
 * 
 * @author eeverman
 */
public class ReachFullIdAction extends Action<ReachFullId> {

	protected ReachClientId clientId;
	
	public ReachFullIdAction(ReachClientId clientId) {
		this.clientId = clientId;
		
		
		//check for over 300 limit
		//check that all have the same modelID
	}

	@Override
	protected void validate() {
		if (clientId == null) {
			addValidationError("The clientId cannot be null");
			return;
		}
	}

	@Override
	public ReachFullId doAction() throws Exception {
		ArrayList<ReachClientId> list = new ArrayList<ReachClientId>(1);
		ReachFullIdCollectionFromDbAction act = new ReachFullIdCollectionFromDbAction(list);
		List<ReachFullId> resultList = act.run();
		
		if (resultList.size() == 1) {
			return resultList.get(0);
		} else {
			return null;
		}
	}

	@Override
	public Long getModelId() {
		if (clientId != null) {
			return clientId.getModelID();
		} else {
			return null;
		}
	}
}
