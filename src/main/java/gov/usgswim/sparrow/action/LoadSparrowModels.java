package gov.usgswim.sparrow.action;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;

import gov.usgswim.sparrow.cachefactory.ModelRequestCacheKey;
import gov.usgswim.sparrow.domain.SourceBuilder;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.domain.SparrowModelBuilder;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

public class LoadSparrowModels extends Action<List<SparrowModel>> {
	
	private boolean isApproved, isPublic, isArchived, getSources;
	private long sparrowModelId;

	public LoadSparrowModels(ModelRequestCacheKey key) {
		this.sparrowModelId = key.getModelId();
		this.isPublic = key.isPublic();
		this.isApproved = key.isApproved();
		this.isArchived = key.isArchived();
		this.getSources = key.isGetSources();
	}
	
	public LoadSparrowModels(boolean isApproved, boolean isPublic, boolean isArchived, boolean getSources) {
		this.sparrowModelId = -1L;
		this.isApproved = isApproved;
		this.isPublic = isPublic;
		this.isArchived = isArchived;
		this.getSources = getSources;
	}
	
	/**
	 * Constructs an action with defaults taken from loadModelsMetaData in the DataLoader class
	 * @param isPublic true
	 * @param isApproved true
	 * @param isArchived false
	 * @param getSources true
	 */
	public LoadSparrowModels() {
		this(true, true, false, true); // Default behavior defined by loadModelsMetaData(Connection)
	}
	
	public LoadSparrowModels(long id) {
		this(id, true);
	}
	
	public LoadSparrowModels(long id, boolean getSources) {
		this.sparrowModelId = id;
		this.getSources = getSources;
	}
	
	/**
	 * @return ImmutableList of immutable SparrowModels
	 */
	@Override
	protected List<SparrowModel> doAction() throws Exception {
		
		//***************** COPIED CODE *******************
		
		List<SparrowModelBuilder> models = new ArrayList<SparrowModelBuilder>(23);//magic number

		// Build filtering parameters and retrieve the queries from properties
		String[] params = {
				"IsApproved", (isApproved ? "T" : "%"),
				"IsPublic", (isPublic ? "T" : "%"),
				"IsArchived", (isArchived ? "T" : "%")
		};
		String selectModels = null;
		
		if (sparrowModelId > 0) { //specific model
			params = new String[] {"SparrowModelId", "" + sparrowModelId};
			selectModels = getTextWithParamSubstitution("SelectModelsById", params);
		} else { //default ID
			selectModels = getTextWithParamSubstitution("SelectModelsByAccess", params);
		}

		Statement stmt = null;
		ResultSet rset = null;

		try {
			stmt = getConnection().createStatement();
			stmt.setFetchSize(100);

			try {
				rset = stmt.executeQuery(selectModels);

				while (rset.next()) {
					SparrowModelBuilder m = new SparrowModelBuilder();
					long modelID = rset.getLong("SPARROW_MODEL_ID");
					m.setId(rset.getLong("SPARROW_MODEL_ID"));
					m.setApproved(StringUtils.equalsIgnoreCase("T", rset.getString("IS_APPROVED")));
					m.setPublic(StringUtils.equalsIgnoreCase("T", rset.getString("IS_PUBLIC")));
					m.setArchived(StringUtils.equalsIgnoreCase("T", rset.getString("IS_ARCHIVED")));
					m.setName(rset.getString("NAME"));
					m.setDescription(rset.getString("DESCRIPTION"));
					m.setDateAdded(rset.getDate("DATE_ADDED"));
					m.setContactId(rset.getLong("CONTACT_ID"));
					m.setEnhNetworkId(rset.getLong("ENH_NETWORK_ID"));
					m.setUrl(rset.getString("URL"));
					m.setNorthBound(rset.getDouble("BOUND_NORTH"));
					m.setEastBound(rset.getDouble("BOUND_EAST"));
					m.setSouthBound(rset.getDouble("BOUND_SOUTH"));
					m.setWestBound(rset.getDouble("BOUND_WEST"));
					m.setConstituent(rset.getString("CONSTITUENT"));
					m.setUnits(rset.getString("UNITS"));

					//************* END COPIED CODE ****************
					//StringBuilder sessions = SavedSessionService.retrieveAllSavedSessionsXML(Long.toString(modelID));
					// ^^ What is this doing? Cause right now it's nothing.
					m.setSessions(SparrowResourceUtils.retrieveAllSavedSessions(Long.toString(modelID)));
					
					//***************** COPIED CODE *******************
					models.add(m);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// rset can be null if there is an sql error. This has happened with the renaming of a field
				if (rset != null) rset.close();
			}

			if (getSources) {
				String inModelsWhereClause = " ";
				if (!models.isEmpty()) {
					List<Long> modelIds = new ArrayList<Long>();
					for (SparrowModelBuilder model: models) {
						modelIds.add(model.getId());
					}
					inModelsWhereClause = " WHERE SPARROW_MODEL_ID in (" + StringUtils.join(modelIds.toArray(), ", ") + ") ";

				}
				String selectSources = getTextWithParamSubstitution("SelectAllSources", "InModels", inModelsWhereClause);

				try {
					rset = stmt.executeQuery(selectSources);

					while (rset.next()) {
						SourceBuilder s = new SourceBuilder();
						s.setId(rset.getLong("SOURCE_ID"));
						s.setName(rset.getString("NAME"));
						s.setDescription(rset.getString("DESCRIPTION"));
						s.setSortOrder(rset.getInt("SORT_ORDER"));
						s.setModelId(rset.getLong("SPARROW_MODEL_ID"));
						s.setIdentifier(rset.getInt("IDENTIFIER"));
						s.setDisplayName(rset.getString("DISPLAY_NAME"));
						s.setConstituent(rset.getString("CONSTITUENT"));
						s.setUnits(rset.getString("UNITS"));

						//The models and sources are sorted by model_id, so scroll forward
						//thru the models until we find the correct one.
						int modelIndex = 0;
						while ((modelIndex < models.size() &&
								models.get(modelIndex).getId() != s.getModelId()) /* don't scroll past last model*/) {
							modelIndex++;
						}

						if (modelIndex < models.size()) {
							models.get(modelIndex).addSource(s);
						} else {
							log.warn("Found sources not matched to a model.  Likely caused by record insertion during the queries.");
						}
					}
				} catch(Exception e) {
					e.printStackTrace(System.err);

				} finally {
					rset.close();
				}
			}
		} finally {
			stmt.close();
		}
		
		//************* END COPIED CODE ****************
		
		List<SparrowModel> result = new ArrayList<SparrowModel>();
		for (SparrowModelBuilder builder: models) {
			result.add(builder.toImmutable());
		}

		//Returns an ImmutableList of immutable SparrowModels.
		return ImmutableList.copyOf(result);
	}

}
