package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.service.predict.aggregator.AggregateType;

/**
 * A data structure used to contain information about a specific row of
 * aggregated data.  One instance is created for each row of aggregate data
 * and the add() method is called for each piece of data added to the aggregation.
 */
public class AggregateData {
	AggregateType aggType;
    private Double value = null;
    private Long aggLevelId;	//The ID of the 'thing' we are aggregating to.  HUC ID for instance.
    private int count;

    /**
     * Constructs a new instance for a specified aggregation type and parent
     * ID.  Parent in this context refers to the thing we are aggregating to,
     * for instance, the ID of the specific HUC we are aggregating to.
     * @param aggType
     * @param aggLevelId
     */
    public AggregateData(AggregateType aggType, Long aggLevelId) {
        this.aggType = aggType;
        this.aggLevelId = aggLevelId;
    }

    /**
     * Returns the current value of the aggregation.
     * All calcs are done as running calcs, so averages and sums are up to date
     * for how ever many values have been added.
     * @return The aggregate value, which may be null if all null values were added or no values added.
     */
    public Double getValue() {
        return value;
    }
    
    /**
     * The number of non-null values added.
     * @return
     */
    public int getCount() {
        return count;
    }

    /**
     * The parent (HUC) ID.
     * @return
     */
	public Long getAggLevelId() {
		return aggLevelId;
	}
    
	
	/**
	 * Adds a value and updates the running aggregate value.
	 * @param aValue
	 */
    public void add(Double aValue) {
    	
    	//Don't count a null toward the aggregate
    	if (aValue == null) {
    		return;
    	}
    	
        switch (aggType) {
        
        case min:
        	
        	if (value == null) {
        		value = aValue;
        	} else if (aValue < value) {
        		value = aValue;
        	}	
        	break;
        
        case max:
        	if (value == null) {
        		value = aValue;
        	} else if (aValue > value) {
        		value = aValue;
        	}	
        	break;
        
        case sum:
        	if (value == null) {
        		value = aValue;
        	} else {
        		value+= aValue;
        	}	
        	break;
        	
        case avg:
        	if (value == null) {
        		value = aValue;
        	} else {
        		double inflatedAvg = value * (double) count;
        		value = (inflatedAvg + aValue) / (double) (count + 1);
        	}	
        	break;
        
        default:
        	throw new RuntimeException("The specified AggregateType " + aggType +
        			" is unexpected.");
    	}
        
        count++;
    }



}