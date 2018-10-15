/*
* Copyright 2014 EUROPEAN DYNAMICS SA <info@eurodyn.com>
*
* Licensed under the EUPL, Version 1.1 only (the "License").
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
* https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and
* limitations under the Licence.
*/
package com.eurodyn.qlack2.util.restutil;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.JsonMappingException;

import com.eurodyn.qlack2.fuse.idm.api.exception.QAuthorisationException;
import com.eurodyn.qlack2.fuse.idm.api.exception.QInvalidTicketException;
import com.eurodyn.qlack2.util.validator.errors.ValidationAttribute;
import com.eurodyn.qlack2.util.validator.errors.ValidationErrorType;
import com.eurodyn.qlack2.util.validator.errors.ValidationErrors;
import com.eurodyn.qlack2.util.validator.errors.ValidationFieldErrors;
import com.eurodyn.qlack2.util.validator.exception.QValidationException;

/**
 * A custom handler for exceptions in REST calls. The main idea here is to
 * catch exceptions QBE produces and provide a more meaningful status code
 * as well as validation errors when they exist.
 * @author European Dynamics SA
 *
 */
@Provider
public class RESTExceptionMapper implements ExceptionMapper<Throwable> {
	private static final Logger LOGGER = Logger.getLogger("qlack");

	@Override
	public Response toResponse(Throwable exception) {
		if ((exception instanceof QInvalidTicketException)
				|| (exception instanceof QAuthorisationException)) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		} else if (exception instanceof QValidationException) {
			LOGGER.log(Level.FINE, "QBECXFExceptionMapper: Validation exception",
					exception);
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(((QValidationException)exception).getErrors())
					.build();
		} else if (exception instanceof javax.ws.rs.WebApplicationException) {
			LOGGER.log(Level.FINE, "QBECXFExceptionMapper: WebApplicationException", exception);
			return ((javax.ws.rs.WebApplicationException)exception).getResponse();
		} else if (exception instanceof org.codehaus.jackson.map.JsonMappingException) {
			LOGGER.log(Level.FINE, "QBECXFExceptionMapper: JSON Mapping Exception", exception);
			JsonMappingException jsonException = (JsonMappingException)exception;
			String fieldName = "";
			Iterator<JsonMappingException.Reference> referenceIt = jsonException.getPath().iterator();
			while (referenceIt.hasNext()) {
				fieldName = fieldName.concat(referenceIt.next().getFieldName());
				if (referenceIt.hasNext()) {
					fieldName = fieldName.concat(".");
				}
			}
			ValidationErrors errors = new ValidationErrors();
			ValidationFieldErrors vfe = new ValidationFieldErrors(fieldName);
			ValidationErrorType vet = new ValidationErrorType("org.codehaus.jackson.map.JsonMappingException");
			vet.putAttribute(ValidationAttribute.Message, "org.codehaus.jackson.map.JsonMappingException");
			vfe.addError(vet);
			errors.addValidationError(vfe);
			return Response.status(Response.Status.BAD_REQUEST)
					.entity(errors)
					.build();
		} else {
			LOGGER.log(Level.SEVERE, "QBECXFExceptionMapper: Unknown exception", exception);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
