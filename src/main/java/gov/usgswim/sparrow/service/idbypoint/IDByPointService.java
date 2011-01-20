package gov.usgswim.sparrow.service.idbypoint;

import gov.usgswim.ThreadSafe;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.filter.ColumnRangeFilter;
import gov.usgswim.datatable.filter.FilteredDataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowModelProperties;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.Adjustment;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.ReachElement;
import gov.usgswim.sparrow.parser.ReachGroup;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.ReturnStatus;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.QueryLoader;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

/**
 * @author ilinkuo
 */
@ThreadSafe
public class IDByPointService implements HttpService<IDByPointRequest> {

	// =============
	// STATIC FIELDS
	// =============
	/** Logger for this class. */
	public static final Logger log = Logger.getLogger(IDByPointService.class);

	/** Response type. */
	public static final String RESPONSE_MIME_TYPE = "application/xml";

	/** Default format for numbers. */
	public static final NumberFormat formatter = new DecimalFormat("#0.00");

	/** Resource path to the properties file for this service. */
	private static final String PROP_FILE = "gov/usgswim/sparrow/service/idbypoint/IDByPointServiceTemplate.properties";

	/** They promise these factories are threadsafe */
	@SuppressWarnings("unused")
	private static Object factoryLock = new Object();

	/** protected static XMLInputFactory xinFact */
	protected static XMLOutputFactory xoFact;

	protected static final Map<String, DisplayRule> DISPLAY_RULES = getDisplayRules();

	// =====================
	// PUBLIC STATIC METHODS
	// =====================
	/**
	 * Returns the percentage difference between {@code newVal} and {@code oldVal}
	 * with respect to {@code oldVal}.  In other words, this method returns the
	 * percentage that {@code oldVal} has changed.
	 *
	 * @param newVal The new value.
	 * @param oldVal The old value.
	 * @return The percentage difference between {@code newVal} and {@code oldVal}.
	 */
	public static Double calculatePercentageChange(Double newVal, Double baseVal) {
		if ( newVal.equals(baseVal)) {
			// This takes care of the case when both nominal and predicted are zero
			return 0D;
		} else if (!baseVal.equals(0D)) {
			return 100 * (newVal - baseVal) / baseVal;
		}
		return Double.NaN; // what remains is division by zero because baseVal == 0 and newVal != 0
	}

	protected static Map<String, DisplayRule> getDisplayRules(){
		Map<String, DisplayRule> result = new HashMap<String, DisplayRule>();

		result.put("Transmits", new ZeroOneAsTrueFalse());
		result.put("Shore Reach", new ZeroOneAsTrueFalse());
		result.put("Terminates in Transport", new ZeroOneAsTrueFalse());
		result.put("Terminates in Estuary", new ZeroOneAsTrueFalse());
		result.put("Terminates in No Connection", new ZeroOneAsTrueFalse());
		return result ;
	}


	// ===============
	// INSTANCE FIELDS
	// ===============
	/** Properties helper for this object. */
	private QueryLoader props = new QueryLoader(PROP_FILE);

	// ===========
	// CONSTRUCTOR
	// ===========
	public IDByPointService() {}

	// ================
	// INSTANCE METHODS
	// ================
	public XMLStreamReader getXMLStreamReader(PipelineRequest o, boolean isNeedsFlattening)  throws Exception{
		return getXMLStreamReader((IDByPointRequest) o, isNeedsFlattening);
	}

