package gov.usgswim.sparrow.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import gov.usgswim.sparrow.action.GetReachGroupsContainingReach;
import gov.usgswim.sparrow.action.GetReachGroupsOverlappingLogicalSet;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.LogicalSet;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgswim.sparrow.service.ServiceResponseOperation;
import gov.usgswim.sparrow.service.ServiceResponseWrapper;

//TODO this service returns two booleans, might want to make it return something more description
/**
 * service takes a reachId or logical group name, and an xml fragment which represents current adjustment groups. Will return a list of group names that overlap
 * with the passed in reachId or logical group name.
 */
public class ReachGroupService extends AbstractSparrowServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
		doPost(req, resp);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) {
		Long modelId = req.getParameter("modelId")==null ? Long.valueOf(0) : Long.parseLong((String)req.getParameter("modelId"));
		
		Long reachId = req.getParameter("reachId")==null ? Long.valueOf(0) : Long.parseLong((String)req.getParameter("reachId")); //individual reach to add to this group
		String group = req.getParameter("logicalSet_xml")==null ? "" : (String)req.getParameter("logicalSet_xml"); //a logical group to add to the given adjustment groups
		String existingGroupsXml = req.getParameter("existingGroups_xml")==null ? "" : (String)req.getParameter("existingGroups_xml"); //xml fragment for existing groups on front end
		
		try {
			ServiceResponseWrapper out = new ServiceResponseWrapper(Criteria.class, ServiceResponseOperation.GET);
			out.setStatus(ServiceResponseStatus.OK);
			
			AdjustmentGroups existingGroups = buildAdjGroups(modelId, existingGroupsXml);
			
			//three different strategies: huc groups, upstream reach groups, individual reaches 
			if(!group.equals("")) {
				GetReachGroupsOverlappingLogicalSet action = new GetReachGroupsOverlappingLogicalSet(buildNewLogicalSet(modelId, group), existingGroups);
				List<Criteria> results = action.run();
				out.addAllEntities(results);
			} else {
				GetReachGroupsContainingReach action = new GetReachGroupsContainingReach(reachId, existingGroups);
				List<Criteria> results = action.run();
				out.addAllEntities(results);
			}
			
			sendResponse(resp, out);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private AdjustmentGroups buildAdjGroups(long modelId, String existingGroupsXml) throws Exception {
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(existingGroupsXml));
		AdjustmentGroups adjGroups = new AdjustmentGroups(modelId);
		reader.next();
		adjGroups = adjGroups.parse(reader);
		
		return adjGroups;
	}
	
	public LogicalSet buildNewLogicalSet(long modelId, String xml) throws Exception {
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
		LogicalSet ls = new LogicalSet(modelId);
		reader.next();
		ls = ls.parse(reader);

		return ls;
	}
}
