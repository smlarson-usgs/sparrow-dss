package gov.usgswim.sparrow.service.idbypoint;

import static gov.usgswim.sparrow.service.predict.ValueType.incremental;
import static gov.usgswim.sparrow.service.predict.ValueType.total;
import gov.usgswim.ThreadSafe;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.filter.ColumnRangeFilter;
import gov.usgswim.datatable.filter.FilteredDataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowModelProperties;
import gov.usgswim.sparrow.cachefactory.ReachID;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.Adjustment;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.ReachElement;
import gov.usgswim.sparrow.parser.ReachGroup;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.ValueType;
import gov.usgswim.sparrow.service.predict.aggregator.AggregateType;
import gov.usgswim.sparrow.util.QueryLoader;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
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
		IDByPointResponse response = new IDByPointResponse();
        response.modelID = populateModelID(req);
        assert(response.modelID != null);

        // Retrieve the reach
		ReachInfo reach = retrieveReach(req, response);
		if (reach != null) {
	        response.setReach(reach);

	        // populate each of the sections
	        if (req.hasAdjustments()) {
	            retrieveAdjustments(req.getContextID(), req, response);
	        }
	        if (req.hasAttributes()) {
	            retrieveAttributes(req, response);
	        }
	        if (req.hasPredicted()) {
	            response.predictionsXML = retrievePredictedsForReach(req.getContextID(), req.getModelID(), Long.valueOf(response.reachID));
	        }

	        response.statusOK = true;
		} else {
		    response.statusOK = false;
		    response.message = "No reach found near that point.";
		}

		XMLInputFactory inFact = XMLInputFactory.newInstance();
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(response.toXML()));

		return reader;
	}

	public void shutDown() {
		xoFact = null;
	}
	// =======================
	// PRIVATE HELPER METHODS
	// =======================
	private ReachInfo retrieveReach(IDByPointRequest req, IDByPointResponse response) throws Exception {
		if (req.getReachID() != null) {
			return SharedApplication.getInstance().getReachByIDResult(new ReachID(response.modelID, req.getReachID()));
		} else if (req.getPoint() != null) {
			return SharedApplication.getInstance().getReachByPointResult(new ModelPoint(response.modelID, req.getPoint()));
		} else {
			throw new Exception("A context-id or a model-id is required for a id request");
		}
	}

	private Long populateModelID(IDByPointRequest req) throws Exception {
		if (req.getContextID() != null) {
			PredictionContext context = SharedApplication.getInstance().getPredictionContext(req.getContextID());
			if (context == null) throw new RuntimeException("Prediction Context with id "
					+ req.getContextID() + " has not been registered. Perhaps the server has been restarted?");
			if (req.getModelID() != null && !req.getModelID().equals(context.getModelID())) {
				throw new RuntimeException("Mismatched model-ids, prediction-context: " + context.getModelID()
						+ ", request: " + req.getModelID());
			}
			return context.getModelID();
		} else if (req.getModelID() != null) {
			return req.getModelID();
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
	 * @param reachID Id for the reach we're identifying.
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
     */
	private String retrievePredictedsForReach(Integer contextId, Long modelId, Long reachId) throws IOException {
		// TODO move to DataLoader when done debugging
		PredictionContext nominalPredictionContext = null;
		PredictResult adjustedPrediction = null;

		if (contextId != null) {
			// Get a nominal (unadjusted) prediction context using the model id
			PredictionContext contextFromCache = SharedApplication.getInstance().getPredictionContext(contextId);
            nominalPredictionContext = new PredictionContext(contextFromCache.getModelID(), null, null, null, null);

            // Get the adjusted prediction results
			adjustedPrediction = SharedApplication.getInstance().getPredictResult(contextFromCache);
		} else {
			nominalPredictionContext = new PredictionContext(modelId, null, null, null, null);
		}
		// Get the nominal prediction results
		PredictResult nominalPrediction = SharedApplication.getInstance().getPredictResult(nominalPredictionContext);

		// Build each section of the predicted result - incremental and total
		String incrementalContribution = buildPredSection(nominalPrediction,
		        adjustedPrediction, reachId, incremental, "Incremental Contribution Values", "inc");
		String totalContribution = buildPredSection(nominalPrediction,
		        adjustedPrediction, reachId, total, "Total (Measurable) Values", "inc");

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
	 * @param display The displayed title of the section.
	 * @param name The name of the section.
	 * @return A fragment of the prediction results XML based on the specified
	 *         {@code type}.
	 */
	private String buildPredSection(PredictResult nominalPrediction,
	        PredictResult adjustedPrediction, Long reachId, ValueType type,
	        String display, String name) {

		if (nominalPrediction == null || reachId == null || reachId == 0) {
		    return "";
		}
		String typeName = (type == null) ? "" : type.name();
		String isTotalVal = AggregateType.sum.name();
		List<Integer> relevantColumns = new ArrayList<Integer>();
		Integer totalColumn = null;

		// Collect all the relevant column indices, as indicated by matching VALUE_TYPE and AGGREGATE_TYPE
		for (int j = 0; j < nominalPrediction.getColumnCount(); j++) {
			boolean isDesiredType = typeName.equals(nominalPrediction.getProperty(j, PredictResult.VALUE_TYPE_PROP));
			String aggType = nominalPrediction.getProperty(j, PredictResult.AGGREGATE_TYPE_PROP);
			boolean isTotal = (aggType != null ) && isTotalVal.equals(aggType);

			if (isDesiredType) {
				if (isTotal) {
					totalColumn = j;
				} else {
					relevantColumns.add(j);
				}
			}
		}

		// Assume adjustedPrediction has same Column structure and rows in same order.
		// Otherwise, we'll have to rewrite the following code.
		assert((adjustedPrediction == null) ||
				(	nominalPrediction.getRowCount() == adjustedPrediction.getRowCount()
						&& nominalPrediction.getColumnCount() == adjustedPrediction.getColumnCount())):
							"Assume adjustedPrediction has same column structure and rows in same order";

		// Add root element for the section
		StringBuilder predictRows = new StringBuilder();
		predictRows.append("<section display=\"").append(display);
		predictRows.append("\" name=\"").append(name).append("\">\n");

		// Get the row index from the original data for the reach we're identifying
        int rowID = nominalPrediction.getRowForId(reachId);

        // Iterate over the relevant column indices, building a row of data for each
		for (Integer j : relevantColumns) {
		    // Calculate and format all of the data
			String columnName = nominalPrediction.getName(j);
			String constituent = nominalPrediction.getProperty(j, PredictResult.CONSTITUENT_PROP);
			String units = nominalPrediction.getUnits(j);
			String precision = nominalPrediction.getProperty(j, PredictResult.PRECISION_PROP);
            Double nominalValue = nominalPrediction.getDouble(rowID, j);
            String nominalDisplay = (nominalValue == null)? "N/A": formatter.format(nominalValue);

            String predictDisplay = "N/A";
            String percentDisplay = "";
            if (adjustedPrediction != null && adjustedPrediction.getString(rowID, j) != null) {
                Double predictValue = Double.valueOf(adjustedPrediction.getDouble(rowID, j));
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

		// Add a row for the total for this section
		// TODO: (with predicted)
        String columnName = nominalPrediction.getName(totalColumn);
		Double nominalValue = nominalPrediction.getDouble(rowID, totalColumn);
		String nominalDisplay = (nominalValue == null) ? "N/A" : formatter.format(nominalValue);

		String predictDisplay = "N/A";
        String percentDisplay = "";
        if (adjustedPrediction != null && adjustedPrediction.getString(rowID, totalColumn) != null) {
            Double predictValue = Double.valueOf(adjustedPrediction.getDouble(rowID, totalColumn));
            Double percentChange = calculatePercentageChange(predictValue, nominalValue);

            predictDisplay = formatter.format(predictValue);
            percentDisplay = formatter.format(percentChange);
        }

        // Put together the XML string for the totaled row
		predictRows.append("<r>");
		{
		    predictRows.append("<c>").append(columnName).append("</c>");
            predictRows.append("<c></c>");
            predictRows.append("<c></c>");
            predictRows.append("<c></c>");
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
	throws IOException, SQLException, NamingException {
		// TODO move to DataLoader when done debugging

	    String[] queryParams = {
            "ReachID", Long.toString(response.reachID),
            "ModelID", Long.toString(response.modelID),
	    };
		String attributesQuery = props.getParametrizedQuery("attributesSQL", queryParams);

		DataTableWritable attributes = SharedApplication.queryToDataTable(attributesQuery);
		// TODO [IK] This 4 is hardcoded for now. Have to go back and use SparrowModelProperties to do properly
		response.sparrowAttributes = new FilteredDataTable(attributes, new ColumnRangeFilter(0, 4)); // first four columns
		response.basicAttributes = new FilteredDataTable(attributes, new ColumnRangeFilter(4, attributes.getColumnCount() - 4)); // remaining columns

		StringBuilder basicAttributesSection = toSection(response.basicAttributes, "Basic Attributes", "basic_attrib");
		StringBuilder sparrowAttributesSection = toSection(response.sparrowAttributes, "SPARROW Attributes", "sparrow_attrib");

		// attributesXMLResponse
		response.attributesXML = props.getParametrizedQuery("attributesXMLResponse",
				new String[] {
				"AttributesCount", Integer.toString(attributes.getColumnCount()),
				"BasicAttributes", basicAttributesSection.toString(),
				"SparrowAttributes", sparrowAttributesSection.toString(),
		});

		// TODO: Create a combo XMLStreamReader to enable several streamreader
		// to be assembled sequentially, using hasNext to query. This makes the
		// pieces combineable.
	}

	/**
	 *
	 * @param basicAttributes
	 * @param display
	 * @param name
	 * @return
	 */
	private StringBuilder toSection(DataTable basicAttributes, String display, String name) {
		StringBuilder sb = null;
		if (basicAttributes != null) {
			sb = new StringBuilder("<section display=\"");
			sb.append(display).append("\" name=\"").append(name).append("\">\n");
			for (int j=0; j<basicAttributes.getColumnCount(); j++) {
				String columnName = basicAttributes.getName(j);
				sb.append("<r><c>").append(columnName).append("</c><c>");
				String value = basicAttributes.getString(0, j);
				value = (value == null)? "N/A": value;
				sb.append(value).append("</c>");
				String units = basicAttributes.getUnits(j);
				// HACK look up the units if not available in DataTable attributes
				// TODO [IK] Should populate the DataTable units property rather
				// than do lookup, but right now that would
				// touch too many classes and needs to be tested.
				units = (units == null)? SparrowModelProperties.HARDCODED_ATTRIBUTE_UNITS.get(columnName): units;

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