	public XMLStreamReader getXMLStreamReader(IDByPointRequest req, boolean isNeedsFlattening) throws Exception {
		// TODO isNeedsFlattening ignored for now because using custom flattener

		// Retrieve the model ID		
		updateRequestModelIDIfNecessary(req);
		
		assert(req.getModelID() != null);

		// Retrieve the reach
		ReachInfo[] reach = retrieveReaches(req);
		IDByPointResponse[] response = new IDByPointResponse[reach.length];
		
		//Aggregate status, since there may be many reaches.
		ReturnStatus aggStatus = ReturnStatus.OK;
		String aggMessage = null;
		
		if (reach.length > 0) {
			
			for (int i = 0; i < reach.length; i++) {
				response[i] = new IDByPointResponse();
				response[i].modelID = req.getModelID();
				
				if (reach[i] != null) {
					response[i].setReach(reach[i]);
	
					// populate each of the sections
					response[i].mapValueXML = buildValueSection(req, i, reach[i]);
					
					if (req.hasAdjustments()) {
						retrieveAdjustments(req.getContextID(), req, response[i]);
					}
					if (req.hasAttributes()) {
						retrieveAttributes(req, response[i]);
					}
					if (req.hasPredicted()) {
						response[i].predictionsXML = retrievePredictedsForReach(req.getContextID(), req.getModelID(), Long.valueOf(response[i].reachID));
					}
					
					response[i].status = ReturnStatus.OK;
				} else {
					if (req.getReachID() != null) {
						response[i].status = ReturnStatus.ID_NOT_FOUND;
						response[i].message = "Could not find a reach with the specified ID";
					} else {
						response[i].status = ReturnStatus.OK_EMPTY;
						response[i].message = "Could not find a reach near the specified point";
					}
				}
			}

		} else {
			//No reaches were found, so create one placeholder response instance
			response = new IDByPointResponse[1];
			response[0] = new IDByPointResponse();
			response[0].modelID = req.getModelID();
			if (req.getReachID() != null) {
				response[0].status = ReturnStatus.ID_NOT_FOUND;
				response[0].message = "Could not find a reach with the specified ID";
			} else {
				response[0].status = ReturnStatus.OK_EMPTY;
				response[0].message = "Could not find a reach near the specified point";
			}
		}
		
		//Build agg status
		for (IDByPointResponse resp : response) {
			if (resp == null) {
				aggStatus = ReturnStatus.ERROR;
			} else if (! ReturnStatus.OK.equals(resp.status)) {
				//Take any non-OK status as the full status
				aggStatus = resp.status;
				aggMessage = resp.message;
			}
		}
		
		//Build full output string
		StringBuffer aggResponse = new StringBuffer();
		
		aggResponse.append(
			IDByPointResponse.writeXMLHead(aggStatus, aggMessage, req.getModelID(), req.getContextID())
		);
		for (IDByPointResponse resp : response) {
			aggResponse.append(resp.toXML());
		}
		aggResponse.append(IDByPointResponse.writeXMLFoot());
		
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(aggResponse.toString()));
		
		return reader;
	}

	public void shutDown() {
		xoFact = null;
	}
	// =======================
	// PRIVATE HELPER METHODS
	// =======================
	
	
	/**
	 * Builds the xml section for the predicted value or null if there is no context.
	 * @throws IOException 
	 */
	private String buildValueSection(IDByPointRequest req, int idIndex, ReachInfo reachInfo) throws Exception {
		if (req.getContextID() != null) {
			PredictionContext pc =
				SharedApplication.getInstance().getPredictionContext(req.getContextID());
			
			if (pc != null) {
				PredictData pd = SharedApplication.getInstance().getPredictData(pc.getModelID());
				
				DataColumn data = SharedApplication.getInstance().getAnalysisResult(pc);
				
				int row = pd.getRowForReachID(reachInfo.getId());
				
				Double value = data.getDouble(row);
				String displayValue = "[No Value Calculated]";	//only if null
				if (value != null) {
					displayValue = formatter.format(value);
				}
				String units = data.getUnits();
				String constituent = data.getConstituent();
				String name = data.getColumnName();
				String description = data.getDescription();
				
				if (units == null) {
					units = "Units Unknown";
				}
				
				if (constituent == null) {
					constituent = "";	//keep it empty so it won't display on client
				}
				
				if (description == null) {
					description = "";	//keep it empty so it won't display on client
				}
				
				String[] params = {
						"value", displayValue,
						"name", name,
						"description", description,
						"units", units,
						"constituent", constituent
				};
				String xmlResult = props.getParametrizedQuery("mappedXMLResponse", params);
				return xmlResult;
			}
			
		}
		return null;
	}
	
	/**
	 * If a point has no close streams, an empty array is returned.
	 * If reach IDs are used, an array of the same size as the list of IDs is
	 * returned, but some of those results may be zero if they are not found.
	 */
	private ReachInfo[] retrieveReaches(IDByPointRequest req) throws Exception {
		if (req.getReachID() != null) {
			int[] reachIds = req.getReachID();
			ReachInfo[] reachResults = new ReachInfo[reachIds.length];
			
			for (int i = 0; i < reachIds.length; i++) {
				ReachID reach = new ReachID(req.getModelID(), reachIds[i]);
				
				reachResults[i] = SharedApplication.getInstance().getReachByIDResult(reach);
			}
			return reachResults;
		} else if (req.getPoint() != null) {
			
			ReachInfo[] reachResults = null;
			
			ReachInfo ri = SharedApplication.getInstance().getReachByPointResult(
					new ModelPoint(req.getModelID(), req.getPoint()));
			
			if (ri != null) {
				reachResults = new ReachInfo[] { ri };
			} else {
				reachResults = new ReachInfo[0]; //default empty response
			}
			
			return reachResults;
		} else {
			throw new Exception("A context-id or a model-id is required for a id request");
		}
	}

