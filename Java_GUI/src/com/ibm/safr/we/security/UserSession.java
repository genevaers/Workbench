package com.ibm.safr.we.security;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2008.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.EnvRole;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.Environment;
import com.ibm.safr.we.model.Group;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.User;
import com.ibm.safr.we.model.associations.GroupComponentAssociation;
import com.ibm.safr.we.model.associations.GroupEnvironmentAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRObject;

/**
 * Represents a user's 'session' with the workbench whereby they are logged in
 * to the workbench (authenticated to SAFR) and have access to a specific
 * Environment. If they are a general user (not a System Administrator) then
 * they will also be logged in for a specific Group which is used to determine
 * their access authority for the current session.
 * <p>
 * This class is a container for three objects:
 * <ul>
 * <li>the currently logged in User
 * <li>the Environment the User is logged into
 * <li>the Group the User belongs to (null for System Administrators)
 * </ul>
 * The class provides some convenience methods that return information from
 * these objects that exists in the context of the user's session. For example,
 * whether the User's login Group has permission to perform a certain function
 * in the current Environment or whether it has certain edit rights on some
 * metadata component belonging to the current Environment.
 * <p>
 * This is an immutable object. If a the user logs in to a different environment
 * or a different user logs in, a new instance of this class must be created.
 * 
 */
public class UserSession extends SAFRObject {

	private User loggedInUser;
	private Environment sessionEnvironment;
	private Group sessionGroup;
	private GroupEnvironmentAssociation sessionGroupEnvAssoc = null;
    private GroupEnvironmentAssociation cacheGroupEnvAssoc = null;

	/**
	 * Create a session object for the specified User,Environment and Group. If
	 * the User is system Administrator then group is passed as null.
	 * 
	 * @param user
	 * @param environment
	 * @param group
	 * @throws NullPointerException
	 *             if either User or Environment parameter is null or User is
	 *             general User and Group is null.
	 */
	public UserSession(User user, Environment environment, Group group) {

		if (user == null || environment == null) {
			throw new NullPointerException();
		}
		if ((!user.isSystemAdmin()) && (group == null)) {
			throw new NullPointerException();
		}

		this.loggedInUser = user;
		this.sessionEnvironment = environment;
		this.sessionGroup = group;
	}

	public void reload() throws SAFRException {
	    
	    
	    String group = null; 
	        
	    if (sessionGroup != null) {
	        group = sessionGroup.getName();
	    }
	    
        // Check if the supplied user exists and the password is correct
        loggedInUser = SAFRApplication.getSAFRFactory().getUser(loggedInUser.getUserid());

        sessionGroup = null;
        if (!loggedInUser.isSystemAdmin()) {
            if (group != null && group != "") {
                sessionGroup = SAFRApplication.getSAFRFactory().getGroup(group);                
            }
            else {
                sessionGroup = loggedInUser.getDefaultGroup();
                if (sessionGroup == null) {
                    throw new SAFRException("User " + loggedInUser.getUserid() + " no longer has any rights on this environment");
                }
            }
        }
        	    
	    sessionGroupEnvAssoc = null;
	    cacheGroupEnvAssoc = null;
	}
	
	/**
	 * Return the logged in User.
	 * 
	 */
	public User getUser() {
		return loggedInUser;
	}

	/**
	 * Return the Environment the user logged into.
	 */
	public Environment getEnvironment() {
		return sessionEnvironment;
	}

	/**
	 * Return the Group the user logged in with or null if the user is a System
	 * Administrator.
	 * 
	 */
	public Group getGroup() {
		return sessionGroup;
	}

	/**
	 * Return true if the logged in user is a System Administrator, otherwise
	 * false.
	 */
	public Boolean isSystemAdministrator() {
		return loggedInUser.isSystemAdmin();
	}

	
	public Boolean isNotSystemAdministrator() {
		return !loggedInUser.isSystemAdmin();
	}

	
	
