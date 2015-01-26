package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.ReachFullId;
import gov.usgswim.sparrow.request.ReachClientId;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Loads a list of ReachFullIds from a list of ReachClientIds.
 * 
 * This provides a mapping between the two, going back to the db to populate the
 * list.  Oracle limits the number of params in an IN clause (used in the SQL by
 * this class), however, this Action will break up the list into multiple queries
 * if needed.
 * 
 * Notes that the request and result list of this Action should not be cached
 * because it is unlikely that the entire same list will be requested again.
 * Instead, cache each individual reach result.
 * 
 * @author eeverman
 */
public class ReachFullIdCollectionFromDbAction extends Action<List<ReachFullId>> {

	protected Collection<ReachClientId> initialClientIds;
	
	//Action initialized var
	protected Long _modelId = null;	//Model ID - all reaches must have the same one

	
	public ReachFullIdCollectionFromDbAction(Collection<ReachClientId> clientIds) {
		this.initialClientIds = clientIds;
	}

	@Override
	protected void validate() {
		if (initialClientIds == null || initialClientIds.isEmpty()) {
			addValidationError("The clientIds cannot be null or empty");
			return;
		}
		
		Long modelId = null;
		
		for (ReachClientId id : initialClientIds) {
			if (modelId == null) {
				modelId = id.getModelID();
			} else {
				if (! id.getModelID().equals(modelId)) {
					addValidationError("All the client IDs must have the same modelId");
					return;
				}
			}
			
		}
	}
	
	@Override
	protected void initFields() {
		_modelId = initialClientIds.iterator().next().getModelID();
	}
	
	

	@Override
	public List<ReachFullId> doAction() throws Exception {
		ArrayList<Collection<ReachClientId>> idSets = splitIdsInto1KSets(initialClientIds);
		ArrayList<ReachFullId> results = new ArrayList<ReachFullId>(initialClientIds.size());
		
		for (Collection<ReachClientId> oneSet : idSets) {
			ArrayList<ReachFullId> oneResultSet = fetchFromDb(_modelId, oneSet);
			results.addAll(oneResultSet);
		}
		
		return results;
	}
	
	protected ArrayList<ReachFullId> fetchFromDb(Long modelId, Collection<ReachClientId> clientIds) throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		ArrayList<String> clientIdStrs = new ArrayList<String>();
		
		Iterator<ReachClientId> it = clientIds.iterator();
		
		while (it.hasNext()) {
			clientIdStrs.add(it.next().getReachClientId());
		}
		
		params.put("modelId", modelId);
		params.put("clientIds", clientIdStrs);

		ResultSet rs = getROPSFromPropertiesFile("LoadReachIds", getClass(), params).executeQuery();
		addResultSetForAutoClose(rs);
		
		
		ArrayList<ReachFullId> result = new ArrayList<ReachFullId>();
		while (rs.next()) {
			Long id = rs.getLong(1);	//reach ID
			String clientReachId = rs.getString(2);	//client reach ID
			ReachFullId fullId = new ReachFullId(modelId, id, clientReachId);
			result.add(fullId);
		}
		
		//Preemptive, otherwise we wait for the entire action to finish
		rs.close();
		
		return result;
	}
	
		/**
	 * Fetches the list of ReachFullIds from the DB.
	 * 
	 * This handles one key thing:  It splits lists of more than 1000 items into
	 * smaller lists of 1000 to be compatible w/ the Oracle IN clause.
	 * @param clientReachIds
	 * @return 
	 */
	protected ArrayList<Collection<ReachClientId>> splitIdsInto1KSets(Collection<ReachClientId> clientReachIds) {
		//ArrayList<ReachFullId> result = new ArrayList<ReachFullId>(clientReachIds.size());
		ArrayList<Collection<ReachClientId>> reqListOfLists = new ArrayList<Collection<ReachClientId>>();
		
		if (clientReachIds.size() < 1001) {
			reqListOfLists.add(clientReachIds);
		} else {
			int currentCountInCurrentList = 0;
			ArrayList<ReachClientId> oneList = new ArrayList<ReachClientId>(1000);
			reqListOfLists.add(oneList);
			Iterator<ReachClientId> reachClientIdIt = clientReachIds.iterator();
			
			while (reachClientIdIt.hasNext()) {
				ReachClientId currentReachClientId = reachClientIdIt.next();
				
				if (currentCountInCurrentList >= 1000) {
					oneList = new ArrayList<ReachClientId>(1000);
					reqListOfLists.add(oneList);
					currentCountInCurrentList = 0;
				}
				
				oneList.add(currentReachClientId);
				currentCountInCurrentList++;
			}
		}
		
		return reqListOfLists;
		
	}

	@Override
	public Long getModelId() {
		return _modelId;
	}
}
