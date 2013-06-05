package gov.usgswim.sparrow.service.deliveryterminalreport;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableSet;
import gov.usgs.cida.datatable.filter.FilteredDataTable;
import gov.usgs.cida.datatable.impl.DataTableSetSimple;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.ReachIdFilter;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.SparrowResourceUtils;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.stream.XMLStreamReader;

public class ReportService implements HttpService<ReportRequest> {

    public XMLStreamReader getXMLStreamReader(ReportRequest req,
            boolean isNeedsCompleteFirstRow) throws Exception {

    	SharedApplication sharedApp = SharedApplication.getInstance();

			Integer predictionContextID = req.getContextID();
			PredictionContext context = req.getContext();
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

			DeliveryReportRequest actionRequest =
					new DeliveryReportRequest(context.getAdjustmentGroups(),
							termReaches, AggregationLevel.NONE, req.isReportYield());

			DataTableSet reportData = sharedApp.getTotalDeliveredLoadSummaryReport(actionRequest);

			Collection<Long> reachIds = SharedApplication.getInstance().getReachFullIdAsLong(
				termReaches.getModelID(),
				termReaches.getReachIdsAsList()
				);
			ReachIdFilter filter = new ReachIdFilter(reachIds);
			//ArrayList<DataTable> tbls = new ArrayList<DataTable>();
			DataTable[] tbls = reportData.getTables();
			DataTable.Immutable[] tblsImm = new DataTable.Immutable[reportData.getTables().length];
			for (int i = 0; i < tbls.length; i++) {
				tblsImm[i] = new FilteredDataTable(tbls[i], filter).toImmutable();
			}

			DataTableSet filteredTableSet = new DataTableSetSimple(tblsImm,
				reportData.getName(), reportData.getDescription());

			//Get info used to provide some links and info in the report header
			SparrowModel model = sharedApp.getModelMetadata(new ModelRequestCacheKey(modelId, false, false, false)).get(0);
			String networkName = model.getEnhNetworkName();
			String networkUrl = model.getEnhNetworkUrl();
			String networkIdColumn = model.getEnhNetworkIdColumn();

			String readmeText = SparrowResourceUtils.lookupMergedHelp(
					context.getModelID().toString(),
					"CommonTerms.Terminal_Reach_Summary_Report",
					null,
					new String[] {"networkName", networkName, "networkUrl", networkUrl, "networkIdColumn", networkIdColumn});

			return new  ReportSerializer(
					req, filteredTableSet, predictData, readmeText, filteredTableSet.getColumnCount() - 1);



	}

	public void shutDown() {
	}
}
