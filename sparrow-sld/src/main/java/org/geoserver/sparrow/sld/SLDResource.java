package org.geoserver.sparrow.sld;

import java.util.List;
import org.geoserver.rest.AbstractResource;
import org.geoserver.rest.format.DataFormat;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 *
 * @author isuftin
 */
public class SLDResource extends AbstractResource {

    @Override
    protected List<DataFormat> createSupportedFormats(Request request, Response response) {
        return null;
    }
    
}
