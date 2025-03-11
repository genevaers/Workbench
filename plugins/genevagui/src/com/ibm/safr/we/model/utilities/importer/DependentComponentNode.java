package com.ibm.safr.we.model.utilities.importer;

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


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.transfer.SAFRComponentTransfer;
import com.ibm.safr.we.data.transfer.SAFRTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;

/**
 * 
 * A vehicle to get data to the GUI for name duplication
 * GUI can change the newName.
 * 
 * This has grown into a node which forms a tree of dependent 
 * components. Each stores the XML transfer object and and possibly an existing DB component
 * 
 * A DCN has states that determine whether it is displayed to the user
 * and in turn reflect what should be done to the records collection
 * 
 * States
 * 		Initial 	- only importComponent is set to the record for the given component
 * 
 * 		NameClashes - the existingComponent is 
 * 		UseExisting - the existingComp
 * And flags for use existing database component and to remember the decision for
 * subsequent XML files being imported.
 * 
 * Setting a flag recurses to the child components in the tree.
 *
 */
public abstract class DependentComponentNode {

	//the following members contstitute the state of the DCN
	protected SAFRComponentTransfer existingComponent = null;
	protected Boolean useExisting;
	protected SAFRComponentTransfer importComponent = null;
	protected String newName;
	protected Boolean remember = false;
	protected Boolean nameClashesFound = false;

	//the variables below are to help manage the DCN
	//and its place in the tree of DCNs

	//the type is just set to help the dialog sort the components
	protected ComponentType type = null;

	private Boolean userExec = false;

	private Boolean breadCrumb = false;

	private TreeMap<Integer, DependentComponentNode> children = new TreeMap<Integer, DependentComponentNode>();
	protected TreeMap<Integer, DependentComponentNode> parents = new TreeMap<Integer, DependentComponentNode>();

	static protected DependentComponentNode root = null;

	//this is passed in by the importer so we can manipulate the records set
	//static so all descendants reference the same set or records
	static protected Map<Class<? extends SAFRTransfer>, Map<Integer, SAFRTransfer>> records = null;

	//flattened out map of transfer records to avoid walking the graph
	//and double checking components
	//Only a root object should set this - how does it get populated?
	//
	// Not sure I like this - we always need to keep it in sync    
	//
	// At the end of a root build Tree
	protected Map<Class<? extends SAFRTransfer>, Map<String, DependentComponentNode>> descendants = null;

	//Map per component type of the name clashes already decided
	//Use the DuplicateNameComponent to track old and new names
	//again passed in by the importer and held on the base as a static
	static protected Map<Class<? extends SAFRTransfer>, Map<String, DependentComponentNode>> decidedComponents = null;

	public Map<Class<? extends SAFRTransfer>, Map<Integer, SAFRTransfer>> getRecords() {
		return records;
	}

	public void setRecords(
			Map<Class<? extends SAFRTransfer>, Map<Integer, SAFRTransfer>> records) {
		DependentComponentNode.records = records;
	}

	/**
	 * Construct a DependentComponentNode
	 * 
	 * Pass in the parent node... if there are no grandparents
	 * the parent must be the root
	 * 
	 * @param parent
	 */
	public DependentComponentNode(DependentComponentNode parent) {
		//if no parent is set then this must be the root
		if(parent != null) {
			setParent(parent);
		}
		else {
			root = this;
		}
		useExisting = false;
	}

	public SAFRTransfer getImportComponent() {
		return importComponent;
	}

	public Boolean getBreadCrumb() {
		return breadCrumb;
	}

	public void setBreadCrumb(Boolean breadCrumb) {
		this.breadCrumb = breadCrumb;
	}

	public void sweepBreadCrumbs() {
		this.breadCrumb = false;
		//Cascade the to all of our children
		for(DependentComponentNode child : children.values()) {
			child.sweepBreadCrumbs();
		}
	}

	public void setImportComponent(SAFRComponentTransfer importComponent) {
		this.importComponent = importComponent;

	}

	public SAFRTransfer getExisitingComponent() {
		return existingComponent;
	}

	public void setExisitingComponent(SAFRComponentTransfer dbComponent) {
		this.nameClashesFound = true;
		this.existingComponent = dbComponent;
		generateSuggestedName(getOldName());
	}

	public Boolean getRemember() {
		return remember;
	}

