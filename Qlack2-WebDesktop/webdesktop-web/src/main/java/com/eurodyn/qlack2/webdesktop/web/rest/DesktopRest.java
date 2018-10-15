package com.eurodyn.qlack2.webdesktop.web.rest;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.eurodyn.qlack2.fuse.idm.api.signing.SignedTicket;
import com.eurodyn.qlack2.webdesktop.api.DesktopService;
import com.eurodyn.qlack2.webdesktop.api.dto.ApplicationInfo;
import com.eurodyn.qlack2.webdesktop.api.request.EmptySignedRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.AddAppToUserDesktopRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.GetAppDetailsRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.GetAppsForGroupRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.GetDesktopIconsForUserRequest;
import com.eurodyn.qlack2.webdesktop.api.request.desktop.RemoveAppFromUserDesktopRequest;
import com.eurodyn.qlack2.webdesktop.web.util.Utils;

@Path("desktop")
public class DesktopRest {
	@Context private HttpHeaders headers;
	private DesktopService desktopService;
	private Utils utils;

	public void setDesktopService(DesktopService desktopService) {
		this.desktopService = desktopService;
	}

	public void setUtils(Utils utils) {
		this.utils = utils;
	}

	@GET
	@Path("application-groups")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getGroups() {
		utils.validateTicket(headers);
		EmptySignedRequest sreq = new EmptySignedRequest();
		utils.sign(sreq, headers);
		return desktopService.getGroups(sreq);
	}

	@GET
	@Path("applications-for-group")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<ApplicationInfo> getAppsWithNoGroup() {
		utils.validateTicket(headers);
		GetAppsForGroupRequest sreq = new GetAppsForGroupRequest(null, true);
		utils.sign(sreq, headers);
		return desktopService.getAppsForGroup(sreq);
	}

	@GET
	@Path("applications-for-group/{groupKeyname}")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<ApplicationInfo> getAppsForGroup(@PathParam("groupKeyname") String groupKeyname) {
		utils.validateTicket(headers);
		GetAppsForGroupRequest sreq = new GetAppsForGroupRequest(groupKeyname, true);
		utils.sign(sreq, headers);
		return desktopService.getAppsForGroup(sreq);
	}

	@GET
	@Path("application/{appUUID}")
	@Produces(MediaType.APPLICATION_JSON)
	public ApplicationInfo getAppDetails(@PathParam("appUUID") String appUUID) {
		utils.validateTicket(headers);
		GetAppDetailsRequest sreq = new GetAppDetailsRequest(appUUID);
		utils.sign(sreq, headers);
		return desktopService.getAppDetails(sreq);
	}

	@GET
	@Path("desktop-app-icons")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ApplicationInfo> getOwnDesktopIcons() {
		utils.validateTicket(headers);
		SignedTicket ticket = utils.getSignedTicket(headers);
		GetDesktopIconsForUserRequest sreq = new GetDesktopIconsForUserRequest(ticket.getUserID());
		utils.sign(sreq, headers);
		return desktopService.getDesktopIconsForUser(sreq);
	}

	@PUT
	@Path("desktop-app-icon/{appUUID}")
	public void addAppToOwnDesktop(@PathParam("appUUID") String appUUID) {
		utils.validateTicket(headers);
		SignedTicket ticket = utils.getSignedTicket(headers);
		AddAppToUserDesktopRequest sreq = new AddAppToUserDesktopRequest(ticket.getUserID(), appUUID);
		utils.sign(sreq, headers);
		desktopService.addAppToUserDesktop(sreq);
	}

	@DELETE
	@Path("desktop-app-icon/{appUUID}")
	public void removeAppFromOwnDesktop(@PathParam("appUUID") String appUUID) {
		utils.validateTicket(headers);
		SignedTicket ticket = utils.getSignedTicket(headers);
		RemoveAppFromUserDesktopRequest sreq = new RemoveAppFromUserDesktopRequest(ticket.getUserID(), appUUID);
		utils.sign(sreq, headers);
		desktopService.removeAppFromUserDesktop(sreq);
	}

}
