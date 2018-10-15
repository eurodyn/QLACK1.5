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
import com.eurodyn.qlack2.fuse.imaging.api.criteria.ImageSearchCriteria;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageAttributeDTO;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageDTO;
import com.eurodyn.qlack2.fuse.imaging.api.dto.ImageInfo;
import com.eurodyn.qlack2.fuse.imaging.api.exception.QImageCannotBeRetrievedException;

/**
 *
 * @author European Dynamics SA
 */
public interface GalleryImageService {

	public static int IMAGE_CONTENT_NONE = 0;
	public static int IMAGE_CONTENT_THUMBNAIL = 1;
	public static int IMAGE_CONTENT_FULLSIZE = 2;

	/**
	 * Creates a new image. This method also posts a JMS message (subject to the
	 * value of the realtime.JMS.notifications application property) of type
	 * MSGTYPE_IMAGE_CREATED. The JMS message has the PARENT_FOLDER_ID, IMAGE_ID
	 * and IMAGE_NAME attributes set to the respective values of the new image,
	 * its SRC_USERID set to the user initiating this action (retrieved from the
	 * srcUserId property of the ImageDTO object) and its PRIVATE_USERID set to
	 * the id of the owner of the parent folder.
	 *
	 * @param image
	 *            The image to create. The id attribute of this object is not
	 *            taken into account by this method, since the image is
	 *            attributed a new id during its creation. In case the mimetype
	 *            attribute of the ImageDTO object passed to this method is not
	 *            set the mimetype of the image is automatically detected by the
	 *            system.
	 * @return The newly created image
	 */
	public ImageDTO createImage(ImageDTO image);

	/**
	 * Updates an image. This method also posts a JMS message (subject to the
	 * value of the realtime.JMS.notifications application property) of type
	 * MSGTYPE_IMAGE_UPDATED. The JMS message has the PARENT_FOLDER_ID, IMAGE_ID
	 * and IMAGE_NAME attributes set to the respective values of the updated
	 * image, its SRC_USERID set to the user initiating this action (retrieved
	 * from the srcUserId property of the ImageDTO object) and its
	 * PRIVATE_USERID set to the id of the old value of the image owner.
	 *
	 * @param image
	 *            An ImageDTO object holding the image update information. The
	 *            id attribute of this object will be used to identify the image
	 *            to be updated, while the values of the following attributes
	 *            will be used to update the image: <br/>
	 *            - name<br/>
	 *            - description<br/>
	 *            - ownerId<br/>
	 *            - filename<br/>
	 *            - mimetype<br/>
	 *            - image<br/>
	 *            In case the image attribute is updated and the mimetype is not
	 *            set the mimetype of the new image will be automatically
	 *            detected by the system.
	 */
	public void updateImage(ImageDTO image);

	/**
	 * Deletes an image. This method also posts a JMS message (subject to the
	 * value of the realtime.JMS.notifications application property) of type
	 * MSGTYPE_IMAGE_DELETED. The JMS message has the PARENT_FOLDER_ID, IMAGE_ID
	 * and IMAGE_NAME attributes set to the respective values of the deleted
	 * image, its SRC_USERID set to the user initiating this action (retrieved
	 * from the srcUserId property of the ImageDTO object) and its
	 * PRIVATE_USERID set to the id of the value of the image owner.
	 *
	 * @param imageId
	 *            The image to delete. This method takes into account the id in
	 *            order to identify the image and the srcUserId (if it exists)
	 *            when creating the relevant notification message.
	 */
	public void deleteImage(ImageDTO image);