	/**
	 * Trick here is that we should not set local flag for the top level component
	 * Bad things happen if its replacement name clashed
	 * (the remembered transfer object is removed from records)
	 * @param remember
	 */
	public void setRemember(Boolean remember) {
		if (parents.size() > 0) {
			this.remember = remember;
		}
		
		//recurse the to all of our children
		for(DependentComponentNode child : children.values()) {
			child.setRemember(remember);
		}
	}

	public ComponentType getComponentType() {
		return type;
	}

	public void setComponentType(ComponentType type) {
		this.type = type;
	}

	public Boolean getUseExisting() {
		return useExisting;
	}

	/**
	 * Set the useExisting flag
	 * And cascade to all child components.
	 * 
	 * Do not allow it to be reset if ANY parent is set
	 * 
	 * @param useExisting
	 */
	public void setUseExisting(Boolean useExisting) {
		Boolean parentBlock = false;
		if( (useExisting == false) ) { 
			for (DependentComponentNode parent : parents.values()) {
				System.out.println("me HC " + hashCode() + "set existing parent" + parent.getUseExisting() + " HC " + parent.hashCode()) ;
				if (parent.getUseExisting() == true) {
					parentBlock = true;
					break;
				}
			}
		}
		if ( parentBlock == false) {
			recurseSetUseExisting(useExisting);
		}
	}

	private void recurseSetUseExisting(Boolean useExisting) {
		this.useExisting = useExisting;
		System.out.println("set existing " + getOldName() + " HC " + hashCode()) ;
		//Cascade the to all of our children
		for(DependentComponentNode child : children.values()) {
			child.recurseSetUseExisting(useExisting);
		}
	}

	public String getOldName() {
		String name = null;
		if (importComponent != null)
		{
			name = importComponent.getName();
		}
		return name;
	}

	public Integer getTxfId() {
		return importComponent.getId();
	}

	public void setOldName(String oldName) {
		//initialise the newName too
		generateSuggestedName(oldName);
		//this.newName = oldName + "_1";
	}

	public String getNewName() {
		return newName;
	}

	public void setNewName(String newName) {
		this.newName = newName;
	}

	public Integer getExistingTxfId() {
		Integer id = null;
		if (existingComponent != null)
		{
			id = existingComponent.getId();
		}
		return id;
	}

	public void setUserExec(Boolean userExec) {
		this.userExec = userExec;
	}

	public Boolean getUserExec() {
		return userExec;
	}

	/**
	 * Add the Transfer object to the map of Children
	 * If it is not already there.
	 * 
	 * Already there could be on a different branch!
	 * Need to go back and start from the root... 
	 * not just the level above.
	 * 
	 * Should we look locally first?
	 * 
	 * @param child
	 */
	public DependentComponentNode addChildComponent(DependentComponentNode child) {
		DependentComponentNode realChild = child;
		Integer childId = child.getTxfId();
		
		//use an algorithm to generate the map key based on the id and component type
		// mapId = id * 10 + compNumber
		// there are 17 components in the ComponentType Enum...
		// we only have 8 DCN types though.
		
		Integer mapId = child.generateComponentKey();
		//check child is us - which is the case for a exit executable
		if(childId.equals(getTxfId())) {
			children.put(mapId, child);
			System.out.println("HC " + hashCode() + " add child exit" + child.getOldName() + " HC " + child.hashCode()) ;
		}
		else {
			// See if the child is already located somewhere in the tree
			DependentComponentNode found = contains(mapId);
			if ( found == null) {
				children.put(mapId, child);
				child.setParent(this);
				System.out.println("HC " + hashCode() + " add " + child.getOldName()+ " HC " + child.hashCode()) ;
			}
			else {
				//link to the existing component instead
				children.put(mapId, found);
				found.setParent(this);
				//override the child so it should be destroyed
				realChild = found;
				System.out.println("HC " + hashCode() + " Link to existing node " + child.getOldName()+ " HC " + child.hashCode()) ;
			}
		}
		return realChild;
	}
	
	public Integer generateComponentKey() {
		//could we just use the hashCode of the object?
		//generate our own Key class?
		//Generate our own DCN type Enum to make the ordinal limit < 10
		return (importComponent.getId() * 100) + type.ordinal();
	}
	
	public TreeMap<Integer, DependentComponentNode> getChildren() {
		return children;
	}

	public void setNameClashesFound(Boolean nameClashesFound) {
		this.nameClashesFound = nameClashesFound;
	}

