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
package com.eurodyn.qlack2.fuse.wiki.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.eurodyn.qlack2.common.search.PagingParams;
import com.eurodyn.qlack2.fuse.wiki.api.WikiService;
import com.eurodyn.qlack2.fuse.wiki.api.dto.WikiDTO;
import com.eurodyn.qlack2.fuse.wiki.api.exception.QAlreadyExists;
import com.eurodyn.qlack2.fuse.wiki.api.exception.QInvalidObject;
import com.eurodyn.qlack2.fuse.wiki.api.exception.QWikiException;
import com.eurodyn.qlack2.fuse.wiki.impl.model.WikWiki;
import com.eurodyn.qlack2.fuse.wiki.impl.util.ConverterUtil;

/**
 * An OSGI service to manage a Wiki. For details regarding the
 * functionality offered see the respective interfaces.
 *
 * @author European Dynamics SA.
 */
public class WikiServiceImpl implements WikiService {
    @PersistenceContext(unitName = "QlackFuse-Wiki-PU")
    private EntityManager em;
    private static final Logger LOGGER = Logger.getLogger(WikiServiceImpl.class.getName());

    public void setEm(EntityManager em) {
		this.em = em;
	}

    @Override
        public String createWiki(WikiDTO dto) throws QWikiException {
        WikWiki entity = new WikWiki();
        if (dto == null) {
            LOGGER.log(Level.SEVERE, "Wiki object passed is not valid");
            throw new QInvalidObject("Wiki object passed is not valid");
        }
        if (dto.getName() == null) {
            LOGGER.log(Level.SEVERE, "Wiki  object passed is not valid");
            throw new QInvalidObject("Wiki object passed is not valid");
        }
        if (null == findWikiByName(dto.getName())) {
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());
            entity.setLogo(dto.getLogo());
            em.persist(entity);
        } else {
            LOGGER.log(Level.SEVERE, "Wiki Entry object already exist");
            throw new QAlreadyExists("Wiki object already exist");
        }
        return entity.getId();
    }

    @Override
        public void editWiki(WikiDTO dto) throws QWikiException {
        if (dto == null) {
            LOGGER.log(Level.SEVERE, "Wiki object passed is not valid");
            throw new QInvalidObject("Wiki object passed is not valid");
        }
        if (dto.getName() == null) {
            LOGGER.log(Level.SEVERE, "Wiki object passed is not valid");
            throw new QInvalidObject("Wiki object passed is not valid");
        }
        WikWiki wiki = em.find(WikWiki.class, dto.getId());
        if (wiki == null) {
            LOGGER.log(Level.SEVERE, "Wiki object does not exist");
            throw new QInvalidObject("Wiki object does not exist");
        }
        wiki.setName(dto.getName());
        wiki.setDescription(dto.getDescription());
        wiki.setLogo(dto.getLogo());
        em.persist(wiki);
    }

    @Override
        public void deleteWiki(String wikiId) throws QWikiException {

        WikWiki entity = null;
        try {
            entity = em.getReference(WikWiki.class, wikiId);
        } catch (EntityNotFoundException enf) {
            LOGGER.log(Level.SEVERE, "Wiki object Does not exist");
            throw new QInvalidObject("Wiki Entry Does not exist");
        }
        em.remove(entity);
    }

    @Override
        public WikiDTO findWiki(String wikiId) {
        WikWiki entity = null;
        Query q = em.createQuery("select w from WikWiki w where w.id = :wikiID");
        q.setParameter("wikiID", wikiId);
        List l = q.getResultList();
        if (l != null && !l.isEmpty()) {
            entity = (WikWiki)l.get(0);
        }

        return entity != null
                    ? (WikiDTO) ConverterUtil.convertToWikiDTO(entity)
                    : null;
    }

    @Override
        public WikiDTO findWikiByName(String wikiName) {
        Query query = em.createQuery("select object(o) from WikWiki as o where o.name=:name");
        query.setParameter("name", wikiName);
        WikiDTO vo = null;
        List l = query.getResultList();
        if ((l != null) && (!l.isEmpty())) {
            WikWiki wiki = (WikWiki) l.get(0);
            vo = ConverterUtil.convertToWikiDTO(wiki);
        }

        return vo;
    }

    @Override
        public List<WikiDTO> getAllWikis(PagingParams pagingParams) {
        Query query = em.createQuery("select w from WikWiki w order by w.name");
        List<WikiDTO> wikis = null;
        if ((pagingParams != null) && (pagingParams.getCurrentPage() > -1)) {
            query.setFirstResult((pagingParams.getCurrentPage() - 1)
					* pagingParams.getPageSize());
			query.setMaxResults(pagingParams.getPageSize());
        }
        List<WikWiki> wikiList = (List<WikWiki>) query.getResultList();
        if (wikiList != null && !wikiList.isEmpty()) {
            wikis = new ArrayList<WikiDTO>();
            for (WikWiki wiki : wikiList) {
                WikiDTO dto = ConverterUtil.convertToWikiDTO(wiki);
                wikis.add(dto);
            }
        }

        return wikis;
    }

    @Override
        public String getWikiName(String wikiId) throws QWikiException {
        return ((WikiDTO) findWiki(wikiId)).getName();
    }

}
