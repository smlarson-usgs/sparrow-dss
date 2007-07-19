package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.Data2DBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Misc {
	public Misc() throws Exception {

	}
	
	public static void main(String[] args) throws Exception {
	  moveColumn();
	}
	
	public static void moveColumn() throws FileNotFoundException,
																										IOException {
	  String path = "/datausgs/projects/sparrow/sparrow_sim_files-1-work/coef_copy.txt";
	  String out_path = "/datausgs/projects/sparrow/sparrow_sim_files-1-work/coef_out.txt";
	  Data2DBuilder data = TabDelimFileUtil.read(new File(path), true);
		
		for (int r = 0; r < data.getRowCount(); r++)  {
			Double error = (Double) data.getValue(r, 13);
			
			//ripple  all values down
			for (int i=12; i>0; i--) {
				Double v = (Double) data.getValue(r, i);
				data.setValueAt(v, r, i+1);
			}
			
		  data.setValueAt(error, r, 1);
			
		}


		TabDelimFileUtil.write(data, new File(out_path)); 
	}
	
	
}
