package gov.usgswim.sparrow.service.help;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

/**
 * @author ikuoikuo
 * TODO delete this class. Used for testing pruposes only
 */
public class Model {
	List<Author> authors;
 	MetaData md;

// 	public Object get(String itemName){}

 	public <T> T getOne(Class<T> clazz){
 		if (clazz.equals(Author.class)) {
 			return (authors == null)? null: clazz.cast(authors.get(0));
 		}
 		return null;
 	}

 	@SuppressWarnings("unchecked")
	public <T> List<T> getMany(Class<T> clazz){
 		if (clazz.equals(Author.class)) {
 			return (authors == null)? null: (List<T>) authors;
 		}
 		return null;
 	}

 	// =================
 	// XML SERIALIZATION
 	// =================
 	protected static final XStreamConfigurer configurer= new XStreamConfigurer() {
			@Override
	 		public XStream configure(XStream xstream) {
	 			xstream.alias("SparrowModel", Model.class);
	 			xstream.aliasField("Model_metadata", Model.class, "md");
	 			xstream.aliasField("MRB", MetaData.class, "mrb");
	 			xstream.aliasField("Name", MetaData.class, "modelName");
	 			xstream.aliasField("Constituent", MetaData.class, "constituent");
	 			xstream.alias("Source", Source.class);
	 			return xstream;
	 		}
	};

	public static XStreamConfigurer getXStreamConfigurer(XStream xstream) {
		return configurer;
	}

 	// =============
 	// INNER CLASSES
 	// =============
 	public static class Author{
 		String link;
 	}

 	public static class MetaData{
 		String modelName;
 		String mrb;
 		Constituent constituent;
 		List<Source> sources = new ArrayList<Source>();

 		public MetaData(String name, String mrb, Constituent constituent) {
 			this.modelName = name; this.mrb = mrb; this.constituent = constituent;
 		}
 	}

 	public static class Constituent{
 		String name,units,description;

 		public Constituent(String name, String units, String description) {
 			this.name = name; this.units = units; this.description = description;
 		}

 	}

 	public static class Source{
 		String name;
 		String units;
 		String description;

 		public Source(String name, String units, String description) {
 			this.name = name; this.units = units; this.description = description;
 		}
 	}




}
