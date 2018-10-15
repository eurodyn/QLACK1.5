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
package com.eurodyn.qlack2.fuse.cm.impl;

import java.util.logging.Logger;

import javax.persistence.EntityManager;

import org.joda.time.DateTime;

import com.eurodyn.qlack2.fuse.cm.api.ConcurrencyControlService;
import com.eurodyn.qlack2.fuse.cm.api.exception.QNodeLockException;
import com.eurodyn.qlack2.fuse.cm.api.util.CMConstants;
import com.eurodyn.qlack2.fuse.cm.impl.model.Node;

public class ConcurrencyControlServiceImpl implements ConcurrencyControlService {
	private static final Logger LOGGER = Logger.getLogger(ConcurrencyControlServiceImpl.class.getName());
	private EntityManager em;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	public void lock(String nodeID, String lockToken, boolean lockChildren,
			String userID) throws QNodeLockException {
		Node node = Node.findNode(nodeID, em);
		node.setLockToken(lockToken);
		node.setAttribute(CMConstants.ATTR_LOCKED_ON, String.valueOf(DateTime.now().getMillis()), em);
		node.setAttribute(CMConstants.ATTR_LOCKED_BY, userID, em);

		if (lockChildren) {
			for (Node child : node.getChildren()) {
				lock(child.getId(), lockToken, true, userID);
			}
		}
	}

	@Override
	public void unlock(String nodeID, String lockToken, boolean overrideLock)
			throws QNodeLockException {
		Node node = Node.findNode(nodeID, em);
		if (node.getLockToken() != null  
				&& overrideLock && node.getLockToken().equals(lockToken)) {
			node.setLockToken(null);
		}
		node.removeAttribute(CMConstants.ATTR_LOCKED_ON, em);
		node.removeAttribute(CMConstants.ATTR_LOCKED_BY, em);
	}

	@Override
	public String isLocked(String nodeID) {
		return Node.findNode(nodeID, em).getLockToken();
	}

}