	/**
	 * Returns the login Group's EditRights in the specified Environment for the
	 * specified component. If the group does not have any rights over the
	 * component then it returns null.
	 * 
	 * @param componentType
	 *            The type of metadata component.
	 * @param id
	 *            The ID of the component.
	 * @param envId
	 *            The ID of the environment containing the component.
	 * @return EditRights
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public EditRights getEditRights(ComponentType componentType, Integer id,
			Integer envId) throws DAOException, SAFRException {
		EditRights editRights;

        if (isSystemAdministrator()) {
            return EditRights.ReadModifyDelete;
        }
        		
		GroupComponentAssociation grpCompAssoc = sessionGroup.getComponentAssociation(componentType, envId, id);
		if (grpCompAssoc == null) {
			editRights = getRoleEditRights(componentType, envId);
		} else {
			editRights = grpCompAssoc.getRights();
		}
		return editRights;
	}

	/**
	 * Returns the login Group's EditRights in the current Environment for
	 * the specified component. If the User's group does not have any rights
	 * over the component then it returns null.
	 * 
	 * @param componentType
	 *            The type of metadata component.
	 * @param id
	 *            The ID of the component.
	 * @return EditRights
	 * @throws DAOException
	 * @throws SAFRException
	 */
	public EditRights getEditRights(ComponentType componentType, Integer id)
			throws DAOException, SAFRException {
	    
        EditRights editRights;
	    
        if (isSystemAdministrator()) {
            return EditRights.ReadModifyDelete;
        }
        
        GroupComponentAssociation grpCompAssoc = sessionGroup.getComponentAssociation(componentType, sessionEnvironment.getId(), id);
        if (grpCompAssoc == null) {
            initSessionEnvAssoc();
            if (sessionGroupEnvAssoc == null) {
                return EditRights.None;
            }
            else {
                editRights = sessionGroupEnvAssoc.getEnvRole().getRights().getComponentRight(componentType);
            }
        } else {
            editRights = grpCompAssoc.getRights();
        }
        return editRights;
	    
	}

    /**
     * Returns the edit rights based on the rights value else on the role
     * @param rights
     * @param type
     * @param envId
     * @return
     * @throws DAOException
     * @throws SAFRException
     */
	public EditRights getEditRights(int rights, ComponentType type, Integer envId) throws DAOException {
	    
        if (isSystemAdministrator()) {
            return EditRights.ReadModifyDelete;
        }
	    
        EditRights edRight;
        if (rights == 0) {
            edRight = SAFRApplication.getUserSession().getRoleEditRights(type, envId);                  
        } else {
            edRight = EditRights.intToEnum(rights);
        }	 
        return edRight;
	}

    /**
     * Returns the edit rights based on the rights value else on the role
     * @param rights
     * @param type
     * @param envId
     * @return
     * @throws DAOException
     * @throws SAFRException
     */
    public EditRights getEditRightsNoUser(int rights, ComponentType type, Integer envId) throws DAOException {
        
        EditRights edRight;
        if (rights == 0) {
            edRight = SAFRApplication.getUserSession().getRoleEditRights(type, envId);                  
        } else {
            edRight = EditRights.intToEnum(rights);
        }    
        return edRight;
    }
	
    /**
     * Returns the edit rights based on just the role of the current logged in user in the given environment
     * @param componentType
     * @param envId
     * @return
     * @throws DAOException
     * @throws SAFRException
     */
	
    public EditRights getRoleEditRights(ComponentType componentType, Integer envId) throws DAOException {
        EditRights editRights;
    
        if (isSystemAdministrator()) {
            return EditRights.ReadModifyDelete;
        }
        if (envId == sessionEnvironment.getId()) {
            initSessionEnvAssoc();        
            if (sessionGroupEnvAssoc == null) {
                return EditRights.None;
            }
            else {
                editRights = sessionGroupEnvAssoc.getEnvRole().getRights().getComponentRight(componentType);
            }
        }
        else {
            initCacheEnvAssoc(envId);
            if (cacheGroupEnvAssoc == null) {
                return EditRights.None;
            }
            else {                
                editRights = cacheGroupEnvAssoc.getEnvRole().getRights().getComponentRight(componentType);
            }
        }
        return editRights;
    }

    /**
     * Returns the edit rights based on just the role of the current logged in user in the given environment
     * @param componentType
     * @param envId
     * @return
     * @throws DAOException
     * @throws SAFRException
     */
    
    public EditRights getRoleEditRights(ComponentType componentType) throws DAOException {
        return getRoleEditRights(componentType,sessionEnvironment.getId());
    }
    
	private void initSessionEnvAssoc() throws DAOException {
        if (sessionGroupEnvAssoc == null) {
            // lazy initialization
            sessionGroupEnvAssoc = SAFRAssociationFactory
                .getGroupToEnvironmentAssociation(sessionGroup,sessionEnvironment.getId());
        }	    
	}
	
    private void initCacheEnvAssoc(int envId) throws DAOException {
        if (cacheGroupEnvAssoc == null ||
            cacheGroupEnvAssoc.getEnvironmentId() != envId) {
            // lazy initialization
            cacheGroupEnvAssoc = SAFRAssociationFactory.
                getGroupToEnvironmentAssociation(sessionGroup,envId);
        }       
    }
	
