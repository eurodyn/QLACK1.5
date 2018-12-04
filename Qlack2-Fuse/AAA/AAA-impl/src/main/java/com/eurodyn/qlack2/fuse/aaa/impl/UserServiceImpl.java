/*
s* Copyright 2014 EUROPEAN DYNAMICS SA <info@eurodyn.com>
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
package com.eurodyn.qlack2.fuse.aaa.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.eurodyn.qlack2.fuse.aaa.api.AccountingService;
import com.eurodyn.qlack2.fuse.aaa.api.UserService;
import com.eurodyn.qlack2.fuse.aaa.api.criteria.UserSearchCriteria;
import com.eurodyn.qlack2.fuse.aaa.api.criteria.UserSearchCriteria.UserAttributeCriteria;
import com.eurodyn.qlack2.fuse.aaa.api.dto.SessionDTO;
import com.eurodyn.qlack2.fuse.aaa.api.dto.UserAttributeDTO;
import com.eurodyn.qlack2.fuse.aaa.api.dto.UserDTO;
import com.eurodyn.qlack2.fuse.aaa.impl.model.Group;
import com.eurodyn.qlack2.fuse.aaa.impl.model.Session;
import com.eurodyn.qlack2.fuse.aaa.impl.model.User;
import com.eurodyn.qlack2.fuse.aaa.impl.model.UserAttribute;
import com.eurodyn.qlack2.fuse.aaa.impl.util.AaaUserSearchHelper;
import com.eurodyn.qlack2.fuse.aaa.impl.util.ConverterUtil;

/**
 *
 * An OSGI service providing users related services. For details of the
 * functionality offered, see the respective interfaces.
 *
 * @author European Dynamics SA
 */
