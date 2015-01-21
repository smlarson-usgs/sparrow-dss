package gov.usgswim.sparrow.action;

import gov.usgs.cida.binning.domain.BinSet;
import org.apache.commons.lang.ArrayUtils;

/**
 * Creates a relative url for SLD template on GeoServer.
 *
 * @author eeverman
 */
public class CalcStyleUrlParams extends Action<String> {
	
	private BinSet binSet;
	
	protected static String[][] POSITIVE_HUES = new String[][] {
		{},
		{"FEC44F"},
		{"FEE391", "EC7014"},
		{"FFFFD4", "FEC44F", "CC4C02"},
		{"FFFFD4", "FEE391", "FE9929", "CC4C02"},
		{"FFFFD4", "FEE391", "FEC44F", "FE9929", "EC7014"}
	};
	
	protected static String[][] NEGATIVE_HUES = new String[][] {
		{},
		{"41B6C4"}, 
		{"225EA8", "41B6C4"}, 
		{"0C2C84", "1D91C0", "7FCDBB"}, 
		{"225EA8", "1D91C0", "41B6C4", "7FCDBB"} , 
		{"0C2C84", "225EA8", "1D91C0", "41B6C4", "7FCDBB"} 
	};
	
    public CalcStyleUrlParams(BinSet binSet) {
		this.binSet = binSet;
    }

	@Override
	protected void validate() {
		if (binSet == null) {
			addValidationError("The binset cannot be null.");
			return;
		}
		
		if (binSet.getActualBinCount() > 5) {
			addValidationError("The binset cannot have more than five bins in it.");
		}
	}
	
	
    
	@Override
	public String doAction() throws Exception {
		int count =  binSet.getActualBinCount();
		
    	Object[] colorList = buildColorList();
		
		Double[] posts = binSet.getActualPostDoubles();
		boolean unbounded = binSet.getBins()[0].getBottom().isUnbounded() || binSet.getBins()[count - 1].getTop().isUnbounded();
		
		
		return 
				"binLowList=" + join(ArrayUtils.subarray(posts, 0, posts.length - 1)) + "&" +
				"binHighList=" + join(ArrayUtils.subarray(posts, 1, posts.length)) + "&" +
				"binColorList=" + join(colorList) + "&" +
				"bounded=" + ((!unbounded)?"true":"false");

	}
	
	protected String join(Object[] array) {
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i].toString());
			sb.append(",");
		}
		
		return sb.substring(0, sb.length() - 1);
	}
	
	protected Object[] buildColorList() {
		int count =  binSet.getActualBinCount();
    	String lowStr = binSet.getBins()[0].getBottom().getFormattedFunctional();
    	String highStr = binSet.getBins()[count - 1].getTop().getFormattedFunctional();
		
		
    	Double low = Double.parseDouble(lowStr);
		Double high = Double.parseDouble(highStr);
		
        // Three cases:
        //	1. lo<0<hi 
        //  2. 0<=lo
        //  3. hi<=0

    	if (low < 0 && 0 < high) {
            // case 1: lo < 0 < hi
            // find bin which brackets zero. For bins below, use negative hues, for bins above, positive hues.
			
			int positiveBin = 0;
			
            for (positiveBin=0; positiveBin<count; positiveBin++){
				if (binSet.getBins()[positiveBin].getTop().getActual().doubleValue() > 0) {
					break;	//found the bin where the upper bound is greater than zero
				}
            }
			
            String[] negColors = useNegativeHues(positiveBin);
			String[] posColors = usePositiveHues(count - positiveBin);
			
            String[] all = new String[count];
			
			Object[] allColors = ArrayUtils.addAll(negColors, posColors);
			
			return allColors;
			
    	} else if (0 <= low) {
    		return usePositiveHues(count);
    	} else {
    		return useNegativeHues(count);
    	}
	}
	
	protected String[] usePositiveHues(int count){
		return POSITIVE_HUES[count];
    }
		
	protected String[] useNegativeHues(int count){
		return NEGATIVE_HUES[count];
    }
	
	
}