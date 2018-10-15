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
package com.eurodyn.qlack2.fuse.cm.api;

import java.util.List;
import java.util.Map;

import com.eurodyn.qlack2.fuse.cm.api.dto.FileDTO;
import com.eurodyn.qlack2.fuse.cm.api.dto.FolderDTO;
import com.eurodyn.qlack2.fuse.cm.api.dto.NodeDTO;
import com.eurodyn.qlack2.fuse.cm.api.exception.QNodeLockException;

public interface DocumentService {
	// **********************
	// Folder functionalities
	// **********************
	
	String createFolder(FolderDTO folder, String userId, String lockToken) throws QNodeLockException;
	
	void deleteFolder(String folderID, String lockToken) throws QNodeLockException;
	
	void renameFolder(String folderID, String newName, String userId, String lockToken) throws QNodeLockException;
	
	FolderDTO getFolderByID(String folderID, boolean lazyRelatives);
	
	/**
     * Gets the content of a folder node in a zip file. This method retrieves the binary contents of all files
     * included in the folder node specified as well as their attributes if the includeAttributes argument is true.
     * The contents of files included in other folders contained by the folder specified are also retrieved in
     * case the isDeep argument is true.
     * @param folderId The ID of the folder the content of which is to be retrieved
     * @param includeAttributes If true then a separate properties file will be created inside the final zip file
     * for each node included in the result, containing the nodes' properties in the form: propertyName = propertyValue
     * @param isDeep If true then the whole tree commencing by the specified folder will be traversed in order to be
     * included in the final result. Otherwise only the file nodes which are direct children of the specified
     * folder will be included in the result.
     * @return The binary content (and properties if applicable) of the specified folder as a byte array
     * representing a zip file.
     */
    byte[] getFolderAsZip(String folderID, boolean includeProperties, boolean isDeep);
    
    
    // **********************
    // File functionalities
    // **********************
	
	String createFile(FileDTO file, String userID, String lockToken) throws QNodeLockException;
	
	void deleteFile(String fileID, String lockToken) throws QNodeLockException;
	
	void renameFile(String fileID, String newName, String userID, String lockToken) throws QNodeLockException;
	
	FileDTO getFileByID(String fileID, boolean includeVersions);
	
    
    // **********************
    // Common functionalities
    // **********************
	
	NodeDTO getNodeByID(String nodeID);
	
	FolderDTO getParent(String nodeID, boolean lazyRelatives);
    
    List<FolderDTO> getAncestors(String nodeID);
    
    void updateAttribute(String nodeID, String attributeName, String attributeValue, String userID, String lockToken) throws QNodeLockException;
    
    void updateAttributes(String nodeID, Map<String, String> attributes,String userID,  String lockToken) throws QNodeLockException;
    
    void deleteAttribute(String nodeID, String attributeName, String userID, String lockToken) throws QNodeLockException;
    
    String copy(String nodeID, String newParentID, String userID, String lockToken);
    
    void move(String nodeID, String newParentID, String userID, String lockToken);
}