	public Boolean getNameClashesFound() {
		return nameClashesFound;
	}

	/**
	 * Walk the tree of this component to see
	 * if there are any name clashes here or in any
	 * of its descendants 
	 * @return
	 */
	public Boolean getAnyNameClashesFound() {
		Boolean found = nameClashesFound;

		Iterator<DependentComponentNode> itr = children.values().iterator();
		while(found == false && itr.hasNext()) {
			DependentComponentNode dcn = itr.next();
			found = dcn.getAnyNameClashesFound();
			if (found == true) {
				System.out.println("Found Name clash on " + dcn.getOldName());
			}
		}
		return found;
	}

	/**
	 * A node can have many parents
	 * 
	 * @return
	 */
	public TreeMap<Integer, DependentComponentNode>  getParents() {
		return parents;
	}

	/**
	 * The DependentComponentNode form a graph and a given node
	 * may have many parents.
	 * 
	 * Just make sure we are not duplicating
	 * 
	 * So we keep them in a collection. 
	 * (Not sure we need a TreeMap as we do for children .. but for the moment)
	 *  
	 * @param parent
	 */
	public void setParent(DependentComponentNode parent) {

		//		if (parents.get(parent.getTxfId()) == null) {
		parents.put(parent.getTxfId(), parent);
		//		}
		System.out.println(this.getOldName() + " added parent " + parent.getOldName()) ;
	}

	private void generateSuggestedName(String name) {
		// here we should generate a new suggested replacement name
		// get the current name 
		// look for _x at the end - where x is a digit
		// if found get x and increment
		//	create new name
		// else
		//  append _1

		int ndx = name.lastIndexOf('_');
		if (ndx > 0) {
			//case of name ending in _ ?
			//case of name ending in _x?
			//case of name ending in _x..x?
			//case of name ending in _n..n?

			ndx++;
			String num = name.substring(ndx, name.length());
			//is it numeric?
			try  
			{  
				Integer d = Integer.parseInt(num); 
				// must be an integer so increment and append.
				d = d + 1;
				newName = name.substring(0,ndx) + d.toString();
			}  
			catch(NumberFormatException nfe)  
			{  
				newName = name +  "_1";
			}  
		}
		else {
			newName = name +  "_1";
		}

	}

	/**
	 * Does the tree contain a DCN with id provided?
	 * 
	 * @param id
	 * @return DependentComponentNode
	 */
	public DependentComponentNode contains(Integer id) {
		return root.find(id);
	}

	/**
	 * @param id - component to find
	 * @return - the component - or null if not found
	 */
	private DependentComponentNode find(Integer id) {
		DependentComponentNode found = null;
		if (generateComponentKey().equals(id)) {
			found = this;
		}

		//recurse through tree
		Iterator<DependentComponentNode> itr = children.values().iterator();
		while(found == null && itr.hasNext()) {
			DependentComponentNode dcn = itr.next();
			found = dcn.find(id);
		}
		return found;
	}

	/**
	 * Because we are using statics we need to walk the tree and release
	 * the memory held by the nodes
	 * 
	 */
	public void releaseAll() {

	}

	/**
	 * Function called on a root component to perform name checks
	 * Internally check to make sure called from a root DCN
	 * 
	 * Walks the tree of components to check each component
	 * use the breadcrumbs to avoid re-checking same node in graph
	 * @throws DAOException 
	 */
	public Boolean dbNameCheckAll() throws DAOException {
		Boolean retval = false;
		if (parents.size() > 0) {
			System.out.println("dbNameCheckAll should only be called on a root DCN");
		}
		else {
			retval = true;

			dbNameCheck();
			sweepBreadCrumbs();
		}
		return retval;

	}
	// Descendants to implement the functions below - to follow state flow

	/**
	 * Iterate through the child components create DCNs for them
	 * and add them as children of the this node.
	 * 
	 * @throws DAOException
	 */
	public abstract void buildDependentsTree(DependentComponentNode parent) throws DAOException;

	/**
	 * Called once the tree has been built to look for DB name clashes
	 */
	public abstract void dbNameCheck() throws DAOException;

	/**
	 *  Component specific function to handle the case where
	 *  the user decided to use the existing db component
	 *  
	 *  So fix up references etc
	 * @param parent 
	 *  
	 * @throws DAOException
	 * @throws SAFRValidationException 
	 */
	public abstract void useExisting(DependentComponentNode parent) throws DAOException, SAFRValidationException;

