package gov.usgswim.sparrow;

import gov.usgswim.sparrow.service.SharedApplication;

import org.apache.log4j.Logger;

public class PredictionComputable implements Computable<PredictionRequest, Double2D> {
	protected static Logger log =
		Logger.getLogger(PredictionComputable.class); //logging for this class
		
		
	public PredictionComputable() {
	}

	public Double2D compute(PredictionRequest request) throws InterruptedException {
		PredictionDataSet data = loadData(request);
		PredictionDataSet adjData = adjustData(request, data);
		
		long startTime = System.currentTimeMillis();
		
		Double2D result = runPrediction(request, adjData);
		
		log.debug(
			"Prediction done for model #" + request.getModelId() + 
			" Time: " + (System.currentTimeMillis() - startTime) + "ms, " +
			adjData.getSrc().getRowCount() + " reaches");
			
		return result;
	}
	
	/**
	 * Loads the model sources, coefs, and values needed to run the prediction.
	 * @param arg
	 * @return
	 */
	public PredictionDataSet loadData(PredictionRequest req) throws InterruptedException {
		return SharedApplication.getInstance().getPredictDatasetCache().compute( req.getModelId() );
	}
	
	/**
	 * Adjusts the passed data based on the adjustments in the requests.
	 * 
	 * The passed data is not modified and it is possible for the initial
	 * data to simply be returned if there are no adjustments.
	 * 
	 * @param arg
	 * @param data
	 * @return
	 */
	public PredictionDataSet adjustData(PredictionRequest req, PredictionDataSet data) {
		PredictionDataSet adjData = data;	//start by assuming we don't have to adjust the data
		
		if (req.getAdjustmentSet().hasAdjustments()) {
			try {
				adjData = (PredictionDataSet) data.clone();
			} catch (CloneNotSupportedException e) {
				//Will not be thrown
			}
			
			//This method does not modify the underlying data
			adjData.setSrc(
					req.getAdjustmentSet().adjustSources(adjData.getSrc())
			);
		}
		
		return adjData;
	}
	
	/**
	 * Runs the actual prediction using the passed base data.
	 * 
	 * The passed data is not modified.
	 * 
	 * @param arg
	 * @param data
	 * @return
	 */
	public Double2D runPrediction(PredictionRequest req, PredictionDataSet data) {
		PredictSimple adjPredict = new PredictSimple(data);
		return adjPredict.doPredict();
	}
}
