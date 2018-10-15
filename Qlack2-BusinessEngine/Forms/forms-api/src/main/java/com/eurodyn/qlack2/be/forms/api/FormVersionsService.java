/**
 * $Id$
 *  
 * Copyright © 2014 Federal Authorities of the Swiss Confederation, Federal
 * Office Office for Agriculture FOAG, Mattenhofstrasse 5, 3003 Bern. All
 * rights reserved.
 *
 * This source code can only be used with explicit permission of its owner.
 */
package com.eurodyn.qlack2.be.forms.api;

import java.util.List;

import com.eurodyn.qlack2.be.forms.api.dto.FormVersionDTO;
import com.eurodyn.qlack2.be.forms.api.dto.FormVersionDetailsDTO;
import com.eurodyn.qlack2.be.forms.api.dto.TranslationDTO;
import com.eurodyn.qlack2.be.forms.api.exception.QImportExportException;
import com.eurodyn.qlack2.be.forms.api.exception.QFaultFormVersionTemplateException;
import com.eurodyn.qlack2.be.forms.api.exception.QInvalidConditionHierarchyException;
import com.eurodyn.qlack2.be.forms.api.exception.QInvalidOperationException;
import com.eurodyn.qlack2.be.forms.api.exception.QServiceNotAvailableException;
import com.eurodyn.qlack2.be.forms.api.request.EmptySignedRequest;
import com.eurodyn.qlack2.be.forms.api.request.form.UpdateFormRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.CanFinaliseFormVersionRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.CountFormVersionsLockedByOtherUserRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.CreateFormVersionRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.DeleteFormVersionRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.DeleteFormVersionTranslationsRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.EnableTestingRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.ExportFormVersionRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.FinaliseFormVersionRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.GetFormVersionIdByNameRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.GetFormVersionRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.GetFormVersionTranslationsRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.GetFormVersionsRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.ImportFormVersionRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.LockFormVersionRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.UnlockFormVersionRequest;
import com.eurodyn.qlack2.be.forms.api.request.version.ValidateFormVersionConditionsHierarchyRequest;
import com.eurodyn.qlack2.fuse.idm.api.exception.QAuthorisationException;
import com.eurodyn.qlack2.fuse.idm.api.exception.QInvalidTicketException;

public interface FormVersionsService {

	List<FormVersionDTO> getFormVersions(GetFormVersionsRequest request)
			throws QInvalidTicketException, QAuthorisationException;

	/**
	 * Retrieves the number of form versions locked by another user.
	 *
	 * @param request
	 * @return
	 */
	Long countFormVersionsLockedByOtherUser(
			CountFormVersionsLockedByOtherUserRequest request)
			throws QInvalidTicketException, QAuthorisationException;

	/**
	 * Creates a new form version.
	 *
	 * @param request
	 * @return
	 */
	String createFormVersion(CreateFormVersionRequest request)
			throws QInvalidTicketException, QAuthorisationException,
			QFaultFormVersionTemplateException;

	/**
	 * Updates a form version.
	 *
	 * @param request
	 */
	void updateFormVersion(UpdateFormRequest request)
			throws QInvalidTicketException, QAuthorisationException,
			QInvalidOperationException;

	/**
	 * Retrieves the metadata of a form version
	 *
	 * @param request
	 * @return
	 */
	FormVersionDetailsDTO getFormVersion(GetFormVersionRequest request)
			throws QInvalidTicketException, QAuthorisationException;

	/**
	 * Retrieve a form version id by its name
	 *
	 * @param qSignedRequest
	 * @return
	 */
	String getFormVersionIdByName(GetFormVersionIdByNameRequest qSignedRequest)
			throws QInvalidTicketException, QAuthorisationException;
	
	/**
	 * Retrieves a form's latest finalized version id by it's id
	 *
	 * @param qSignedRequest the q signed request
	 * @return the latest finalized form version id by name
	 * @throws QInvalidTicketException the q invalid ticket exception
	 * @throws QAuthorisationException the q authorisation exception
	 */
	String getLatestFinalizedFormVersionIdByFormId(GetFormVersionIdByNameRequest qSignedRequest)
			throws QInvalidTicketException, QAuthorisationException;

	/**
	 * Validates the conditions of the form version
	 *
	 * @param request
	 * @return
	 */
	void validateFormVersionConditionsHierarchy(
			ValidateFormVersionConditionsHierarchyRequest request)
			throws QInvalidTicketException, QInvalidConditionHierarchyException;

	/**
	 * Deletes a form version
	 *
	 * @param request
	 */
	void deleteFormVersion(DeleteFormVersionRequest request)
			throws QInvalidTicketException, QAuthorisationException,
			QInvalidOperationException;

	/**
	 * Gets the form version translations
	 *
	 * @param request
	 * @return
	 */
	List<TranslationDTO> getFormVersionTranslations(
			GetFormVersionTranslationsRequest request)
			throws QInvalidTicketException, QAuthorisationException;

	/**
	 * Deletes the translations of a form version
	 *
	 * @param request
	 */
	void deleteFormVersionTranslations(
			DeleteFormVersionTranslationsRequest request)
			throws QInvalidTicketException, QAuthorisationException;

	/**
	 * Locks a form version.
	 *
	 * @param request
	 */
	void lockFormVersion(LockFormVersionRequest request)
			throws QInvalidTicketException, QAuthorisationException,
			QInvalidOperationException;

	/**
	 * Unlocks a form version.
	 *
	 * @param request
	 */
	void unlockFormVersion(UnlockFormVersionRequest request)
			throws QInvalidTicketException, QAuthorisationException,
			QInvalidOperationException;

	/**
	 * Checks whether the form version can be finalised. In order for a form
	 * version to be finalised, all working sets and rules that are used in its
	 * conditions should finalised.
	 *
	 * @param req
	 * @return
	 * @throws QInvalidTicketException
	 * @throws QAuthorisationException
	 * @throws QInvalidOperationException
	 */
	boolean canFinaliseFormVersion(CanFinaliseFormVersionRequest req)
			throws QInvalidTicketException, QAuthorisationException,
			QInvalidOperationException;

	/**
	 * Finalises a form version.
	 *
	 * @param request
	 */
	void finaliseFormVersion(FinaliseFormVersionRequest request)
			throws QInvalidTicketException, QAuthorisationException,
			QInvalidOperationException;

	/**
	 * Enables or disables testing for a form version.
	 *
	 * @param request
	 */
	void enableTestingForFormVersion(EnableTestingRequest request)
			throws QInvalidTicketException, QAuthorisationException,
			QInvalidOperationException;

	/**
	 * Imports a form version.
	 *
	 * @param request
	 */
	String importFormVersion(ImportFormVersionRequest request)
			throws QInvalidTicketException, QAuthorisationException, QImportExportException;

	/**
	 * Exports a form version
	 *
	 * @param request
	 * @return
	 * @throws QInvalidTicketException
	 * @throws QAuthorisationException
	 * @throws QInvalidOperationException
	 * @throws QImportExportException
	 * @throws QServiceNotAvailableException
	 */
	byte[] exportFormVersion(ExportFormVersionRequest request)
			throws QInvalidTicketException, QAuthorisationException,
			QInvalidOperationException, QImportExportException, QServiceNotAvailableException;

	/**
	 * Retrieves the validation condition types
	 *
	 * @param request
	 * @return
	 */
	List<Integer> getConditionTypes(EmptySignedRequest request)
			throws QInvalidTicketException;

}
