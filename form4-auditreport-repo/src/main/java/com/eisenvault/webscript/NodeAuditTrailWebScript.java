package com.eisenvault.webscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.http.HttpStatus;

import com.eisenvault.utils.PermissionHelper;

public class NodeAuditTrailWebScript extends DeclarativeWebScript {
	private static Log logger = LogFactory.getLog(NodeAuditTrailWebScript.class);
	private NodeService nodeService;
	private AuditService auditService;
	private Repository repository;
	private NamespaceService namespaceService;
	private PermissionHelper permissionHelper;

	public void setPermissionHelper(PermissionHelper permissionHelper) {
		this.permissionHelper = permissionHelper;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>();

		try{

			String nodeRefString = req.getParameter("nodeRef");
			if(StringUtils.isBlank(nodeRefString)) {
				throw new Exception("'nodeRef' missing while processing request...");
			}

			nodeRefString = nodeRefString.replace(":/", "");
			NodeRef nodeRef = repository.findNodeRef("node", nodeRefString.split("/"));
			
			if (!permissionHelper.checkPermissions(nodeRef)) {
				model.put("data", new ArrayList<>());
				status.setCode(HttpStatus.FORBIDDEN.value());
				return model;
			}

			List<TemplateAuditInfo> auditTrailList = getAuditTrail(nodeRef);
			List<Map<String,Object>> auditList = new ArrayList<Map<String,Object>>(auditTrailList.size());
			for(TemplateAuditInfo auditInfo : auditTrailList){
				if(logger.isDebugEnabled()){
					logger.debug(auditInfo.toString());
				}
				Map<String,Object> auditInfoMap = new HashMap<String,Object>();
				auditInfoMap.put("userName", auditInfo.getUserIdentifier());
				auditInfoMap.put("applicationName", auditInfo.getAuditApplication());
				auditInfoMap.put("applicationMethod", auditInfo.getAuditMethod());
				auditInfoMap.put("date", auditInfo.getDate());
				//auditInfoMap.put("auditValues", auditInfo.getValues());
				if(auditInfo.getValues() != null){
					//Convert values to Strings
					Map<String, String> valueStrings = new HashMap<String,String>(auditInfo.getValues().size()*2);
					for(Map.Entry<String, Serializable> mapEntry : auditInfo.getValues().entrySet()){
						String key = mapEntry.getKey();
						Serializable value = mapEntry.getValue();
						try{
							String valueString = DefaultTypeConverter.INSTANCE.convert(String.class, value);
							valueStrings.put(key, valueString);
						}catch(TypeConversionException e){
							valueStrings.put(key, value.toString());
						}
					}
					auditInfoMap.put("auditValues",valueStrings);
				}

				auditList.add(auditInfoMap);
			}
			Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
			String fileName = (String) props.get(ContentModel.PROP_NAME);

			model.put("data", auditList);
			model.put("nodeRef", nodeRef.getId());
			model.put("fileName",fileName);
			model.put("count", auditList.size());
			model.put("returnStatus", Boolean.TRUE);
			model.put("statusMessage", "Successfully retrieved audit trail for nodeRef["+nodeRef.getId()+"]");

		}catch(Exception e){
			logger.warn(e.getMessage());
			model.put("returnStatus", Boolean.FALSE);
			model.put("statusMessage", e.getMessage());
		}
		return model;
	}

	private List<TemplateAuditInfo> getAuditTrail(NodeRef nodeRef) {
		final List<TemplateAuditInfo> result = new ArrayList<TemplateAuditInfo>();

		final AuditQueryCallback callback = new AuditQueryCallback() {

			@Override
			public boolean valuesRequired() {
				return true;
			}

			@Override
			public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error) {
				throw new AlfrescoRuntimeException("Failed to retrieve audit data.", error);
			}

			@Override
			public boolean handleAuditEntry(Long entryId, String applicationName, String userName, long time, Map<String, Serializable> values) {
				TemplateAuditInfo auditInfo = new TemplateAuditInfo(applicationName, userName, time, values);
				result.add(auditInfo);
				return true;
			}
		};

		// resolve the path of the node
		final String nodePath = ISO9075.decode(nodeService.getPath(nodeRef).toPrefixString(namespaceService));

		AuthenticationUtil.runAs(new RunAsWork<Object>() {
			@Override
			public Object doWork() throws Exception {
				String applicationName = "alfresco-access";
				AuditQueryParameters pathParams = new AuditQueryParameters();
				pathParams.setApplicationName(applicationName);
				pathParams.addSearchKey("/alfresco-access/transaction/path", nodePath);
				auditService.auditQuery(callback, pathParams, Integer.MAX_VALUE);

				AuditQueryParameters copyFromPathParams = new AuditQueryParameters();
				copyFromPathParams.setApplicationName(applicationName);
				copyFromPathParams.addSearchKey("/alfresco-access/transaction/copy/from/path", nodePath);
				auditService.auditQuery(callback, copyFromPathParams, Integer.MAX_VALUE);

				AuditQueryParameters moveFromPathParams = new AuditQueryParameters();
				moveFromPathParams.setApplicationName(applicationName);
				moveFromPathParams.addSearchKey("/alfresco-access/transaction/move/from/path", nodePath);
				auditService.auditQuery(callback, moveFromPathParams, Integer.MAX_VALUE);

				return null;
			}
		}, AuthenticationUtil.getAdminUserName());

		// sort audit entries by time of generation
		Collections.sort(result, new Comparator<TemplateAuditInfo>()
		{
			@Override
			public int compare(TemplateAuditInfo o1, TemplateAuditInfo o2)
			{
				return o1.getDate().compareTo(o2.getDate());
			}
		});
		return result;
	}

	public class TemplateAuditInfo
	{
		private String applicationName;
		private String userName;
		private long time;
		private Map<String, Serializable> values;

		public TemplateAuditInfo(String applicationName, String userName, long time, Map<String, Serializable> values)
		{
			this.applicationName = applicationName;
			this.userName = userName;
			this.time = time;
			this.values = values;
		}

		public String getAuditApplication()
		{
			return this.applicationName;
		}

		public String getUserIdentifier()
		{
			return this.userName;
		}

		public Date getDate()
		{
			return new Date(time);
		}

		public String getAuditMethod()
		{
			if(this.values.get("/alfresco-access/transaction/action").equals("updateNodeProperties")){
				return "UPDATE NODE PROPERTIES";
			}else if(this.values.get("/alfresco-access/transaction/action").equals("readContent")){
				return "READ CONTENT";
			} else {
				return this.values.get("/alfresco-access/transaction/action").toString();
			}
		}

		public Map<String, Serializable> getValues()
		{
			return this.values;
		}

		@Override
		public String toString(){
			return "TemplateAuditInfo [applicationName=" + applicationName + ",userName= " + userName + ",time= " + time + ",values= "+ values +"]";
		}
	}
}
