package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.ReachFullId;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.request.ReachClientId;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Public face of fetching reach full ID collections.
 *
 * This will first check for IDs in the cache, then fetch any remaining ones
 * from the db.
 *
 * @author eeverman
 */
public class ReachFullIdCollectionAction extends Action<List<ReachFullId>> {

	protected Collection<ReachClientId> clientIds;

	//Action initialized var
	protected Long _modelId = null;	//Model ID - all reaches must have the same one
	protected boolean canUseSimpleConversion = false;	//if true, don't got to db, just convert strings to Longs.

	public ReachFullIdCollectionAction(Collection<ReachClientId> clientIds) {
		this.clientIds = clientIds;
	}

	@Override
	protected void validate() {
		if (clientIds == null || clientIds.isEmpty()) {
			addValidationError("The clientIds cannot be null or empty");
			return;
		}

		Long modelId = null;

		for (ReachClientId id : clientIds) {
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
		_modelId = clientIds.iterator().next().getModelID();
		
		ModelRequestCacheKey modelKey = new ModelRequestCacheKey(_modelId, false, false, false);
		List<SparrowModel> modelList = SharedApplication.getInstance().getModelMetadata(modelKey);
		
		if (modelList != null && modelList.size() == 1) {
			canUseSimpleConversion = modelList.get(0).isUsingSimpleReachIds();
		}
		
	}

	@Override
	public List<ReachFullId> doAction() throws Exception {

		if (canUseSimpleConversion) {
			return doSimpleConversion();
		} else {
			return doComplexConversion();
		}

	}
	
	private ArrayList<ReachFullId> doComplexConversion() throws Exception {
		Collection<ReachClientId> modifiableIds = new ArrayList<ReachClientId>();
		modifiableIds.addAll(clientIds);
		
		ArrayList<ReachFullId> result = new ArrayList<ReachFullId>(modifiableIds.size());

		Iterator<ReachClientId> modifiableIdsIt = modifiableIds.iterator();

		//Fetch all the full IDs that are already in the cache
		while (modifiableIdsIt.hasNext()) {
			ReachClientId currentClientId = modifiableIdsIt.next();
			ReachFullId currentFullId = SharedApplication.getInstance().getReachFullId(currentClientId, true);	//don't create if not present

			if (currentFullId != null) {
				result.add(currentFullId);
				modifiableIdsIt.remove();
			}

		}

		//Fetch all the others in one big batch from the DB
		ReachFullIdCollectionFromDbAction action = new ReachFullIdCollectionFromDbAction(modifiableIds);
		result.addAll(action.run());

		return result;
	}

	private ArrayList<ReachFullId> doSimpleConversion() {
		
		ArrayList<ReachFullId> result = new ArrayList<ReachFullId>(clientIds.size());

		Iterator<ReachClientId> iterator = clientIds.iterator();

		//Fetch all the full IDs that are already in the cache
		while (iterator.hasNext()) {
			ReachClientId currentClientId = iterator.next();
			String clientIdStr = currentClientId.getReachClientId();
			ReachFullId currentFullId = new ReachFullId(_modelId, Long.parseLong(clientIdStr), clientIdStr);
			result.add(currentFullId);
		}

		return result;
	}
}
