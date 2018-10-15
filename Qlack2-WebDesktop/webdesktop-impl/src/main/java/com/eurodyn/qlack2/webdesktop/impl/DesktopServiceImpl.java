package com.eurodyn.qlack2.webdesktop.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;

import com.eurodyn.qlack2.fuse.aaa.api.OperationService;
import com.eurodyn.qlack2.fuse.idm.api.exception.QAuthorisationException;
import com.eurodyn.qlack2.fuse.idm.api.signing.SignedTicket;
import com.eurodyn.qlack2.webdesktop.api.DesktopService;
import com.eurodyn.qlack2.webdesktop.api.dto.ApplicationInfo;
import com.eurodyn.qlack2.webdesktop.api.request.EmptySignedRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.AddAppToUserDesktopRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.GetAllAppsRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.GetAppAccessRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.GetAppDetailsRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.GetAppsForGroupRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.GetDesktopIconsForUserRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.RemoveAppFromUserDesktopRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.UpdateApplicationRequest;
import com.eurodyn.qlack2.webdesktop.api.util.Constants;
import com.eurodyn.qlack2.webdesktop.impl.model.Application;
import com.eurodyn.qlack2.webdesktop.impl.model.DesktopIcon;
import com.eurodyn.qlack2.webdesktop.impl.util.ConverterUtil;

public class DesktopServiceImpl implements DesktopService {
	private static final Logger LOGGER = Logger.getLogger(DesktopServiceImpl.class.getName());

