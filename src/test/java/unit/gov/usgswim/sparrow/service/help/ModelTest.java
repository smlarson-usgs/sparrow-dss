package gov.usgswim.sparrow.service.help;

import gov.usgswim.sparrow.service.help.Model.Constituent;
import gov.usgswim.sparrow.service.help.Model.MetaData;
import gov.usgswim.sparrow.service.help.Model.Source;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;


public class ModelTest {

	@Test
	public void testModelSerialization() {
		Model model = new Model();
		{
			Constituent constituent = new Constituent("Total Nitrogen", "kg/yr", "");
			model.md = new MetaData("National Total Nitrogen SparrowModel - 1987", "", constituent);
			Source source = new Source("ALL", "kg/yr", "All nitrogen sources - POINT, ATMDEP, FERTILIZER, WASTE, and NANAGR.");
			model.md.sources.add(source);
		}

		XStream xstream = new XStream();
		XStreamConfigurer configurer = Model.getXStreamConfigurer(xstream);
		configurer.configure(xstream);
		System.out.println(xstream.toXML(model));
	}


}
