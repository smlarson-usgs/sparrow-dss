package gov.usgswim.sparrow.service.deliveryaggreport;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableSet;
import gov.usgswim.datatable.impl.DataTableSetCoord;
import gov.usgswim.datatable.impl.DataTableSetSimple;
import gov.usgswim.datatable.view.RelativePercentageView;
import gov.usgswim.sparrow.service.deliveryterminalreport.ReportRequest;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
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
			boolean includeRelativePercentage = true;

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

			DeliveryReportRequest actionRequest = new DeliveryReportRequest(context.getAdjustmentGroups(), termReaches, aggLevel);


			DataTableSet reportDataTableSet = sharedApp.getTotalDeliveredLoadByUpstreamRegionReport(actionRequest);


			String readmeText = SparrowResourceUtils.lookupMergedHelp(
					context.getModelID().toString(),
					"CommonTerms.Terminal_Reach_Aggregate_Report",
					null,
					new String[] {});
			
			//Coord's of the total column
			DataTableSetCoord columnCoordToDetermineIfARowIsEmpty = null;
			
			if (includeRelativePercentage) {
				DataTable.Immutable[] tables = (DataTable.Immutable[]) reportDataTableSet.getTables();
				int lastTableIndex = tables.length - 1;
				DataTable actualResultTable = tables[lastTableIndex];
				
				
				RelativePercentageView relPercentView = new RelativePercentageView(
						actualResultTable, null, null,
						actualResultTable.getColumnCount() - 1, false
				);
				
				tables[lastTableIndex] = relPercentView;
				
				DataTableSet reportDataWRelPercent = new DataTableSetSimple(
						tables, reportDataTableSet.getName(), reportDataTableSet.getDescription());
				
				reportDataTableSet = reportDataWRelPercent;
				
				//Total column is now second to last column
				columnCoordToDetermineIfARowIsEmpty = new DataTableSetCoord(
						lastTableIndex, -1, reportDataWRelPercent.getTableColumnCount(lastTableIndex) - 2);
			} else {
				//Total column is the last column
				columnCoordToDetermineIfARowIsEmpty = new DataTableSetCoord(
						reportDataTableSet.getTableCount() -1, -1,
						reportDataTableSet.getTableColumnCount(reportDataTableSet.getTableCount() -1) - 1);
			}


			return new  ReportSerializer(
					req, reportDataTableSet, readmeText, columnCoordToDetermineIfARowIsEmpty.col);



	}

	public void shutDown() {
	}
}
