package gov.usgswim.sparrow.action;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ImmutableList;

import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.domain.SourceBuilder;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.domain.SparrowModelBuilder;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.request.PredefinedSessionRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

public class LoadModelMetadata extends Action<List<SparrowModel>> {

	/**
	 * Assign a system property with this value to 'true' to skip loading the
	 * predefined themes during metadata loading.  This is currently for one
	 * use case:  During model testing when a model is first loaded, the
	 * ComparePredictionToTextLongRunTest is run which loads the model data
	 * from the db and the predicted values from the text file and compares
	 * the result.
	 * 
	 * The predefined sessions are stored in the transactional db, which has
	 * a different pwd / login info than the db where the model data is kept.
	 * The test user is prompted for the pwd for the main db and we don't want
	 * to also prompt for the transactional db, especially since the predefined
	 * session data is not required for the test.
	 */
	public static final String SKIP_LOADING_PREDEFINED_THEMES = "skip_loading_predefined_themes";
	
	private boolean isApproved, isPublic, isArchived, getSources;
	private Long sparrowModelId;

	public LoadModelMetadata(ModelRequestCacheKey key) {
		this.sparrowModelId = key.getModelId();
		this.isPublic = key.isPublic();
		this.isApproved = key.isApproved();
		this.isArchived = key.isArchived();
		this.getSources = true;
	}

	public LoadModelMetadata(boolean isApproved, boolean isPublic, boolean isArchived, boolean getSources) {
		this.sparrowModelId = null;
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
	public LoadModelMetadata() {
		this(true, true, false, true); // Default behavior defined by loadModelsMetaData(Connection)
	}

	public LoadModelMetadata(Long id, boolean getSources) {
		this.sparrowModelId = id;
		this.getSources = getSources;
	}

	/**
	 * @return ImmutableList of immutable SparrowModels
	 */
	@Override
	public List<SparrowModel> doAction() throws Exception {

		List<SparrowModelBuilder> models = new ArrayList<SparrowModelBuilder>(23);//magic number

		// Build filtering parameters and retrieve the queries from properties
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("IsApproved", (isApproved ? "T" : "%"));
		paramMap.put("IsPublic", (isPublic ? "T" : "%"));
		paramMap.put("IsArchived", (isArchived ? "T" : "%"));

		PreparedStatement selectModels = null;

		if (sparrowModelId != null) { //specific model
			paramMap = new HashMap<String, Object>();
			paramMap.put("SparrowModelId", new Long(sparrowModelId));
			selectModels = getROPSFromPropertiesFile("SelectModelsById", null, paramMap);
		} else { //default ID
			selectModels = getROPSFromPropertiesFile("SelectModelsByAccess", null, paramMap);
		}

		ResultSet rset = null;

		try {
			rset = selectModels.executeQuery();
			addResultSetForAutoClose(rset);

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
				m.setEnhNetworkName(rset.getString("ENH_NAME"));
				m.setEnhNetworkUrl(rset.getString("ENH_URL"));
				m.setEnhNetworkIdColumn(rset.getString("ENH_ID_COL_NAME"));
				m.setThemeName(rset.getString("THEME_NAME"));
				m.setUrl(rset.getString("URL"));
				m.setNorthBound(rset.getDouble("BOUND_NORTH"));
				m.setEastBound(rset.getDouble("BOUND_EAST"));
				m.setSouthBound(rset.getDouble("BOUND_SOUTH"));
				m.setWestBound(rset.getDouble("BOUND_WEST"));
				m.setConstituent(rset.getString("CONSTITUENT"));
				String sUnits = rset.getString("UNITS");
				
				//assume the string form of the unit is the enum name
				SparrowUnits unit = SparrowUnits.parse(sUnits);
				m.setUnits(unit);


				if ("true".equals(System.getProperty(SKIP_LOADING_PREDEFINED_THEMES))) {
					//Don't load the predefined sessions - we just want the basic
					//data for model testing.
					m.setSessions(new ArrayList<IPredefinedSession>());
				} else {
					PredefinedSessionRequest psRequest = new PredefinedSessionRequest(modelID, true, PredefinedSessionType.FEATURED);
					List<IPredefinedSession> sessions = SharedApplication.getInstance().getPredefinedSessions(psRequest);
					m.setSessions(sessions);
				}
				
				models.add(m);
			}

		} finally {
			// rset can be null if there is an sql error. This has happened with the renaming of a field
			if (rset != null) rset.close();
		}

		if (getSources) {
			StringBuilder inModelsWhereClause = new StringBuilder();
			Map<String, Object> modelIds = new HashMap<String, Object>();
			inModelsWhereClause.append(" ");
			if (!models.isEmpty()) {
				inModelsWhereClause.append(" WHERE SPARROW_MODEL_ID in (");
				for (int i = 0; i < models.size(); i++) {
					modelIds.put("" + i, models.get(i).getId());
					inModelsWhereClause.append("$" + i + "$, ");
				}
				// kill the last comma
				inModelsWhereClause.delete(inModelsWhereClause.length() - 2, inModelsWhereClause.length());
				inModelsWhereClause.append(") ");

			}
			String selectSources = getTextWithParamSubstitution("SelectAllSources", "InModels", inModelsWhereClause);

			try {
				
				rset = getROPSFromString(selectSources, modelIds).executeQuery();
				int modelIndex = 0;
				
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
					s.setUnits(SparrowUnits.parse(rset.getString("UNITS")));

					//The models and sources are sorted by model_id, so scroll forward
					//thru the models until we find the correct one.
					while (
								(modelIndex < models.size() &&
								!(models.get(modelIndex).getId().equals(s.getModelId())))
							) {
						modelIndex++;
					}

					if (modelIndex < models.size()) {
						models.get(modelIndex).addSource(s);
					} else {
						throw new Exception("Found sources not matched to a model.  Likely caused by record insertion during the queries.");
					}
				}
			} finally {
				rset.close();
			}
		}


		List<SparrowModel> result = new ArrayList<SparrowModel>();
		for (SparrowModelBuilder builder: models) {
			result.add(builder.toImmutable());
		}

		//Returns an ImmutableList of immutable SparrowModels.
		return ImmutableList.copyOf(result);
	}

}
