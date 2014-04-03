package org.geoserver.sparrow.rest.resource.finder;

import org.geoserver.sparrow.rest.resource.SparrowSLDResource;
import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

/**
 *
 * @author isuftin
 */
public class ReflectiveSparrowSLDFormatFinder extends Finder {

    public ReflectiveSparrowSLDFormatFinder(){}
    
    @Override
    public Resource findTarget(Request request, Response response) {
        return new SparrowSLDResource(getContext(), request, response);
    }
    /**
     *
     * @param context
     */
    public ReflectiveSparrowSLDFormatFinder(Context context) {
        super(context);
    }
}
