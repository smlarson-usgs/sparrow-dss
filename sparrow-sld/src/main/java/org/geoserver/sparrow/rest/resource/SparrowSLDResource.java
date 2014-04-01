package org.geoserver.sparrow.rest.resource;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.geoserver.rest.AbstractResource;
import org.geoserver.rest.format.DataFormat;
import org.geoserver.rest.format.MediaTypes;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 *
 * @author isuftin
 */
public class SparrowSLDResource extends AbstractResource {

    public static final MediaType MEDIATYPE_SLD = new MediaType("application/vnd.ogc.sld+xml");
    static {
        MediaTypes.registerExtension("sld", MEDIATYPE_SLD);
    }

    public SparrowSLDResource() {
    }

    public SparrowSLDResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    protected List<DataFormat> createSupportedFormats(Request request, Response response) {
        List<DataFormat> handledFormats = new ArrayList<>();
        handledFormats.add(new RefectiveSparrowSLDFormat(this.getClass(), super.getRequest(), super.getResponse(), this));
        return handledFormats;
    }

    @Override
    public void handleGet() {
        String workspace = getAttribute("workspace");
        String layer = getAttribute("layer");
        String sldName = getAttribute("sldName");
        String binLowList = getQueryStringValue("binLowList", String.class, "");
        String binHighList = getQueryStringValue("binHighList", String.class, "");
        String binColorList = getQueryStringValue("binColorList", String.class, "");
        String[] binLowListArray = StringUtils.split(binLowList, ',');
        String[] binHighListArray = StringUtils.split(binHighList, ',');
        String[] binColorListArray = StringUtils.split(binColorList, ',');
        
        DataFormat format = getFormatGet();
        SparrowSLDInfo sldInfo = new SparrowSLDInfo(workspace, layer, sldName, binLowListArray, binHighListArray, binColorListArray);
        Representation sldRepresentation = format.toRepresentation(sldInfo);
        getResponse().setEntity(sldRepresentation);
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return false;
    }

    @Override
    public boolean allowPost() {
        return false;
    }

    @Override
    public boolean allowPut() {
        return false;
    }

}
