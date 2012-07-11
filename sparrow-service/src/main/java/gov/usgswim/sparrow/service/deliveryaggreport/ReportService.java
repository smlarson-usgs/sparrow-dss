package gov.usgswim.sparrow.service.deliveryaggreport;

import gov.usgswim.datatable.DataTable;
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


			DataTable reportData = sharedApp.getTotalDeliveredLoadByStateSummaryReport(actionRequest);


			String readmeText = SparrowResourceUtils.lookupMergedHelp(
					context.getModelID().toString(),
					"CommonTerms.Terminal_Reach_Aggregate_Report",
					null,
					new String[] {});


			return new  StateReportSerializer(
					req, reportData, readmeText);



	}

	public void shutDown() {
	}
}
