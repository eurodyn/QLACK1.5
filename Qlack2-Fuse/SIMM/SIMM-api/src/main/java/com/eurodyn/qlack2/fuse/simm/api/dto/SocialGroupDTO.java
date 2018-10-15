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
package com.eurodyn.qlack2.fuse.simm.api.dto;

import java.util.List;

/**
 *
 * @author European Dynamics SA
 */
public class SocialGroupDTO extends BaseDTO {

    private static final long serialVersionUID = 1992838725845305209L;
    private String id;
    private String parentGroupId;
    private String name;
    private byte[] logo;
    private String description;
    private Byte privacy;
    private String tags;
    private byte status = -1;
    private long createdOn;
    private List<SocialGroupUserDTO> simGroupHasUsers;

    public SocialGroupDTO(String id) {
        this.id = id;
    }

    public SocialGroupDTO() {
    }



    /**
     * @return the parentGroupId
     */
    public String getParentGroupId() {
        return parentGroupId;
    }

    /**
     * @param parentGroupId the parentGroupId to set
     */
    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the logo
     */
    public byte[] getLogo() {
        return logo;
    }

    /**
     * @param logo the logo to set
     */
    public void setLogo(byte[] logo) {
        this.logo = logo;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the privacy
     */
    public Byte getPrivacy() {
        return privacy;
    }

    /**
     * @param privacy the privacy to set
     */
    public void setPrivacy(Byte privacy) {
        this.privacy = privacy;
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * @return the status
     */
    public byte getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(byte status) {
        this.status = status;
    }

    /**
     * @return the createdOn
     */
    public long getCreatedOn() {
        return createdOn;
    }

    /**
     * @param createdOn the createdOn to set
     */
    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the simGroupHasUsers
     */
    public List<SocialGroupUserDTO> getSimGroupHasUsers() {
        return simGroupHasUsers;
    }

    /**
     * @param simGroupHasUsers the simGroupHasUsers to set
     */
    public void setSimGroupHasUsers(List<SocialGroupUserDTO> simGroupHasUsers) {
        this.simGroupHasUsers = simGroupHasUsers;
    }


    @Override
    public boolean equals(Object o) {

        return this == o || (o instanceof SocialGroupDTO && o != null && ((SocialGroupDTO)o).getId() != null && this.getId().equals(((SocialGroupDTO)o).getId()));
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }


}