	/**
	 * Return true if the User's login Group has the specified permission in the
	 * current Environment, otherwise false. If the User is a System
	 * Administrator this method always returns true.
	 * 
	 * @param permission
	 *            The required permission.
	 * @return true if permission exists, otherwise false
	 * @throws DAOException
	 */
	public Boolean hasPermission(Permissions permission) throws DAOException {
		if (isSystemAdministrator()) {
			return true;
		}
		initSessionEnvAssoc();
        if (sessionGroupEnvAssoc == null) {
            return false;
        }
        else {
            return sessionGroupEnvAssoc.hasPermission(permission);
        }
	}

	/**
	 * Return true if the User's login Group has the specified permission in the
	 * Environment passed as a parameter to this function, otherwise false. If
	 * the User is a System Administrator this method always returns true.
	 * 
	 * @param permission
	 *            The required permission.
	 * @param envId
	 *            The id of the environment in which permission needs to be
	 *            checked
	 * @return true if permission exists, otherwise false
	 * @throws DAOException
	 */
	public Boolean hasPermission(Permissions permission, Integer envId)
			throws DAOException {
		if (isSystemAdministrator()) {
			return true;
		}		
        if (sessionEnvironment.getId() == envId) {
            return hasPermission(permission);
        } else {
            initCacheEnvAssoc(envId);
            if (cacheGroupEnvAssoc == null) {
                return false;
            }            
            else {
                return cacheGroupEnvAssoc.hasPermission(permission);
            }
        }
		
	}
	
	/**
	 * Return true if user is an ordinary user in the specified Environment, 
	 * otherwise false.
	 * 
	 * @throws DAOException
	 */
	public Boolean isOrdinaryUser(Integer envId) throws DAOException {
		if (isSystemAdministrator() || isEnvironmentAdministrator(envId)) {
			return false;
		} else {
			return true;
		}
	}
	
    /**
     * Return true if user has migrate access in the specified Environment,
     * This can be either SA, EA or MigrateIn in env 
     * otherwise false.
     * 
     * @throws DAOException
     */
    public Boolean isAdminOrMigrateInUser(Integer envId) throws DAOException {
        if (isSystemAdministrator() || hasPermission(Permissions.MigrateIn, envId)) {
            return true;
        } else {
            return false;
        }
    }
    
	/**
	 * Return true if user is an ordinary user in the current Environment, 
	 * otherwise false.
	 * 
	 * @throws DAOException
	 */
	public Boolean isOrdinaryUser() throws DAOException {
		return isOrdinaryUser(this.sessionEnvironment.getId());
	}
	
	/**
	 * Return true if user is an Environment Administrator in the specified Environment, 
	 * otherwise false.
	 * 
	 * @throws DAOException
	 */
	public Boolean isEnvironmentAdministrator(Integer envId) throws DAOException {
	    if (sessionEnvironment.getId() == envId) {
	        return isEnvironmentAdministrator();
	    }
	    else {
	        initCacheEnvAssoc(envId);
            if (cacheGroupEnvAssoc == null) {
                return false;
            }
            else {
                return cacheGroupEnvAssoc.getEnvRole().equals(EnvRole.ADMIN);
            }
	    }
	}
	
	/**
	 * Return true if user is an Environment Administrator in the current Environment, 
	 * otherwise false.
	 * 
	 * @throws DAOException
	 */
	public Boolean isEnvironmentAdministrator() throws DAOException {
        initSessionEnvAssoc();
        if (sessionGroupEnvAssoc == null) {
            return false;
        }
        else {
            return sessionGroupEnvAssoc.getEnvRole().equals(EnvRole.ADMIN);
        }
	}

    /**
     * This method is to check whether current user is System Admin or
     * environment admin or not.
     * 
     * @return true if user is Sys Admin or Env Admin else false.
     * @throws SAFRException
     */
    public Boolean isSystemAdminOrEnvAdmin(int envId) throws SAFRException {
        if (isSystemAdministrator()) {
            return true;
        } else {
            if (sessionEnvironment.getId() == envId) {
                return isEnvironmentAdministrator();
            } else {
                return isEnvironmentAdministrator(envId);
            }
        }
    }
	
    /**
     * This method is to check whether current user is System Admin or
     * environment admin or not.
     * 
     * @return true if user is Sys Admin or Env Admin else false.
     * @throws SAFRException
     */
    public Boolean isSystemAdminOrEnvAdmin() throws SAFRException {
        if (isSystemAdministrator() || 
            isEnvironmentAdministrator() ) {
            return true;
        } else {
            return false;
        }
    }
	
}