	private EntityManager em;
	private OperationService operationService;

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}

	// --

	private boolean canAccessApp(SignedTicket ticket, String appId) {
		Boolean permission = operationService.isPermitted(ticket.getUserID(), Constants.OP_ACCESS_APPLICATION, appId);
		return (permission != null) && permission;
	}

	private boolean canUpdateApp(SignedTicket ticket) {
		Boolean permission = operationService.isPermitted(ticket.getUserID(), Constants.OP_UPDATE_APPLICATION);
		return (permission != null) && permission;
	}

	@Override
	public List<String> getGroups(EmptySignedRequest sreq) {
		List<Application> applications = Application.getAllApps(null, em);

		List<String> groups = new ArrayList<>();
		for (Application application : applications) {
			// Only return the groups the user is allowed to see, ie. the groups containing
			// not restricted applications or the groups containing applications available to the user.
			if ((!application.isRestrictAccess()) || canAccessApp(sreq.getSignedTicket(), application.getAppUuid())) {
				if (!groups.contains(application.getGroupKey())) {
					groups.add(application.getGroupKey());
				}
			} else {
				LOGGER.log(
						Level.FINE,
						"User with ticket {0} does not have access to application with UUID {1}.",
						new String[] { sreq.getSignedTicket().toString(), application.getAppUuid() });
			}
		}
		Collections.sort(groups);
		return groups;
	}

	@Override
	public List<ApplicationInfo> getAllApps(GetAllAppsRequest sreq) {
		List<Application> applications = Application.getAllApps(sreq.getActive(), em);

		List<ApplicationInfo> appInfo = new ArrayList<>(applications.size());
		for (Application application : applications) {
			// Check that the user is allowed access to the application
			// before returning it if the application has restricted access.
			if ((!application.isRestrictAccess()) || canAccessApp(sreq.getSignedTicket(), application.getAppUuid())) {
				appInfo.add(ConverterUtil.applicationToApplicationInfo(application));
			} else {
				LOGGER.log(
						Level.FINE,
						"Application with UUID {0} is not returned for user with ticket {1} since the user "
								+ "is not permitted access",
						new String[] { application.getAppUuid(), sreq.getSignedTicket().toString() });
			}
		}

		return appInfo;
	}

	@Override
	public List<ApplicationInfo> getAppsForGroup(GetAppsForGroupRequest sreq) {
		List<Application> applications = null;
		// Check if a group keyname has been passed otherwise retrieve applications with no group.
		if (sreq.getGroupKeyname() != null) {
			applications = Application.getAppsForGroup(sreq.getGroupKeyname(), sreq.getActive(), em);
		} else {
			applications = Application.getAppsWithNoGroup(sreq.getActive(), em);
		}

		List<ApplicationInfo> appInfo = new ArrayList<>(applications.size());
		for (Application application : applications) {
			if ((!application.isRestrictAccess()) || canAccessApp(sreq.getSignedTicket(), application.getAppUuid())) {
				appInfo.add(ConverterUtil.applicationToApplicationInfo(application));
			} else {
				LOGGER.log(
						Level.FINE,
						"Application with UUID {0} of group {1} is not returned for user with ticket {2} since the user "
								+ "is not permitted access",
						new String[] { application.getAppUuid(), sreq.getGroupKeyname(), sreq.getSignedTicket().toString() });
			}
		}

		return appInfo;
	}

	@Override
	public ApplicationInfo getAppDetails(GetAppDetailsRequest sreq) {
		Application application = em.find(Application.class, sreq.getAppID());
		if ((!application.isRestrictAccess()) || canAccessApp(sreq.getSignedTicket(), application.getAppUuid())) {
			return ConverterUtil.applicationToApplicationInfo(application);
		} else {
			throw new QAuthorisationException(
					sreq.getSignedTicket().getUserID(),
					sreq.getSignedTicket().toString(),
					Constants.OP_ACCESS_APPLICATION,
					application.getAppUuid());
		}
	}

	@Override
	public boolean getAppAccess(GetAppAccessRequest sreq) {
		Application application = em.find(Application.class, sreq.getAppID());
		if ((!application.isRestrictAccess()) || canAccessApp(sreq.getSignedTicket(), application.getAppUuid())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void updateApplication(UpdateApplicationRequest sreq) {
		if (canUpdateApp(sreq.getSignedTicket())) {
			Application application = ConverterUtil.applicationInfoToApplication(sreq.getAppInfo());
			em.merge(application);
		}
		else {
			throw new QAuthorisationException(
					sreq.getSignedTicket().getUserID(),
					sreq.getSignedTicket().getTicketID(),
					Constants.OP_UPDATE_APPLICATION,
					null);
		}
	}

	// --

	private void checkUserIsDesktopOwner(SignedTicket ticket, String desktopOwnerId) {
		// Users are only allowed to manage their own desktop icons
		if (!desktopOwnerId.equals(ticket.getUserID())) {
			throw new QAuthorisationException(
					"User with ticket "
							+ ticket.toString()
							+ " is not allowed to manage the desktop icons of user with ID "
							+ desktopOwnerId
							+ "; users are only allowed to manage their own desktop icons");
		}
	}

	@Override
	public List<ApplicationInfo> getDesktopIconsForUser(GetDesktopIconsForUserRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		String desktopOwnerId = sreq.getDesktopOwnerID();

		checkUserIsDesktopOwner(ticket, desktopOwnerId);

		List<DesktopIcon> icons = DesktopIcon.getDesktopIconsForUser(desktopOwnerId, em);
		List<ApplicationInfo> retVal = new ArrayList<>(icons.size());
		for (DesktopIcon icon : icons) {
			// Check that the user is still allowed access to the application
			// before returning it if the application has restricted access.
			Application application = icon.getApplication();
			if ((!application.isRestrictAccess()) || canAccessApp(ticket, application.getAppUuid())) {
				retVal.add(ConverterUtil.applicationToApplicationInfo(application));
			} else {
				LOGGER.log(
						Level.FINE,
						"Desktop application with UUID {0} is not returned for user with ticket {1} since the user "
								+ "is no longer permitted access",
						new String[] { application.getAppUuid(), ticket.toString() });
			}
		}

		return retVal;
	}

	@Override
	public void addAppToUserDesktop(AddAppToUserDesktopRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		String desktopOwnerId = sreq.getDesktopOwnerID();

		checkUserIsDesktopOwner(ticket, desktopOwnerId);

		DesktopIcon existingIcon = DesktopIcon.getDesktopIconForUserAndApplication(desktopOwnerId, sreq.getAppID(), em);
		if (existingIcon == null) {
			Application application = em.find(Application.class, sreq.getAppID());
			DesktopIcon icon = new DesktopIcon();
			icon.setUserId(desktopOwnerId);
			icon.setApplication(application);
			em.persist(icon);
		} else {
			LOGGER.log(
					Level.FINE,
					"Application with UUID {0} could not be added to the "
							+ "desktop of user with ID {1}; the desktop icon already exists",
					new String[] { sreq.getAppID(), desktopOwnerId });
		}
	}

	@Override
	public void removeAppFromUserDesktop(RemoveAppFromUserDesktopRequest sreq) {
		SignedTicket ticket = sreq.getSignedTicket();
		String desktopOwnerId = sreq.getDesktopOwnerID();

		checkUserIsDesktopOwner(ticket, desktopOwnerId);

		DesktopIcon existingIcon = DesktopIcon.getDesktopIconForUserAndApplication(desktopOwnerId, sreq.getAppID(), em);
		if (existingIcon != null) {
			em.remove(existingIcon);
		} else {
			LOGGER.log(
					Level.FINE,
					"Application with UUID {0} could not be removed from the "
							+ "desktop of user with ID {1}; the desktop icon did not exist",
					new String[] { sreq.getAppID(), desktopOwnerId });
		}
	}

}
