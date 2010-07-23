package gov.usgswim.sparrow.parser;


/**
 *
 */
public enum BaseDataSeriesType {

	//Note:  I am removing the error_estimate types here.  I'm thinking that
	//error (of different types) can be added as a generic flag on the analysis rather than a series.

	//flags:
	//delievered (multiply the resulting amount by the delivery fraction)
	//Per total-area/catch-area/flow (divide the result by the this amount)
	//error [est, bootstrap]

	//Delivered:
	/*
	 * Delivery Fraction
	 * Total Delivered Amount
	 * Incremental Delivered Amount
	 * Total Delivered Yield
	 * Incremental Delivered Yield
	 */

	//Other
	/*
	 * total-area
	 * catch-area
	 * flow
	 * delivery_fraction
	 */

	//Total (i.e. includes upstream) predicted load.
    total						(true, false, false, false, 1, false), // active

    //Incremental (i.e. does not include upstream) predicted load.
    incremental					(true, false, false, false, 1, false), //active
    decayed_incremental			(true, false, false, false, 1, false),

    delivered_fraction			(false, false, true, false, 0, false), //fraction delivered - calc w/o prediction

    source_value				(false, true, false, false, 2, false), // active

    std_error_estimate_coef		(false, false, false, true, 1, false),

    
    
    total_decay					(true, false, false, false, 1, true),	//The amount decayed in the stream
    total_no_decay				(true, false, false, false, 1, true),	//Total as it would be w/o decay
    
    //Base reach, model, and catchment attributes
    land_to_water_coef			(false, false, false, false, 2, false),	//l_to_w is not a predictive value for us
    instream_decay_coef			(false, false, false, false, 0, false),	//instream decay is not predictive for us
    catch_area					(false, false, false, false, 0, false),	//area of a catchment
    
    /* cumulative_catch_area and huc_area not supported as of 07/23/2010 */
    cumulative_catch_area		(false, false, false, false, 0, false),	//cumulative area of a catchment (include upstream)
    huc_area					(false, false, false, false, 0, false)	//area of a huc (NEED FLAG FOR NOT REACH RELATED?)
    ;


    private final boolean predictionBased;
    private final boolean predictDataBased;
    private final boolean deliveryBased;
    private final boolean stdErrorCoefBased;
    private final int srcRequirement;	//0 Not allowed, 1 allowed, 2 required
    private static final int NO_SOURCES_ALLOWED = 0,SOURCES_ALLOWED = 1,SOURCES_REQUIRED = 2;
    private final boolean nonstandardPrediction;



    BaseDataSeriesType(boolean predictionBased, boolean predictDataBased, boolean deliveryBased,
    		boolean stdErrorCoefBased, int srcRequirement, boolean nonstandardPrediction) {

        this.predictionBased = predictionBased;
        this.predictDataBased = predictDataBased;
        this.deliveryBased = deliveryBased;
        this.stdErrorCoefBased = stdErrorCoefBased;
        this.srcRequirement = srcRequirement;
        this.nonstandardPrediction = nonstandardPrediction;

    }

    /**
     * Returns true if this dataSeries uses PredictResult data (possibly in
     * combination with other data) to generate the returned data.
     * @return
     */
    public boolean isPredictionBased() {
        return predictionBased;
    }

    /**
     * Returns true if this dataseries is just showing data from PredictData
     * (possibly slightly massaged).  Source, coef values, and other values
     * pulled directly from the db are examples.
     * @return
     */
    public boolean isPredictDataBased() {
        return predictDataBased;
    }

    /**
     * Returns true if the actual data being mapped is the delivery fraction.
     * This is unrelated to is the delivery fraction is used in the calculation
     * of a final value.  i.e., returns true if you actually want to map the
     * delivery fraction value.  Returns false if you want to map delivered
     * incremental yield, which is based on the incremental value.
     * @return
     */
    public boolean isDeliveryBased() {
    	return deliveryBased;
    }

    /**
     * Returns true if the actual data being mapped is the standard error
     * coef.
     * This is only true if the actual data is the error coef, not for
     * predicted standard errors, which are based on predicted data, then
     * multiplied by std error coefs.
     * @return
     */
    public boolean isStdErrorCoefBased() {
    	return stdErrorCoefBased;
    }

    /**
     * Returns true if a source is either required or allowed for this dataseries.
     * @return
     */
    public boolean isSourceAllowed() {
        return srcRequirement == SOURCES_ALLOWED || srcRequirement == SOURCES_REQUIRED;
    }

    /**
     * Returns true if this dataseries does not allow a source to be specified.
     * instream_decay_coef is an example:  There are no source specific values
     * for this coef.
     *
     * @return
     */
    public boolean isSourceDisallowed() {
        return srcRequirement == NO_SOURCES_ALLOWED;
    }

    /**
     * Returns true if this dataseries requires a source to specified.
     * source_value is an example:  To report source values, you must specify
     * which source value.
     * @return
     */
    public boolean isSourceRequired() {
        return srcRequirement == SOURCES_REQUIRED;
    }

    /**
     * This dataseries requires an extra non-standard at the time the prediction
     * is run.  This will perhaps be an alternate implementation of PredictionRunner.
     * @return
     */
    public boolean isNonstandardPredictionColumn() {
        return nonstandardPrediction;
    }
}