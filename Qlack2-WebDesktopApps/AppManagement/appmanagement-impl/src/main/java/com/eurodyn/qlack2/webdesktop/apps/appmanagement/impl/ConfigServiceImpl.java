package com.eurodyn.qlack2.webdesktop.apps.appmanagement.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eurodyn.qlack2.fuse.idm.api.IDMService;
import com.eurodyn.qlack2.fuse.idm.api.annotations.ValidateTicket;
import com.eurodyn.qlack2.fuse.idm.api.signing.SignedTicket;
import com.eurodyn.qlack2.util.auditclient.api.AuditClientService;
import com.eurodyn.qlack2.webdesktop.api.DesktopGroupService;
import com.eurodyn.qlack2.webdesktop.api.DesktopUserService;
import com.eurodyn.qlack2.webdesktop.api.SecurityService;
import com.eurodyn.qlack2.webdesktop.api.dto.SecureOperationAccessDTO;
import com.eurodyn.qlack2.webdesktop.api.dto.UserDTO;
import com.eurodyn.qlack2.webdesktop.api.dto.UserGroupDTO;
import com.eurodyn.qlack2.webdesktop.api.request.security.AllowSecureOperationForGroupRequest;
import com.eurodyn.qlack2.webdesktop.api.request.security.AllowSecureOperationForUserRequest;
import com.eurodyn.qlack2.webdesktop.api.request.security.DenySecureOperationForGroupRequest;
import com.eurodyn.qlack2.webdesktop.api.request.security.DenySecureOperationForUserRequest;
import com.eurodyn.qlack2.webdesktop.api.request.security.GetAllowedGroupsForOperationRequest;
import com.eurodyn.qlack2.webdesktop.api.request.security.GetAllowedUsersForOperationRequest;
import com.eurodyn.qlack2.webdesktop.api.request.security.RemoveSecureOperationFromGroupRequest;
import com.eurodyn.qlack2.webdesktop.api.request.security.RemoveSecureOperationFromUserRequest;
import com.eurodyn.qlack2.webdesktop.api.request.security.RequirePermittedRequest;
import com.eurodyn.qlack2.webdesktop.api.request.user.GetUsersRequest;
import com.eurodyn.qlack2.webdesktop.api.request.user.IsUserRequest;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.api.ConfigService;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.api.exception.QInvalidOperationException;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.api.request.EmptySignedRequest;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.api.request.config.AddManagedSubjectRequest;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.api.request.config.GetManagedGroupsRequest;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.api.request.config.GetManagedUsersRequest;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.api.request.config.GetSecureOperationsRequest;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.api.request.config.RemoveManagedSubjectRequest;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.api.request.config.SaveSecureOperationsRequest;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.impl.dto.SecureOperationAuditDTO;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.impl.util.AuditConstants.EVENT;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.impl.util.AuditConstants.GROUP;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.impl.util.AuditConstants.LEVEL;
import com.eurodyn.qlack2.webdesktop.apps.appmanagement.impl.util.SecureOperation;

public class ConfigServiceImpl implements ConfigService {
	private static final Logger LOGGER = Logger.getLogger(ConfigServiceImpl.class.getName());

	@SuppressWarnings("unused")
	private IDMService idmService;
	private SecurityService security;
	private DesktopUserService desktopUserService;
	private DesktopGroupService desktopGroupService;
	private AuditClientService audit;

	public void setIdmService(IDMService idmService) {
		this.idmService = idmService;
	}

	public void setSecurity(SecurityService security) {
		this.security = security;
	}

	public void setDesktopUserService(DesktopUserService desktopUserService) {
		this.desktopUserService = desktopUserService;
	}

	public void setDesktopGroupService(DesktopGroupService desktopGroupService) {
		this.desktopGroupService = desktopGroupService;
	}

	public void setAudit(AuditClientService audit) {
		this.audit = audit;
	}

	private boolean isUser(String subjectId, SignedTicket signedTicket) {
		IsUserRequest wdReq = new IsUserRequest(subjectId);
		return desktopUserService.isUser(wdReq);
	}

