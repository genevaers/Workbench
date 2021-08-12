package com.ibm.safr.we.exceptions;

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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.model.SAFRValidationToken;

/**
 * This implements SAFRException.It collects all errors and can be thrown as
 * "cause" in SAFR Exceptions.
 * 
 */
public class SAFRValidationException extends SAFRException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SAFRValidationType safrValidationType;
	private SAFRValidationToken safrValidationToken;
	private String contextMessage;
	private Map<Object, ArrayList<String>> errorMessageMap = new TreeMap<Object, ArrayList<String>>();
	private Map<ComponentType, List<DependentComponentTransfer>> dependencyMap = new TreeMap<ComponentType, List<DependentComponentTransfer>>();

	/**
	 * Default Constructor of SAFRValidationException. Sets the
	 * SAFRValidationType to ERROR.
	 */
	public SAFRValidationException() {
		super();
		safrValidationType = SAFRValidationType.ERROR;
		safrValidationToken = null;
	}

	/**
	 * The constructor of SAFRValidationException class. This constructor
	 * accepts the cause of Validation Exception.
	 * 
	 * @param cause
	 *            : The cause for this exception.
	 */
	public SAFRValidationException(Throwable cause) {
		super(cause);
		safrValidationType = SAFRValidationType.ERROR;
	}
	
	/**
	 * Creates an object with the specified error message the SAFRValidationType
	 * ERROR.
	 * 
	 * @param key
	 *            for the error message map
	 * @param msg
	 *            the error message
	 */
	public SAFRValidationException(Object key, String msg) {
		this();
		setErrorMessage(key, msg);
	}

	public SAFRValidationToken getSafrValidationToken() {
		return safrValidationToken;
	}

	public void setSafrValidationToken(SAFRValidationToken safrValidationToken) {
		this.safrValidationToken = safrValidationToken;
	}

	/**
	 * Get list of error messages of the SAFRValidationException.
	 * 
	 * @return list of ErrorMessages of the SAFRValidationException.
	 */
	public ArrayList<String> getErrorMessages() {
		ArrayList<String> errorList = new ArrayList<String>();
		if (!errorMessageMap.isEmpty()) {
			for (Object property : errorMessageMap.keySet()) {
				errorList.addAll(errorMessageMap.get(property));
			}
		}
		return errorList;
	}

	/**
	 * This method is used to add a list of error messages of
	 * SAFRValidationException, along with the key,in the error message map.If a
	 * list with the key already exists, this method will append to that list.
	 * 
	 * @param key
	 *            : The Property of model class.
	 * @param errorMessage
	 *            : List of error messages.
	 */
	public void setErrorMessages(Object key, ArrayList<String> errorMessage) {
		if (errorMessageMap.containsKey(key)) {
			errorMessageMap.get(key).addAll(errorMessage);
		} else {
			ArrayList<String> newErrorList = new ArrayList<String>();
			newErrorList.addAll(errorMessage);
			errorMessageMap.put(key, newErrorList);
		}
	}

	/**
	 * This method is used to add a single error message of
	 * SAFRValidationException,along with the key,in the error message map.If a
	 * list with the key already exists, this method will append to that list.
	 * 
	 * @param key
	 *            : The Property of model class.
	 * @param message
	 *            : The error message to be set in the map.
	 */
	public void setErrorMessage(Object key, String message) {
		if (errorMessageMap.containsKey(key)) {
			errorMessageMap.get(key).add(message);
		} else {
			ArrayList<String> newErrorList = new ArrayList<String>();
			newErrorList.add(message);
			errorMessageMap.put(key, newErrorList);
		}

	}

	/**
	 * This method is used to get the error message map which has java object as
	 * key and an ArrayList of error message as value.
	 * 
	 * @return a map which has java object as key and an ArrayList of error
	 *         message as value.
	 */
	public Map<Object, ArrayList<String>> getErrorMessageMap() {
		return errorMessageMap;
	}

	/**
	 * This method will return a single string containing all the error
	 * messages.
	 * 
	 * @return all error messages in form of single String.
	 */
	public String getMessageString() {
		StringBuffer errorString = new StringBuffer();
		if (!errorMessageMap.isEmpty()) {
			for (Object property : errorMessageMap.keySet()) {
				for (String errorMesg : errorMessageMap.get(property)) {
					errorString.append(errorMesg + SAFRUtilities.LINEBREAK);
				}
			}
			errorString.setLength(errorString.length()-SAFRUtilities.LINEBREAK.length());
		}
		return errorString.toString();
	}
	
	/**
	 * This method will return a single string containing all the error messages
	 * stored under the specified key.
	 * 
	 * @return all error messages for a key as a single String.
	 */
	public String getMessageString(Object key) {
		StringBuffer errorString = new StringBuffer();
		if (!errorMessageMap.isEmpty()) {
			for (String errorMsg : errorMessageMap.get(key)) {
				errorString.append(errorMsg + SAFRUtilities.LINEBREAK);
			}
            errorString.setLength(errorString.length()-SAFRUtilities.LINEBREAK.length());
		}
		return errorString.toString();
	}
	
	@Override
	public String getMessage() {
		return getMessageString();
	}

	public SAFRValidationType getSafrValidationType() {
		return safrValidationType;
	}

	public void setSafrValidationType(SAFRValidationType safrValidationType) {
		this.safrValidationType = safrValidationType;
	}

	/**
	 * This method will clear all the messages from the error message map.
	 */
	public void clearMessages() {
		errorMessageMap.clear();
	}
	
	/**
	 * Indicates if the SAFRValidationType of this exception represents a
	 * dependency error.
	 * 
	 * @see SAFRValidationType
	 * 
	 * @return true if this is a dependency error, otherwise false
	 */
	public boolean isDependencyError() {
		SAFRValidationType type = null;
		if (safrValidationToken != null) {
			type = safrValidationToken.getValidationFailureType();
		} else {
			type = safrValidationType;
		}
		return type.isDependencyError() ? true : false;
	}
	
	/**
	 * Indicates if the SAFRValidationType of this exception represents a
	 * dependency warning.
	 * 
	 * @see SAFRValidationType
	 * 
	 * @return true if this is a dependency warning, otherwise false
	 */
	public boolean isDependencyWarning() {
		SAFRValidationType type = null;
		if (safrValidationToken != null) {
			type = safrValidationToken.getValidationFailureType();
		} else {
			type = safrValidationType;
		}
		return type.isDependencyWarning() ? true : false;
	}

	/**
	 * Returns additional context information for the validation message(s)
	 * returned by the getMessageString methods.
	 * 
	 * @return the contextMessage
	 */
	public String getContextMessage() {
		return contextMessage;
	}

	/**
	 * Sets additional context information for the validation message(s)
	 * returned by the getMessageString methods.
	 * 
	 * @param contextMessage
	 *            the contextMessage to set
	 */
	public void setContextMessage(String contextMessage) {
		this.contextMessage = contextMessage;
	}

	/**
	 * Returns the map of dependencies reported by this exception. Map contains
	 * Lists of DependentComponentTransfers key by ComponentType.
	 * 
	 * @return the dependency map
	 */
	public Map<ComponentType, List<DependentComponentTransfer>> getDependencies() {
		return dependencyMap;
	}

	/**
	 * Get the list of dependencies for the specified component type. Returns
	 * null if dependency not found.
	 * 
	 * @param type
	 *            dependent ComponentType
	 * @return a List of DependentComponentTransfer
	 */
	public List<DependentComponentTransfer> getDependencies(ComponentType type) {
		if (type != null) {
			return dependencyMap.get(type);
		} else {
			return null;
		}
	}

	/**
	 * Sets the map of dependencies to be reported by this exception.
	 * 
	 * @param dependencies
	 */
	public void setDependencies(Map<ComponentType, List<DependentComponentTransfer>> dependencies) {
		if (dependencies != null) {
			this.dependencyMap = dependencies;
		}
	}

	/**
	 * Get the dependency with the specified component type and ID.
	 * Returns null if dependency not found.
	 * 
	 * @param type
	 *            dependent ComponentType
	 * @param id
	 *            dependent component ID
	 * @return a DependentComponentTransfer
	 */
	public DependentComponentTransfer getDependency(ComponentType type, Integer id) {
		if (type != null && id != null && dependencyMap.get(type) != null) {
			for (DependentComponentTransfer dependency : dependencyMap.get(type)) {
				if (id.equals(dependency.getId())) {
					return dependency;
				}
			}
		}
		return null;
	}

	/**
	 * Check if the specified component is one of the dependencies reported by
	 * this exception.
	 * 
	 * @param type
	 *            dependent ComponentType
	 * @param id
	 *            dependent component ID
	 * @return true if it is a dependency, otherwise false
	 */
	public boolean hasDependency(ComponentType type, Integer id) {
		if (getDependency(type, id) != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Add a dependency to the map of dependencies reported by this exception.
	 * Action will be ignored if either argument is null.
	 * 
	 * @param type
	 *            dependent ComponentType
	 * @param dependency
	 *            DependentComponentTransfer to be added
	 */
	public void addDependency(ComponentType type, DependentComponentTransfer dependency) {
		if (type != null && dependency != null) {
			List<DependentComponentTransfer> deps = dependencyMap.get(type);
			if (deps != null) {
				if (!deps.contains(dependency)) {
					deps.add(dependency);
				}
			} else {
				deps = new ArrayList<DependentComponentTransfer>();
				deps.add(dependency);
				dependencyMap.put(type, deps);
			}
		}
	}

	/**
	 * Remove all occurrences of the specified dependency from the map of
	 * dependencies reported by this exception.
	 * 
	 * @param type
	 *            dependent ComponentType
	 * @param id
	 *            dependent component ID
	 */
	public void removeDependency(ComponentType type, Integer id) {
		List<DependentComponentTransfer> deps = dependencyMap.get(type);
		if (deps != null) {
			List<DependentComponentTransfer> deps2 = new ArrayList<DependentComponentTransfer>();
			for (DependentComponentTransfer dep : deps) {
				// coded this way to avoid ConcurrentModificationException
				// when using the remove() method
				if (id.equals(dep.getId())) {
					continue; // skip it
				} else {
					deps2.add(dep); // add it to new list
				}
			}
			deps.clear();
			deps.addAll(deps2);
			//dependencyMap.put(type, deps2); // replace with new list
		}
	}

	/**
	 * Generates the dependency error message for the specified message
	 * key from the map of dependencies reported by this exception.
	 * Stores the message so that it can be retrieved later.
	 * The action will be ignored if the message key is null.
	 * 
	 * @param key
	 *            the error msg key
	 * @return the dependency error message string
	 * 
	 * @see getErrorMessageMap()
	 */
	public String createDependencyErrorMessage(Object key) {
		String msg = "";
		List<Integer> uniqueComponentList =  new ArrayList<Integer>();
		if (key != null) {
			errorMessageMap.remove(key);
			for (ComponentType compType : dependencyMap.keySet()) {
				List<DependentComponentTransfer> deps = dependencyMap
						.get(compType);
				if (deps.size() > 0) {
					msg += compType.getLabel() + "s" + LINEBREAK;

					for (DependentComponentTransfer dep : deps) {
						if (!uniqueComponentList.contains(dep.getId())) {
							Integer id = dep.getId();
							String name = dep.getName();
							msg += "    " + name + " [" + id + "]" + LINEBREAK;
							uniqueComponentList.add(id); // to ignore duplicates
						}
					}
					msg += LINEBREAK;
				}
			}
			if (msg != "") {
				setErrorMessage(key, msg);
			}
		}
		return msg;
	}
	
    @Override
    public String toString() {
        return getMessage();
    }

	
}
