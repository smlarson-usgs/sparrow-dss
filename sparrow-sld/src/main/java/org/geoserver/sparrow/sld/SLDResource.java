package org.geoserver.sparrow.sld;

import java.util.ArrayList;
import java.util.List;
import org.geoserver.rest.AbstractResource;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.rest.format.StringFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 *
 * @author isuftin
 */
public class SLDResource extends AbstractResource {

    @Override
    protected List<DataFormat> createSupportedFormats(Request request, Response response) {
        List<DataFormat> handledFormats = new ArrayList<>();
        handledFormats.add(new StringFormat(MediaType.TEXT_PLAIN));
        return handledFormats;
    }
    
    @Override
    public void handleGet() {
        DataFormat format = getFormatGet();
        getResponse().setEntity(format.toRepresentation("It works!"));
    }
    
}