	/**
	 * Called when a user has finished deciding on which components to use
	 * pass in parent so can refer back
	 * 
	 * again a root only function
	 * 
	 * Need to walk the tree of components 
	 * 
	 * @throws DAOException 
	 * @throws SAFRValidationException 
	 */
	public void processAllDecisions() throws DAOException, SAFRValidationException {
		if (parents.size() > 0) {
			System.out.println("dbNameCheckAll should only be called on a root DCN");
		}
		else {
			sweepBreadCrumbs();
			processDecision(null);
			sweepBreadCrumbs();
		}
	}

	public void processDecision(DependentComponentNode parent) throws DAOException, SAFRValidationException {
		Map<Integer, SAFRTransfer> nameChangeMap = null;
		Class<? extends SAFRTransfer> compClass = importComponent.getClass(); 

		//Don't need to do this the importedComponent record is this
		nameChangeMap = records.get(compClass);


		//		SAFRTransfer txfr = null; 

		//		txfr = nameChangeMap.get(dcn.getTxfId());

		// Need to check if we already have an entry for this name
		if(nameChangeMap != null) {
			Map<String, DependentComponentNode> compDecidedMap = decidedComponents.get(compClass);
			// If we have already decided on this component remove it 
			// and use the already decided component?
			if ( compDecidedMap != null ) {
				DependentComponentNode component = compDecidedMap.get(getOldName());
				if (component != null ){
					nameChangeMap.remove(getTxfId());
					if(component.getUseExisting() == true) {
						nameChangeMap.put(getTxfId(), component.getExisitingComponent());
					}
					else {
						nameChangeMap.put(getTxfId(), component.getImportComponent());
					}
				}
			}

				// not sure this is correct since we may need the record
				//probably should do nothing and switch the if logic
//				nameChangeMap.remove(getTxfId());
//			}
			{
				//call super buildDiplicateTree underlying component we need
				//top level first
				// for views this cannot be null? Unless we stuff up the id
				// then something has gone seriously wrong
				//if ( txfr != null ) {
				if(getUseExisting() == true)
				{
					System.out.println("use existing component " + compClass.toString() + " ID " + getExistingTxfId() + ":" + getOldName() );

					// Component specific function to handle useExisting 
					useExisting(parent);
					nameChangeMap.remove(getTxfId());
					if (existingComponent != null ) {
						getExisitingComponent().setForImport(true); // make sure the replacement component is treated as for import
						//replace the record with the db record
						nameChangeMap.put(getExistingTxfId(), getExisitingComponent());
					}
					else {
						//this is the case where a component on the XML if overridden by the decision
						//to use an existing parent component
						//so this component should be removed from the records set (it is above)
						//and all references to the XML component are to be replaced
						//to refer to the database component
						orphanedComponent();
					}
				}
				else {
					// Rename the XML component - can't be a null name
					if (getNewName() != null && getNewName().length() > 0) {
						importComponent.setName(getNewName());
						System.out.println("Changed " + compClass.getSimpleName() + " " + getOldName() + " to " + getNewName());
					}
					else {
						System.out.println("Null or empty name found for " + compClass.getSimpleName() + " " + getOldName() );
					}

					// we should get rid of the existing component from the DCN?

					//
				}
				if(getRemember() == true) {
					//remember decision
					Map<String, DependentComponentNode> compDecided = decidedComponents.get(compClass);

					//Do we already have decisions for the component type?
					if (compDecided == null) {
						compDecided = new HashMap<String, DependentComponentNode>();
					} 
					compDecided.put(getOldName(), this);
					decidedComponents.put(compClass, compDecided);
				}
			}
		}
		//reset the flag since we should have renamed or set to use existing
		setNameClashesFound(false);
	}

	public void orphanedComponent() throws SAFRValidationException {
		Class<? extends SAFRTransfer> compClass = importComponent.getClass(); 
		System.out.println("Base call to replace Orphaned " + compClass.getSimpleName() + " " + getOldName() + " with DB version ");
	}

	public Integer getMatchingDBLF(Integer lfID) {
		System.out.println("Whilst I may be your parent I am not interested in your well being at this time... get your mummy to find LF " + lfID);	
		return 0;
	}

	public Integer getMatchingDBPF(Integer pfID) {
		System.out.println("Whilst I may be your parent I am not interested in your well being at this time... get your daddy to find PF " + pfID);	
		return 0;
	}

}
