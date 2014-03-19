package gov.usgswim.sparrow.service.predictcontext;

import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.action.WriteDbfFileForContext;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.ReturnStatus;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.QueryLoader;
import java.io.File;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

public class PredictContextService implements HttpService<PredictContextRequest> {
	protected static Logger log =
		Logger.getLogger(PredictContextService.class);
	
	private QueryLoader props = new QueryLoader("gov/usgswim/sparrow/service/predictcontext/PredictContextServiceTemplate.properties");

	public XMLStreamReader getXMLStreamReader(PredictContextRequest o,
			boolean isNeedsCompleteFirstRow) throws Exception {

		XMLInputFactory inFact = XMLInputFactory.newInstance();
		
		//Store to cache
		PredictionContext context = o.getPredictionContext();
		boolean isSuccess = false;
		try {
			SharedApplication.getInstance().putPredictionContext(context);
			isSuccess = true;
			log.trace("Created and put new prediction context:" + context.getId());
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Could not put the new prediction context" + context.getId(), e);
		}

		if (isSuccess) {
			SparrowColumnSpecifier dataColumn = null;
			
			dataColumn = context.getDataColumn();	//The actual response data

			if (dataColumn == null) {
				//A calc fail returns a null data column
				String response = props.getParametrizedQuery("ResponseCalcFail",
						new String[] {
						"ModelId", context.getModelID().toString(),
				});

				return inFact.createXMLStreamReader(new StringReader(response));
			}
			
			
			String adjustmentGroups = "";
			String terminalReaches = "";
			String areaOfInterest = "";
			String units = dataColumn.getUnits();
			String constituent = dataColumn.getConstituent();
			String name = dataColumn.getColumnName();
			String description = dataColumn.getDescription();
			
			if (context.getAdjustmentGroups() != null) {
				String contextId = Integer.toString(context.getAdjustmentGroups().hashCode());
				adjustmentGroups = props.getParametrizedQuery("adjustmentGroups",
						new String[] { "AdjustmentContextId",  contextId });
			}
			if (context.getTerminalReaches() != null) {
				String contextId = Integer.toString(context.getTerminalReaches().hashCode());
				terminalReaches = props.getParametrizedQuery("terminalReaches",
						new String[] { "TerminalContextId",  contextId });
			}
			if (context.getAreaOfInterest() != null) {
				String contextId = Integer.toString(context.getAreaOfInterest().hashCode());
				areaOfInterest = props.getParametrizedQuery("areaOfInterest",
						new String[] { "AreaOfInterstContextId",  contextId });
			}
			
			if (units == null) {
				units = "Units Unknown";
			}
			
			if (constituent == null) {
				constituent = "";	//keep it empty so it won't display on client
			}
			
			if (description == null) {
				description = "";	//keep it empty so it won't display on client
			}
			

			String response = props.getParametrizedQuery("ResponseOK",
				new String[] {
					"ModelId", context.getModelID().toString(),
					"ContextId", Integer.toString( context.hashCode() ),
					"RowIdType", "reach",
					"adjustmentGroups", adjustmentGroups,
					"AnalysisContextId", Integer.toString( context.getAnalysis().hashCode() ),
					"terminalReaches", terminalReaches,
					"areaOfInterest", areaOfInterest,
					"name", name,
					"description", description,
					"units", units,
					"constituent", constituent,
					"rowCount", Integer.toString(dataColumn.getRowCount())
			});

			return inFact.createXMLStreamReader(new StringReader(response));
		}
		// failure
		return inFact.createXMLStreamReader(
				new StringReader("<PredictionContext-response><status>" + ReturnStatus.ERROR + "</status></PredictionContext-response>"));
	}

	public void shutDown() {
		// TODO Auto-generated method stub

	}



}