	/**
	 * Adds an attribute to an image. This method also posts a JMS message
	 * (subject to the value of the realtime.JMS.notifications application
	 * property) of type MSGTYPE_IMAGE_ATTRIBUTE_CREATED. The JMS message has
	 * the IMAGE_ID and IMAGE_NAME attributes set to the respective values of
	 * the owner image, the ATTRIBUTE_ID and ATTRIBUTE_NAME attributes set to
	 * the values of the new attribute, its SRC_USERID set to the user
	 * initiating this action (retrieved from the srcUserId property of the
	 * ImageAttributeDTO object) and its PRIVATE_USERID set to the id of the
	 * image owner.
	 *
	 * @param imageId
	 *            The id of the image to which to add the attribute
	 * @param attribute
	 *            An ImageAttributeDTO object holding the information of the
	 *            attribute to add. Only the name and value attributes of this
	 *            object are taken into account since the new attribute is given
	 *            a new id during its creation.
	 * @return ImageAttributeDTO
	 */
	public ImageAttributeDTO addImageAttribute(String imageId,
			ImageAttributeDTO attribute);

	/**
	 * Updates an image attribute. This method also posts a JMS message (subject
	 * to the value of the realtime.JMS.notifications application property) of
	 * type MSGTYPE_IMAGE_ATTRIBUTE_UPDATED. The JMS message has the IMAGE_ID
	 * and IMAGE_NAME attributes set to the respective values of the owner
	 * image, the ATTRIBUTE_ID and ATTRIBUTE_NAME attributes set to the values
	 * of the updated attribute, its SRC_USERID set to the user initiating this
	 * action (retrieved from the srcUserId property of the ImageAttributeDTO
	 * object) and its PRIVATE_USERID set to the id of the image owner.
	 *
	 * @param attribute
	 *            The attribute to update. The attribute's id is used to
	 *            identify the attribute and the name and value attributes are
	 *            used to update the attribute.
	 */
	public void updateImageAttribute(ImageAttributeDTO attribute);

	/**
	 * Deletes an image attribute. This method also posts a JMS message (subject
	 * to the value of the realtime.JMS.notifications application property) of
	 * type MSGTYPE_IMAGE_ATTRIBUTE_DELETED. The JMS message has the IMAGE_ID
	 * and IMAGE_NAME attributes set to the respective values of the owner
	 * folder, the ATTRIBUTE_ID and ATTRIBUTE_NAME attributes set to the values
	 * of the deleted attribute, its SRC_USERID set to the user initiating this
	 * action (retrieved from the srcUserId property of the ImageAttributeDTO
	 * object) and its PRIVATE_USERID set to the id of the image owner.
	 *
	 * @param attribute
	 *            The attribute to delete. This method takes into account the id
	 *            in order to identify the attribute and the srcUserId (if it
	 *            exists) when creating the relevant notification message.
	 */
	public void deleteImageAttribute(ImageAttributeDTO attribute);

	/**
	 * Retrieves a specific image attribute
	 *
	 * @param attributeId
	 *            The id of the image attribute to retrieve
	 * @return The retrieved attribute
	 */
	public ImageAttributeDTO getImageAttribute(String attributeId);

	/**
	 * Retrieves a specific image attribute
	 *
	 * @param imageId
	 *            The id of the image whose attribute to retrieve
	 * @param attributeName
	 *            The name of the attribute to retrieve
	 * @return The retrieved attribute or null if no attribute with the
	 *         specified name exists
	 */
	public ImageAttributeDTO getImageAttribute(String imageId,
			String attributeName);

	/**
	 * Retrieves an image
	 *
	 * @param imageId
	 *            The id of the image to retrieve
	 * @param imageContent
	 *            The type of the image content to retrieve (do not retrieve the
	 *            image content, retrieve as thumbnail, retrieve full sized
	 *            image).
	 * @return An ImageDTO object holding the retrieved image.
	 */
	public ImageDTO getImage(String imageId, int imageContent);
	
	/**
	 * Gets an image in a zip file. This method retrieves the image as well as
	 * its attributes if the includeAttributes argument is true.<br/>
	 *
	 * @param imageId
	 *            The id of the image which will be retrieved.
	 * @param includeAttributes
	 *            If true then a separate properties file will be created inside
	 *            the final zip file containing the image's attributes in the
	 *            form: attributeName = attributeValue
	 * @return The binary content (and attributes if applicable) of the
	 *         specified image as a byte array representing a zip file.
	 * @throws QImageCannotBeRetrievedException
	 *             If the image contents cannot be retrieved
	 */
	public byte[] getImageAsZip(String imageId, boolean includeAttributes) 
			throws QImageCannotBeRetrievedException;

