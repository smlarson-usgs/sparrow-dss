package org.geoserver.sparrow.rest.resource;

import freemarker.core.ParseException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.geoserver.rest.format.DataFormat;
import org.geotools.util.logging.Logging;
import org.restlet.data.Request;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;

/**
 *
 * @author isuftin
 */
public class RefectiveSparrowSLDFormat extends DataFormat {

    private static final Logger LOGGER = Logging.getLogger(RefectiveSparrowSLDFormat.class);
    private final Request request;

    public RefectiveSparrowSLDFormat(Request request) {
        super(SparrowSLDResource.MEDIATYPE_SLD);
        this.request = request;
    }

    @Override
    public Representation toRepresentation(Object object) {
        Template template = null;
        String templateName;

        Configuration configuration = createConfiguration(object);
        final ObjectWrapper wrapper = configuration.getObjectWrapper();
        configuration.setObjectWrapper(wrapper);

        // Try to use the request to get the template name
        if (template == null && request != null) {
            // Use the last segment of the reqyest to try to find the template name
            String lastRequestSegment = request.getResourceRef().getLastSegment();
            if (StringUtils.isBlank(lastRequestSegment)) {
                // Alternate way to get last segment if not yet found
                lastRequestSegment = request.getResourceRef().getBaseRef().getLastSegment();
            }
            // Dump the extension name from the segment
            int i = lastRequestSegment.lastIndexOf(".");
            if (i != -1) {
                lastRequestSegment = lastRequestSegment.substring(0, i);
            }
            template = tryLoadTemplate(configuration, lastRequestSegment + ".ftl");
        }

        if (template != null) {
            templateName = template.getName();
        } else {
            //use a fallback
            templateName = "failover.ftl";
        }

        return new TemplateRepresentation(templateName, configuration, object, getMediaType());
    }

    protected Template tryLoadTemplate(Configuration configuration, String templateName) {
        try {
            return configuration.getTemplate(templateName);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (IOException io) {
            LOGGER.log(Level.FINE, "Failed to lookup template " + templateName, io);
            return null;
        }
    }

    protected Configuration createConfiguration(Object data) {
        Configuration cfg = new Configuration();
        cfg.setLocale(Locale.US);
        cfg.setClassForTemplateLoading(getClass(), "templates");
        cfg.setObjectWrapper(new ObjectToMapWrapper<>(SparrowSLDInfo.class));
        return cfg;
    }

    protected class ObjectToMapWrapper<T> extends BeansWrapper {

        Class<T> clazz;

        public ObjectToMapWrapper(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public TemplateModel wrap(Object object) throws TemplateModelException {
            SimpleHash properties = new SimpleHash(ObjectWrapper.BEANS_WRAPPER);
            SparrowSLDInfo sldInfo = (SparrowSLDInfo) object;
            properties.put("workspace", sldInfo.getWorkspace());
            properties.put("layer", sldInfo.getLayer());
            properties.put("sldName", sldInfo.getSldName());
            properties.put("bins", new SimpleSequence(sldInfo.getBins(), null));
            return properties;
        }

    }

}