	// --

	@Override
	@ValidateTicket
	public List<SecureOperationAccessDTO> getSecureOperations(GetSecureOperationsRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		security.requirePermitted(new RequirePermittedRequest(ticket, SecureOperation.APPMANAGEMENT_CONFIGURE.toString()));

		List<SecureOperationAccessDTO> retVal = null;
		if (isUser(sreq.getSubjectId(), ticket)) {
			retVal = getSecureOperationsForUser(sreq);
		} else {
			retVal = getSecureOperationsForGroup(sreq);
		}

		SecureOperationAuditDTO auditDTO = new SecureOperationAuditDTO();
		auditDTO.setApplicationId(sreq.getApplicationId());
		auditDTO.setSubjectId(sreq.getSubjectId());
		auditDTO.setOperations(retVal);
		audit.audit(LEVEL.WD_APPMANAGEMENT.toString(), EVENT.VIEW.toString(), GROUP.SECURE_OPERATIONS.toString(),
				null, ticket.getUserID(), auditDTO);

		return retVal;
	}

	private List<SecureOperationAccessDTO> getSecureOperationsForUser(GetSecureOperationsRequest sreq) {
		com.eurodyn.qlack2.webdesktop.api.request.security.GetSecureOperationsForUserRequest wdReq =
				new com.eurodyn.qlack2.webdesktop.api.request.security.GetSecureOperationsForUserRequest();
		wdReq.setSignedTicket(sreq.getSignedTicket());
		wdReq.setUserId(sreq.getSubjectId());
		wdReq.setResourceObjectId(sreq.getApplicationId());
		wdReq.setOperations(getSecureOperations(sreq.getApplicationId()));
		return security.getSecureOperationsForUser(wdReq);
	}

	private List<SecureOperationAccessDTO> getSecureOperationsForGroup(GetSecureOperationsRequest sreq) {
		com.eurodyn.qlack2.webdesktop.api.request.security.GetSecureOperationsForGroupRequest wdReq =
				new com.eurodyn.qlack2.webdesktop.api.request.security.GetSecureOperationsForGroupRequest();
		wdReq.setSignedTicket(sreq.getSignedTicket());
		wdReq.setGroupId(sreq.getSubjectId());
		wdReq.setResourceObjectId(sreq.getApplicationId());
		wdReq.setOperations(getSecureOperations(sreq.getApplicationId()));
		return security.getSecureOperationsForGroup(wdReq);
	}

	private List<String> getSecureOperations(String appId) {
		List<String> operations = new ArrayList<>();
		for (SecureOperation operation : SecureOperation.class.getEnumConstants()) {
			if (operation.isUiManaged()) {
				// Only show generic operations is an application has not been selected
				// and non-generic operations otherwise
				if (((appId == null) && (operation.isGeneric()))
						|| ((appId != null) && (!operation.isGeneric()))) {
					operations.add(operation.toString());
				}
			}
		}
		return operations;
	}

	@Override
	@ValidateTicket
	public void saveSecureOperations(SaveSecureOperationsRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		security.requirePermitted(new RequirePermittedRequest(ticket, SecureOperation.APPMANAGEMENT_CONFIGURE.toString()));

		if (isUser(sreq.getSubjectId(), ticket)) {
			saveSecureOperationsForUser(sreq);
		} else {
			saveSecureOperationsForGroup(sreq);
		}

		SecureOperationAuditDTO auditDTO = new SecureOperationAuditDTO();
		auditDTO.setApplicationId(sreq.getApplicationId());
		auditDTO.setSubjectId(sreq.getSubjectId());
		auditDTO.setOperations(sreq.getOperations());
		audit.audit(LEVEL.WD_APPMANAGEMENT.toString(), EVENT.UPDATE.toString(), GROUP.SECURE_OPERATIONS.toString(),
				null, ticket.getUserID(), auditDTO);
	}