public class UserServiceImpl implements UserService {
	private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());
	private EntityManager em;
	private AccountingService accountingService;
	private LdapUserUtil ldapUserUtil;
	private static final int SALT_LENGTH = 16;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	public void setAccountingService(AccountingService accountingService) {
		this.accountingService = accountingService;
	}

	public void setLdapUserUtil(LdapUserUtil ldapUserUtil) {
		this.ldapUserUtil = ldapUserUtil;
	}

	@Override
	public String createUser(UserDTO dto) {
		User user = ConverterUtil.userDTOToUser(dto);

		// Generate salt and hash password
		user.setSalt(RandomStringUtils.randomAlphanumeric(SALT_LENGTH));
		String password = user.getSalt() + dto.getPassword();
		user.setPassword(DigestUtils.md5Hex(password));

		em.persist(user);
		if (user.getUserAttributes() != null) {
			for (UserAttribute attribute : user.getUserAttributes()) {
				em.persist(attribute);
			}
		}

		return user.getId();
	}

	@Override
	public void updateUser(UserDTO dto, boolean updatePassword) {
		User user = User.find(dto.getId(), em);
		user.setUsername(dto.getUsername());
		if (updatePassword) {
			user.setSalt(RandomStringUtils.randomAlphanumeric(SALT_LENGTH));
			String password = user.getSalt() + dto.getPassword();
			user.setPassword(DigestUtils.md5Hex(password));
		}
		user.setStatus(dto.getStatus());
		user.setSuperadmin(dto.isSuperadmin());
		user.setExternal(dto.isExternal());

		if (dto.getUserAttributes() != null) {
			for (UserAttributeDTO attribute : dto.getUserAttributes()) {
				updateAttribute(attribute, true);
			}
		}

		// flush persistence context so that we can load the user with the updated attributes
		em.flush();
	}

	@Override
	public void deleteUser(String userID) {
		User user = User.find(userID, em);
		em.remove(user);
	}

	@Override
	public UserDTO getUserById(String userID) {
		User user = User.find(userID, em);
		return ConverterUtil.userToUserDTO(user);
	}

	@Override
	public Set<UserDTO> getUsersById(Collection<String> userIDs) {
		Set<UserDTO> retVal = new HashSet<>();
		Query query = em.createQuery("SELECT u FROM User u WHERE "
				+ " u.id in (:userIds)");
		query.setParameter("userIds", userIDs);
		List<User> queryResult = query.getResultList();
		for (User user : queryResult) {
			retVal.add(ConverterUtil.userToUserDTO(user));
		}
		return retVal;
	}

	@Override
	public Map<String, UserDTO> getUsersByIdAsHash(Collection<String> userIDs) {
		Map<String, UserDTO> retVal = new HashMap<>();
		Query query = em.createQuery("SELECT u FROM User u WHERE "
				+ " u.id in (:userIds)");
		query.setParameter("userIds", userIDs);
		List<User> queryResult = query.getResultList();
		for (User user : queryResult) {
			retVal.put(user.getId(), ConverterUtil.userToUserDTO(user));
		}
		return retVal;
	}

	@Override
	public UserDTO getUserByName(String userName) {
		return ConverterUtil.userToUserDTO(User.findByUsername(userName, em));
	}

	@Override
	public void updateUserStatus(String userID, byte status) {
		User user = User.find(userID, em);
		user.setStatus(status);
	}

	@Override
	public byte getUserStatus(String userID) {
		User user = User.find(userID, em);
		return user.getStatus();
	}

	@Override
	public boolean isSuperadmin(String userID) {
		User user = User.find(userID, em);
		return user.isSuperadmin();
	}

	@Override
	public boolean isExternal(String userID) {
		User user = User.find(userID, em);
		return user.isExternal() != null && user.isExternal();
	}

	@Override
	public String canAuthenticate(String username, String password) {
		User user = User.findByUsername(username, em);
		if (user != null) {
			if (user.isExternal() == null || !user.isExternal()) {
				// Generate password hash to compare with password stored in the DB.
				String checkPassword = (DigestUtils.md5Hex(user.getSalt() + password));
				if (checkPassword.equals(user.getPassword())) {
					return user.getId();
				}
			}
			else {
				return ldapUserUtil.canAuthenticate(username, password);
			}
		}
		else {
			return ldapUserUtil.canAuthenticate(username, password);
		}

		return null;
	}

	@Override
	public UserDTO login(String userID, String applicationSessionID,
			boolean terminateOtherSessions) {
		User user = User.find(userID, em);

		// Check if other sessions of this user need to be terminated first.
		if (terminateOtherSessions && user.getSessions() != null) {
			for (Session session : user.getSessions()) {
				accountingService.terminateSession(session.getId());
			}
		}

		// Create a new session for the user.
		SessionDTO session = new SessionDTO();
		session.setUserId(user.getId());
		session.setApplicationSessionID(applicationSessionID);
		accountingService.createSession(session);

		return ConverterUtil.userToUserDTO(user);
	}

	@Override
	public void logout(String userID, String applicationSessionID) {
		User user = User.find(userID, em);

		if (user.getSessions() != null) {
			for (Session session : user.getSessions()) {
				if (((applicationSessionID != null) && (session.getApplicationSessionId().equals(
						applicationSessionID)))
				|| ((applicationSessionID == null) && (session.getApplicationSessionId() == null))) {
					accountingService.terminateSession(session.getId());
				}
			}
		}
	}

	@Override
	public void logoutAll() {
		Query query = em
				.createQuery("SELECT s FROM Session s WHERE s.terminatedOn IS NULL");
		List<Session> queryResult = query.getResultList();
		if (queryResult != null) {
			for (Session session : queryResult) {
				logout(session.getUser().getId(), session.getApplicationSessionId());
			}
		}
	}

	@Override
	public List<SessionDTO> isUserAlreadyLoggedIn(String userID) {
		User user = User.find(userID, em);
		Query q = em.createQuery("SELECT s FROM Session s WHERE "
				+ "s.user = :user " + "and s.terminatedOn IS NULL "
				+ "ORDER BY s.createdOn ASC");
		q.setParameter("user", user);

		List<Session> queryResult = q.getResultList();
		List<SessionDTO> retVal = new ArrayList<>(queryResult.size());
		for (Session session : queryResult) {
			retVal.add(ConverterUtil.sessionToSessionDTO(session));
		}

		return retVal.isEmpty() ? null : retVal;
	}

	@Override
	public String updatePassword(String username, String newPassword) {
		User user = User.findByUsername(username, em);
		user.setSalt(RandomStringUtils.randomAlphanumeric(SALT_LENGTH));
		if (StringUtils.isBlank(newPassword)) {
			newPassword = RandomStringUtils.randomAlphanumeric(8);
		}
		user.setPassword(DigestUtils.md5Hex(user.getSalt() + newPassword));
		return newPassword;
	}

	@Override
	public boolean belongsToGroupByName(String userID, String groupName,
			boolean includeChildren) {
		User user = User.find(userID, em);
		Group group = Group.findByName(groupName, em);
		boolean retVal = group.getUsers().contains(user);

		if (!retVal && includeChildren) {
			for (Group child : group.getChildren()) {
				if (belongsToGroupByName(userID, child.getName(),
						includeChildren)) {
					return true;
				}
			}
		}

		return retVal;
	}

	@Override
	public void updateAttributes(Collection<UserAttributeDTO> attributes, boolean createIfMissing) {
		for (UserAttributeDTO attributeDTO : attributes) {
			updateAttribute(attributeDTO, createIfMissing);
		}
	}

	@Override
	public void updateAttribute(UserAttributeDTO attributeDTO, boolean createIfMissing) {
		String userId = attributeDTO.getUserId();
		String name = attributeDTO.getName();

		UserAttribute attribute = User.findAttribute(userId, name, em);
		if (attribute != null) {
			mapAttribute(attribute, attributeDTO);
			em.merge(attribute);
		}
		else {
			if (createIfMissing) {
				attribute = new UserAttribute();
				mapAttribute(attribute, attributeDTO);
				em.persist(attribute);
			}
			else {
				return;
			}
		}
	}

	private void mapAttribute(UserAttribute attribute, UserAttributeDTO attributeDTO) {
		String userId = attributeDTO.getUserId();
		String name = attributeDTO.getName();

		User user = User.find(userId, em);
		attribute.setUser(user);
		attribute.setName(name);

		attribute.setData(attributeDTO.getData());
		attribute.setBindata(attributeDTO.getBinData());
		attribute.setContentType(attributeDTO.getContentType());
	}

	@Override
	public void deleteAttribute(String userID, String attributeName) {
		UserAttribute attribute = User.findAttribute(userID, attributeName, em);
		em.remove(attribute);
	}

	@Override
	public UserAttributeDTO getAttribute(String userID, String attributeName) {
		UserAttribute attribute = User.findAttribute(userID, attributeName, em);
		return ConverterUtil.userAttributeToUserAttributeDTO(attribute, userID);
	}

	@Override
	public Set<String> getUserIDsForAttribute(Collection<String> userIDs,
			String attributeName, String attributeValue) {
		String queryString = "SELECT u.id FROM User u "
				+ "JOIN u.userAttributes ua "
				+ "WHERE ua.name = :attributeName "
				+ "AND ua.data = :attributeValue";
		if ((userIDs != null) && (userIDs.size() > 0)) {
			queryString = queryString.concat(" AND u.id IN (:users)");
		}
		Query q = em.createQuery(queryString);
		q.setParameter("attributeName", attributeName);
		q.setParameter("attributeValue", attributeValue);
		if ((userIDs != null) && (userIDs.size() > 0)) {
			q.setParameter("users", userIDs);
		}
		return new HashSet<>(q.getResultList());
	}

	// TODO check performance - jpa criteria do not support unions, a native
	// query
	// may be needed instead if performance is not good.
	@Override
	public List<UserDTO> findUsers(UserSearchCriteria criteria) {
		List<UserDTO> result = new ArrayList<>();

		// AaaUserSearchHelper used in order to allow including the sort
		// criterion in the select
		// clause which is required by certain DBs such as PostgreSQL and H2.
		// Using a separate
		// class for the CriteriaQuery allows us to to a cq.multiselect when
		// applying sorting
		// criteria (see below) since this requires the object being returned by
		// the query to have
		// a two-argument constructor accepting the values passed in the
		// multiselect.
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<AaaUserSearchHelper> cq = cb.createQuery(
				AaaUserSearchHelper.class).distinct(true);
		Root<User> root = cq.from(User.class);

		cq = applySearchCriteria(cb, cq, root, criteria);

		// Apply sorting. The sort criterion should be included in the select
		// clause
		// in order to support certain DBs such as PostgreSQL and H2
		Expression orderExpr = null;
		if (criteria.getSortColumn() != null) {
			cq.multiselect(root, root.get(criteria.getSortColumn()));
			orderExpr = root.get(criteria.getSortColumn());
		} else if (criteria.getSortAttribute() != null) {
			Join<User, UserAttribute> join = root.joinSet("userAttributes",
					JoinType.LEFT);
			cq.multiselect(root, join.get("data"));
			Predicate pr = cb.equal(join.get("name"),
					criteria.getSortAttribute());
			cq = addPredicate(cq, cb, pr);
			orderExpr = join.get("data");
		}
		Order order = null;
		if (criteria.isAscending()) {
			order = cb.asc(orderExpr);
		} else {
			order = cb.desc(orderExpr);
		}
		cq = cq.orderBy(order);

		TypedQuery<AaaUserSearchHelper> query = em.createQuery(cq);

		// Apply pagination
		if (criteria.getPaging() != null
				&& criteria.getPaging().getCurrentPage() > -1) {
			query.setFirstResult((criteria.getPaging().getCurrentPage() - 1)
					* criteria.getPaging().getPageSize());
			query.setMaxResults(criteria.getPaging().getPageSize());
		}

		for (AaaUserSearchHelper helper : query.getResultList()) {
			result.add(helper.getUserDTO());
		}
		return result;
	}

	@Override
	public long findUserCount(UserSearchCriteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<User> root = cq.from(User.class);
		cq = cq.select(cb.count(root));
		cq = applySearchCriteria(cb, cq, root, criteria);
		TypedQuery<Long> query = em.createQuery(cq);
		return query.getSingleResult();
	}

	private <T> CriteriaQuery<T> applySearchCriteria(CriteriaBuilder cb,
			CriteriaQuery<T> query, Root<User> root, UserSearchCriteria criteria) {
		CriteriaQuery<T> cq = query;

		// Include/exclude user IDs
		if (criteria.getIncludeIds() != null) {
			Predicate pr = root.get("id").in(criteria.getIncludeIds());
			cq = addPredicate(cq, cb, pr);
		}
		if (criteria.getExcludeIds() != null) {
			Predicate pr = cb.not(root.get("id").in(criteria.getExcludeIds()));
			cq = addPredicate(cq, cb, pr);
		}
		// Include/exclude user group IDs
		if (criteria.getIncludeGroupIds() != null) {
			Predicate pr = root.get("groups.id").in(criteria.getIncludeGroupIds());
			cq = addPredicate(cq, cb, pr);
		}
		if (criteria.getExcludeGroupIds() != null) {
			Predicate pr = cb.not(root.get("groups.id").in(criteria.getExcludeGroupIds()));
			cq = addPredicate(cq, cb, pr);
		}
		// Filter by status
		if (criteria.getIncludeStatuses() != null) {
			Predicate pr = root.get("status").in(criteria.getIncludeStatuses());
			cq = addPredicate(cq, cb, pr);
		}
		if (criteria.getExcludeStatuses() != null) {
			Predicate pr = cb.not(root.get("status").in(
					criteria.getExcludeStatuses()));
			cq = addPredicate(cq, cb, pr);
		}
		// Filter by username
		if (criteria.getUsername() != null) {
			Predicate pr = cb.like(root.<String>get("username"), criteria.getUsername());
			cq = addPredicate(cq, cb, pr);
		}
		// Filter by attributes
		if (criteria.getAttributeCriteria() != null) {
			cq = addPredicate(
					cq,
					cb,
					getAttributePredicate(cb, cq, root,
							criteria.getAttributeCriteria()));
		}
		// Filter by superadmin flag
		if (criteria.getSuperadmin() != null) {
			Predicate pr = cb.equal(root.get("superadmin"),
					criteria.getSuperadmin());
			cq = addPredicate(cq, cb, pr);
		}
		return cq;
	}

	private <T> Predicate getAttributePredicate(CriteriaBuilder cb,
			CriteriaQuery<T> cq, Root<User> root,
			UserAttributeCriteria attCriteria) {
		Predicate attributePredicate = null;

		// Please note that the way UserAttributeCriteria are constructed by the
		// UserSearchCriteriaBuilder
		// they are guaranteed to only have not null attributes OR not null
		// attCriteria (not both).
		// The code below however uses two separate if statements instead of an
		// if/else in order
		// to be able to handle a future modification of the
		// UserAttributeCriteria which will allow
		// both properties to be not null.
		if (attCriteria.getAttributes() != null) {
			// For each attribute construct a predicate of the type:
			// user.id IN (SELECT user FROM aaa_user_attribute WHERE name = ...
			// and data = ...
			// and join it with AND/OR (depending on the type of the attribute
			// criteria) with
			// other such predicates constructed in previous loops.
			attributePredicate = constructPredicate(cb, cq, root, attCriteria, attributePredicate);
		}
		if (attCriteria.getAttCriteria() != null) {
			// For each attribute criterion get the predicate corresponding to
			// it recursively
			// and then join it with AND/OR (depending on the type of the
			// attribute criteria) with
			// other such predicates retrieved in previous loops.
			for (UserAttributeCriteria nestedCriteria : attCriteria
					.getAttCriteria()) {
				Predicate nestedPredicate = getAttributePredicate(cb, cq, root,
						nestedCriteria);
				if (attributePredicate == null) {
					attributePredicate = nestedPredicate;
				} else {
					switch (attCriteria.getType()) {
					case AND:
						attributePredicate = cb.and(attributePredicate,
								nestedPredicate);
						break;
					case OR:
						attributePredicate = cb.or(attributePredicate,
								nestedPredicate);
						break;
					}
				}
			}
		}

		return attributePredicate;
	}

	private <T> Predicate constructPredicate(CriteriaBuilder cb, CriteriaQuery<T> cq, Root<User> root,
			UserAttributeCriteria attCriteria, Predicate attributePredicate) {
		for (UserAttributeDTO attribute : attCriteria.getAttributes()) {
			Subquery<String> sq = cq.subquery(String.class);
			Root<UserAttribute> sqRoot = sq.from(UserAttribute.class);
			sq = sq.select(sqRoot.get("user").<String> get("id")); // field
																	// to
																	// map
																	// with
																	// main-query

			// Construct the WHERE clause of the subquery
			if (StringUtils.isNotBlank(attribute.getName())) {
				addPredicate(sq, cb,
						cb.equal(sqRoot.get("name"), attribute.getName()));
			}
			if (StringUtils.isNotBlank(attribute.getData())) {
				addPredicate(sq, cb,
						cb.equal(sqRoot.get("data"), attribute.getData()));
			}
			if ((attribute.getBinData() != null)
					&& (attribute.getBinData().length > 0)) {
				addPredicate(
						sq,
						cb,
						cb.equal(sqRoot.get("bindata"),
								attribute.getBinData()));
			}
			if (StringUtils.isNotBlank(attribute.getContentType())) {
				addPredicate(
						sq,
						cb,
						cb.equal(sqRoot.get("contentType"),
								attribute.getContentType()));
			}

			Predicate pr = root.get("id").in(sq);
			attributePredicate = combineExressions(cb, attCriteria, attributePredicate, pr);
		}
		return attributePredicate;
	}

	private Predicate combineExressions(CriteriaBuilder cb, UserAttributeCriteria attCriteria, Predicate attributePredicate,
			Predicate pr) {
		if (attributePredicate == null) {
			attributePredicate = pr;
		} else {
			if (attCriteria.getType() == UserAttributeCriteria.Type.AND) {
				attributePredicate = cb.and(attributePredicate, pr);
			} else {
				attributePredicate = cb.or(attributePredicate, pr);
			}
		}
		return attributePredicate;
	}

	private <T> CriteriaQuery<T> addPredicate(CriteriaQuery<T> query,
			CriteriaBuilder cb, Predicate pr) {
		CriteriaQuery<T> cq = query;
		if (cq.getRestriction() != null) {
			cq = cq.where(cb.and(cq.getRestriction(), pr));
		} else {
			cq = cq.where(pr);
		}
		return cq;
	}

	private <T> Subquery<T> addPredicate(Subquery<T> query, CriteriaBuilder cb,
			Predicate pr) {
		Subquery<T> sq = query;
		if (sq.getRestriction() != null) {
			sq = sq.where(cb.and(sq.getRestriction(), pr));
		} else {
			sq = sq.where(pr);
		}
		return sq;
	}

}
