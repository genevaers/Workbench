package com.ibm.safr.we.model.utilities.export;

/*
 * Copyright Contributors to the GenevaERS Project.
								SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation
								2023
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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.ExportElementType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.data.DAOFactoryHolder;
import com.ibm.safr.we.data.transfer.DependentComponentTransfer;
import com.ibm.safr.we.data.transfer.ViewFolderViewAssociationTransfer;
import com.ibm.safr.we.data.transfer.ViewTransfer;
import com.ibm.safr.we.data.transfer.XMLTableDataTransfer;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.Folder;
import com.ibm.safr.we.model.ModelUtilities;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.Views;
import com.ibm.safr.we.model.base.SAFRObject;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.utilities.TaskLogger;
import com.ibm.safr.we.ui.dialogs.ExportInactiveDialog;
import com.ibm.safr.we.ui.editors.ExportUtilityEditor;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.FileUtils;
import com.ibm.safr.we.utilities.ProfileLocation;

public class ExportUtility extends SAFRObject {

    Date date = new Date();
	static transient Logger logger = Logger
	.getLogger("com.ibm.safr.we.model.utilities.ExportUtility");
	
	static private final String ROOT_TAG = "safrxml";
    static private final String NOT_SPECIFIED = "[not specified]";
	
	private TaskLogger taskLogger = new TaskLogger(logger, "Export"); // CQ10366
	
	private EnvironmentQueryBean environment; // added for CQ10366
	private String exportPath;
	private ComponentType componentType;

	private int indent = 0;
	private String fileName = null;
	private OutputStream xmlStream;
	private String rootElement;
	Map<ComponentType, List<DependentComponentTransfer>> depComponentTransferMap = new LinkedHashMap<ComponentType, List<DependentComponentTransfer>>();
	private String encoding = null;
	private String linebreakString = SAFRUtilities.LINEBREAK;
	private boolean viewId;
	private boolean viewNameViewId;
	private boolean folderId;
	private boolean folderNameFolderId;
	private boolean multiple;
	public boolean result=true;
	/**
	 * Use this constructor to create an Export Utility object to export a
	 * component to XML file.
	 * 
	 * @param environment
	 *            the EnvironmentQueryBean object representing the environment
	 *            in which the component to be exported is present.
	 * @param exportPath
	 *            The Path of the folder where the XML file will be created. If
	 *            the path doesn't exist, it will created automatically.<br>
	 *            <i>Note:</i> The XML file is not created directly under this
	 *            path. There will another subfolder created under this path and
	 *            the XML file will be created in this subfolder. The name of
	 *            this subfolder depends on what type of component is selected.
	 * @param componentType
	 *            : The type of the component to be exported.It can be one of
	 *            Physical File, Logical File,Logical Record,Lookup Path,View.
	 * @param b 
	 */
	public ExportUtility(EnvironmentQueryBean environment, String exportPath, String fileName,
			ComponentType componentType, boolean viewId, boolean viewNameViewId, boolean folderId, boolean folderNameFolderId, boolean multiple) {
		super();
		this.environment = environment;
		this.exportPath = exportPath;
		this.fileName = fileName;
		this.componentType = componentType;
		this.viewId = viewId;
		this.viewNameViewId = viewNameViewId;
		this.folderId = folderId;
		this.folderNameFolderId = folderNameFolderId;
		this.multiple = multiple;
		StringBuffer buffer = new StringBuffer();
		buffer.append("Environment    = " + (environment != null ? environment.getDescriptor() : NOT_SPECIFIED));
		buffer.append(LINEBREAK + "Component Type = " + (componentType != null ? componentType.getLabel() : NOT_SPECIFIED));
		buffer.append(LINEBREAK + "File Location  = " + (exportPath != null ? exportPath : NOT_SPECIFIED));
		taskLogger.logInfo("Export Parameters:", null, buffer.toString());
	}

	/**
	 * Set the encoding to be written at the start of the generated XML file
	 * e.g. "IBM-1047" for zOS
	 * 
	 * @param encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Export the selected component to an XML file.The token will not be
	 * checked in validate because all the validations are either of type error.
	 * Validates all the Components selected for export and adds the errors to
	 * the list if any.
	 * 
	 * @throws DAOException
	 * @throws SAFRValidationException
	 * 
	 * @throws IOException
	 *             : Gives an IOEXception when there is some problem while
	 *             generating an XML file or when the specified file path is not
	 *             found.
	 */
	public void export(List<ExportComponent> exportCompList,Shell shell)
			throws SAFRValidationException {
		try {
	        validateParms();
        	String totalpath="";
	        if(this.componentType.getLabel().equals("View Folder")) {
            		 if (multiple) {
                         exportEachComponent(exportCompList,shell);
                     } else {
     	        		if(checkfileexists(totalpath,shell)==true) {
		                    if(validateExport(exportCompList)) {
		                    exportComponents(exportCompList, fileName);
	                    }
                     }
	        }
	        }
	        else if(this.componentType.getLabel().equals("View")) {
	        	if(multiple) {
	        		exportEachComponent(exportCompList,shell);
	        	}
	        	else {
	        		if(validateExport(exportCompList)) {
	            		if (viewId) {
	                         fileName ="V"+String.format("%07d",exportCompList.get(0).getComponent().getId());
	                    }
	                    if (viewNameViewId) {
	                         fileName = exportCompList.get(0).getComponent().getName() + "[" + exportCompList.get(0).getComponent().getId() + "].xml";
	                    }    
	                    totalpath = exportPath.toString()  + "\\" + fileName.toString();
	                    if(checkfileexists(totalpath,shell)==true) {
		                    exportComponents(exportCompList, fileName);
	                    }
	                }
	                }
	        	}

		} catch (SAFRValidationException sve) {
			if (!taskLogger.isAlreadyRecorded(sve)) {
				taskLogger.logError("Export error:", sve);
			}
			throw sve;
		} finally {
			// log any export component errors
			StringBuffer msgBuffer = new StringBuffer();
			for (ExportComponent comp : exportCompList) {
				ActivityResult result = comp.getResult();
				if (result != null && !result.equals(ActivityResult.PASS)) {
					msgBuffer.setLength(0); // clear the buffer
					msgBuffer.append("Message:   ");
					for (String msg : comp.getErrors()) {
						msgBuffer.append(LINEBREAK + msg);
					}
					taskLogger.logError("Component: "
							+ comp.getComponent().getDescriptor(),
							"Result:    " + comp.getResult().getLabel(),
							msgBuffer.toString());
				}
			}
			
			// write messages to log file
			taskLogger.writeLog(true);
		}
	}

    private void exportEachComponent(List<ExportComponent> exportCompList, Shell shell) {
    	String fileName = null;
    	List<ExportComponent> singleComp = new ArrayList<ExportComponent>();
    	String totalpath="";
        for (ExportComponent component : exportCompList) {
            singleComp.add(component);
            if(validateExport(singleComp)) {
            if (component.getComponent().getComponentType().toString() == "View" && viewId) {
                fileName ="V"+String.format("%07d",component.getComponent().getId());
                totalpath = exportPath.toString()  + "\\" + fileName.toString();
                if(checkfileexists(totalpath,shell)==true) {
                    exportComponents(singleComp, fileName);
                }
                
            }
            if(component.getComponent().getComponentType().toString() == "ViewFolder" && folderId) {
            	fileName ="F"+String.format("%07d",component.getComponent().getId());
            	totalpath = exportPath.toString()  + "\\" + fileName.toString();
            	if(checkfileexists(totalpath,shell)==true) {
                    exportComponents(singleComp, fileName);
                }
            }
            if(component.getComponent().getComponentType().toString() == "ViewFolder" && folderNameFolderId) {
                fileName = component.getComponent().getName() + "[" + component.getComponent().getId() + "].xml";
                totalpath = exportPath.toString()  + "\\" + fileName.toString();
                if(checkfileexists(totalpath,shell)==true) {
                    exportComponents(singleComp, fileName);
                }
            }
            if (component.getComponent().getComponentType().toString() == "View" && viewNameViewId) {
                fileName = component.getComponent().getName() + "[" + component.getComponent().getId() + "].xml";
                totalpath = exportPath.toString()  + "\\" + fileName.toString();
                if(checkfileexists(totalpath,shell)==true) {
                    exportComponents(singleComp, fileName);
                }
            }    
            }
            singleComp.clear();
        }
    }
   	
    private boolean checkfileexists(String path,Shell shell) {
		// TODO Auto-generated method stub
    	File f = new File(path);
    	boolean result = true;
        if(f.exists()) {
        	result = MessageDialog.openQuestion(shell, "Confirm Save As",
        			f.getName() + " already exists." + "\n" + "Do you want to replace it?");
        }        
        return result;
	}

	
    protected boolean validateExport(List<ExportComponent> exportCompList) {
        StringBuffer compBuffer = new StringBuffer();
        // log the list of export components
        if (exportCompList.size() > 0) {
        	for (ExportComponent exportComponent : exportCompList) {
        		compBuffer.append(exportComponent.getComponent()
        				.getDescriptor() + LINEBREAK);
        	}
        	compBuffer.delete(compBuffer.lastIndexOf(LINEBREAK),
        			compBuffer.length());
        } else {
        	compBuffer.append(NOT_SPECIFIED);
        }
        taskLogger.logInfo("Export components:", null,compBuffer.toString());
        taskLogger.writeLog();
        taskLogger.clearAll();
        
        // export each component.
        depComponentTransferMap.clear();
        boolean canExport = false;
        List<Integer> folderIds=new ArrayList<>();
        for (ExportComponent exportComponent : exportCompList) {
        	folderIds.add(exportComponent.getComponent().getId());
            exportComponent.setResult(ActivityResult.CANCEL);
        }
 	   	result = determineInactiveViewsFromFolders(environment.getId(),folderIds);
 	   	if(result){
        for (ExportComponent exportComponent : exportCompList) {
        	// clear the existing errors, if any, first.
        	exportComponent.getErrors().clear();
        	// get the list of components which have security restriction on
        	// dependent components.
        	List<String> errList = new ArrayList<String>();
        	try {
        		// Add the export dependencies
        	    Map<ComponentType, List<DependentComponentTransfer>> newDeps = DAOFactoryHolder
                    .getDAOFactory()
                    .getExportDAO()
                    .getComponentDependencies(this.componentType,
                            exportComponent.getComponent().getId(),
                            environment.getId());
        		
        	    if (componentType.equals(ComponentType.View)) {
        	        newDeps.remove(ComponentType.View);
        	    }
        	    
                for (ComponentType type : newDeps.keySet()) {
                    if (depComponentTransferMap.get(type) == null) {
                        depComponentTransferMap.put(type, new ArrayList<DependentComponentTransfer>());
                    }
                    depComponentTransferMap.get(type).addAll(newDeps.get(type));			            
                }
	              
        		if (!SAFRApplication.getUserSession().isSystemAdministrator()) {
        			// Check user rights on export components
        			errList = checkComponentSecurity(exportComponent
        					.getComponent().getId());
        			
                    // if no dependency found then store id to export. and set the
                    // result as Pass.
                    if (errList != null && !errList.isEmpty()) {
                        // if dependency found then set the list of errors to pass
                        // to UI and set the result as Load errors.
                        exportComponent.setErrors(errList);
	                        if(!exportComponent.getResult().equals(ActivityResult.FAIL)) {
                        exportComponent.setResult(ActivityResult.LOADERRORS);
	                        }
                        canExport = false;
                    }						
        		}
	        		exportComponent.setResult(ActivityResult.PASS);
	                canExport = true;
        	} catch (DAOException de) {
        		storeError(exportComponent, de);
        		logStackTrace(exportComponent, de);
        		canExport = false;
        	} 

        } // end check loop
 	   	}
        return canExport;
 	   	
 	   	}

	private boolean determineInactiveViewsFromFolders(int environmentid,List<Integer> folderIds) {
		

		List<ViewFolderViewAssociationTransfer> inactiveViewInFoldersList = DAOFactoryHolder.getDAOFactory().getViewFolderDAO().getinactiveviewinfolders(environmentid, folderIds);
		List<Views> list = new ArrayList<>();
		for(ViewFolderViewAssociationTransfer vf : inactiveViewInFoldersList) {
			Folder f = new Folder(vf.getAssociatingComponentId(),vf.getAssociatingComponentName());
			list.add(new Views(f,vf.getAssociatedComponentId(),vf.getAssociatedComponentName()));
		}

		if(list.size()!=0) {
			ExportInactiveDialog areaDialog = new ExportInactiveDialog(ExportUtilityEditor.shell, list);
			int open = areaDialog.open();
			if (open == Dialog.OK) {
				result = true;
			} else {
				result = false;
			}
		}
		return result;
    }

    private void exportComponents(List<ExportComponent> exportComponents, String fileName) {
        try {
            xmlStream = openFile(exportComponents, fileName);				        
        	if (encoding != null) {
        		xmlStream.write(new String(
        				"<?xml version=\"1.0\" encoding=\"" + encoding
        						+ "\"?>" + linebreakString).getBytes());
        	}
        	rootElement = ROOT_TAG;
        	xmlStream.write(openElement(rootElement, true, true).getBytes());
        	
            // export generation record
        	exportGeneration();
        	List<Integer> idList = new ArrayList<Integer>();
        	for (ExportComponent exportComponent : exportComponents) {
        	    idList.add(exportComponent.getComponent().getId());
        	}        	    

        	exportMetadata(componentType, idList, environment.getId());
        	// export dependent components.
            // export Views
            retrieveComponentFromMap(ComponentType.View);
        	
        	// export Lookup Paths
        	retrieveComponentFromMap(ComponentType.LookupPath);

        	// export Logical Records
        	retrieveComponentFromMap(ComponentType.LogicalRecord);

        	// export Logical Files
        	retrieveComponentFromMap(ComponentType.LogicalFile);

        	// export Physical Files
        	retrieveComponentFromMap(ComponentType.PhysicalFile);

        	// export User-Exit Routines
        	retrieveComponentFromMap(ComponentType.UserExitRoutine);

        	// export Control Record
        	retrieveComponentFromMap(ComponentType.ControlRecord);

        	retrieveComponentFromMap(ComponentType.ViewFolder);

        	xmlStream.write(closeElement(rootElement, true, true).getBytes());
        } catch (IOException ioe) {
        	storeError(exportComponents, ioe);
        	logStackTrace(exportComponents, ioe);
        } catch (DAOException de) {
        	storeError(exportComponents, de);
        	logStackTrace(exportComponents, de);
        } finally {
        	try {
        		closeFile();
        	} catch (IOException ioe) {
        		storeError(exportComponents, ioe); 
        		logStackTrace(exportComponents, ioe);
        	}
        }
        for (ExportComponent exportComponent : exportComponents) {
            if (exportComponent.getErrors().isEmpty()) {
                exportComponent.setResult(ActivityResult.PASS);
            }
        }
    }

	private void exportGeneration() throws IOException {
        xmlStream.write(openElement("Generation", true, true).getBytes());

        xmlStream.write(openElement("Record", true, true).getBytes());
        
        xmlStream.write(openElement("XMLVERSION", true, false).getBytes());
        xmlStream.write("3".getBytes());
        xmlStream.write(closeElement("XMLVERSION", false, true).getBytes());

        xmlStream.write(openElement("TYPE", true, false).getBytes());
        xmlStream.write(getComponentString().getBytes());
        xmlStream.write(closeElement("TYPE", false, true).getBytes());
        
        xmlStream.write(openElement("PROGRAM", true, false).getBytes());
        xmlStream.write(("Workbench " + SAFRUtilities.getWEVersion()).getBytes());
        xmlStream.write(closeElement("PROGRAM", false, true).getBytes());
        
        xmlStream.write(openElement("FILENAME", true, false).getBytes());
        xmlStream.write(fileName.getBytes());
        xmlStream.write(closeElement("FILENAME", false, true).getBytes());
        
        xmlStream.write(openElement("CREATEDTIMESTAMP", true, false).getBytes());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //Date date = new Date();
        Date samedate = date;
        xmlStream.write(dateFormat.format(samedate).getBytes());
        xmlStream.write(closeElement("CREATEDTIMESTAMP", false, true).getBytes());

        xmlStream.write(openElement("CREATEDUSERID", true, false).getBytes());
        xmlStream.write(SAFRApplication.getUserSession().getUser().getUserid().getBytes());
        xmlStream.write(closeElement("CREATEDUSERID", false, true).getBytes());
                
        xmlStream.write(closeElement("Record", true, true).getBytes());
        
        xmlStream.write(closeElement("Generation", true, true).getBytes());
    }

    private void retrieveComponentFromMap(ComponentType type)
			throws DAOException, IOException {
    	if(result) {
    		retrieveComponentFromMapFilter(type);
    	}
    	else {
    		List<DependentComponentTransfer> depList = depComponentTransferMap.get(type);
    		List<Integer> idList = new ArrayList<Integer>();
    		if (depList != null) {
    			for (DependentComponentTransfer depComTrans : depList) {
    				idList.add(depComTrans.getId());
    			}
    			exportMetadata(type, idList, environment.getId());
    		}
    	}
	}
    
    public void retrieveComponentFromMapFilter(ComponentType type)
			throws DAOException, IOException {
    	List<DependentComponentTransfer> depList = depComponentTransferMap.get(type);
		List<Integer> idList = new ArrayList<Integer>();
		ViewTransfer viewTransfer = null;
		if (depList != null) {
			for (DependentComponentTransfer depComTrans : depList) {
				if(type.getLabel().equals("View")) {
					viewTransfer = DAOFactoryHolder.getDAOFactory().getViewDAO().getView(depComTrans.getId(), this.environment.getId());
					if(viewTransfer!=null && viewTransfer.getStatusCode().equals("ACTVE")) {
				idList.add(depComTrans.getId());
			}
				}
				else {
					idList.add(depComTrans.getId());
				}
			}
			exportMetadata(type, idList, environment.getId());
		}
	}


	private void exportMetadata(ComponentType type, List<Integer> idList,
			Integer environmentId) throws DAOException, IOException {
        if (type == ComponentType.ControlRecord) {
			exportControlRecord(environmentId, idList);
		} else if (type == ComponentType.UserExitRoutine) {
			exportUserExitRoutine(environmentId, idList);
		} else if (type == ComponentType.PhysicalFile) {
			exportPhysicalFile(environmentId, idList);
		} else if (type == ComponentType.LogicalFile) {
			exportLogicalFile(environmentId, idList);
		} else if (type == ComponentType.LogicalRecord) {
			exportLogicalRecord(environmentId, idList);
		} else if (type == ComponentType.LookupPath) {
			exportLookupPath(environmentId, idList);
		} else if (type == ComponentType.View) {
			exportView(environmentId, idList);
		} else if (type == ComponentType.ViewFolder) {
            exportViewFolder(environmentId, idList);
        }
	}

    private void exportViewFolder(Integer environmentId, List<Integer> componentIds)
        throws DAOException, IOException {
        Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> compDataMap = DAOFactoryHolder
                .getDAOFactory().getExportDAO().getViewFolderData(environmentId,componentIds);
        writeComponentData(compDataMap);
    }
	
    private void exportView(Integer environmentId, List<Integer> componentIds)
			throws DAOException, IOException {
		Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> compDataMap = DAOFactoryHolder
				.getDAOFactory().getExportDAO().getViewData(environmentId,
						componentIds);
		writeComponentData(compDataMap);

	}

	private void exportLookupPath(Integer environmentId,
			List<Integer> componentIds) throws DAOException, IOException {
		Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> compDataMap = DAOFactoryHolder
				.getDAOFactory().getExportDAO().getLookupPathData(
						environmentId, componentIds);
		writeComponentData(compDataMap);
	}

	private void exportLogicalFile(Integer environmentId,
			List<Integer> componentIds) throws IOException, DAOException {
		Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> compDataMap = DAOFactoryHolder
				.getDAOFactory().getExportDAO().getLogicalFileData(
						environmentId, componentIds);
		writeComponentData(compDataMap);
	}

	private void exportUserExitRoutine(Integer environmentId,
			List<Integer> componentIds) throws DAOException, IOException {
		Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> compDataMap = DAOFactoryHolder
				.getDAOFactory().getExportDAO().getUserExitRoutineData(
						environmentId, componentIds);
		writeComponentData(compDataMap);
	}

	private void exportControlRecord(Integer environmentId,
			List<Integer> componentIds) throws DAOException, IOException {
		Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> compDataMap = DAOFactoryHolder
				.getDAOFactory().getExportDAO().getControlRecordData(
						environmentId, componentIds);
		writeComponentData(compDataMap);
	}

	private String getComponentString() {
		if (componentType == ComponentType.PhysicalFile) {
			return "PhysicalFile";
		} else if (componentType == ComponentType.LogicalFile) {
			return "LogicalFile";
		} else if (componentType == ComponentType.LogicalRecord) {
			return "LogicalRecord";
		} else if (componentType == ComponentType.LookupPath) {
			return "Lookup";
		} else if (componentType == ComponentType.View) {
            return "View";
        } else if (componentType == ComponentType.ViewFolder) {
			return "ViewFolder";
		} else {
		    return "";
		}
	}

	private void exportPhysicalFile(Integer environmentId,
			List<Integer> componentIds) throws DAOException, IOException {
		Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> compDataMap = DAOFactoryHolder
				.getDAOFactory().getExportDAO().getPhysicalFileData(
						environmentId, componentIds);
		writeComponentData(compDataMap);
	}

	private void exportLogicalRecord(Integer environmentId,
			List<Integer> componentIds) throws DAOException, IOException {
		Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> compDataMap = DAOFactoryHolder
				.getDAOFactory().getExportDAO().getLogicalRecordData(
						environmentId, componentIds);
		writeComponentData(compDataMap);
	}

	private void writeComponentData(
			Map<ExportElementType, Map<Integer, List<XMLTableDataTransfer>>> componentDataMap)
			throws IOException {
		// get data to be exported from Datalayer by calling GP_ARCHLR
		// the data layer should return data in a form of a MAP.
		// the key of this map will be 'type' of data
		// value will be another MAP, whose key will be a number and value will be
		// a list of XMLTableDataTransfer objects.
		for (ExportElementType type : componentDataMap.keySet()) {
			// write this key to XML as parent element eg. this could be <LR-File>
			// Don't write empty tags (which don't contain any records) to XML
			if (!componentDataMap.get(type).isEmpty()) {
				xmlStream.write(openElement(type.getXmlString(), true,true).getBytes());
				Map<Integer, List<XMLTableDataTransfer>> records = componentDataMap.get(type);
				// loop through all the records in this key
				for (Integer i : records.keySet()) {
					// write a <Record> element under the above parent element.
					xmlStream.write(openElement("Record", true, true).getBytes());
					for (XMLTableDataTransfer tableRecord : records.get(i)) {
						// check for cdata
						if (tableRecord.isCdata()) {
							// write this table record inside the above <Record> element.
							xmlStream.write(openCdataElement(tableRecord.getName().toUpperCase(), true, false).getBytes());
							if (tableRecord.getValue() != null) {
								// normalize line endings
								String cdstr =  FileUtils.fixCdataLineEndings(tableRecord.getValue());
								xmlStream.write(cdstr.getBytes());
							}
							xmlStream.write(closeCdataElement(tableRecord.getName().toUpperCase(), false, true).getBytes());							
						}
						else {
							// write this table record inside the above <Record>
							// element.
							xmlStream.write(openElement(tableRecord.getName().toUpperCase(),
									true, false).getBytes());
							if (tableRecord.getValue() != null) {
								// handle special characters and write the data
								xmlStream.write(handleSpecialChars(
										tableRecord.getValue()).getBytes());
							}
							xmlStream.write(closeElement(tableRecord.getName().toUpperCase(),
									false, true).getBytes());
						}
					}
					xmlStream.write(closeElement("Record", true, true)
							.getBytes());
				}
				xmlStream.write(closeElement(type.getXmlString(), true,
						true).getBytes());
			}
		}

	}

	/**
	 * Generates a string representing an xml open element. eg. /<Person/><![CDATA[. This
	 * function will also increment the <code>indent</code> variable with '4'.
	 * This means that the next open element will be indented with 4 spaces on
	 * left.
	 * 
	 * @param element
	 *            using which the open element String is to be generated.
	 * @param indented
	 *            boolean. If the returned string is to be indented based on
	 *            previous element.
	 * @param lineBreak
	 *            boolean. If a line break is to be appended to the returned
	 *            string.
	 * @return indented xml open element string.
	 */
	private String openCdataElement(String element, boolean indented,
			boolean lineBreak) {
		String returnStr = "<" + element + "><![CDATA[";
		if (indented) {
			returnStr = genSpaces(indent) + returnStr;
			indent += 4;
		}
		if (lineBreak ) {
			returnStr += linebreakString;
		}
		return returnStr;
	}
	
	/**
	 * Generates a string representing an xml open element. eg. /<Person/>. This
	 * function will also increment the <code>indent</code> variable with '4'.
	 * This means that the next open element will be indented with 4 spaces on
	 * left.
	 * 
	 * @param element
	 *            using which the open element String is to be generated.
	 * @param indented
	 *            boolean. If the returned string is to be indented based on
	 *            previous element.
	 * @param lineBreak
	 *            boolean. If a line break is to be appended to the returned
	 *            string.
	 * @return indented xml open element string.
	 */
	private String openElement(String element, boolean indented,
			boolean lineBreak) {
		String returnStr = "<" + element + ">";
		if (indented) {
			returnStr = genSpaces(indent) + returnStr;
			indent += 4;
		}
		if (lineBreak ) {
			returnStr += linebreakString;
		}
		return returnStr;
	}

	/**
	 * Generates a string representing an xml close element. eg. /</Person/>.
	 * This function will also decrement the <code>indent</code> variable with
	 * '4'. This means that the next close element will be indented with -4
	 * spaces on left.
	 * 
	 * @param element
	 *            using which the close element String is to be generated.
	 * @param indented
	 *            boolean. If the returned string is to be indented based on
	 *            previous element.
	 * @param lineBreak
	 *            boolean. If a line break is to be appended to the returned
	 *            string.
	 * @return indented xml close element string.
	 */
	private String closeElement(String element, boolean indented,
			boolean lineBreak) {
		String returnStr = "</" + element + ">";
		indent -= 4;
		if (indented) {
			returnStr = genSpaces(indent) + returnStr;
		}
		if (lineBreak ) {
			returnStr += linebreakString;
		}
		return returnStr;
	}

	/**
	 * Generates a string representing an xml close element. eg. /</Person/>.
	 * This function will also decrement the <code>indent</code> variable with
	 * '4'. This means that the next close element will be indented with -4
	 * spaces on left.
	 * 
	 * @param element
	 *            using which the close element String is to be generated.
	 * @param indented
	 *            boolean. If the returned string is to be indented based on
	 *            previous element.
	 * @param lineBreak
	 *            boolean. If a line break is to be appended to the returned
	 *            string.
	 * @return indented xml close element string.
	 */
	private String closeCdataElement(String element, boolean indented,
			boolean lineBreak) {
		String returnStr = "]]></" + element + ">";
		indent -= 4;
		if (indented) {
			returnStr = genSpaces(indent) + returnStr;
		}
		if (lineBreak ) {
			returnStr += linebreakString;
		}
		return returnStr;
	}
	
	/**
	 * This method is used to generate spaces.For eg. if the parameter is given
	 * as 2, then this method returns a string having 2 spaces("  ").
	 * 
	 * @param num
	 *            : The number of spaces to be generated.
	 * @return: a string which comprise of the spaces.
	 */
	private String genSpaces(int num) {
		return ModelUtilities.genChar(num, " ");
	}

	/**
	 * Creates the xml file for writing. This function also creates an output
	 * folder if it doesn't exists. The variable <code>xmlstream</code> will be
	 * initialized with this newly created file.
	 * 
	 * @param type
	 *            the component type for which the file is to be created. This
	 *            is used to create an output folder.
	 * @throws IOException
	 */
	private OutputStream openFile(List<ExportComponent> exportComponents, String fileName) {
		File outputFile = new File(exportPath);
		outputFile.mkdir(); // create the required directory
		outputFile = new File(exportPath + "/" + fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e) {
			UIUtilities.handleWEExceptions(e, "Unable to open file", fileName);
		}
		return fos;
	}
	
	public static String getDefaultLocation(ComponentType componentType) {
	    String defLoc = ProfileLocation.getProfileLocation().getLocalProfile() 
	        + "xml\\" + getFolderName(componentType);
	    return defLoc;
	}

    protected static String getFolderName(ComponentType componentType) {
        String compName = "";
        if (componentType == ComponentType.PhysicalFile) {
            compName = "PhysicalFiles";
        } else if (componentType == ComponentType.LogicalFile) {
            compName = "LogicalFiles";
        } else if (componentType == ComponentType.LogicalRecord) {
            compName = "LogicalRecords";
        } else if (componentType == ComponentType.LookupPath) {
            compName = "LookupPaths";
        } else if (componentType == ComponentType.View) {
            compName = "Views";
        } else if (componentType == ComponentType.ViewFolder) {
            compName = "ViewFolders";
        }
        return compName;
    }

    
	/**
	 * Closes the XML file and flushes all the data in memory.
	 * 
	 * @throws IOException
	 */
	private void closeFile() throws IOException {
		if (xmlStream != null) {
			xmlStream.flush();
			xmlStream.close();
		}
	}

	/**
	 * This enum maintains the properties of export utility.
	 * 
	 */
	public enum Property {
		ENVIRONMENT, COMPONENT_TYPE, LOCATION, FILENAME
	}

	private void validateParms() throws SAFRValidationException {
		SAFRValidationException safrValidationException = new SAFRValidationException();
		
		if (this.environment == null || this.environment.getId() == 0l) {
			safrValidationException.setErrorMessage(Property.ENVIRONMENT,
					"Environment has not been specified.");
		}
		if (this.componentType == null) {
			safrValidationException.setErrorMessage(Property.COMPONENT_TYPE,
					LINEBREAK + "Component Type has not been specified.");
		}
		if (this.exportPath == null || this.exportPath == ""
				|| !isFilePathCorrect(this.exportPath)) {
			safrValidationException.setErrorMessage(Property.LOCATION,
					LINEBREAK + "Location has not been specified or is incorrect.");
		}
		if (!safrValidationException.getErrorMessages().isEmpty()) {
			safrValidationException
					.setSafrValidationType(SAFRValidationType.PARAMETER_ERROR);
			taskLogger.logError("Export parameter error:", safrValidationException);
			throw safrValidationException;
		}
	}

	private Boolean isFilePathCorrect(String filePath) {
		File file = new File(filePath);
		if (file.exists() || file.mkdirs()) {
			// file path exists or is created
			return true;
		} else {
			return false;
		}
	}

	public static boolean isFilenameValid(String file) {
	    File f = new File(file);
	    try {
	      f.getCanonicalPath();
	      return true;
	    } catch (IOException e) {
	      return false;
	    }
	  }	

	private List<String> checkComponentSecurity(Integer componentID) {
		List<String> errorList = null;
		Map<ComponentType, List<DependentComponentTransfer>> tempMap = new LinkedHashMap<ComponentType, List<DependentComponentTransfer>>();

		// loop through the map
		for (ComponentType type : depComponentTransferMap.keySet()) {
			if (type != ComponentType.ControlRecord) {
				List<DependentComponentTransfer> depCompTransList = depComponentTransferMap.get(type);
				for (DependentComponentTransfer depCompTransfer : depCompTransList) {
					// add the components whose edit rights is null, to
					// the
					// temporary map.

					if (depCompTransfer.getEditRights() == EditRights.None) {
						if (tempMap.containsKey(type)) {
							tempMap.get(type).add(depCompTransfer);
						} else {
							List<DependentComponentTransfer> depCompList = new ArrayList<DependentComponentTransfer>();
							depCompList.add(depCompTransfer);
							tempMap.put(type, depCompList);
						}
					}
				}
			}
		}// end of for loop

		// prepare String
		if (!tempMap.isEmpty()) {
			errorList = new ArrayList<String>();
			for (ComponentType compType : tempMap.keySet()) {
				String message = "";
				for (DependentComponentTransfer depComponentTransfer : tempMap
						.get(compType)) {
					message = compType.getLabel() + " : "
							+ depComponentTransfer.getDescriptor() + LINEBREAK;
					errorList.add(message);
				}
			}
		}
		return errorList;
	}

	/**
	 * Converts special characters in a string to XML escape chars.
	 * 
	 * @param input
	 *            the string.
	 * @return modified string.
	 */
	private String handleSpecialChars(String input) {
		String returnStr;
		returnStr = input.replaceAll("&", "&amp;");
		returnStr = returnStr.replaceAll("<", "&lt;");
		returnStr = returnStr.replaceAll(">", "&gt;");
		returnStr = returnStr.replaceAll("\"", "&quot;");
		returnStr = returnStr.replaceAll("'", "&apos;");
		return returnStr;
	}

    private void storeError(List<ExportComponent> comps, Throwable t) {
        for (ExportComponent comp : comps) {
            storeError(comp, t);
        }
    }
	
	private void storeError(ExportComponent comp, Throwable t) {
		Throwable cause;
		if (t.getMessage() != null && t.getMessage() != "") {
			comp.getErrors().add(t.getMessage());
		}
		cause = t.getCause();
		if (cause != null && cause.getMessage() != null
				&& cause.getMessage() != "") {
			comp.getErrors().add(cause.getMessage());
		}
		comp.setResult(ActivityResult.FAIL);
	}
	
    private void logStackTrace(List<ExportComponent> comps, Throwable t) {
        for (ExportComponent comp : comps) {
            logStackTrace(comp, t);
        }        
    }

	private void logStackTrace(ExportComponent comp, Throwable t) {
		logger.log(Level.SEVERE, "Error exporting component "
				+ comp.getComponent().getDescriptor(), t);
	}
	
}
