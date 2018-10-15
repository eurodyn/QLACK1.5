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
package com.eurodyn.qlack2.fuse.imaging.api;

import java.util.List;

import com.eurodyn.qlack2.common.search.PagingParams;
import com.eurodyn.qlack2.fuse.imaging.api.dto.FolderDTO;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageAttributeDTO;
import com.eurodyn.qlack2.fuse.imaging.api.exception.QFolderCannotBeRetrievedException;

/**
 *
 * @author European Dynamics SA
 */
public interface GalleryFolderService {

    /**
     * Creates a new folder in the system. This method also posts a JMS message (subject to the
     * value of the realtime.JMS.notifications application property) of type MSGTYPE_FOLDER_CREATED.
     * The JMS message has the PARENT_FOLDER_ID, FOLDER_ID and FOLDER_NAME properties set to the respective values
     * of the new folder, its SRC_USERID set to the user initiating this action (retrieved from the srcUserId property
     * of the FolderDTO object) and its PRIVATE_USERID set to the id of the owner of the parent folder
     * (if one exists).
     * @param folder A FolderDTO object carrying the information of the folder to be created.
     * The id attribute of this object is not taken into account, since the folder is attributed
     * a new id during its creation.
     * @return The newly created folder, including the id attributed to it.
     */
    public FolderDTO createFolder(FolderDTO folder);

    /**
     * Updates an existing folder. This method also posts a JMS message (subject to the
     * value of the realtime.JMS.notifications application property) of type MSGTYPE_FOLDER_UPDATED.
     * The JMS message has the PARENT_FOLDER_ID, FOLDER_ID and FOLDER_NAME properties set to the respective values
     * of the updated folder, its SRC_USERID set to the user initiating this action (retrieved from the srcUserId property
     * of the FolderDTO object) and its PRIVATE_USERID set to the id of the old value of the
     * folder owner.
     * @param folder The FolderDTO object holding the folder update information. The id
     * attribute of this object will be used to identify the folder to be updated, while
     * the values of the following attributes will be used to update the folder: <br/>
     * - name <br/>
     * - description <br/>
     * - ownerId <br/>
     * The rest of the object's attributes are not taken into account be this method.
     */
    public void updateFolder(FolderDTO folder);

    /**
     * Deletes a folder from the system. This method also posts a JMS message (subject to the
     * value of the realtime.JMS.notifications application property) of type MSGTYPE_FOLDER_DELETED.
     * The JMS message has the PARENT_FOLDER_ID, FOLDER_ID and FOLDER_NAME attributes set to the respective values
     * of the deleted folder, its SRC_USERID set to the user initiating this action (retrieved from the srcUserId property
     * of the FolderDTO object) and its PRIVATE_USERID set to the id of the value of the
     * folder owner.
     * @param folder The folder to delete. This method takes into account the id in order to
     * identify the folder and the srcUserId (if it exists) when creating the relevant notification message.
     */
    public void deleteFolder(FolderDTO folder);

    /**
     * Adds an attribute to a folder. This method also posts a JMS message (subject to the
     * value of the realtime.JMS.notifications application property) of type
     * MSGTYPE_FOLDER_ATTRIBUTE_CREATED. The JMS message has the FOLDER_ID and FOLDER_NAME
     * attributes set to the respective values of the owner folder, the ATTRIBUTE_ID and
     * ATTRIBUTE_NAME attributes set to the values of the new attribute, its
     * SRC_USERID set to the user initiating this action (retrieved from the srcUserId property
     * of the ImageAttributeDTO object) and its PRIVATE_USERID set to the id of the folder owner.
     * @param folderId The id of the folder to which to add the attribute
     * @param attribute An ImageAttributeDTO object holding the information of the
     * attribute to add. Only the name and value attributes of this object are
     * taken into account since the new attribute is given a new id during its creation.
     * @return ImageAttributeDTO
     */
    public ImageAttributeDTO addFolderAttribute(String folderId, ImageAttributeDTO attribute);

    /**
     * Updates a folder attribute. This method also posts a JMS message (subject to the
     * value of the realtime.JMS.notifications application property) of type
     * MSGTYPE_FOLDER_ATTRIBUTE_UPDATED. The JMS message has the FOLDER_ID and FOLDER_NAME
     * attributes set to the respective values of the owner folder, the ATTRIBUTE_ID and
     * ATTRIBUTE_NAME attributes set to the values of the updated attribute, its
     * SRC_USERID set to the user initiating this action (retrieved from the srcUserId property
     * of the ImageAttributeDTO object) and its PRIVATE_USERID set to the id of the folder owner.
     * @param attribute The attribute to update. The attribute's id is used to
     * identify the attribute and the name and value attributes are used to update the
     * attribute.
     */
    public void updateFolderAttribute(ImageAttributeDTO attribute);

    /**
     * Deletes a folder attribute. This method also posts a JMS message (subject to the
     * value of the realtime.JMS.notifications application property) of type
     * MSGTYPE_FOLDER_ATTRIBUTE_DELETED. The JMS message has the FOLDER_ID and FOLDER_NAME
     * attributes set to the respective values of the owner folder, the ATTRIBUTE_ID and
     * ATTRIBUTE_NAME attributes set to the values of the deleted attribute , its
     * SRC_USERID set to the user initiating this action (retrieved from the srcUserId property
     * of the ImageAttributeDTO object) and its PRIVATE_USERID set to the id of the folder owner.
     * @param attribute The attribute to delete. This method takes into account the id in order to
     * identify the attribute and the srcUserId (if it exists) when creating the relevant notification message.
     */
    public void deleteFolderAttribute(ImageAttributeDTO attribute);


