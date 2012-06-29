package gov.usgswim.sparrow.service.watershed;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.service.DataTableSerializer;
import gov.usgswim.sparrow.service.SharedApplication;

import javax.xml.stream.XMLStreamReader;

public class WatershedService implements HttpService<WatershedRequest> {

    public XMLStreamReader getXMLStreamReader(WatershedRequest req,
            boolean isNeedsCompleteFirstRow) throws Exception {
    	
    	SharedApplication sharedApp = SharedApplication.getInstance();
    	


			if (req.getModelId() != null) {
				DataTable data = sharedApp.getPredefinedWatershedsForModel(req.getModelId());
				return new DataTableSerializer(data, "Predefined watersheds for model " + req.getModelId());
			} else if (req.getWatershedId() != null) {
				DataTable data = sharedApp.getPredefinedWatershedReachesForModel(req.getWatershedId());
				return new DataTableSerializer(data, "Reaches in the Predefined watershed " + req.getWatershedId());
			} else {
				throw new Exception("Either the model id or the watershed ID must be specified.");
			}

	}

	public void shutDown() {
	}
}