//	private Long populateModelID(IDByPointRequest req) throws Exception {
//		if (req.getContextID() != null) {
//			PredictionContext context = SharedApplication.getInstance().getPredictionContext(req.getContextID());
//			if (context == null) throw new RuntimeException("Prediction Context with id "
//					+ req.getContextID() + " has not been registered. Perhaps the server has been restarted?");
//			if (req.getModelID() != null && !req.getModelID().equals(context.getModelID())) {
//				throw new RuntimeException("Mismatched model-ids, PredictionContext: " + context.getModelID()
//						+ ", request: " + req.getModelID());
//			}
//			return context.getModelID();
//		} else if (req.getModelID() != null) {
//			return req.getModelID();
//		} else {
//			throw new RuntimeException("A context-id or a model-id is required for a id request");
//		}
//	}
	
	private void updateRequestModelIDIfNecessary(IDByPointRequest req) throws Exception {
		if (req.getContextID() != null) {
			PredictionContext context = SharedApplication.getInstance().getPredictionContext(req.getContextID());
			if (context == null) throw new RuntimeException("Prediction Context with id "
					+ req.getContextID() + " has not been registered. Perhaps the server has been restarted?");
			
			else if (req.getModelID() != null && !req.getModelID().equals(context.getModelID())) {
				throw new RuntimeException("Mismatched model-ids, PredictionContext: " + context.getModelID()
						+ ", request: " + req.getModelID());
			} else if (req.getModelID() == null) {
				if (!req.updateModelId(context)) {
					throw new RuntimeException("Unable to update "
							+ req.getClass().getSimpleName() + " model-id via associated PredictionContext");
				}
			}
		} else if (req.getModelID() != null) {
			// do nothing, just return
		} else {
			throw new RuntimeException("A context-id or a model-id is required for a id request");
		}
	}
	

	/**
	 * Populates the adjustments section in the {@code response}.
	 *
	 * @param contextId The prediction context id.
	 * @param req The service request object.
	 * @param response The service response object.
	 */
	private void retrieveAdjustments(Integer contextId, IDByPointRequest req,
			IDByPointResponse response) throws Exception {
		//	TODO move to DataLoader when done debugging

		if (contextId == null || contextId.equals(0)) {
			return; // no prediction context means no adjustments
		}

		// Get the unadjusted model data using the prediction context's model id
		PredictionContext context = SharedApplication.getInstance().getPredictionContext(contextId);
		PredictData nomPredictData = SharedApplication.getInstance().getPredictData(context.getModelID());

		// Get the adjusted data using the adjustment groups from the prediction context
		AdjustmentGroups adjGroups = context.getAdjustmentGroups();
		adjGroups = (adjGroups == null) ? new AdjustmentGroups(context.getModelID()) : adjGroups;
		DataTable adjSrc = SharedApplication.getInstance().getAdjustedSource(adjGroups);

		// Build the xml fragment for the reach's adjustment data
		response.adjustmentsXML = buildAdjustment(nomPredictData, adjSrc, context.getModelID(), response.reachID, adjGroups);
	}

	/**
	 * Returns an XML fragment consisting of the adjustment data for the
	 * specified reach.  The adjustment data includes original values and
	 * adjusted values for each source in the given model.  Metadata for each
	 * source is also included with this fragment which includes name,
	 * constituent, units, and precision.
	 *
	 * @param predictData The original (nominal) data set for the current
	 *                    prediction context.
	 * @param adjSrc The adjusted data set for the current prediction context.
	 * @param MODEL_ID Current model id.
	 * @param reachIDs Id for the reach we're identifying.
	 * @param adjGroups Adjustment request.
	 * @return An XML fragment containing the adjustment data for the specified
	 *         reach.
	 */
	private String buildAdjustment(PredictData predictData, DataTable adjSourceData,
			Long modelId, Long reachId, AdjustmentGroups adjGroups) throws Exception {

		// Retrieve the data tables for the original data and the source metadata
		// Note that the adjusted data is already in the correct format
		DataTable origSourceData = predictData.getSrc();
		DataTable sourceMetadata = predictData.getSrcMetadata();
		assert(sourceMetadata.getRowCount() == origSourceData.getColumnCount());

		// Get the row index in the original data for the reach we're identifying
		int rowID = predictData.getRowForReachID(reachId);

		// Get the column indices for the metadata
		Integer displayNameCol = sourceMetadata.getColumnByName("DISPLAY_NAME");
		Integer constituentCol = sourceMetadata.getColumnByName("CONSTITUENT");
		Integer unitsCol = sourceMetadata.getColumnByName("UNITS");
		Integer precisionCol = sourceMetadata.getColumnByName("PRECISION");

		// Build each row of the adjustment
		StringBuilder adjustmentRows = new StringBuilder();
		for (int j = 0; j < sourceMetadata.getRowCount(); j++) {

			// Pull the source's metadata
			Long sourceId = sourceMetadata.getIdForRow(j);
			String sourceName = sourceMetadata.getString(j, displayNameCol);
			String constituent = sourceMetadata.getString(j, constituentCol);
			String units = sourceMetadata.getString(j, unitsCol);
			Long precision = sourceMetadata.getLong(j, precisionCol);

			// Get the data values (original, override, and adjusted)
			Double origValue = origSourceData.getDouble(rowID, j);
			Double overrideValue = getOverrideValue(adjGroups, reachId, sourceId);
			String override = (overrideValue == null) ? "" : formatter.format(overrideValue);
			Double adjValue = adjSourceData.getDouble(rowID, j);

			// Put together the XML string - columns for metadata and data
			adjustmentRows.append("<r id=\"").append(sourceId).append("\">");
			{
				adjustmentRows.append("<c>").append(sourceName).append("</c>");
				adjustmentRows.append("<c>").append(constituent).append("</c>");
				adjustmentRows.append("<c>").append(units).append("</c>");
				adjustmentRows.append("<c>").append(precision).append("</c>");
				adjustmentRows.append("<c>").append(formatter.format(origValue)).append("</c>");
				adjustmentRows.append("<c>").append(override).append("</c>");
				adjustmentRows.append("<c>").append(formatter.format(adjValue)).append("</c>");
			}
			adjustmentRows.append("</r>");
		}

		// Retrieve the response template and insert the data we just built
		String[] params = new String[] {
				"rowCount", "" + sourceMetadata.getRowCount(),
				"adjustments", adjustmentRows.toString()
		};
		String xmlResult = props.getParametrizedQuery("adjustmentsXMLResponse", params);

		return xmlResult;
	}

	/**
	 * Returns the user-supplied override value for the specified source and
	 * reach if it exists.  If an override value has not been specified by the
	 * user, this method returns null.
	 *
	 * @param adjGroups The adjustment groups section of the prediction context.
	 * @param reachId The reach we're identifying.
	 * @param sourceId The source for which to search for an override.
	 * @return The override value for the specified source and reach.
	 */
	private Double getOverrideValue(AdjustmentGroups adjGroups, long reachId, Long sourceId) {
		ReachGroup individualGroup = adjGroups.getIndividualGroup();

		// Iterate over the reaches and sources in the individual group
		if (individualGroup != null && individualGroup.isEnabled()) {
			List<ReachElement> reachList = individualGroup.getExplicitReaches();
			for (ReachElement reach : reachList) {
				if (reach.getId() == reachId) {
					for (Adjustment adj: reach.getAdjustments()) {
						if (adj.getSource() == sourceId.intValue()) {
							// If we find it, return the override value
							return adj.getAbsolute();
						}
					}
				}
			}
		}

		// If we don't find it, return null
		return null;
	}

	/**
	 * Returns an XML fragment representing the prediction results for the
	 * specified reach.
	 * 
	 * @param contextId Id for the prediction context on which to base the
	 *                  prediction results.
	 * @param modelId Id for the model supplying the base data values.
	 * @param reachId Id for the reach we're identifying.
	 * @return An XML fragment representing the prediction results for the
	 *         specified reach.
	 * @throws Exception 
	 */
	private String retrievePredictedsForReach(Integer contextId, Long modelId, Long reachId) throws Exception {
		// TODO move to DataLoader when done debugging
		//PredictionContext nominalPredictionContext = null;
		PredictResult adjustedPrediction = null;
		AdjustmentGroups nonAdjusted;
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		
		

		if (contextId != null) {
			// Get a nominal (unadjusted) prediction context using the model id
			PredictionContext contextFromCache = SharedApplication.getInstance().getPredictionContext(contextId);
			nonAdjusted = contextFromCache.getAdjustmentGroups().getNoAdjustmentVersion();

			// Get the adjusted prediction results
			adjustedPrediction = SharedApplication.getInstance().getPredictResult(contextFromCache.getAdjustmentGroups());
		} else {
			nonAdjusted = new AdjustmentGroups(modelId);
			
		}
		
		// Get the nominal prediction results
		PredictResult nominalPrediction = SharedApplication.getInstance().getPredictResult(nonAdjusted);

		// Build each section of the predicted result - incremental and total
		String incrementalContribution = buildPredSection(nominalPrediction,
				adjustedPrediction, reachId, DataSeriesType.decayed_incremental, predictData);
		String totalContribution = buildPredSection(nominalPrediction,
				adjustedPrediction, reachId, DataSeriesType.total, predictData);

		// Retrieve the response template and insert the data we just built
		String[] params = {
				"rowCount", "" + nominalPrediction.getColumnCount(),
				"incContribution", incrementalContribution,
				"totalContribution", totalContribution
		};
		String xmlResult = props.getParametrizedQuery("predictedXMLResponse", params);

		return xmlResult;
	}
	
	/**
	 * Builds a section of the prediction results XML using the {@code type}.
	 *
	 * @param nominalPrediction The unadjusted results.
	 * @param adjustedPrediction The adjusted results.
	 * @param reachId The reach we're identifying.
	 * @param type The type of results we're building for this section.
	 * @param predictData Used to convert from decayed to non-decayed values.
	 * @return A fragment of the prediction results XML based on the specified
	 *         {@code type}.
	 * @throws Exception 
	 */
	private String buildPredSection(PredictResult nominalPrediction,
			PredictResult adjustedPrediction, Long reachId, DataSeriesType type,
			PredictData predictData) throws Exception {

		if (nominalPrediction == null || reachId == null || reachId == 0) {
			return "";
		}


		// Assume adjustedPrediction has same Column structure and rows in same order.
		// Otherwise, we'll have to rewrite the following code.
		assert((adjustedPrediction == null) ||
				(	nominalPrediction.getRowCount() == adjustedPrediction.getRowCount()
						&& nominalPrediction.getColumnCount() == adjustedPrediction.getColumnCount())):
							"Assume adjustedPrediction has same column structure and rows in same order";

		// Add root element for the section
		StringBuilder predictRows = new StringBuilder();
		predictRows.append("<section display=\"").append(Action.getDataSeriesProperty(type, false));
		predictRows.append("\" name=\"").append(type.name()).append("\">\n");

		
		// Get the row index from the original data for the reach we're identifying
		int rowID = nominalPrediction.getRowForId(reachId);

		// Iterate over the relevant column indices, building a row of data for each
		for (Integer srcIndex=0; srcIndex < nominalPrediction.getSourceCount(); srcIndex++) {
			
			Long srcId = predictData.getSourceIdForSourceIndex(srcIndex);
			int colForIncSource = nominalPrediction.getIncrementalColForSrc(srcId);
			int colForTotalSource = nominalPrediction.getTotalColForSrc(srcId);
			
			// Calculate and format all of the data
			String columnName = "";	//fetch based on series
			String constituent = nominalPrediction.getProperty(colForIncSource, TableProperties.CONSTITUENT.getPublicName());
			String units = "";	//fetch based on series
			String precision = "";	//fetch based on series
			Double nominalValue = null;
			Double predictValue = null;
			String nominalDisplay = "N/A";
			String predictDisplay = "N/A";
			String percentDisplay = "";
			
			switch (type) {
			case decayed_incremental:
				//All these attribs are the same, decayed or not
				columnName = nominalPrediction.getName(colForIncSource);
				units = nominalPrediction.getUnits(colForIncSource);
				precision = nominalPrediction.getProperty(colForIncSource, TableProperties.PRECISION.getPublicName());
				
				nominalValue = nominalPrediction.getDecayedIncrementalForSrc(rowID, srcId, predictData);
				
				if (adjustedPrediction != null) {
					predictValue = adjustedPrediction.getDecayedIncrementalForSrc(rowID, srcId, predictData);
				}
				break;
			case incremental:
				columnName = nominalPrediction.getName(colForIncSource);
				units = nominalPrediction.getUnits(colForIncSource);
				precision = nominalPrediction.getProperty(colForIncSource, TableProperties.PRECISION.getPublicName());
				
				nominalValue = nominalPrediction.getIncrementalForSrc(rowID, srcId);
				
				if (adjustedPrediction != null) {
					predictValue = adjustedPrediction.getIncrementalForSrc(rowID, srcId);
				}
				break;
			case total:
				columnName = nominalPrediction.getName(colForTotalSource);
				units = nominalPrediction.getUnits(colForTotalSource);
				precision = nominalPrediction.getProperty(colForTotalSource, TableProperties.PRECISION.getPublicName());
				
				nominalValue = nominalPrediction.getTotalForSrc(rowID, srcId);
				
				if (adjustedPrediction != null) {
					predictValue = adjustedPrediction.getTotalForSrc(rowID, srcId);
				}
				break;
			default:
				throw new Exception("Unsupported type " + type + " for building source values.");
			}
			
			
			if (nominalValue != null) {
				nominalDisplay = formatter.format(nominalValue);
			}
			
			if (nominalValue != null && predictValue != null) {
				Double percentChange = calculatePercentageChange(predictValue, nominalValue);

				predictDisplay = formatter.format(predictValue);
				percentDisplay = formatter.format(percentChange);
			}

			// Put together the XML string for the predicted values
			predictRows.append("<r>");
			{
				predictRows.append("<c>").append(columnName).append("</c>");
				predictRows.append("<c>").append(constituent).append("</c>");
				predictRows.append("<c>").append(units).append("</c>");
				predictRows.append("<c>").append(precision).append("</c>");
				predictRows.append("<c>").append(nominalDisplay).append("</c>");
				predictRows.append("<c>").append(predictDisplay).append("</c>");
				predictRows.append("<c>").append(percentDisplay).append("</c>");
			}
			predictRows.append("</r>");
		}

		
		int colForInc = nominalPrediction.getIncrementalCol();
		int colForTotal = nominalPrediction.getTotalCol();
		
		// Calculate and format all of the data
		String columnName = "";	//fetch based on series
		String constituent = nominalPrediction.getProperty(colForInc, TableProperties.CONSTITUENT.getPublicName());
		String units = "";	//fetch based on series
		String precision = "";	//fetch based on series
		Double nominalValue = null;
		Double predictValue = null;
		String nominalDisplay = "N/A";
		String predictDisplay = "N/A";
		String percentDisplay = "";
		
		// Add a row for the total for this section
		// TODO: (with predicted)
//		String columnName = nominalPrediction.getName(totalColumn);
//		Double nominalValue = nominalPrediction.getDouble(rowID, totalColumn);
//		String nominalDisplay = (nominalValue == null) ? "N/A" : formatter.format(nominalValue);
//
//		String predictDisplay = "N/A";
//		String percentDisplay = "";
		
		
		switch (type) {
		case decayed_incremental:
			//All these attribs are the same, decayed or not
			columnName = nominalPrediction.getName(colForInc);
			units = nominalPrediction.getUnits(colForInc);
			precision = nominalPrediction.getProperty(colForInc, TableProperties.PRECISION.getPublicName());
			
			nominalValue = nominalPrediction.getDecayedIncremental(rowID, predictData);
			
			if (adjustedPrediction != null) {
				predictValue = adjustedPrediction.getDecayedIncremental(rowID, predictData);
			}
			break;
		case incremental:
			columnName = nominalPrediction.getName(colForInc);
			units = nominalPrediction.getUnits(colForInc);
			precision = nominalPrediction.getProperty(colForInc, TableProperties.PRECISION.getPublicName());
			
			nominalValue = nominalPrediction.getIncremental(rowID);
			
			if (adjustedPrediction != null) {
				predictValue = adjustedPrediction.getIncremental(rowID);
			}
			break;
		case total:
			columnName = nominalPrediction.getName(colForTotal);
			units = nominalPrediction.getUnits(colForTotal);
			precision = nominalPrediction.getProperty(colForTotal, TableProperties.PRECISION.getPublicName());
			
			nominalValue = nominalPrediction.getTotal(rowID);
			
			if (adjustedPrediction != null) {
				predictValue = adjustedPrediction.getTotal(rowID);
			}
			break;
		default:
			throw new Exception("Unsupported type " + type + " for building source values.");
		}
		
		if (nominalValue != null) {
			nominalDisplay = formatter.format(nominalValue);
		}
		
		if (nominalValue != null && predictValue != null) {
			Double percentChange = calculatePercentageChange(predictValue, nominalValue);

			predictDisplay = formatter.format(predictValue);
			percentDisplay = formatter.format(percentChange);
		}

		// Put together the XML string for the totaled row
		predictRows.append("<r>");
		{
			predictRows.append("<c>").append(columnName).append("</c>");
			predictRows.append("<c>").append(constituent).append("</c>");
			predictRows.append("<c>").append(units).append("</c>");
			predictRows.append("<c>").append(precision).append("</c>");
			predictRows.append("<c>").append(nominalDisplay).append("</c>");
			predictRows.append("<c>").append(predictDisplay).append("</c>");
			predictRows.append("<c>").append(percentDisplay).append("</c>");
		}
		predictRows.append("</r>");
		predictRows.append("</section>");

		return predictRows.toString();
	}

	/**
	 * Populates the attributes section in the {@code response}.
	 *
	 * @param req The service request object.
	 * @param response The service response object.
	 */
	private void retrieveAttributes(IDByPointRequest req, IDByPointResponse response)
			throws Exception {

		DataTable attributes = getAttributeData(response.modelID, response.reachID);

		// TODO [IK] This 4 is hardcoded for now. Have to go back and use SparrowModelProperties to do properly
		response.basicAttributes = new FilteredDataTable(attributes, new ColumnRangeFilter(0, 13)); // first four columns
		response.sparrowAttributes = new FilteredDataTable(attributes, new ColumnRangeFilter(13, attributes.getColumnCount() - 4)); // remaining columns

		StringBuilder basicAttributesSection = toSection(response.basicAttributes, "Basic Attributes", "basic_attrib", DISPLAY_RULES);
		StringBuilder sparrowAttributesSection = toSection(response.sparrowAttributes, "SPARROW Attributes", "sparrow_attrib", DISPLAY_RULES);

		// attributesXMLResponse
		response.attributesXML = props.getParametrizedQuery("attributesXMLResponse",
				new String[] {
				"AttributesCount", Integer.toString(attributes.getColumnCount()),
				"BasicAttributes", basicAttributesSection.toString(),
				"SparrowAttributes", sparrowAttributesSection.toString(),
		});
	}
	
	public DataTable getAttributeData(long modelId, long reachId) throws Exception {
		DataTable tab = SharedApplication.getInstance().getReachAttributes(new ReachID(modelId, reachId));
		return tab;
	}


	/**
	 *
	 * @param basicAttributes
	 * @param display
	 * @param name
	 * @param display_rules2
	 * @return
	 */
	private StringBuilder toSection(DataTable basicAttributes, String display, String name, Map<String, DisplayRule> displayRules) {
		StringBuilder sb = null;
		if (basicAttributes != null) {
			sb = new StringBuilder("<section display=\"");
			sb.append(display).append("\" name=\"").append(name).append("\">\n");
			for (int j=0; j<basicAttributes.getColumnCount(); j++) {
				String columnName = basicAttributes.getName(j);
				sb.append("<r><c>").append(columnName).append("</c><c>");
				String value = basicAttributes.getString(0, j);

				// apply applicable display rules
				DisplayRule rule = displayRules.get(columnName);
				String precision = basicAttributes.getProperty(j, TableProperties.PRECISION.getPublicName());
				if (rule != null) {
					value = rule.apply(value);
				} else if (precision != null) {
					try {
						int p = Integer.parseInt(precision);
						TruncateDecimal td = new TruncateDecimal(p);
						value = td.apply(value);
					} catch (Exception e) {
						//could be a misc property stuck in the precision field - ok to ignore
					}
				}

				// null values are displayed as "N/A"
				value = (value == null)? "N/A": value;
				sb.append(value).append("</c>");
				String units = basicAttributes.getUnits(j);

				if (units != null) {
					sb.append("<c>").append(units).append("</c>");
				} else {
					sb.append("<c/>");
				}
				sb.append("</r>");
			}
			sb.append("</section>");
		}
		return sb;
	}



}