    /**
     * Retrieves a specific folder attribute
     * @param attributeId The id of the folder attribute to retrieve
     * @return The retrieved attribute
     */
    public ImageAttributeDTO getFolderAttribute(String attributeId);


    /**
     * Retrieves a specific folder attribute
     * @param folderId The id of the folder whose attribute to retrieve
     * @param attributeName The name of the attribute to retrieve
     * @return The retrieved attribute or null if no attribute with the specified name exists
     */
    public ImageAttributeDTO getFolderAttribute(String folderId, String attributeName);


    /**
     * Retrieves a folder
     * @param folderId The id of the folder to retrieve
     * @return A FolderDTO object holding the retrieved folder's information.
     */
    public FolderDTO getFolder(String folderId);

    /**
     * Retrieves a root folder (folder which is not contained into another folder) by its name
     * @param name The name of the folder to retrieve
     * @return A FolderDTO object holding the retrieved folder's information.
     */
    public FolderDTO getFolderByName(String name);

    /**
     * Retrieves a folder by its name
     * @param parentId The id of the folder in which the folder to be retrieved
     * is contained.
     * @param name The name of the folder to retrieve
     * @return A FolderDTO object holding the retrieved folder's information.
     */
    public FolderDTO getFolderByName(String name, String parentId);

    /**
     * Retrieves all the folders contained in a specified folder using paging parameters.
     * @param folder A folder object specifying the properties of the folders
     * to retrieve. The following attributes of the folder object are taken into
     * account: <br/>
     * - parentId: The folder whose child folders will be retrieved. In case the
     * parentId is null folders which do not have a parent will be retrieved.
     * - ownerId: The id of the owner of the folders to be retrieved. In case the
     * ownerId is null, all the folders contained in the specified parent will be retrieved,
     * irrespective of their owner.
     * @param pagingParams The paging parameters to use during the folder retrieval.
     * @return A list with the folders contained in the specified folder and having
     * the specified owner.
     */
    public List<FolderDTO> getChildFolders(FolderDTO folder, PagingParams pagingParams);

    /**
     * Retrieves all the folders contained in a specified folder which include a specified search term
     * in their names using paging parameters.
     * @param folder A folder object specifying the properties of the folders
     * to retrieve. The following attributes of the folder object are taken into
     * account: <br/>
     * - parentId: The folder whose child folders will be retrieved. In case the
     * parentId is null folders which do not have a parent will be retrieved.
     * - ownerId: The id of the owner of the folders to be retrieved. In case the
     * ownerId is null, all the folders contained in the specified parent will be retrieved,
     * irrespective of their owner.
     * @param pagingParams The paging parameters to use during the folder retrieval.
     * @param searchTerm The search term to use.
     * @return A list with the folders contained in the specified folder and having
     * the specified owner.
     */
    public List<FolderDTO> getChildFolders(FolderDTO folder, PagingParams pagingParams, String searchTerm);

    /**
     * Retrieves all the folders contained in a specified folder which include an attribute whose
     * value contains a specified search term using paging parameters.
     * @param folder A folder object specifying the properties of the folders
     * to retrieve. The following attributes of the folder object are taken into
     * account: <br/>
     * - parentId: The folder whose child folders will be retrieved. In case the
     * parentId is null folders which do not have a parent will be retrieved.
     * - ownerId: The id of the owner of the folders to be retrieved. In case the
     * ownerId is null, all the folders contained in the specified parent will be retrieved,
     * irrespective of their owner.
     * @param pagingParams The paging parameters to use during the folder retrieval.
     * @param searchAttribute An object containing the name of the attribute to filter for as well as
     * the search term it should contain.
     * @return A list with the folders contained in the specified folder and having
     * the specified owner.
     */
    public List<FolderDTO> getChildFolders(FolderDTO folder, PagingParams pagingParams,
            ImageAttributeDTO searchAttribute);

    /**
     * Gets the content of a folder node in a zip file. This method retrieves the binary contents of all images
     * included in the folder node specified as well as their attributes if the includeAttributes argument is true.
     * The contents of other folders contained by the folder specified are also retrieved in
     * case the isDeep argument is true.
     * @param folderId The id of the folder whose content is to be retrieved
     * @param includeAttributes If true then a separate attributes file will be created inside the final zip file
     * for each folder/image included in the result, containing the folder's/image's attributes
     * in the form: attributeName = attributeValue
     * @param isDeep If true then the whole tree commencing by the specified folder will be traversed in order to be
     * included in the final result. Otherwise only the images which are direct children of the specified
     * folder will be included in the result.
     * @return The binary content (and attributes if applicable) of the specified folder as a byte array
     * representing a zip file.
     * @throws QFolderCannotBeRetrievedException If an image contained in the folder cannot be retrieved 
     * or if the operation cannot be performed
     */
    public byte[] getFolderAsZip(String folderId, boolean includeAttributes, boolean isDeep) 
    		throws QFolderCannotBeRetrievedException;
}
