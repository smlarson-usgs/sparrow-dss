package gov.usgswim.sparrow.domain;

import java.io.Serializable;

/**
 *
 */
public enum DataSeriesType implements Serializable {

	//Organized as presented in the application

	//Model Estimates//
    total						(BaseDataSeriesType.total, false, false, false, false, true),
    decayed_incremental			(BaseDataSeriesType.decayed_incremental, false, false, false, false, true),
    total_concentration			(BaseDataSeriesType.total, false, false, true, true, true),
    incremental_yield			(BaseDataSeriesType.yield, false, false, true, false, true),
    total_yield					(BaseDataSeriesType.yield, false, false, true, false, true),

    //Model Inputs//
    source_value				(BaseDataSeriesType.source_value, false, false, false, false, true),

    //Stream Network//
    flux						(BaseDataSeriesType.flux, false, false, false, true, true),

    //Model Uncertainty//
    total_std_error_estimate	(BaseDataSeriesType.total, true, false, false, true, false),
    incremental_std_error_estimate	(BaseDataSeriesType.incremental, true, false, false, true, false),

    ///////////////////////
    //Downstream Tracking//
    incremental_delivered_flux	(BaseDataSeriesType.incremental, false, true, false, false, true),
    total_delivered_flux		(BaseDataSeriesType.total, false, true, false, false, true),
    delivered_fraction			(BaseDataSeriesType.delivered_fraction, false, true, false, false, true),
    incremental_delivered_yield	(BaseDataSeriesType.incremental, false, true, true, false, true),
    total_delivered_yield	(BaseDataSeriesType.yield, false, true, true, false, true),


    ///////////////////////
    //Used internally//
    ///////////////////////

    //Undecayed incremental load is always calculated - we derive the decayed version which is
    //what the user gets if they select 'incremental' in the UI.
    incremental					(BaseDataSeriesType.incremental, false, false, false, false, true),
    catch_area					(BaseDataSeriesType.catch_area, false, false, false, true, true),
    incremental_area				(BaseDataSeriesType.catch_area, false, false, false, true, true),
    total_contributing_area			(BaseDataSeriesType.watershed_area, false, false, false, true, true),

    //Area of the reach's catchment, plus the catchments of all upstream reaches
    total_upstream_area		(BaseDataSeriesType.watershed_area, false, false, false, true, true),

    ///////////////////////
    //Completely unused
    ///////////////////////
    total_decay					(BaseDataSeriesType.total_decay, false, false, false, true, true),
    total_no_decay				(BaseDataSeriesType.total_no_decay, false, false, false, true, true),
    land_to_water_coef			(BaseDataSeriesType.land_to_water_coef, false, false, false, false, true),
    instream_decay_coef			(BaseDataSeriesType.instream_decay_coef, false, false, false, false, true),
    huc_area					(BaseDataSeriesType.huc_area, false, false, false, true, true)

    ;

    // TODO cut down the list of attributes
    private final BaseDataSeriesType baseType;
    private final boolean errEstBased;
    private final boolean deliveryRequired;
    private final boolean weighted;
    private final boolean extraColumn;
    private final boolean analysisAllowed;



    DataSeriesType(BaseDataSeriesType baseType,
            boolean errEstBased, boolean deliveryRequired,
            boolean weighted, boolean extraColumn,
            boolean analysisAllowed) {

    	this.baseType = baseType;
        this.errEstBased = errEstBased;
        this.deliveryRequired = deliveryRequired;
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
     * This dataseries requires delivery to calculate, thus a set of
     * Target reaches must to be defined as well.
     *
     * @return
     */
    public boolean isDeliveryRequired() {
        return deliveryRequired;
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

	/**
	 * Returns the more generic base type of this type.
	 * @return the baseType
	 */
	public BaseDataSeriesType getBaseType() {
		return baseType;
	}

	/**
	 * Returns true if the series is error estimate based.
	 * @return the errEstBased
	 */
	public boolean isErrEstBased() {
		return errEstBased;
	}


}