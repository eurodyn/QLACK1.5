package com.eurodyn.qlack2.webdesktop.apps.cloudmanager.web.rest;

import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import com.eurodyn.qlack2.webdesktop.apps.cloudmanager.api.CloudService;

@Path("/validation-example")
public class CloudManagerRest {
	private static final Logger LOGGER = Logger.getLogger(CloudManagerRest.class.getName());

	@Context
	private HttpHeaders headers;

	private CloudService cloudService;

	public void setCloudService(CloudService cloudService) {
		this.cloudService = cloudService;
	}

//	@POST
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	@Path("/user")
//	@ValidateSingleArgument
//	public String saveUser(UserRDTO user) {
//		// Perform custom business-checks here.
//		if (user.getSum() != 2) {
//			ValidationErrors ve = new ValidationErrors();
//			ValidationFieldErrors vfe = new ValidationFieldErrors("sum");
//			ValidationErrorType vet = new ValidationErrorType("validation.error.wrongSum");
//			vet.putAttribute(ValidationAttribute.InvalidValue, user.getSum());
//			vfe.addError(vet);
//			ve.addValidationError(vfe);
//			throw new QValidationException(ve);
//		}
//
//		SaveUserRequest sreq = new SaveUserRequest();
//		sreq.setAge(user.getAge());
//		sreq.setFullname(user.getFullname());
//		sreq.setGender(user.getGender());
//		Utils.sign(sreq, headers);
//
//		SaveUserResponse res = sampleAppService.saveUser(sreq);
//		System.out.println("server ok = " + res.getUserID());
//
//		return res.getUserID();
//	}
//
//	@GET
//	@Produces(MediaType.APPLICATION_JSON)
//	@Path("/form/{formId}")
//	public FormRDTO getForm(@PathParam("formId") String formId) {
//		FormRDTO form = new FormRDTO();
//		form.setId(formId);
//		form.setTitle("Title " + formId);
//		form.setDescription("Description " + formId);
//		List<FormVersionNameRDTO> versions = new ArrayList<>();
//		versions.add(new FormVersionNameRDTO(formId + "-1", "Version " + formId + "-1"));
//		versions.add(new FormVersionNameRDTO(formId + "-2", "Version " + formId + "-2"));
//		versions.add(new FormVersionNameRDTO(formId + "-3", "Version " + formId + "-3"));
//		form.setVersions(versions);
//		return form;
//	}

}
