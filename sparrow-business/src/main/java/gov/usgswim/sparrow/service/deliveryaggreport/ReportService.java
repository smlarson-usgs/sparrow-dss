package gov.usgswim.sparrow.service.deliveryaggreport;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableSet;
import gov.usgs.cida.datatable.impl.DataTableSetSimple;
import gov.usgs.cida.datatable.view.RelativePercentageView;
import gov.usgswim.sparrow.service.deliveryterminalreport.ReportRequest;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

import javax.xml.stream.XMLStreamReader;

public class ReportService implements HttpService<ReportRequest> {

    public XMLStreamReader getXMLStreamReader(ReportRequest req,
            boolean isNeedsCompleteFirstRow) throws Exception {

    	SharedApplication sharedApp = SharedApplication.getInstance();

			Integer predictionContextID = req.getContextID();
			PredictionContext context = req.getContext();
			AggregationLevel aggLevel = req.getAggregationLevel();
			Long modelId = null;

			//Relative percentage can be reported for abs vals, but doesn't make sense
			//for yield values.   This could be added back by basing the rel-percent
			//on the abs values, but there would be no way for the user to check the
			//values in what they are looking at.
			boolean includeRelativePercentage = ! req.isReportYield();

			if (context != null) {
				//The context was supplied w/ the request
				modelId = context.getModelID();
			} else if (predictionContextID != null) {
				//The context was passed by ID
					context = sharedApp.getPredictionContext(predictionContextID);
					modelId = context.getModelID();
			}

			PredictData predictData = sharedApp.getPredictData(modelId);
			TerminalReaches termReaches = context.getTerminalReaches();

			if (termReaches == null || termReaches.isEmpty()) {
				throw new Exception("There must be downstream reaches selected to generate the deliver report.");
			}

			DeliveryReportRequest actionRequest = new DeliveryReportRequest(context.getAdjustmentGroups(), termReaches, aggLevel, req.isReportYield());


			DataTableSet reportDataTableSet = sharedApp.getTotalDeliveredLoadByUpstreamRegionReport(actionRequest);
			SparrowModel model = sharedApp.getModelMetadata(new ModelRequestCacheKey(modelId, false, false, false)).get(0);
			String networkName = model.getEnhNetworkName();
			String networkUrl = model.getEnhNetworkUrl();
			String networkIdColumn = model.getEnhNetworkIdColumn();
			String modelName = model.getName();
			String modelConstituent = model.getConstituent();

			String readmeText = SparrowResourceUtils.lookupMergedHelp(
					context.getModelID(),
					"CommonTerms.Terminal_Reach_Aggregate_Report",
					null,
					new Object[] {"networkName", networkName, "networkUrl", networkUrl, "networkIdColumn", networkIdColumn, "modelName", modelName, "modelConstituent", modelConstituent});

			//Assumed indexes of the data tables
			int INFO_TBL_INDEX = 0;
			int LOAD_TBL_INDEX = 1;
			int YIELD_TBL_INDEX = 2;

			DataTableSet finalReportDataSet = null;
			int columnToDetermineIfARowIsEmpty = 0;
			DataTable.Immutable[] reportTables = (DataTable.Immutable[]) reportDataTableSet.getTables();
			DataTable.Immutable infoTable = reportTables[INFO_TBL_INDEX];
			DataTable.Immutable loadTable = reportTables[LOAD_TBL_INDEX];
			DataTable.Immutable yieldTable = reportTables[YIELD_TBL_INDEX];

			if (includeRelativePercentage) {
				loadTable = new RelativePercentageView(
						loadTable, null, null,
						loadTable.getColumnCount() - 1, false
				);
			}

			if (req.isReportYield()) {
				finalReportDataSet = new DataTableSetSimple(
						new DataTable.Immutable[] {infoTable, yieldTable},
						reportDataTableSet.getName(), reportDataTableSet.getDescription());

				columnToDetermineIfARowIsEmpty =
						includeRelativePercentage?
							finalReportDataSet.getColumnCount() - 2:
							finalReportDataSet.getColumnCount() - 1;
			} else {
				finalReportDataSet = new DataTableSetSimple(
						new DataTable.Immutable[] {infoTable, loadTable},
						reportDataTableSet.getName(), reportDataTableSet.getDescription());
				columnToDetermineIfARowIsEmpty = finalReportDataSet.getColumnCount() - 1;
			}


			return new  ReportSerializer(
					req, finalReportDataSet, readmeText, columnToDetermineIfARowIsEmpty);



	}

	public void shutDown() {
	}
}