	/**
	 * Retrieves the information of an image
	 *
	 * @param imageId
	 *            The id of the image whose information to retrieve
	 * @return An ImageInfo object holding the retrieved image's information.
	 * @throws QImageCannotBeRetrievedException
	 *             If the operation cannot be performed
	 */
	 public ImageInfo getImageInfo(String imageId) throws QImageCannotBeRetrievedException;

	/**
	 * Retrieved the images contained in a specific folder using paging
	 * parameters
	 *
	 * @param folderId
	 *            The id of the folder whose image contents to retrieve
	 * @param imageContent
	 *            The of the image content to retrieve (do not retrieve the
	 *            image content, retrieve as thumbnail, retrieve full sized
	 *            image).
	 * @param pagingParams
	 *            The paging parameters to use.
	 * @return A list with the retrieved images
	 */
	public List<ImageDTO> getImagesInFolder(String folderId, int imageContent,
			PagingParams pagingParams);

	/**
	 * Retrieved the images contained in a specific folder which include a
	 * specified search term in their names using paging parameters
	 *
	 * @param folderId
	 *            The id of the folder whose image contents to retrieve
	 * @param imageContent
	 *            The of the image content to retrieve (do not retrieve the
	 *            image content, retrieve as thumbnail, retrieve full sized
	 *            image).
	 * @param pagingParams
	 *            The paging parameters to use.
	 * @param searchTerm
	 *            The search term to use.
	 * @return A list with the retrieved images
	 */
	public List<ImageDTO> getImagesInFolder(String folderId, int imageContent,
			PagingParams pagingParams, String searchTerm);

	/**
	 * Retrieved the images contained in a specific folder which include an
	 * attribute whose value contains a specified search term using paging
	 * parameters
	 *
	 * @param folderId
	 *            The id of the folder whose image contents to retrieve
	 * @param imageContent
	 *            The of the image content to retrieve (do not retrieve the
	 *            image content, retrieve as thumbnail, retrieve full sized
	 *            image).
	 * @param pagingParams
	 *            The paging parameters to use.
	 * @param searchAttribute
	 *            An object containing the name of the attribute to filter for
	 *            as well as the search term it should contain.
	 * @return A list with the retrieved images
	 */
	public List<ImageDTO> getImagesInFolder(String folderId, int imageContent,
			PagingParams pagingParams, ImageAttributeDTO searchAttribute);

	/**
	 * Search for images using a set of search criteria
	 * @param criteria The search criteria to use
	 * @param imageContent
	 *            The type of the image content to retrieve (do not retrieve the
	 *            image content, retrieve as thumbnail, retrieve full sized
	 *            image).
	 * @return The images satisfying the specified critera
	 */
	public List<ImageDTO> findImages(ImageSearchCriteria criteria, int imageContent);

	/**
	 * Copies an image from one folder into another.
	 *
	 * @param image
	 *            The image to copy. The following attributes of the ImageDTO
	 *            object passed to this method are taken into account:<br/>
	 *            - id: The id of the image to copy<br/>
	 *            - name: If this attribute is set the image will be copied
	 *            under a new name (the one set here).<br/>
	 *            - description: If this attribute is set the image will be
	 *            copied with a new description (the one set here).<br/>
	 *            - folderId: The folder under which to copy the image.<br/>
	 * @return The id of the created image copy
	 */
	public String copyImage(ImageDTO image);

	/**
	 * Moves an image from one folder into another. The image's id is preserved
	 * during the move
	 *
	 * @param image
	 *            The image to move. The following attributes of the ImageDTO
	 *            object passed to this method are taken into account:<br/>
	 *            - id: The id of the image to move<br/>
	 *            - name: If this attribute is set the image will be moved under
	 *            a new name (the one set here).<br/>
	 *            - description: If this attribute is set the image will be
	 *            moved with a new description (the one set here).<br/>
	 *            - folderId: The folder under which to move the image.<br/>
	 */
	public void moveImage(ImageDTO image);
}