	private void saveSecureOperationsForUser(SaveSecureOperationsRequest sreq) {
		LOGGER.log(Level.FINE, "Saving secure operations for user with ID {0}", sreq.getSubjectId());

		for (SecureOperationAccessDTO access : sreq.getOperations()) {
			if (!SecureOperation.contains(access.getOperation())) {
				throw new QInvalidOperationException("Operation "
						+ access.getOperation()
						+ " is not managed by the Application Management application");
			}
			if (access.getAccess() == null) {
				RemoveSecureOperationFromUserRequest wdReq = new RemoveSecureOperationFromUserRequest();
				wdReq.setSignedTicket(sreq.getSignedTicket());
				wdReq.setUserId(sreq.getSubjectId());
				wdReq.setOperationName(access.getOperation());
				wdReq.setResourceObjectId(sreq.getApplicationId());
				security.removeSecureOperationFromUser(wdReq);
			} else if (access.getAccess()) {
				AllowSecureOperationForUserRequest wdReq = new AllowSecureOperationForUserRequest();
				wdReq.setSignedTicket(sreq.getSignedTicket());
				wdReq.setUserId(sreq.getSubjectId());
				wdReq.setOperationName(access.getOperation());
				wdReq.setResourceObjectId(sreq.getApplicationId());
				security.allowSecureOperationForUser(wdReq);
			} else {
				DenySecureOperationForUserRequest wdReq = new DenySecureOperationForUserRequest();
				wdReq.setSignedTicket(sreq.getSignedTicket());
				wdReq.setUserId(sreq.getSubjectId());
				wdReq.setOperationName(access.getOperation());
				wdReq.setResourceObjectId(sreq.getApplicationId());
				security.denySecureOperationForUser(wdReq);
			}
		}
	}

	private void saveSecureOperationsForGroup(SaveSecureOperationsRequest sreq) {
		LOGGER.log(Level.FINE, "Saving secure operations for group with ID {0}", sreq.getSubjectId());

		for (SecureOperationAccessDTO access : sreq.getOperations()) {
			if (!SecureOperation.contains(access.getOperation())) {
				throw new QInvalidOperationException("Operation "
						+ access.getOperation()
						+ " is not managed by the Application Management application");
			}
			if (access.getAccess() == null) {
				RemoveSecureOperationFromGroupRequest wdReq = new RemoveSecureOperationFromGroupRequest();
				wdReq.setSignedTicket(sreq.getSignedTicket());
				wdReq.setGroupId(sreq.getSubjectId());
				wdReq.setOperationName(access.getOperation());
				wdReq.setResourceObjectId(sreq.getApplicationId());
				security.removeSecureOperationFromGroup(wdReq);
			} else if (access.getAccess()) {
				AllowSecureOperationForGroupRequest wdReq = new AllowSecureOperationForGroupRequest();
				wdReq.setSignedTicket(sreq.getSignedTicket());
				wdReq.setGroupId(sreq.getSubjectId());
				wdReq.setOperationName(access.getOperation());
				wdReq.setResourceObjectId(sreq.getApplicationId());
				security.allowSecureOperationForGroup(wdReq);
			} else {
				DenySecureOperationForGroupRequest wdReq = new DenySecureOperationForGroupRequest();
				wdReq.setSignedTicket(sreq.getSignedTicket());
				wdReq.setGroupId(sreq.getSubjectId());
				wdReq.setOperationName(access.getOperation());
				wdReq.setResourceObjectId(sreq.getApplicationId());
				security.denySecureOperationForGroup(wdReq);
			}
		}
	}

	// --

	@Override
	@ValidateTicket
	public void addManagedSubject(AddManagedSubjectRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		security.requirePermitted(new RequirePermittedRequest(ticket, SecureOperation.APPMANAGEMENT_CONFIGURE.toString()));

		if (isUser(sreq.getSubjectId(), ticket)) {
			addManagedUser(sreq);
		} else {
			addManagedGroup(sreq);
		}
	}

