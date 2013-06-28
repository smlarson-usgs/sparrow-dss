package gov.usgswim.sparrow.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Reads and writes a float array into a comma/whitespace separated list.
 * 
 * Since there is no concept of null, multiple consecutive delimiters are always
 * merged.
 * @author eeverman
 *
 */
public class FloatArrayXStreamConverter extends AbstractCollectionConverter {

	public FloatArrayXStreamConverter(Mapper mapper) {
		super(mapper);
	}

	public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
		return (type.isArray() && "[F".equals(type.getName()));
	}

	public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    	
		float[] array = (float[]) source;
		
    	if (array != null && array.length > 0) {
	    	
	    	StringBuffer sb = new StringBuffer(array.length * 10);
	    	
	        for (int i = 0; i < array.length; i++) {
	            float f = array[i];
	            sb.append(Float.toString(f));
	            sb.append(",");
	        }
	        
	        sb.deleteCharAt(sb.length() - 1);	//rm extra comma
	        
	        writer.setValue(sb.toString());
    	} else {
    		//nothing to do - leave parent tag empty
    	}

    }

	/**
	 * This implementation is not particularly efficent, since it autoboxes
	 * the primatives prior to creating a primative collection.
	 */
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		
		// read the items from xml into a list
		List<Float> items = new ArrayList<Float>();
		String value = StringUtils.trimToNull(reader.getValue());
		
		if (value != null) {
			StringTokenizer st = new StringTokenizer(value, ",  \t\n\r\f");
			while (st.hasMoreTokens()) {
				items.add(Float.parseFloat(st.nextToken()));
			}
		} else {
			//nothing to do
		}
		

		float[] array = new float[items.size()];
		
		for (int i = 0; i < array.length; i++) {
			array[i] = items.get(i);
		}
		
		return array;
	}
}
