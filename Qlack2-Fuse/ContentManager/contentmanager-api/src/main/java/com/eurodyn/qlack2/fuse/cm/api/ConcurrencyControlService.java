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

import com.eurodyn.qlack2.fuse.cm.api.exception.QNodeLockException;

public interface ConcurrencyControlService {
	/** Locks a node, so that it cannot be modified. If the node is already locked then an exception is thrown.
     * @param nodeID The id of the node to be locked.
     * @param lockToken A token used to lock the node. All future operations altering this node
     * will be blocked while the node is locked unless the token used to lock the node
     * is passed to the operation.
     * @param lockChildren Whether child nodes of this node should also be locked
     * @param userID The ID of the user who locked this node. This will be used simply as metadata
     * for the lock and will not be taken into account when checking if a locked node can be edited.
     * @throws QNodeLockException If the specified node is already locked
     */
    void lock(String nodeID, String lockToken, boolean lockChildren, String userID) throws QNodeLockException;

    /**
     * Unlocks a node. Before this method attempts to unlock a node 
     * it checks to see whether it has been previously locked or not
     * therefore you can safely use it even if you do not know the current 
     * lock state of a node.
     * @param nodeID The id of the node to be unlocked.
     * @param lockToken The token with which the node was locked which will be used
     * to unlock the node
     * @param overrideLock If true then this method will not attempt to use the
     * passed lock token but will just unlock the node.
     * @throws QNodeLockException If the node cannot be unlocked with the passed token
     */
    void unlock(String nodeID, String lockToken, boolean overrideLock) throws QNodeLockException;

    /**
     * Checks whether a node is currently locked.
     * @param nodeID The UUID of the node to check.
     * @return The lock token or 'null' in case the node is not locked.
     */
    String isLocked(String nodeID);
}
