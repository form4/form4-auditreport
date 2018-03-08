package com.eisenvault.webscript;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.eisenvault.utils.PermissionHelper;

public class HasPermissions extends DeclarativeWebScript {

	private static Log logger = LogFactory.getLog(HasPermissions.class);

	private Repository repository;

	private PermissionHelper permissionHelper;

	public void setPermissionHelper(PermissionHelper permissionHelper) {
		this.permissionHelper = permissionHelper;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}



	@Override
	public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>();

		try {
			String nodeRefString = req.getParameter("nodeRef");
			if (StringUtils.isBlank(nodeRefString)) {
				throw new Exception("'nodeRef' missing while processing request...");
			}

			nodeRefString = nodeRefString.replace(":/", "");
			NodeRef nodeRef = repository.findNodeRef("node", nodeRefString.split("/"));

			model.put("result", permissionHelper.checkPermissions(nodeRef));
		} catch (Exception e) {
			logger.error("Error", e);
			model.put("result", false);
		}

		return model;
	}



}
