package com.eisenvault.utils;


import java.util.Collections;
import java.util.Set;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

public class PermissionHelper {

	private static Log logger = LogFactory.getLog(PermissionHelper.class);

	private AuthorityService authorityService;
	private PermissionService permissionService;
	private SiteService siteService;

	private Set<String> allowedGroups = Collections.emptySet();
	private Boolean writePermission = true;
	private Set<String> allowedSiteGroup = Collections.emptySet();

	public void setAllowedGroups(Set<String> allowedGroups) {
		this.allowedGroups = allowedGroups;
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setAllowedSiteGroup(Set<String> allowedSiteGroup) {
		this.allowedSiteGroup = allowedSiteGroup;
	}

	public Boolean isWritePermission() {
		return writePermission;
	}

	public void setWritePermission(Boolean writePermission) {
		this.writePermission = writePermission;
	}

	public boolean checkPermissions(final NodeRef ref) {
		return (hasRole() || hasSiteRole(ref)) && hasWritePermissions(ref);
	}

	public boolean hasWritePermissions(NodeRef ref) {
		String permission = PermissionService.WRITE;
		if (!isWritePermission()) {
			permission = PermissionService.READ;
		}
		AccessStatus status = permissionService.hasPermission(ref, permission);
		return AccessStatus.ALLOWED.equals(status);
	}

	private boolean hasRole() {
		if (CollectionUtils.isEmpty(this.allowedGroups)) {
			return false;
		}
		if (this.allowedGroups.contains("ALL")) {
			return true;
		}
		final String username = AuthenticationUtil.getFullyAuthenticatedUser();
		try {
			AuthenticationUtil.runAs(new RunAsWork<Void>() {

				@Override
				public Void doWork() throws Exception {
					boolean hasPermissions = false;
					final Set<String> auths = authorityService.getAuthoritiesForUser(username);
					for (String group : allowedGroups) {
						if (auths.contains("GROUP_" + group)) {
							hasPermissions = true;
							break;
						}
					}
					if (!hasPermissions) {
						throw new AccessDeniedException("Unauthorized");
					}
					return null;
				}
			}, AuthenticationUtil.getAdminUserName());
		} catch (AccessDeniedException e) {
			return false;
		} catch (Exception e) {
			logger.error("Error while check Permissions", e);
			return false;
		}
		return true;
	}

	private boolean hasSiteRole(final NodeRef nodeRef) {
		if (CollectionUtils.isEmpty(this.allowedSiteGroup)) {
			return false;
		}
		if (this.allowedSiteGroup.contains("ALL")) {
			return true;
		}
		final String username = AuthenticationUtil.getFullyAuthenticatedUser();
		try {
			AuthenticationUtil.runAs(new RunAsWork<Void>() {

				@Override
				public Void doWork() throws Exception {
					SiteInfo site = siteService.getSite(nodeRef);
					if (site == null) {
						throw new AccessDeniedException("Unauthorized");
					}

					String memberrole = siteService.getMembersRole(site.getShortName(), username);

					if (StringUtils.isEmpty(memberrole)) {
						throw new AccessDeniedException("Unauthorized");
					}
					if (!allowedSiteGroup.contains(memberrole)) {
						throw new AccessDeniedException("Unauthorized");
					}
					return null;
				}
			}, AuthenticationUtil.getAdminUserName());
		} catch (AccessDeniedException e) {
			return false;
		} catch (Exception e) {
			logger.error("Error while check Permissions", e);
			return false;
		}
		return true;
	}
}
