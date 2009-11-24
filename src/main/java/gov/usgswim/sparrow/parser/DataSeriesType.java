package gov.usgswim.sparrow.parser;

import java.io.Serializable;

/**
 *
 */
public enum DataSeriesType implements Serializable {

	//Total (i.e. includes upstream) predicted load.  
    total						(BaseDataSeriesType.total, false, false, false, false, true), // active
    total_std_error_estimate	(BaseDataSeriesType.total, true, false, false, true, false),
    
    //Incremental (i.e. does not include upstream) predicted load.  
    incremental					(BaseDataSeriesType.incremental, false, false, false, false, true), //active
    incremental_std_error_estimate	(BaseDataSeriesType.incremental, true, false, false, true, false), //active
    
    incremental_yield			(BaseDataSeriesType.incremental, false, false, true, false, true), // active
    total_concentration			(BaseDataSeriesType.total, false, false, true, false, true), // active
    incremental_delivered_yield	(BaseDataSeriesType.incremental, false, true, true, false, true), // inaccurate?
    total_delivered_flux		(BaseDataSeriesType.total, false, true, false, false, true), // inaccurate?
    incremental_delivered_flux	(BaseDataSeriesType.incremental, false, true, false, false, true), // inaccurate?
    delivered_fraction			(BaseDataSeriesType.delivered_fraction, false, true, false, false, true), // inaccurate?
    total_decay					(BaseDataSeriesType.total_decay, false, false, false, true, true),
    total_no_decay				(BaseDataSeriesType.total_no_decay, false, false, false, true, true),
    source_value				(BaseDataSeriesType.source_value, false, false, false, false, true), // active
    land_to_water_coef			(BaseDataSeriesType.land_to_water_coef, false, false, false, false, true),
    instream_decay_coef			(BaseDataSeriesType.instream_decay_coef, false, false, false, false, true)
    ;

    // TODO cut down the list of attributes
    private final BaseDataSeriesType baseType;
    private final boolean errEstBased;
    private final boolean targetRequired;
    private final boolean weighted;
    private final boolean extraColumn;
    private final boolean analysisAllowed;



    DataSeriesType(BaseDataSeriesType baseType,
            boolean errEstBased, boolean targetRequired,
            boolean weighted, boolean extraColumn,
            boolean analysisAllowed) {

    	this.baseType = baseType;
        this.errEstBased = errEstBased;
        this.targetRequired = targetRequired;
        this.weighted = weighted;
        this.extraColumn = extraColumn;
        this.analysisAllowed = analysisAllowed;
    }

    /**
     * Returns true if this dataSeries uses PredictResult data (possibly in
     * combination with other data) to generate the returned data.
     * @return
     */
    public boolean isPredictionBased() {
        return baseType.isPredictionBased();
    }
    
    /**
     * Returns true if this dataseries is just showing data from PredictData
     * (possibly slightly massaged).  Source, coef values, and other values
     * pulled directly from the db are examples.
     * @return
     */
    public boolean isDataBased() {
        return baseType.isPredictDataBased();
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
    	return baseType.isDeliveryBased();
    }
    
    /**
     * Return true if this estimate is an estimate of of standard error.
     * @return
     */
    public boolean isStandardErrorEstimateBased() {
    	return errEstBased;
    }

    /**
     * Returns true if a source is either required or allowed for this dataseries.
     * @return
     */
    public boolean isSourceAllowed() {
        return baseType.isSourceAllowed();
    }

    /**
     * Returns true if this dataseries does not allow a source to be specified.
     * instream_decay_coef is an example:  There are no source specific values
     * for this coef.
     *
     * @return
     */
    public boolean isSourceDisallowed() {
        return baseType.isSourceDisallowed();
    }

    /**
     * Returns true if this dataseries requires a source to specified.
     * source_value is an example:  To report source values, you must specify
     * which source value.
     * @return
     */
    public boolean isSourceRequired() {
        return baseType.isSourceRequired();
    }

    /**
     * This dataseries requires Target reaches to be defined.
     * @return
     */
    public boolean isTargetRequired() {
        return targetRequired;
    }

    /**
     * Returns {@code true} if this data series requires a weighting be applied
     * to the returned data, {@code false} otherwise.
     *
     * @return {@code true} if this data series requires a weighting be applied
     *         to the returned data, {@code false} otherwise.
     */
    public boolean isWeighted() {
        return weighted;
    }

    /**
     * This dataseries requires an extra non-standard at the time the prediction
     * is run.  This will perhaps be an alternate implementation of PredictionRunner.
     * @return
     */
    public boolean isExtraColumn() {
        return extraColumn;
    }
    
    /**
     * Returns true if analysis (like aggregation) is allowed on this series.
     * @return
     */
    public boolean isAnalysisAllowed() {
    	return analysisAllowed;
    }
    
    /**
     * Returns true if analysis (like aggregation) is NOT allowed on this series.
     * @return
     */
    public boolean isAnalysisDisallowed() {
    	return ! analysisAllowed;
    }
}