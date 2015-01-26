package gov.usgswim.sparrow.action;

import java.util.ArrayList;

import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;

/**
 * Action will load a context, check all of its results to see if they are in only 1 bin, if less than 3 bins specified
 * 
 * Will return a true or false string value
 */
public class VerifyBinningResolution extends Action<Boolean> {
	String[] binHighList;
	String[] binLowList;
	ArrayList<Double[]> bins;
	SparrowColumnSpecifier data;
	
    public VerifyBinningResolution(SparrowColumnSpecifier data, String[] high, String[] low) throws Exception {
    	this.data = data;
    	this.binHighList = high;
    	this.binLowList = low;
    	
    	if(this.binHighList.length != this.binLowList.length) throw new Exception("Invalid bin specification");
    	
    	bins = new ArrayList<Double[]>();
    	
    	for(int i =0; i < binHighList.length; i ++) {
    		bins.add(new Double[]{Double.parseDouble(binLowList[i]), Double.parseDouble(binHighList[i])});
    	}
    }
    
	public Boolean doAction() throws Exception {
		if(bins.size() < 3) return Boolean.TRUE; //verification doesn't need to happen with 1 or 2 bins
		
		int[] binCounts = new int[bins.size()];
		for(int i =0; i< binCounts.length; i++ ) binCounts[i] = 0; //init all counts to 0
		
		//iterate through all values and increment the binCount that the value falls in.
		for (int r=0; r<data.getTable().getRowCount(); r++) {
			Double value = data.getTable().getDouble(r, data.getColumn());
			
			if (value != null && !Double.isNaN(value)) {
				for(int i = 0; i<bins.size(); i ++) {
					if (value.compareTo(bins.get(i)[0])>=0 && value.compareTo(bins.get(i)[1])<=0) binCounts[i]++;
				}
			}
		}
		
		//check the bin counts so to make sure more than one bin has values
		int numOfBinsWithValues = 0;
		for(int i = 0; i < binCounts.length; i++){
			if(binCounts[i]>0) numOfBinsWithValues++;
		}
		if(numOfBinsWithValues == 1 ) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}
	
	@Override
	public Long getModelId() {
		if (data != null) {
			return data.getModelId();
		} else {
			return null;
		}
	}
}