	private void addManagedUser(AddManagedSubjectRequest sreq) {
		LOGGER.log(Level.INFO, "Adding security managed user with ID {0}", sreq.getSubjectId());
		AllowSecureOperationForUserRequest wdReq = new AllowSecureOperationForUserRequest();
		wdReq.setSignedTicket(sreq.getSignedTicket());
		wdReq.setUserId(sreq.getSubjectId());
		wdReq.setOperationName(SecureOperation.APPMANAGEMENT_MANAGED.toString());
		wdReq.setResourceObjectId(sreq.getApplicationId());
		security.allowSecureOperationForUser(wdReq);
	}

	private void addManagedGroup(AddManagedSubjectRequest sreq) {
		LOGGER.log(Level.INFO, "Adding security managed group with ID {0}", sreq.getSubjectId());
		AllowSecureOperationForGroupRequest wdReq = new AllowSecureOperationForGroupRequest();
		wdReq.setSignedTicket(sreq.getSignedTicket());
		wdReq.setGroupId(sreq.getSubjectId());
		wdReq.setOperationName(SecureOperation.APPMANAGEMENT_MANAGED.toString());
		wdReq.setResourceObjectId(sreq.getApplicationId());
		security.allowSecureOperationForGroup(wdReq);
	}

	@Override
	@ValidateTicket
	public void removeManagedSubject(RemoveManagedSubjectRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		security.requirePermitted(new RequirePermittedRequest(ticket, SecureOperation.APPMANAGEMENT_CONFIGURE.toString()));

		if (isUser(sreq.getSubjectId(), ticket)) {
			removeManagedUser(sreq);
		} else {
			removeManagedGroup(sreq);
		}
	}

	private void removeManagedUser(RemoveManagedSubjectRequest sreq) {
		LOGGER.log(Level.INFO, "Removing security managed user with ID {0}", sreq.getSubjectId());

		SecureOperationAuditDTO auditDTO = new SecureOperationAuditDTO();
		auditDTO.setApplicationId(sreq.getApplicationId());
		auditDTO.setSubjectId(sreq.getSubjectId());
		auditDTO.setOperations(new ArrayList<SecureOperationAccessDTO>());

		// First remove all managed operations from the user
		for (SecureOperation operation : SecureOperation.class.getEnumConstants()) {
			if (operation.isUiManaged()) {
				RemoveSecureOperationFromUserRequest wdReq = new RemoveSecureOperationFromUserRequest();
				wdReq.setSignedTicket(sreq.getSignedTicket());
				wdReq.setUserId(sreq.getSubjectId());
				wdReq.setOperationName(operation.toString());
				wdReq.setResourceObjectId(sreq.getApplicationId());
				security.removeSecureOperationFromUser(wdReq);

				SecureOperationAccessDTO auditAccess = new SecureOperationAccessDTO(operation.toString(), false);
				auditDTO.getOperations().add(auditAccess);
			}
		}

		audit.audit(LEVEL.WD_APPMANAGEMENT.toString(), EVENT.UPDATE.toString(), GROUP.SECURE_OPERATIONS.toString(),
				null, sreq.getSignedTicket().getUserID(), auditDTO);

		// Before removing the "MANAGED" operation to make them unmanaged
		RemoveSecureOperationFromUserRequest wdReq = new RemoveSecureOperationFromUserRequest();
		wdReq.setSignedTicket(sreq.getSignedTicket());
		wdReq.setUserId(sreq.getSubjectId());
		wdReq.setOperationName(SecureOperation.APPMANAGEMENT_MANAGED.toString());
		wdReq.setResourceObjectId(sreq.getApplicationId());
		security.removeSecureOperationFromUser(wdReq);
	}

