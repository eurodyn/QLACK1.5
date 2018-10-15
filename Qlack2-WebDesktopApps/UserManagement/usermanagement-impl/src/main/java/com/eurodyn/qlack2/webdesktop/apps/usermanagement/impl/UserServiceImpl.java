package com.eurodyn.qlack2.webdesktop.apps.usermanagement.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eurodyn.qlack2.fuse.eventpublisher.api.EventPublisherService;
import com.eurodyn.qlack2.fuse.idm.api.IDMService;
import com.eurodyn.qlack2.fuse.idm.api.annotations.ValidateTicket;
import com.eurodyn.qlack2.fuse.idm.api.signing.SignedTicket;
import com.eurodyn.qlack2.util.auditclient.api.AuditClientService;
import com.eurodyn.qlack2.webdesktop.api.DesktopUserService;
import com.eurodyn.qlack2.webdesktop.api.dto.UserDTO;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.api.UserService;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.api.request.user.CreateUserRequest;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.api.request.user.DeleteUserRequest;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.api.request.user.GetUserRequest;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.api.request.user.GetUsersRequest;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.api.request.user.UpdateUserRequest;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.api.util.Constants;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.impl.util.AuditConstants.EVENT;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.impl.util.AuditConstants.GROUP;
import com.eurodyn.qlack2.webdesktop.apps.usermanagement.impl.util.AuditConstants.LEVEL;

public class UserServiceImpl implements UserService {
	@SuppressWarnings("unused")
	private IDMService idmService;
	private DesktopUserService desktopUserService;
	private AuditClientService audit;
	private EventPublisherService eventPublisher;

	public void setDesktopUserService(DesktopUserService desktopUserService) {
		this.desktopUserService = desktopUserService;
	}

	public void setIdmService(IDMService idmService) {
		this.idmService = idmService;
	}

	public void setAudit(AuditClientService audit) {
		this.audit = audit;
	}

	public void setEventPublisher(EventPublisherService eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	private void publishEvent(SignedTicket signedTicket, String event, String userId) {
		Map<String, Object> message = new HashMap<>();
		message.put("srcUserId", signedTicket.getUserID());
		message.put("event", event);
		message.put(Constants.EVENT_DATA_USER_ID, userId);

		eventPublisher.publishSync(message,
				"com/eurodyn/qlack2/wd/apps/usermanagement/"
						+ Constants.RESOURCE_TYPE_USER + "/" + event);
	}

	@Override
	@ValidateTicket
	public List<UserDTO> getUsers(GetUsersRequest sreq) {
		com.eurodyn.qlack2.webdesktop.api.request.user.GetUsersRequest dreq =
				new com.eurodyn.qlack2.webdesktop.api.request.user.GetUsersRequest();
		dreq.setGroupId(sreq.getGroupId());
		dreq.setIncludeGroups(sreq.isIncludeGroups());
		dreq.setSignedTicket(sreq.getSignedTicket());

		audit.audit(LEVEL.WD_USERMANAGEMENT.toString(), EVENT.VIEW.toString(), GROUP.ALL_USERS.toString(),
				null, sreq.getSignedTicket().getUserID(), null);

		return desktopUserService.getUsers(dreq);
	}

	@Override
	@ValidateTicket
	public UserDTO getUser(GetUserRequest sreq) {
		com.eurodyn.qlack2.webdesktop.api.request.user.GetUserRequest dreq =
				new com.eurodyn.qlack2.webdesktop.api.request.user.GetUserRequest(sreq.getUserId(), sreq.isIncludeGroups());
		dreq.setSignedTicket(sreq.getSignedTicket());
		UserDTO user = desktopUserService.getUser(dreq);

		audit.audit(LEVEL.WD_USERMANAGEMENT.toString(), EVENT.VIEW.toString(), GROUP.USER.toString(),
				null, sreq.getSignedTicket().getUserID(), user);

		return user;
	}

	@Override
	@ValidateTicket
	public String createUser(CreateUserRequest sreq) {
		com.eurodyn.qlack2.webdesktop.api.request.user.CreateUserRequest dreq =
				new com.eurodyn.qlack2.webdesktop.api.request.user.CreateUserRequest();
		dreq.setSignedTicket(sreq.getSignedTicket());
		dreq.setActive(sreq.isActive());
		dreq.setEmail(sreq.getEmail());
		dreq.setFirstName(sreq.getFirstName());
		dreq.setGroupIds(sreq.getGroupIds());
		dreq.setLastName(sreq.getLastName());
		dreq.setPassword(sreq.getPassword());
		dreq.setSuperadmin(sreq.isSuperadmin());
		dreq.setUsername(sreq.getUsername());

		String userId = desktopUserService.createUser(dreq);

		UserDTO user = getUserForAudit(sreq.getSignedTicket(), userId);
		audit.audit(LEVEL.WD_USERMANAGEMENT.toString(), EVENT.CREATE.toString(), GROUP.USER.toString(),
				null, sreq.getSignedTicket().getUserID(), user);

		publishEvent(sreq.getSignedTicket(), Constants.EVENT_CREATE, userId);

		return userId;
	}

	@Override
	@ValidateTicket
	public void updateUser(UpdateUserRequest sreq) {
		com.eurodyn.qlack2.webdesktop.api.request.user.UpdateUserRequest dreq =
				new com.eurodyn.qlack2.webdesktop.api.request.user.UpdateUserRequest();
		dreq.setSignedTicket(sreq.getSignedTicket());
		dreq.setActive(sreq.isActive());
		dreq.setEmail(sreq.getEmail());
		dreq.setFirstName(sreq.getFirstName());
		dreq.setGroupIds(sreq.getGroupIds());
		dreq.setLastName(sreq.getLastName());
		dreq.setPassword(sreq.getPassword());
		dreq.setSuperadmin(sreq.isSuperadmin());
		dreq.setUsername(sreq.getUsername());
		dreq.setUserId(sreq.getUserId());

		desktopUserService.updateUser(dreq);

		UserDTO user = getUserForAudit(sreq.getSignedTicket(), sreq.getUserId());
		audit.audit(LEVEL.WD_USERMANAGEMENT.toString(), EVENT.UPDATE.toString(), GROUP.USER.toString(),
				null, sreq.getSignedTicket().getUserID(), user);

		publishEvent(sreq.getSignedTicket(), Constants.EVENT_UPDATE, sreq.getUserId());
	}

	@Override
	@ValidateTicket
	public void deleteUser(DeleteUserRequest sreq) {
		UserDTO user = getUserForAudit(sreq.getSignedTicket(), sreq.getUserId());

		com.eurodyn.qlack2.webdesktop.api.request.user.DeleteUserRequest dreq =
				new com.eurodyn.qlack2.webdesktop.api.request.user.DeleteUserRequest(sreq.getUserId());
		dreq.setSignedTicket(sreq.getSignedTicket());
		desktopUserService.deleteUser(dreq);

		audit.audit(LEVEL.WD_USERMANAGEMENT.toString(), EVENT.DELETE.toString(), GROUP.USER.toString(),
				null, sreq.getSignedTicket().getUserID(), user);

		publishEvent(sreq.getSignedTicket(), Constants.EVENT_DELETE, sreq.getUserId());
	}

	private UserDTO getUserForAudit(SignedTicket ticket, String userId) {
		com.eurodyn.qlack2.webdesktop.api.request.user.GetUserUncheckedRequest getReq =
				new com.eurodyn.qlack2.webdesktop.api.request.user.GetUserUncheckedRequest(userId, true);
		return desktopUserService.getUserUnchecked(getReq);
	}

}
