package gov.usgswim.sparrow.revised;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import gov.usgswim.sparrow.revised.SparrowRequestProcessor.DataSeriesTypes;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction.Levels;


public class SparrowRequest {
	public final DataSeriesTypes calcType;
	public final ProductTypes prodType;
//	public final Product

	// ============
	// CONSTRUCTORS
	// ============
	public SparrowRequest(DataSeriesTypes type) {
		this.calcType = type;
		prodType = null;
	}

	public SparrowRequest asDataRequest() {
		// TODO Implement this later
		throw new NotImplementedException();
		//return null;
	}

	// ===========
	// VARIANTS
	// ============
	public static SparrowRequest makeModelRequest(SparrowRequest req) {
		return null;
	}
	public static SparrowRequest makeAdjustmentRequest(SparrowRequest req) {
		return null;
	}
	public static SparrowRequest makeTargetRequest(SparrowRequest req) {
		return null;
	}
	public static SparrowRequest makeAdjustmentAndTargetRequest(SparrowRequest req) {
		return null;
	}

	// -------

	public Object determineCalculationType() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object determineProductType() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object determineProductOption() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object determineCalculationInputs() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean requiresComparison() {
		// TODO Auto-generated method stub
		return false;
	}

	public DataSeriesTypes getDataSeries() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean requiresAggregation() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	public SparrowRequest stripAggregation() {
		// TODO Auto-generated method stub
		return null;
	}

	public Levels getAggregationLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	public AggregationFunction getAggregationFunction() {
		// TODO Auto-generated method stub
		return null;
	}

	public SparrowRequest makeBaseRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	public SparrowRequest makeComparingRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getCompareOptions() {
		// TODO Auto-generated method stub
		return null;
	}


}