	private void removeManagedGroup(RemoveManagedSubjectRequest sreq) {
		LOGGER.log(Level.INFO, "Removing security managed group with ID {0}", sreq.getSubjectId());

		SecureOperationAuditDTO auditDTO = new SecureOperationAuditDTO();
		auditDTO.setApplicationId(sreq.getApplicationId());
		auditDTO.setSubjectId(sreq.getSubjectId());
		auditDTO.setOperations(new ArrayList<SecureOperationAccessDTO>());

		// First remove all managed operations from the groups
		for (SecureOperation operation : SecureOperation.class.getEnumConstants()) {
			if (operation.isUiManaged()) {
				RemoveSecureOperationFromGroupRequest wdReq = new RemoveSecureOperationFromGroupRequest();
				wdReq.setSignedTicket(sreq.getSignedTicket());
				wdReq.setGroupId(sreq.getSubjectId());
				wdReq.setOperationName(operation.toString());
				wdReq.setResourceObjectId(sreq.getApplicationId());
				security.removeSecureOperationFromGroup(wdReq);

				SecureOperationAccessDTO auditAccess = new SecureOperationAccessDTO(operation.toString(), false);
				auditDTO.getOperations().add(auditAccess);
			}
		}

		audit.audit(LEVEL.WD_APPMANAGEMENT.toString(), EVENT.UPDATE.toString(), GROUP.SECURE_OPERATIONS.toString(),
				null, sreq.getSignedTicket().getUserID(), auditDTO);

		// Before removing the "MANAGED" operation to make it unmanaged
		RemoveSecureOperationFromGroupRequest wdReq = new RemoveSecureOperationFromGroupRequest();
		wdReq.setSignedTicket(sreq.getSignedTicket());
		wdReq.setGroupId(sreq.getSubjectId());
		wdReq.setOperationName(SecureOperation.APPMANAGEMENT_MANAGED.toString());
		wdReq.setResourceObjectId(sreq.getApplicationId());
		security.removeSecureOperationFromGroup(wdReq);
	}

	@Override
	@ValidateTicket
	public Set<UserDTO> getManagedUsers(GetManagedUsersRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		security.requirePermitted(new RequirePermittedRequest(ticket, SecureOperation.APPMANAGEMENT_CONFIGURE.toString()));

		GetAllowedUsersForOperationRequest wdReq = new GetAllowedUsersForOperationRequest();
		wdReq.setSignedTicket(ticket);
		wdReq.setOperationName(SecureOperation.APPMANAGEMENT_MANAGED.toString());
		wdReq.setResourceObjectId(sreq.getApplicationId());
		wdReq.setCheckUserGroups(false);
		return security.getAllowedUsersForOperation(wdReq);
	}

	@Override
	@ValidateTicket
	public Set<UserGroupDTO> getManagedGroups(GetManagedGroupsRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		security.requirePermitted(new RequirePermittedRequest(ticket, SecureOperation.APPMANAGEMENT_CONFIGURE.toString()));

		GetAllowedGroupsForOperationRequest wdReq = new GetAllowedGroupsForOperationRequest();
		wdReq.setSignedTicket(ticket);
		wdReq.setOperationName(SecureOperation.APPMANAGEMENT_MANAGED.toString());
		wdReq.setResourceObjectId(sreq.getApplicationId());
		wdReq.setCheckAncestors(false);
		wdReq.setIncludeRelatives(sreq.isIncludeRelatives());
		wdReq.setIncludeUsers(sreq.isIncludeUsers());
		return security.getAllowedGroupsForOperation(wdReq);
	}

	// --

	@Override
	@ValidateTicket
	public List<UserDTO> getUsers(EmptySignedRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		security.requirePermitted(new RequirePermittedRequest(ticket, SecureOperation.APPMANAGEMENT_CONFIGURE.toString()));

		GetUsersRequest wdReq = new GetUsersRequest();
		wdReq.setSignedTicket(ticket);
		return desktopUserService.getUsers(wdReq);
	}

	@Override
	@ValidateTicket
	public List<UserGroupDTO> getGroups(EmptySignedRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		security.requirePermitted(new RequirePermittedRequest(ticket, SecureOperation.APPMANAGEMENT_CONFIGURE.toString()));

		com.eurodyn.qlack2.webdesktop.api.request.EmptySignedRequest wdReq =
				new com.eurodyn.qlack2.webdesktop.api.request.EmptySignedRequest();
		wdReq.setSignedTicket(ticket);
		return desktopGroupService.getDomainsAsTree(wdReq);
	}

}
