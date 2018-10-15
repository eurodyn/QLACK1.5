package com.eurodyn.qlack2.webdesktop.api;

import java.util.List;

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


public interface DesktopService {
	List<String> getGroups(EmptySignedRequest sreq);

	List<ApplicationInfo> getAllApps(GetAllAppsRequest sreq);
	
	List<ApplicationInfo> getAppsForGroup(GetAppsForGroupRequest sreq);
	
	ApplicationInfo getAppDetails(GetAppDetailsRequest sreq);
	
	boolean getAppAccess(GetAppAccessRequest sreq);
	
	void updateApplication(UpdateApplicationRequest sreq);

	List<ApplicationInfo> getDesktopIconsForUser(GetDesktopIconsForUserRequest sreq);

	void addAppToUserDesktop(AddAppToUserDesktopRequest sreq);

	void removeAppFromUserDesktop(RemoveAppFromUserDesktopRequest sreq);

}
