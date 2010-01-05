package gov.usgswim.sparrow.revised;

public enum Actions {
	/*
	 * Base PredictData
	 * Adjusted PredictData
	 * Incrmental Calculation
	 * Flux Transport Calculation (output = up or downstream node)
	 *
	 * Weight
	 * Delivery Fraction
	 * Aggregate
	 *
	 */
	LOAD_PREDICT_DATA(new LoadPredictDataAction()),
	LOAD_ANCIL_DATA(),
	CALCULATE_INCREMENTAL_FLUX,
	CALCULATE_CUMULATIVE_FLUX,
	APPLY_WEIGHTS,
	CALCULATE_DELIVERY_FRACTION,
	AGGREGATE;

	private Action _action;

	private Actions() {
		this(null);
	}

	private Actions(Action action) {
		this._action = action;
	}

	public Need analyze(SparrowRequest request) {
		return null;
	}

	public Action makeAction() {
		return _action;
	}
}
