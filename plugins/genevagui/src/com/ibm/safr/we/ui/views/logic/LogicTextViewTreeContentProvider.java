package com.ibm.safr.we.ui.views.logic;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.CodeCategories;
import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.model.LRField;
import com.ibm.safr.we.model.LookupPathSourceField;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.ui.editors.logic.ECLLogicTextEditorInput;
import com.ibm.safr.we.ui.editors.logic.ERFLogicTextEditorInput;
import com.ibm.safr.we.ui.editors.logic.EROLogicTextEditorInput;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditorInput;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.logic.LogicTextViewTreeNode.TreeItemId;

/**
 *A content provider for Logic Text tree.
 * 
 */
public class LogicTextViewTreeContentProvider implements ITreeContentProvider {

	private LogicTextViewTreeNode logicTextItemsRoot;
	private LogicTextType logicTextType;
	private LogicTextEditorInput logicTextEditorInput;

	/**
	 * This method returns the root element of the model used for the
	 * construction of the tree.
	 * 
	 * @return the root element of the model used for the construction of the
	 *         tree.
	 */
	public LogicTextViewTreeNode getLogicTextItemsRoot() {
		return logicTextItemsRoot;
	}

	/**
	 * This method sets the root element of the model used for the construction
	 * of the tree.
	 * 
	 * @param logicTextItemsRoot
	 *            LogicTextModel to use for the construction of the tree.
	 */
	public void setLogicTextItemsRoot(LogicTextViewTreeNode logicTextItemsRoot) {
		this.logicTextItemsRoot = logicTextItemsRoot;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		LogicTextViewTreeNode item = (LogicTextViewTreeNode) parentElement;
		addChildNodesTo(item);
		if (item.getChildren() == null) {
			return new ArrayList<LogicTextViewTreeNode>().toArray();
		} else {
			return item.getChildren().toArray();
		}
	}

	private void addChildNodesTo(LogicTextViewTreeNode item) {
		try {
			setCursorToWait();
			if (item.getChildren() == null) {
				getNodesSpecificToLogicType(item);
			}
		} finally {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setCursor(null);
		}
	}

	private void getNodesSpecificToLogicType(LogicTextViewTreeNode item) {
		try {
			if (logicTextType == LogicTextType.Extract_Record_Filter) {
				getERFChildren(item);
			} else if (logicTextType == LogicTextType.Extract_Column_Assignment || logicTextType == LogicTextType.Extract_Record_Output) {
				getExtractChildren(item);
			}
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e, "Error in getting contents for Logic Text Helper", null);
		}
	}

	private void setCursorToWait() {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()
				.setCursor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
	}

	private void getExtractChildren(LogicTextViewTreeNode item) {
		ViewSource viewSource = getExtractViewSource();
		addNodesForSource(item, viewSource);
	}

	private void addNodesForSource(LogicTextViewTreeNode item, ViewSource viewSource) {
		if ((item.getId() == TreeItemId.LOOKUPPATHS) || (item.getId() == TreeItemId.LOOKUPSYMBOLS)) {
			addLookupPathAndSymbols(item, viewSource);
		} else if (item.getId() == TreeItemId.LOOKUPPATHS_CHILD) {
			addLookupPathChildNodes(item, viewSource);
		} else if ((item.getId() == TreeItemId.USEREXITROUTINES)) {
			addExtractWriteUserExits(item);
		} else if (item.getId() == TreeItemId.PROCEDURES) {
			addExtractWriteProcedures(item);
		} else if (item.getId() == TreeItemId.FILES) {
			addExtractFiles(item, viewSource);
		} else if (item.getId() == TreeItemId.FILES_CHILDLF) {
			addFileChildNodes(item);
		}
	}

	private void addFileChildNodes(LogicTextViewTreeNode item) {
		List<FileAssociation> lfPfAssociationlist = SAFRAssociationFactory
				.getLogicalFileToPhysicalFileAssociations(
						(LogicalFileQueryBean) item
								.getData())
				.getActiveItems();
		List<LogicTextViewTreeNode> lfChildrenList = new ArrayList<LogicTextViewTreeNode>();
		String lfName = ((LogicalFileQueryBean) item.getData()).getNameLabel();
		if (!lfPfAssociationlist.isEmpty()) {
			for (FileAssociation lfPfAssociation : lfPfAssociationlist) {
				String pfEditorText = "{"
						+ lfName
						+ "."
						+ lfPfAssociation.getAssociatedComponentName()
						+ "}";
				LogicTextViewTreeNode lfpf = new LogicTextViewTreeNode(
						TreeItemId.FILES_CHILDLF_CHILDPF,
						"." + lfPfAssociation.getAssociatedComponentName() + 
						" [" + lfPfAssociation.getAssociatedComponentIdString() + "]",
						item,
						pfEditorText,
						pfEditorText.length(),
						"."+ lfPfAssociation.getAssociatedComponentName() +
						" [" + lfPfAssociation.getAssociatedComponentIdString() + "]",
						null);
				lfpf.setData(lfPfAssociation);
				lfChildrenList.add(lfpf);
			}
		}
		item.setChildren(lfChildrenList);
	}

	private void addLookupPathChildNodes(LogicTextViewTreeNode item, ViewSource viewSource) {
		LogicTextViewTreeNode lookUpPathChild = item;
		// populate Lookup path fields.
		List<LRField> lrFieldList = viewSource.getLookupFields(
		    ((LookupQueryBean) lookUpPathChild.getData()).getId());
		List<LogicTextViewTreeNode> lookUpPathsFields = 
		    new ArrayList<LogicTextViewTreeNode>();
		String lookUpPathChildName = lookUpPathChild.getName().substring(
		    0,lookUpPathChild.getName().lastIndexOf('}'));
		if (lrFieldList != null) {
			for (LRField field : lrFieldList) {
				String fieldText = lookUpPathChildName
						+ "." + field.getName() + "}";
				LogicTextViewTreeNode lookupLTModel = new LogicTextViewTreeNode(
						TreeItemId.LOOKUPPATHS_CHILD_FIELD,
						"." + field.getName() + " [" + field.getId() + "]",
						lookUpPathChild, fieldText,
						fieldText.length(), "."+ field.getName() + "[" + field.getId() + "]", 
						null);
				lookupLTModel
						.setData((LookupQueryBean) lookUpPathChild
								.getData());
				lookUpPathsFields.add(lookupLTModel);
			}
		}
		Collections.sort(lookUpPathsFields, new LookupPathFields());
		lookUpPathChild.setChildren(lookUpPathsFields);
	}

	private void addExtractFiles(LogicTextViewTreeNode item, ViewSource viewSource) {
		// load logical files.
		List<LogicalFileQueryBean> lfQueryBeanList = SAFRQuery.queryAllLogicalFiles(viewSource.getEnvironment().getId(), SortType.SORT_BY_NAME);
		List<LogicTextViewTreeNode> filesChildList = new ArrayList<LogicTextViewTreeNode>();
		if (lfQueryBeanList != null) {
			for (LogicalFileQueryBean logicalFileQueryBean : lfQueryBeanList) {
				String lfName = logicalFileQueryBean.getNameLabel();
				String lfTitle = lfName + " [" + logicalFileQueryBean.getId() + "]";
				LogicTextViewTreeNode filesChild = new LogicTextViewTreeNode(TreeItemId.FILES_CHILDLF, lfTitle,	item, "", 0, lfTitle, null);
				// set data for future use of populating lf's Children.
				filesChild.setData(logicalFileQueryBean);
				filesChildList.add(filesChild);
			}
		}
		item.setChildren(filesChildList);
	}

	private void addExtractWriteProcedures(LogicTextViewTreeNode item) {
		// get the list of UXR query beans of type WRITE
		List<UserExitRoutineQueryBean> procList = SAFRQuery
				.queryUserExitRoutines(
						UIUtilities
								.getCurrentEnvironmentID(),
						SAFRApplication
								.getSAFRFactory()
								.getCodeSet(
										CodeCategories.EXITTYPE)
								.getCode(Codes.WRITE)
								.getKey(),
						SortType.MODULE_NAME);
		List<LogicTextViewTreeNode> proceduresChildList = new ArrayList<LogicTextViewTreeNode>();
		if (procList != null) {
			for (UserExitRoutineQueryBean proc : procList) {
				// add procedure node child.
				String pText = "{" + proc.getProgramLabel() + "}";
				String pTitle = pText + " [" + proc.getId() + "]";
				LogicTextViewTreeNode uxr = new LogicTextViewTreeNode(
						TreeItemId.PROCEDURES_CHILD, pTitle,
						item, pText, pText.length(), pTitle,
						null);
				uxr.setData(proc);
				proceduresChildList.add(uxr);
			}
		}
		item.setChildren(proceduresChildList);
		// find USEREXITROUTINES node and set children.
		LogicTextViewTreeNode parent = findChild(item
				.getParent(),
				TreeItemId.PROCEDURES);
		parent.setChildren(proceduresChildList);
	}

	private void addExtractWriteUserExits(LogicTextViewTreeNode item) {
		// get the list of UXR query beans of type WRITE
		List<UserExitRoutineQueryBean> userExitRoutineList = SAFRQuery
				.queryUserExitRoutines(
						UIUtilities
								.getCurrentEnvironmentID(),
						SAFRApplication
								.getSAFRFactory()
								.getCodeSet(
										CodeCategories.EXITTYPE)
								.getCode(Codes.WRITE)
								.getKey(),
						SortType.SORT_BY_NAME);
		List<LogicTextViewTreeNode> userExitRoutineChildList = new ArrayList<LogicTextViewTreeNode>();
		if (userExitRoutineList != null) {
			for (UserExitRoutineQueryBean userExitRoutine : userExitRoutineList) {
				// add user exit routine node child.
				String uerText = "{" + userExitRoutine.getName() + "}";
		        String uerTitle = uerText + " [" + userExitRoutine.getId() + "]";
		        LogicTextViewTreeNode uxr = new LogicTextViewTreeNode(
						TreeItemId.USEREXITROUTINES_CHILD,
						uerTitle, item, uerText, uerText
								.length(), uerTitle, null);
				uxr.setData(userExitRoutine);
				userExitRoutineChildList.add(uxr);
			}
		}
		item.setChildren(userExitRoutineChildList);
		// find PROCEDURES node and set children.
		LogicTextViewTreeNode parent = findChild(item
				.getParent(), TreeItemId.USEREXITROUTINES);
		parent.setChildren(userExitRoutineChildList);
	}

	private void addLookupPathAndSymbols(LogicTextViewTreeNode item, ViewSource viewSource) {
		List<LookupQueryBean> lookUpPaths = viewSource.getAllLookupPaths();
		List<LogicTextViewTreeNode> lookUpPathsChildren = new ArrayList<LogicTextViewTreeNode>();
		List<LogicTextViewTreeNode> lookUpPathSymbolChildren = new ArrayList<LogicTextViewTreeNode>();
		if (lookUpPaths != null) {
			for (Iterator<LookupQueryBean> iterator = lookUpPaths.iterator(); iterator.hasNext();) {
				LookupQueryBean lookupQueryBean = (LookupQueryBean) iterator.next();
				String text = "{" + lookupQueryBean.getName() + "} [" + lookupQueryBean.getId() + "]";
				String edtext = "{" + lookupQueryBean.getName() + "}";
				LogicTextViewTreeNode lookUpPathChild = new LogicTextViewTreeNode(TreeItemId.LOOKUPPATHS_CHILD, text, item, edtext, text.length(), text, null);
				// store lookup on the model for future
				// use.eg.loading of children.
				lookUpPathChild.setData(lookupQueryBean);
				lookUpPathsChildren.add(lookUpPathChild);
				// ---------------------------------------------
				// Load lookup symbol childs.
				String stext = lookupQueryBean.getName();
				LogicTextViewTreeNode lookUpPathSymbolChild = new LogicTextViewTreeNode(TreeItemId.LOOKUPSYMBOLS_CHILD,	stext, item, null, 0, stext, null);
				lookUpPathSymbolChild.setData(lookupQueryBean);
				// populate Lookup path fields.
				List<LookupPathSourceField> symbolList = viewSource.getLookupSymbolicFields(lookupQueryBean.getId());
				List<LogicTextViewTreeNode> lookUpPathSymbols = new ArrayList<LogicTextViewTreeNode>();
				if (symbolList != null) {
					for (LookupPathSourceField field : symbolList) {
						String symbolText = "$"	+ field.getSymbolicName();
						LogicTextViewTreeNode lookupSymb = new LogicTextViewTreeNode(TreeItemId.LOOKUPSYMBOLS_CHILD_SYMBOL,	symbolText,	lookUpPathSymbolChild, symbolText, symbolText.length(),	symbolText, null);
						lookupSymb.setData(lookupQueryBean);
						lookUpPathSymbols.add(lookupSymb);
					}
					lookUpPathSymbolChild.setChildren(lookUpPathSymbols);
					lookUpPathSymbolChildren.add(lookUpPathSymbolChild);
				}
			}
		}
		if (item.getId() == TreeItemId.LOOKUPPATHS) {
			item.setChildren(lookUpPathsChildren);
			// find lookup path symbol node and set
			// children.
			LogicTextViewTreeNode parent = findChild(logicTextItemsRoot, TreeItemId.LOOKUPSYMBOLS);
			parent.setChildren(lookUpPathSymbolChildren);
		}
		if (item.getId() == TreeItemId.LOOKUPSYMBOLS) {
			item.setChildren(lookUpPathSymbolChildren);
			// find lookup path symbol node and set
			// children.
			LogicTextViewTreeNode parent = findChild(logicTextItemsRoot, TreeItemId.LOOKUPPATHS);
			parent.setChildren(lookUpPathsChildren);
		}
	}

	private ViewSource getExtractViewSource() {
		ViewSource viewSource = null;
		if (logicTextType == LogicTextType.Extract_Column_Assignment) {
		    viewSource = ((ECLLogicTextEditorInput)logicTextEditorInput).getViewSource();                            
		} else if (logicTextType == LogicTextType.Extract_Record_Output) {
		    viewSource = ((EROLogicTextEditorInput)logicTextEditorInput).getViewSource();                                                        
		}
		return viewSource;
	}

	private void getERFChildren(LogicTextViewTreeNode item) {
		ViewSource viewSource = ((ERFLogicTextEditorInput)logicTextEditorInput).getViewSource();  
		addNodesForSource(item, viewSource);
	}

	/**
	 * 
	 * Finds a child with specified tree item id in the specified parent.
	 * 
	 * @param parent
	 * @param treeItemId
	 */
	private LogicTextViewTreeNode findChild(LogicTextViewTreeNode parent,
			TreeItemId treeItemId) {
		List<LogicTextViewTreeNode> childrenList = parent.getChildren();
		for (Iterator<LogicTextViewTreeNode> iterator = childrenList.iterator(); iterator.hasNext();) {
			LogicTextViewTreeNode logicTextModel = (LogicTextViewTreeNode) iterator.next();
			if (logicTextModel.getId() == treeItemId) {
				return logicTextModel;
			}
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		LogicTextViewTreeNode item = (LogicTextViewTreeNode) element;
		return item.getParent();
	}

	@Override
	public boolean hasChildren(Object element) {

		LogicTextViewTreeNode item = (LogicTextViewTreeNode) element;
		if (item.getChildren() == null) {
			// has children is called for the first time so item has no children
			// set.

			try {
				// if the logic text is of type format record filter.
				if (logicTextType == LogicTextType.Format_Record_Filter) {
					// if the tree item is of type columns then populate its
					// children.

					if (item.getId() == TreeItemId.COLUMNS) {
						List<ViewColumn> viewColumns = logicTextEditorInput
								.getView().getViewColumns().getActiveItems();
						List<LogicTextViewTreeNode> columnsChildren = new ArrayList<LogicTextViewTreeNode>();
						if (viewColumns != null) {
							for (Iterator<ViewColumn> iterator = viewColumns.iterator(); iterator.hasNext();) {
								ViewColumn viewColumn = (ViewColumn) iterator.next();
								// if the column is not sort key then add as
								// child.
								if ((!viewColumn.isSortKey()) && (viewColumn.getDataTypeCode().getGeneralId() != Codes.ALPHANUMERIC)) {
									String columnText = "COL." + viewColumn.getColumnNo();
									String columnDesc = viewColumn.getHeading1() == null || viewColumn.getHeading1().equals("") ? "" : " ("	+ viewColumn.getHeading1() + ")";
									columnsChildren.add(new LogicTextViewTreeNode(
													TreeItemId.COLUMNS_CHILD,
													columnText + columnDesc,
													item, " " + columnText + " ", columnText.length() + 2,
													null, null));
								}
							}
						}
						item.setChildren(columnsChildren);
					}
				} else if (logicTextType == LogicTextType.Extract_Record_Filter) {
					ViewSource viewSource = ((ERFLogicTextEditorInput)logicTextEditorInput).getViewSource();
					if (item.getId() == TreeItemId.FIELDS) {
						List<LRField> fields = null;
						fields = SAFRApplication.getSAFRFactory().getLRFields(viewSource.getLrFileAssociation().getAssociatingComponentId());
						List<LogicTextViewTreeNode> fieldsChildren = new ArrayList<LogicTextViewTreeNode>();
						if (fields != null) {
							for (Iterator<LRField> iterator = fields.iterator(); iterator.hasNext();) {
								LRField field = (LRField) iterator.next();
								String fieldText = "{" + field.getName() + "}";
								String titleText = fieldText + " [" + field.getId() + "]";
								LogicTextViewTreeNode fieldChild = new LogicTextViewTreeNode(TreeItemId.FIELDS_CHILD, titleText, item, fieldText, fieldText.length(), titleText, null);
								fieldChild.setData(viewSource.getLrFileAssociation());
								fieldsChildren.add(fieldChild);
							}
						}
						Collections.sort(fieldsChildren, new FieldChildren());
						item.setChildren(fieldsChildren);
					} else if ((item.getId() == TreeItemId.LOOKUPPATHS)
							|| (item.getId() == TreeItemId.LOOKUPSYMBOLS)) {
						return true;
					} else if (item.getId() == TreeItemId.WRITEPARAM) {
						List<LogicTextViewTreeNode> writeParamChildList = new ArrayList<LogicTextViewTreeNode>();
						LogicTextViewTreeNode proceduresModel = new LogicTextViewTreeNode(TreeItemId.PROCEDURES, "Procedures", item, null, 0, "Procedures", null);
						LogicTextViewTreeNode userExitRoutineModel = new LogicTextViewTreeNode(TreeItemId.USEREXITROUTINES,	"User-Exit Routines", item, null, 0, "User-Exit Routines", null);
						LogicTextViewTreeNode files = new LogicTextViewTreeNode(TreeItemId.FILES, "Files", item, null, 0, "Files", null);
						writeParamChildList.add(files);
						writeParamChildList.add(proceduresModel);
						writeParamChildList.add(userExitRoutineModel);
						item.setChildren(writeParamChildList);

					} else if ((item.getId() == TreeItemId.PROCEDURES) || (item.getId() == TreeItemId.USEREXITROUTINES)) {
						return true;
					} else if (item.getId() == TreeItemId.FILES) {
						return true;
					} else if (item.getId() == TreeItemId.FILES_CHILDLF) {
						return true;
					} else if (item.getId() == TreeItemId.LOOKUPPATHS_CHILD) {
						return true;
					}
				} else if (logicTextType == LogicTextType.Extract_Column_Assignment || logicTextType == LogicTextType.Extract_Record_Output) {
                    ViewSource viewSource = getExtractViewSource();
					if (item.getId() == TreeItemId.FIELDS) {
						List<LRField> fields = null;
						fields = SAFRApplication.getSAFRFactory().getLRFields(viewSource.getLrFileAssociation().getAssociatingComponentId());
						List<LogicTextViewTreeNode> fieldsChildren = new ArrayList<LogicTextViewTreeNode>();
						if (fields != null) {
							for (Iterator<LRField> iterator = fields.iterator(); iterator.hasNext();) {
								LRField field = (LRField) iterator.next();									
								String fieldText = "{" + field.getName()+ "}";
								String titleText = fieldText + " [" + field.getId() + "]";
								LogicTextViewTreeNode fieldChild = new LogicTextViewTreeNode(TreeItemId.FIELDS_CHILD, titleText, item, fieldText, fieldText.length(), titleText, null);
								fieldChild.setData(viewSource.getLrFileAssociation());
								fieldsChildren.add(fieldChild);
							}
						}
						Collections.sort(fieldsChildren, new FieldChildren());
						item.setChildren(fieldsChildren);
					} else if ((item.getId() == TreeItemId.LOOKUPPATHS) || (item.getId() == TreeItemId.LOOKUPSYMBOLS)) {
						return true;
					} else if (item.getId() == TreeItemId.LOOKUPPATHS_CHILD) {
						return true;
					} else if (item.getId() == TreeItemId.WRITEPARAM) {
						List<LogicTextViewTreeNode> writeParamChildList = new ArrayList<LogicTextViewTreeNode>();
						LogicTextViewTreeNode proceduresModel = new LogicTextViewTreeNode(TreeItemId.PROCEDURES, "Procedures", item, null, 0, "Procedures", null);
						LogicTextViewTreeNode userExitRoutineModel = new LogicTextViewTreeNode(TreeItemId.USEREXITROUTINES,	"User-Exit Routines", item, null, 0, "User-Exit Routines", null);
						LogicTextViewTreeNode files = new LogicTextViewTreeNode(TreeItemId.FILES, "Files", item, null, 0, "Files", null);
						writeParamChildList.add(files);
						writeParamChildList.add(proceduresModel);
						writeParamChildList.add(userExitRoutineModel);
						item.setChildren(writeParamChildList);

					} else if ((item.getId() == TreeItemId.PROCEDURES) || (item.getId() == TreeItemId.USEREXITROUTINES)) {
						return true;
					} else if (item.getId() == TreeItemId.FILES) {
						return true;

					} else if (item.getId() == TreeItemId.FILES_CHILDLF) {
						return true;
					}
				} else if (logicTextType == LogicTextType.Format_Column_Calculation) {
					if (item.getId() == TreeItemId.COLUMNS) {
						List<ViewColumn> viewColumns = logicTextEditorInput.getView().getViewColumns().getActiveItems();
						// since column no =column index+1
						Integer currentColumnNo = logicTextEditorInput.getViewEditor().getCurrentColIndex();
						List<LogicTextViewTreeNode> columnsChildren = new ArrayList<LogicTextViewTreeNode>();
						if (viewColumns != null) {
							for (Iterator<ViewColumn> iterator = viewColumns.iterator(); iterator.hasNext();) {
								ViewColumn viewColumn = (ViewColumn) iterator.next();
								if (viewColumn.getColumnNo() > currentColumnNo) {
									// if the view column no is greater than
									// Current column no then break out of
									// the
									// loop.ie.populate columns till current
									// column.
									break;
								}
								// if the column is not sort key then add as
								// child.
								if ((!viewColumn.isSortKey()) && 
								    (viewColumn.getDataTypeCode().getGeneralId() != Codes.ALPHANUMERIC)) {
									String columnText = "COL."+ viewColumn.getColumnNo();
									String columnDesc = viewColumn.getHeading1() == null || 
									    viewColumn.getHeading1().equals("") ? "" : " ("+ viewColumn.getHeading1() + ")";
									columnsChildren.add(new LogicTextViewTreeNode(
									    TreeItemId.COLUMNS_CHILD,
									    columnText + columnDesc,
									    item, " " + columnText + " ", columnText.length() + 2,
									    null, null));
								}
							}
						}
						item.setChildren(columnsChildren);
					}
				}

			} catch (SAFRException e) {
				UIUtilities.handleWEExceptions(e, "Error in getting contents for Logic text Helper", null);
			}

		}

		if ((item.getChildren() == null) || (item.getChildren().isEmpty())) {
			// item has no children.
			return false;
		} else {
			// item has children.
			return true;
		}
	}

	private void getElementsExtractFilter(
			LogicTextViewTreeNode keywordsMenu,
			LogicTextViewTreeNode functions,
			LogicTextViewTreeNode logicalOperators,
			LogicTextViewTreeNode arithmeticOperators) {
		
		// menus specific to Extract Record Filter
		LogicTextViewTreeNode fieldsMenu = new LogicTextViewTreeNode(TreeItemId.FIELDS, "Fields", logicTextItemsRoot, null, 0, null, null);
		LogicTextViewTreeNode lookupPathsMenu = new LogicTextViewTreeNode(TreeItemId.LOOKUPPATHS, "Lookup Paths", logicTextItemsRoot, null, 0, null, null);
		LogicTextViewTreeNode lookupSymbolsMenu = new LogicTextViewTreeNode(TreeItemId.LOOKUPSYMBOLS, "Lookup Path Symbols", logicTextItemsRoot, null, 0, null, null);
		LogicTextViewTreeNode writeParamMenu = new LogicTextViewTreeNode(TreeItemId.WRITEPARAM, "Write Parameters", logicTextItemsRoot, null, 0, null, null);		

		LogicTextViewTreeNode langConstructsExtractRecordFilter = addExtractFiltLanguageConstructs(keywordsMenu);

		addExtractFunctions(functions);

        LogicTextViewTreeNode comparisonOperatorsExtractRecordFilter = addComparisonOperators(keywordsMenu);
        LogicTextViewTreeNode castOperatorsExtractFilter = addCastOperators(keywordsMenu);
        LogicTextViewTreeNode stringOperatorsExtractFilter = addStringOperators(keywordsMenu);

        // Keywords Menu children list
        List<LogicTextViewTreeNode> keywordsMenuChildren = new ArrayList<LogicTextViewTreeNode>();
        keywordsMenuChildren.add(langConstructsExtractRecordFilter);
        keywordsMenuChildren.add(arithmeticOperators);
        keywordsMenuChildren.add(castOperatorsExtractFilter);
        keywordsMenuChildren.add(comparisonOperatorsExtractRecordFilter);
        keywordsMenuChildren.add(functions);
        keywordsMenuChildren.add(logicalOperators);
        keywordsMenuChildren.add(stringOperatorsExtractFilter);
        keywordsMenu.setChildren(keywordsMenuChildren);
        
		// Root
		List<LogicTextViewTreeNode> children = new ArrayList<LogicTextViewTreeNode>();
		children.add(keywordsMenu);
		children.add(fieldsMenu);
		children.add(lookupPathsMenu);
		children.add(lookupSymbolsMenu);
		children.add(writeParamMenu);		

		logicTextItemsRoot.setChildren(children);
		
	}

    protected void addExtractFunctions(LogicTextViewTreeNode functions) {
        // functions
        LogicTextViewTreeNode left = 			new LogicTextViewTreeNode(TreeItemId.LEFT, "LEFT()", functions, "LEFT()", 5, "LEFT({FieldName},integer)", null);
        LogicTextViewTreeNode right = 			new LogicTextViewTreeNode(TreeItemId.RIGHT, "RIGHT()", functions, "RIGHT()", 6, "RIGHT({FieldName},integer)", null);
        LogicTextViewTreeNode substr = 			new LogicTextViewTreeNode(TreeItemId.SUBSTR, "SUBSTR()", functions, "SUBSTR()", 7, "SUBSTR({FieldName} | {LookupPathName},integer,integer)", null);
		LogicTextViewTreeNode all = 			new LogicTextViewTreeNode(TreeItemId.ALL, "ALL()", functions, "ALL()", 4, "ALL([hex])", null);
		LogicTextViewTreeNode current = 		new LogicTextViewTreeNode(TreeItemId.CURRENT, "CURRENT()", functions, "CURRENT()", 8, "CURRENT({FieldName})", null);				
		LogicTextViewTreeNode prior = 			new LogicTextViewTreeNode(TreeItemId.PRIOR, "PRIOR()", functions, "PRIOR()", 6, "PRIOR({FieldName})", null);								
		LogicTextViewTreeNode date = 			new LogicTextViewTreeNode(TreeItemId.DATE, "DATE()", functions, "DATE()", 5, "DATE({Fieldame} | DateFunction() | DateText, Format)", null);								
		LogicTextViewTreeNode daysbetween = 	new LogicTextViewTreeNode(TreeItemId.DAYSBETWEEN, "DAYSBETWEEN()", functions, "DAYSBETWEEN()", 12, "DAYSBETWEEN({Fieldame} | DateFunction() | DateText, {Fieldame} | DateFunction() | DateText)", null);								
		LogicTextViewTreeNode monthsbetween = 	new LogicTextViewTreeNode(TreeItemId.MONTHSBETWEEN, "MONTHSBETWEEN()", functions, "MONTHSBETWEEN()", 14, "MONTHSBETWEEN({Fieldame} | DateFunction() | DateText, {Fieldame} | DateFunction() | DateText)", null);								
		LogicTextViewTreeNode yearsbetween = 	new LogicTextViewTreeNode(TreeItemId.YEARSBETWEEN, "YEARSBETWEEN()", functions, "YEARSBETWEEN()", 13, "YEARSBETWEEN({Fieldame} | DateFunction() | DateText, {Fieldame} | DateFunction() | DateText)", null);		
		LogicTextViewTreeNode batchDate = 		new LogicTextViewTreeNode(TreeItemId.BATCHDATE, "BATCHDATE()", functions,	"BATCHDATE()", 10, "BATCHDATE([integer])", null);
        LogicTextViewTreeNode timestamp = 		new LogicTextViewTreeNode(TreeItemId.TIMESTAMP, "TIMESTAMP()", functions, "TIMESTAMP()", 10, "TIMESTAMP([integer])", null);		
		LogicTextViewTreeNode fiscalDay = 		new LogicTextViewTreeNode(TreeItemId.FISCALDAY, "FISCALDAY()", functions,	"FISCALDAY()", 10, "FISCALDAY([integer])", null);
		LogicTextViewTreeNode fiscalMonth = 	new LogicTextViewTreeNode(TreeItemId.FISCALMONTH, "FISCALMONTH()", functions, "FISCALMONTH()", 12, "FISCALMONTH([integer])", null);
		LogicTextViewTreeNode fiscalYear = 		new LogicTextViewTreeNode(TreeItemId.FISCALYEAR, "FISCALYEAR()", functions, "FISCALYEAR()", 11, "FISCALYEAR([integer])", null);
		LogicTextViewTreeNode isFound = 		new LogicTextViewTreeNode(TreeItemId.ISFOUND, "ISFOUND()", functions, "ISFOUND()", 8, "ISFOUND({LookupPathName [,EffectiveDate][;$SymbConst1=val1 [,$SymbConst2=val2...[,SymbConstn=valn]]]})",	null);
		LogicTextViewTreeNode isNotFound = 		new LogicTextViewTreeNode(TreeItemId.ISNOTFOUND, "ISNOTFOUND()", functions, "ISNOTFOUND()", 11, "ISNOTFOUND({LookupPathName [,EffectiveDate][;$SymbConst1=val1 [,$SymbConst2=val2...[,SymbConstn=valn]]]})", null);
		LogicTextViewTreeNode isNull = 			new LogicTextViewTreeNode(TreeItemId.ISNULL, "ISNULL()", functions, "ISNULL()", 7, "ISNULL({[LookupPathName.]FieldName [,EffectiveDate][;$SymbConst1=val1 [,$SymbConst2=val2...[,SymbConstn=valn]]]})", null);
		LogicTextViewTreeNode isNotNull = 		new LogicTextViewTreeNode(TreeItemId.ISNOTNULL, "ISNOTNULL()", functions, "ISNOTNULL()", 10, "ISNOTNULL({[LookupPathName.]FieldName [,EffectiveDate][;$SymbConst1=val1 [,$SymbConst2=val2...[,SymbConstn=valn]]]})", null);
		LogicTextViewTreeNode isNumeric = 		new LogicTextViewTreeNode(TreeItemId.ISNUMERIC, "ISNUMERIC()", functions, "ISNUMERIC()", 10, "ISNUMERIC({[LookupPathName.]FieldName [,EffectiveDate][;$SymbConst1=val1 [,$SymbConst2=val2...[,$SymbConstn=valn]]]})", null);
		LogicTextViewTreeNode isNotNumeric = 	new LogicTextViewTreeNode(TreeItemId.ISNOTNUMERIC, "ISNOTNUMERIC()", functions, "ISNOTNUMERIC()", 13, "ISNOTNUMERIC({[LookupPathName.]FieldName [,EffectiveDate][;$SymbConst1=val1 [,$SymbConst2=val2...[,$SymbConstn=valn]]]})", null);
		LogicTextViewTreeNode isSpaces = 		new LogicTextViewTreeNode(TreeItemId.ISSPACES, "ISSPACES()", functions, "ISSPACES()", 9, "ISSPACES({LookupPathName [,EffectiveDate][;$SymbConst1=val1 [,$SymbConst2=val2...[,SymbConstn=valn]]]})", null);
		LogicTextViewTreeNode isNotSpaces = 	new LogicTextViewTreeNode(TreeItemId.ISNOTSPACES, "ISNOTSPACES()", functions, "ISNOTSPACES()", 12, "ISNOTSPACES({LookupPathName [,EffectiveDate][;$SymbConst1=val1 [,$SymbConst2=val2...[,SymbConstn=valn]]]})", null);
		LogicTextViewTreeNode repeat = 			new LogicTextViewTreeNode(TreeItemId.REPEAT, "REPEAT()", functions, "REPEAT()", 7, "REPEAT(string, integer)", null);
		LogicTextViewTreeNode runDay = 			new LogicTextViewTreeNode(TreeItemId.RUNDAY, "RUNDAY()", functions, "RUNDAY()", 7, "RUNDAY([integer])", null);
		LogicTextViewTreeNode runMonth = 		new LogicTextViewTreeNode(TreeItemId.RUNMONTH, "RUNMONTH()", functions, "RUNMONTH()", 9, "RUNMONTH([integer])", null);
		LogicTextViewTreeNode runYear = 		new LogicTextViewTreeNode(TreeItemId.RUNYEAR, "RUNYEAR()", functions, "RUNYEAR()", 8, "RUNYEAR([integer])", null);
		
		LogicTextViewTreeNode write = 			new LogicTextViewTreeNode(TreeItemId.WRITE, "WRITE()", functions, "WRITE()", 6, "WRITE [SOURCE={INPUT}]" + SAFRUtilities.LINEBREAK + "\t[DEST|DESTINATION={FILENAME=<filename>}]" + SAFRUtilities.LINEBREAK + "\t[PROCEDURE=<pgmname> [, MYPARAM=<text>] [USEREXIT=<userexitname>]", null);

		// write children list.
		LogicTextViewTreeNode procedure = 		new LogicTextViewTreeNode(TreeItemId.PROCEDURE, "PROCEDURE", write, "PROCEDURE ", 10, "An optional write exit to be used with an option parameter MYPARAM.", null);
		LogicTextViewTreeNode userexit = 		new LogicTextViewTreeNode(TreeItemId.USEREXIT, "USEREXIT", write, "USEREXIT ", 9, null, null);
		LogicTextViewTreeNode sourceinput = 	new LogicTextViewTreeNode(TreeItemId.SOURCEINPUT, "SOURCE=INPUT", write, "SOURCE = INPUT ", 15, "Causes a record read from the event file to be written.", null);
		LogicTextViewTreeNode sourceview = 		new LogicTextViewTreeNode(TreeItemId.SOURCEVIEW, "SOURCE=VIEW", write, "SOURCE = VIEW ", 14, "Means the extract record constructed to that point is written in Standard Extract File Format.", null);
		LogicTextViewTreeNode sourcedata = 		new LogicTextViewTreeNode(TreeItemId.SOURCEDATA, "SOURCE=DATA", write, "SOURCE = DATA ", 14, "Writes the extract record constructed to that point.", null);
		LogicTextViewTreeNode destinationextract = new LogicTextViewTreeNode(TreeItemId.DESTINATIONEXTRACT, "DESTINATION=EXTRACT", write, "DESTINATION = EXTRACT = ", 25, "The Extract File number that the data should be written to.", null);
		LogicTextViewTreeNode destinationdefault = new LogicTextViewTreeNode(TreeItemId.DESTINATIONDEFAULT, "DESTINATION=DEFAULT", write, "DESTINATION = DEFAULT ", 22, "Default the file that is written to.", null);
		LogicTextViewTreeNode destinationfile = new LogicTextViewTreeNode(TreeItemId.DESTINATIONFILE, "DESTINATION=FILE", write, "DESTINATION = FILE ", 19, "A standard extract file.", null);
		// write Children List
		List<LogicTextViewTreeNode> writeChildren = new ArrayList<LogicTextViewTreeNode>();
		writeChildren.add(destinationextract);
		writeChildren.add(destinationdefault);
		writeChildren.add(destinationfile);
		writeChildren.add(procedure);
		writeChildren.add(sourcedata);
		writeChildren.add(sourceinput);
		writeChildren.add(sourceview);
		writeChildren.add(userexit);
		write.setChildren(writeChildren);
		
		// Functions Children List
		List<LogicTextViewTreeNode> functionsChildren = new ArrayList<LogicTextViewTreeNode>();
		functionsChildren.add(all);
		functionsChildren.add(current);
		functionsChildren.add(date);
		functionsChildren.add(daysbetween);
		functionsChildren.add(fiscalDay);
		functionsChildren.add(fiscalMonth);
		functionsChildren.add(fiscalYear);
		functionsChildren.add(isFound);
		functionsChildren.add(isNotFound);
		functionsChildren.add(isNull);
		functionsChildren.add(isNotNull);
		functionsChildren.add(isNotNumeric);
		functionsChildren.add(isNotSpaces);
		functionsChildren.add(isNumeric);
		functionsChildren.add(isSpaces);
		functionsChildren.add(left);
		functionsChildren.add(monthsbetween);
		functionsChildren.add(prior);
		functionsChildren.add(right);
		functionsChildren.add(substr);
        functionsChildren.add(timestamp);            
		functionsChildren.add(yearsbetween);
		functionsChildren.add(repeat);
		functionsChildren.add(runDay);
		functionsChildren.add(runMonth);
		functionsChildren.add(runYear);
		functionsChildren.add(write);		
		functions.setChildren(functionsChildren);
    }

    protected LogicTextViewTreeNode addExtractFiltLanguageConstructs(LogicTextViewTreeNode keywordsMenu) {
        // Keyword Menu specific to Extract Record Filter
		LogicTextViewTreeNode langConstructsExtractRecordFilter = new LogicTextViewTreeNode(
				TreeItemId.LANGCONSTRUCTS, "Language Constructs",
				keywordsMenu, null, 0, null, null);

		// Language Constructs
		LogicTextViewTreeNode select = new LogicTextViewTreeNode(
				TreeItemId.SELECT,
				"SELECT",
				langConstructsExtractRecordFilter,
				"SELECT",
				0,
				"SELECT, which includes records where IF condition is true",
				null);
		LogicTextViewTreeNode selectIf = new LogicTextViewTreeNode(
				TreeItemId.SELECTIF,
				"SELECTIF()",
				langConstructsExtractRecordFilter,
				"SELECTIF()",
				9,
				"SELECTIF(Condition), which includes all input records where condition is true",
				null);
		LogicTextViewTreeNode skip = new LogicTextViewTreeNode(
				TreeItemId.SKIP,
				"SKIP",
				langConstructsExtractRecordFilter,
				"SKIP",
				0,
				"SKIP, which excludes records where IF condition is true",
				null);

		LogicTextViewTreeNode skipIf = new LogicTextViewTreeNode(
				TreeItemId.SKIPIF,
				"SKIPIF()",
				langConstructsExtractRecordFilter,
				"SKIPIF()",
				7,
				"SKIPIF(Condition), which will exclude records where condition is true",
				null);
		LogicTextViewTreeNode ifThenElseEndif = new LogicTextViewTreeNode(
				TreeItemId.ITEE,
				"IF THEN ELSE ENDIF",
				langConstructsExtractRecordFilter,
				"IF  THEN" + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK + "ELSE" + SAFRUtilities.LINEBREAK + SAFRUtilities.LINEBREAK + "ENDIF",
				3,
				"If[Condition] Then [Statements] Else [Statements] Endif",
				null);

		// Language Constructs List
		List<LogicTextViewTreeNode> languageConstructsChildren = new ArrayList<LogicTextViewTreeNode>();
		languageConstructsChildren.add(selectIf);
		languageConstructsChildren.add(skipIf);
		languageConstructsChildren.add(select);
		languageConstructsChildren.add(skip);
		languageConstructsChildren.add(ifThenElseEndif);
		langConstructsExtractRecordFilter.setChildren(languageConstructsChildren);
        return langConstructsExtractRecordFilter;
    }
	
	private void getElementsExtractColumn(
			LogicTextViewTreeNode keywordsMenu,
			LogicTextViewTreeNode functions,
			LogicTextViewTreeNode logicalOperators,
			LogicTextViewTreeNode arithmeticOperators,
			boolean recordOutput) {	    
		// menus specific to Extract Column Assignment
		LogicTextViewTreeNode fieldsMenu = new LogicTextViewTreeNode(TreeItemId.FIELDS, "Fields", logicTextItemsRoot, null, 0, null, null);
		LogicTextViewTreeNode lookupPathsMenu = new LogicTextViewTreeNode(TreeItemId.LOOKUPPATHS, "Lookup Paths", logicTextItemsRoot, null, 0, null, null);
		LogicTextViewTreeNode lookupSymbolsMenu = new LogicTextViewTreeNode(TreeItemId.LOOKUPSYMBOLS, "Lookup Path Symbols", logicTextItemsRoot, null, 0, null, null);
		LogicTextViewTreeNode writeParamMenu = new LogicTextViewTreeNode(TreeItemId.WRITEPARAM, "Write Parameters", logicTextItemsRoot, null, 0, null, null);

		LogicTextViewTreeNode languageConstructsExtractColumnAss = addExtractCalcLanguageConstructs(keywordsMenu, recordOutput);

		addExtractFunctions(functions);

        LogicTextViewTreeNode comparisonOperatorsExtractColumnAss = addComparisonOperators(keywordsMenu);
        LogicTextViewTreeNode castOperatorsExtractColumnAss = addCastOperators(keywordsMenu);
        LogicTextViewTreeNode stringOperatorsExtractColumnAss =	addStringOperators(keywordsMenu);

        // Keywords Menu children list
        List<LogicTextViewTreeNode> keywordsMenuChildren = new ArrayList<LogicTextViewTreeNode>();
        keywordsMenuChildren.add(languageConstructsExtractColumnAss);
        keywordsMenuChildren.add(arithmeticOperators);
        keywordsMenuChildren.add(castOperatorsExtractColumnAss);
        keywordsMenuChildren.add(comparisonOperatorsExtractColumnAss);
        keywordsMenuChildren.add(functions);
        keywordsMenuChildren.add(logicalOperators);
        keywordsMenuChildren.add(stringOperatorsExtractColumnAss);
        keywordsMenu.setChildren(keywordsMenuChildren);
        
		// Root
		List<LogicTextViewTreeNode> children = new ArrayList<LogicTextViewTreeNode>();
		children.add(keywordsMenu);
		children.add(fieldsMenu);
		children.add(lookupPathsMenu);
		children.add(lookupSymbolsMenu);
		children.add(writeParamMenu);

		logicTextItemsRoot.setChildren(children);		
	}

    protected LogicTextViewTreeNode addExtractCalcLanguageConstructs(LogicTextViewTreeNode keywordsMenu, boolean recordOutput) {
        // Keywords Menu
		LogicTextViewTreeNode languageConstructsExtractColumnAss = new LogicTextViewTreeNode(
				TreeItemId.LANGCONSTRUCTS, "Language Constructs",
				keywordsMenu, null, 0, null, null);
		
		// Language Construct for Extract column assignment
		LogicTextViewTreeNode colEqual = new LogicTextViewTreeNode(
				TreeItemId.COLEQUAL, "COLUMN = ",
				languageConstructsExtractColumnAss, "COLUMN = ", 0,
				"COLUMN = {Expression/Value}", null);
        LogicTextViewTreeNode colDot = new LogicTextViewTreeNode(
            TreeItemId.COLDOT, "COL.",
            languageConstructsExtractColumnAss, "COL. = ", 4,
            "COL.{Column Number} = {Expression/Value}", null);
		LogicTextViewTreeNode ifThenEndif = new LogicTextViewTreeNode(
				TreeItemId.ITE, "IF THEN ENDIF",
				languageConstructsExtractColumnAss,
				"IF  THEN" + SAFRUtilities.LINEBREAK + "" + SAFRUtilities.LINEBREAK + "ENDIF", 3,
				"IF(Condition) THEN(Statements) ENDIF(Statements)",
				null);
		LogicTextViewTreeNode ifThenElseEndif = new LogicTextViewTreeNode(
				TreeItemId.ITEE,
				"IF THEN ELSE ENDIF",
				languageConstructsExtractColumnAss,
				"IF  THEN" + SAFRUtilities.LINEBREAK + "" + SAFRUtilities.LINEBREAK + "ELSE" + SAFRUtilities.LINEBREAK + "" + SAFRUtilities.LINEBREAK + "ENDIF",
				3,
				"IF[Condition] THEN [Statements] ELSE [Statements] ENDIF",
				null);

		// Language Constructs List
		List<LogicTextViewTreeNode> languageConstructsChildren = new ArrayList<LogicTextViewTreeNode>();
		if (!recordOutput) {
		    languageConstructsChildren.add(colEqual);
		}
        languageConstructsChildren.add(colDot);
		languageConstructsChildren.add(ifThenEndif);
		languageConstructsChildren.add(ifThenElseEndif);
		languageConstructsExtractColumnAss.setChildren(languageConstructsChildren);
        return languageConstructsExtractColumnAss;
    }

    protected LogicTextViewTreeNode addComparisonOperators(LogicTextViewTreeNode keywordsMenu) {        
        LogicTextViewTreeNode comparisonOperatorsExtractColumnAss = new LogicTextViewTreeNode(
            TreeItemId.COMPARISIONOPR, "Comparison Operators",
            keywordsMenu, null, 0, null, null);
        
        // Comparison Operators
		LogicTextViewTreeNode beginsWith = new LogicTextViewTreeNode(
				TreeItemId.BEGINSWITH, "BEGINS_WITH",
				comparisonOperatorsExtractColumnAss, " BEGINS_WITH ",
				0, "Expression BEGINS_WITH " + "Text", null);
		LogicTextViewTreeNode contains = new LogicTextViewTreeNode(
				TreeItemId.CONTAINS, "CONTAINS",
				comparisonOperatorsExtractColumnAss, " CONTAINS ", 0,
				"Expression CONTAINS " + "Text", null);
		LogicTextViewTreeNode endsWith = new LogicTextViewTreeNode(
				TreeItemId.ENDWITH, "ENDS_WITH",
				comparisonOperatorsExtractColumnAss, " ENDS_WITH ", 0,
				"Expression ENDS_WITH " + "Text", null);
		LogicTextViewTreeNode lessThan = new LogicTextViewTreeNode(
				TreeItemId.LESSTHAN, "<",
				comparisonOperatorsExtractColumnAss, " < ", 0,
				"Expression < Expression: Comparison Operator", null);
		LogicTextViewTreeNode greaterThan = new LogicTextViewTreeNode(
				TreeItemId.GREATERTHAN, ">",
				comparisonOperatorsExtractColumnAss, " > ", 0,
				"Expression > Expression: Comparison Operator", null);
		LogicTextViewTreeNode equals = new LogicTextViewTreeNode(
				TreeItemId.EQUALSTO, "=",
				comparisonOperatorsExtractColumnAss, " = ", 0,
				"Expression = Expression: Comparison Operator", null);
		LogicTextViewTreeNode greaterThanEqual = new LogicTextViewTreeNode(
				TreeItemId.GREATERTHANEQUALS, " >= ",
				comparisonOperatorsExtractColumnAss, ">=", 0,
				"Expression >= Expression: Comparison Operator", null);
		LogicTextViewTreeNode lessThanEqual = new LogicTextViewTreeNode(
				TreeItemId.LESSTHANEQUALS, " <= ",
				comparisonOperatorsExtractColumnAss, "<=", 0,
				"Expression <= Expression: Comparison Operator", null);
		LogicTextViewTreeNode notEqual = new LogicTextViewTreeNode(
				TreeItemId.NOTEQUALS, "<>",
				comparisonOperatorsExtractColumnAss, " <> ", 0,
				"Expression <> Expression: Comparison Operator", null);

		// Comparison Operators Children List
		List<LogicTextViewTreeNode> comparisonOperatorsChildren = new ArrayList<LogicTextViewTreeNode>();
		comparisonOperatorsChildren.add(beginsWith);
		comparisonOperatorsChildren.add(contains);
		comparisonOperatorsChildren.add(endsWith);
		comparisonOperatorsChildren.add(lessThan);
		comparisonOperatorsChildren.add(greaterThan);
		comparisonOperatorsChildren.add(equals);
		comparisonOperatorsChildren.add(lessThanEqual);
		comparisonOperatorsChildren.add(greaterThanEqual);
		comparisonOperatorsChildren.add(notEqual);
		comparisonOperatorsExtractColumnAss.setChildren(comparisonOperatorsChildren);
		
		return comparisonOperatorsExtractColumnAss;
    }

    protected LogicTextViewTreeNode addStringOperators(LogicTextViewTreeNode keywordsMenu){
    	LogicTextViewTreeNode stringOperatorsExtractColumnAss = new LogicTextViewTreeNode(
    			TreeItemId.STRINGOPERATORS,"String Operators", keywordsMenu, null, 0, null, null);
    	
    	
    	makeAndAddStringNodes(stringOperatorsExtractColumnAss);
        return stringOperatorsExtractColumnAss;
    }
    protected LogicTextViewTreeNode addCastOperators(LogicTextViewTreeNode keywordsMenu) {
        // Cast Children List
        LogicTextViewTreeNode castOperatorsExtractColumnAss = new LogicTextViewTreeNode(
            TreeItemId.CAST_OPR, "Cast Operators",
            keywordsMenu, null, 0, null, null);		
		
        LogicTextViewTreeNode castAlpha = new LogicTextViewTreeNode(TreeItemId.CAST_ALPHA, "<ALPHA>", castOperatorsExtractColumnAss, "<ALPHA>", 7, "Alphanumeric Cast", null);
        LogicTextViewTreeNode castNodTf = new LogicTextViewTreeNode(TreeItemId.CAST_NODTF, "<NODTF>", castOperatorsExtractColumnAss, "<NODTF>", 7, "No Date/Time Format", null);
        LogicTextViewTreeNode castBinary = new LogicTextViewTreeNode(TreeItemId.CAST_BINARY, "<BINARY>", castOperatorsExtractColumnAss, "<BINARY>", 8, "Binary Cast", null);
        LogicTextViewTreeNode castBCD = new LogicTextViewTreeNode(TreeItemId.CAST_BCD, "<BCD>", castOperatorsExtractColumnAss, "<BCD>", 5, "BCD Cast", null);
        LogicTextViewTreeNode castEdited = new LogicTextViewTreeNode(TreeItemId.CAST_EDITED, "<EDITED>", castOperatorsExtractColumnAss, "<EDITED>", 8, "Edited Numeric Cast", null);
        LogicTextViewTreeNode castMasked = new LogicTextViewTreeNode(TreeItemId.CAST_MASKED, "<MASKED>", castOperatorsExtractColumnAss, "<MASKED>", 8, "Masked Cast", null);
        LogicTextViewTreeNode castPacked = new LogicTextViewTreeNode(TreeItemId.CAST_PACKED, "<PACKED>", castOperatorsExtractColumnAss, "<PACKED>", 8, "Packed Cast", null);
        LogicTextViewTreeNode castSBinary = new LogicTextViewTreeNode(TreeItemId.CAST_SBINARY, "<SBINARY>", castOperatorsExtractColumnAss, "<SBINARY>", 9, "Binary Sorted Cast", null);
        LogicTextViewTreeNode castSPacked = new LogicTextViewTreeNode(TreeItemId.CAST_SPACKED, "<SPACKED>", castOperatorsExtractColumnAss, "<SPACKED>", 9, "Packed Sorted Cast", null);
        LogicTextViewTreeNode castZoned = new LogicTextViewTreeNode(TreeItemId.CAST_ZONED, "<ZONED>", castOperatorsExtractColumnAss, "<ZONED>", 7, "Zoned Cast", null);
        List<LogicTextViewTreeNode> castOperatorsChildren = new ArrayList<LogicTextViewTreeNode>();
        castOperatorsChildren.add(castAlpha);
        castOperatorsChildren.add(castBinary);
        castOperatorsChildren.add(castBCD);
        castOperatorsChildren.add(castEdited);
        castOperatorsChildren.add(castMasked);
        castOperatorsChildren.add(castNodTf);
        castOperatorsChildren.add(castPacked);
        castOperatorsChildren.add(castSBinary);
        castOperatorsChildren.add(castSPacked);
        castOperatorsChildren.add(castZoned);
        castOperatorsExtractColumnAss.setChildren(castOperatorsChildren);
        return castOperatorsExtractColumnAss;
    }
	
	private void getElementsFormatColumn(
			LogicTextViewTreeNode keywordsMenu,
			LogicTextViewTreeNode functions,
			LogicTextViewTreeNode logicalOperators,
			LogicTextViewTreeNode arithmeticOperators) {
		// Menus specific to Format Phase Calculation
		LogicTextViewTreeNode columnsMenu = new LogicTextViewTreeNode(
				TreeItemId.COLUMNS, "Columns", logicTextItemsRoot,
				null, 0, null, null);

		// Keyword Menu specific to Format Column Calculation
		LogicTextViewTreeNode langConstructsFormatPhaseCalculation = new LogicTextViewTreeNode(
				TreeItemId.LANGCONSTRUCTS, "Language Constructs",
				keywordsMenu, null, 0, null, null);
		LogicTextViewTreeNode comparisonOperatorsFormatPhaseCalculation = new LogicTextViewTreeNode(
				TreeItemId.COMPARISIONOPR, "Comparison Operators",
				keywordsMenu, null, 0, null, null);

		// Keywords Menu children list
		List<LogicTextViewTreeNode> keywordsMenuChildren = new ArrayList<LogicTextViewTreeNode>();
		keywordsMenuChildren.add(langConstructsFormatPhaseCalculation);
		keywordsMenuChildren.add(logicalOperators);
		keywordsMenuChildren.add(arithmeticOperators);
		keywordsMenuChildren
				.add(comparisonOperatorsFormatPhaseCalculation);
		keywordsMenu.setChildren(keywordsMenuChildren);

		// Language Constructs for Format Phase Calculation
		LogicTextViewTreeNode colDot = new LogicTextViewTreeNode(
				TreeItemId.COLDOT,
				"COL.",
				langConstructsFormatPhaseCalculation,
				"COL.",
				0,
				"COLUMN = [Expression][Arithmetic Operator]COL.integer[arithmetic Operator][COL.integer]",
				null);
		LogicTextViewTreeNode colEqual = new LogicTextViewTreeNode(
				TreeItemId.COLEQUAL, "COLUMN = ",
				langConstructsFormatPhaseCalculation, "COLUMN = ", 0,
				"COLUMN = {Expression/Value}", null);
		LogicTextViewTreeNode ifThenEndif = new LogicTextViewTreeNode(
				TreeItemId.ITE, "IF THEN ENDIF",
				langConstructsFormatPhaseCalculation,
				"IF\tTHEN" + SAFRUtilities.LINEBREAK + "" + SAFRUtilities.LINEBREAK + "ENDIF", 3,
				"If(condition) Then(Statements) EndIf(statements)",
				null);
		LogicTextViewTreeNode ifThenElseEndif = new LogicTextViewTreeNode(
				TreeItemId.ITEE,
				"IF Then Else EndIf",
				langConstructsFormatPhaseCalculation,
				"IF  Then" + SAFRUtilities.LINEBREAK + "" + SAFRUtilities.LINEBREAK + "Else" + SAFRUtilities.LINEBREAK + "" + SAFRUtilities.LINEBREAK + "EndIf",
				3,
				"If[Condition] Then [Statements] Else [Statements] Endif",
				null);

		// Language Constructs List
		List<LogicTextViewTreeNode> languageConstructsChildren = new ArrayList<LogicTextViewTreeNode>();
		languageConstructsChildren.add(colDot);
		languageConstructsChildren.add(colEqual);
		languageConstructsChildren.add(ifThenEndif);
		languageConstructsChildren.add(ifThenElseEndif);
		langConstructsFormatPhaseCalculation
				.setChildren(languageConstructsChildren);

		// comparison Operators
		LogicTextViewTreeNode lessThan = new LogicTextViewTreeNode(
				TreeItemId.LESSTHAN, "<",
				comparisonOperatorsFormatPhaseCalculation, " < ", 0,
				"Expression < Expression: Comparison Operator", null);
		LogicTextViewTreeNode greaterThan = new LogicTextViewTreeNode(
				TreeItemId.GREATERTHAN, ">",
				comparisonOperatorsFormatPhaseCalculation, " > ", 0,
				"Expression > Expression: Comparison Operator", null);
		LogicTextViewTreeNode equals = new LogicTextViewTreeNode(
				TreeItemId.EQUALSTO, "=",
				comparisonOperatorsFormatPhaseCalculation, " = ", 0,
				"Expression = Expression: Comparison Operator", null);
		LogicTextViewTreeNode greaterThanEqual = new LogicTextViewTreeNode(
				TreeItemId.GREATERTHANEQUALS, " >= ",
				comparisonOperatorsFormatPhaseCalculation, ">=", 0,
				"Expression >= Expression: Comparison Operator", null);
		LogicTextViewTreeNode lessThanEqual = new LogicTextViewTreeNode(
				TreeItemId.LESSTHANEQUALS, " <= ",
				comparisonOperatorsFormatPhaseCalculation, "<=", 0,
				"Expression <= Expression: Comparison Operator", null);
		LogicTextViewTreeNode notEqual = new LogicTextViewTreeNode(
				TreeItemId.NOTEQUALS, "<>",
				comparisonOperatorsFormatPhaseCalculation, " <> ", 0,
				"Expression <> Expression: Comparison Operator", null);

		// Comparison Operators Children List
		List<LogicTextViewTreeNode> comparisonOperatorsChildren = new ArrayList<LogicTextViewTreeNode>();
		comparisonOperatorsChildren.add(lessThan);
		comparisonOperatorsChildren.add(greaterThan);
		comparisonOperatorsChildren.add(equals);
		comparisonOperatorsChildren.add(lessThanEqual);
		comparisonOperatorsChildren.add(greaterThanEqual);
		comparisonOperatorsChildren.add(notEqual);
		comparisonOperatorsFormatPhaseCalculation
				.setChildren(comparisonOperatorsChildren);

		// Root
		List<LogicTextViewTreeNode> children = new ArrayList<LogicTextViewTreeNode>();
		children.add(keywordsMenu);
		children.add(columnsMenu);
		logicTextItemsRoot.setChildren(children);		
	}
	
	private void getElementsFormatFilter(
			LogicTextViewTreeNode keywordsMenu,
			LogicTextViewTreeNode functions,
			LogicTextViewTreeNode logicalOperators,
			LogicTextViewTreeNode arithmeticOperators) {
		// Menus specific to Format Record Filter
		LogicTextViewTreeNode columnsMenu = new LogicTextViewTreeNode(
				TreeItemId.COLUMNS, "Columns", logicTextItemsRoot,
				null, 0, null, null);

		// Keyword Menu specific to Format Record Filter
		LogicTextViewTreeNode langConstructsFormatRecordFilter = new LogicTextViewTreeNode(
				TreeItemId.LANGCONSTRUCTS, "Language Constructs",
				keywordsMenu, null, 0, null, null);
		LogicTextViewTreeNode comparisonOperatorsFormatRecordFilter = new LogicTextViewTreeNode(
				TreeItemId.COMPARISIONOPR, "Comparison Operators",
				keywordsMenu, null, 0, null, null);
		LogicTextViewTreeNode stringOperatorsFormatRecordFilter = new LogicTextViewTreeNode(
				TreeItemId.STRINGOPERATORS, "String Operators",
				keywordsMenu, null, 0, null, null);
		// Keywords Menu children list
		List<LogicTextViewTreeNode> keywordsMenuChildren = new ArrayList<LogicTextViewTreeNode>();
		keywordsMenuChildren.add(langConstructsFormatRecordFilter);
		keywordsMenuChildren.add(logicalOperators);
		keywordsMenuChildren.add(arithmeticOperators);
		keywordsMenuChildren.add(comparisonOperatorsFormatRecordFilter);
		keywordsMenuChildren.add(stringOperatorsFormatRecordFilter);
		keywordsMenu.setChildren(keywordsMenuChildren);

		// Language Constructs for Format Record filter
		LogicTextViewTreeNode selectIf = new LogicTextViewTreeNode(
				TreeItemId.SELECTIF,
				"SELECTIF()",
				langConstructsFormatRecordFilter,
				"SELECTIF()",
				9,
				"SELECTIF(Condition), which includes all input records where condition is true",
				null);
		LogicTextViewTreeNode skipIf = new LogicTextViewTreeNode(
				TreeItemId.SKIPIF,
				"SKIPIF()",
				langConstructsFormatRecordFilter,
				"SKIPIF()",
				7,
				"SKIPIF(Condition), which will exclude records where condition is true",
				null);

		// Language Constructs List
		List<LogicTextViewTreeNode> languageConstructsChildren = new ArrayList<LogicTextViewTreeNode>();
		languageConstructsChildren.add(selectIf);
		languageConstructsChildren.add(skipIf);
		langConstructsFormatRecordFilter
				.setChildren(languageConstructsChildren);

		// comparison Operators
		LogicTextViewTreeNode lessThan = new LogicTextViewTreeNode(
				TreeItemId.LESSTHAN, "<",
				comparisonOperatorsFormatRecordFilter, " < ", 0,
				"Expression < Expression: Comparison Operator", null);
		LogicTextViewTreeNode greaterThan = new LogicTextViewTreeNode(
				TreeItemId.GREATERTHAN, ">",
				comparisonOperatorsFormatRecordFilter, " > ", 0,
				"Expression > Expression: Comparison Operator", null);
		LogicTextViewTreeNode equals = new LogicTextViewTreeNode(
				TreeItemId.EQUALSTO, "=",
				comparisonOperatorsFormatRecordFilter, " = ", 0,
				"Expression = Expression: Comparison Operator", null);
		LogicTextViewTreeNode greaterThanEqual = new LogicTextViewTreeNode(
				TreeItemId.GREATERTHANEQUALS, " >= ",
				comparisonOperatorsFormatRecordFilter, ">=", 0,
				"Expression >= Expression: Comparison Operator", null);
		LogicTextViewTreeNode lessThanEqual = new LogicTextViewTreeNode(
				TreeItemId.LESSTHANEQUALS, " <= ",
				comparisonOperatorsFormatRecordFilter, "<=", 0,
				"Expression <= Expression: Comparison Operator", null);
		LogicTextViewTreeNode notEqual = new LogicTextViewTreeNode(
				TreeItemId.NOTEQUALS, "<>",
				comparisonOperatorsFormatRecordFilter, " <> ", 0,
				"Expression <> Expression: Comparison Operator", null);

		// Comparison Operators Children List
		List<LogicTextViewTreeNode> comparisonOperatorsChildren = new ArrayList<LogicTextViewTreeNode>();
		comparisonOperatorsChildren.add(lessThan);
		comparisonOperatorsChildren.add(greaterThan);
		comparisonOperatorsChildren.add(equals);
		comparisonOperatorsChildren.add(lessThanEqual);
		comparisonOperatorsChildren.add(greaterThanEqual);
		comparisonOperatorsChildren.add(notEqual);
		comparisonOperatorsFormatRecordFilter
				.setChildren(comparisonOperatorsChildren);

		// Root
		List<LogicTextViewTreeNode> children = new ArrayList<LogicTextViewTreeNode>();
		children.add(keywordsMenu);
		children.add(columnsMenu);
		logicTextItemsRoot.setChildren(children);		
	}
	
	/* This is the root of the data supplied to the Logic Text Helper */
	@Override
	public Object[] getElements(Object inputElement) {
		logicTextEditorInput = ((LogicTextEditorInput) inputElement);
		logicTextType = logicTextEditorInput.getLogicTextType();
		if (logicTextItemsRoot == null) {
			logicTextItemsRoot = new LogicTextViewTreeNode(TreeItemId.ROOT,	"SAFR", null, null, 0, null, null);
			LogicTextViewTreeNode keywordsMenu = new LogicTextViewTreeNode(TreeItemId.KEYWORDS, "Keywords", logicTextItemsRoot, null, 0, null, null);

			// Keywords Menu
			LogicTextViewTreeNode functions = new LogicTextViewTreeNode(TreeItemId.FUNCTIONS, "Functions", keywordsMenu, null, 0, null, null);
			LogicTextViewTreeNode logicalOperators = new LogicTextViewTreeNode(TreeItemId.LOGICALOPERATORS, "Logical Operators", keywordsMenu, null, 0, null, null);
			LogicTextViewTreeNode arithmeticOperators = new LogicTextViewTreeNode(TreeItemId.ARITHMETICOPR, "Arithmetic Operators",	keywordsMenu, null, 0, null, null);
			LogicTextViewTreeNode stringOperators = new LogicTextViewTreeNode(TreeItemId.STRINGOPERATORS, "String Operators", keywordsMenu, null, 0, null, null);

			makeAndAddLogicalNodes(logicalOperators);
			makeAndAddStringNodes(stringOperators);
			makeAndAddArithmeticNodes(arithmeticOperators);
			getElementsForTheLogicType(keywordsMenu, functions, logicalOperators, arithmeticOperators);
		}
		return getChildren(logicTextItemsRoot);
	}

	private void getElementsForTheLogicType(LogicTextViewTreeNode keywordsMenu, LogicTextViewTreeNode functions,
			LogicTextViewTreeNode logicalOperators, LogicTextViewTreeNode arithmeticOperators) {
		if (logicTextType == LogicTextType.Extract_Record_Filter) {
			getElementsExtractFilter(keywordsMenu, functions, logicalOperators, arithmeticOperators);
		}
		else if (logicTextType == LogicTextType.Extract_Column_Assignment) {
			getElementsExtractColumn(keywordsMenu, functions, logicalOperators, arithmeticOperators,false);
		}
		else if (logicTextType == LogicTextType.Extract_Record_Output) {
		    getElementsExtractColumn(keywordsMenu, functions, logicalOperators, arithmeticOperators,true);
		}
		else if (logicTextType == LogicTextType.Format_Record_Filter) {
			getElementsFormatFilter(keywordsMenu, functions, logicalOperators, arithmeticOperators);
		}
		else if (logicTextType == LogicTextType.Format_Column_Calculation) {
			getElementsFormatColumn(keywordsMenu, functions, logicalOperators, arithmeticOperators);
		}
	}

	private void makeAndAddArithmeticNodes(LogicTextViewTreeNode arithmeticOperators) {
		// Arithmetic Operators
		LogicTextViewTreeNode add = new LogicTextViewTreeNode(TreeItemId.ADD, "+", arithmeticOperators, " + ", 0, "(Expression) + (Expression): Arithmetic Operator", null);
		LogicTextViewTreeNode minus = new LogicTextViewTreeNode(TreeItemId.MINUS, "-", arithmeticOperators, " - ", 0, "(Expression) - (Expression): Arithmetic Operator", null);
		LogicTextViewTreeNode mul = new LogicTextViewTreeNode(TreeItemId.MUL, "*", arithmeticOperators, " * ", 0, "(Expression) * (Expression): Arithmetic Operator", null);
		LogicTextViewTreeNode divide = new LogicTextViewTreeNode(TreeItemId.DIVIDE, "/", arithmeticOperators, " / ", 0, "(Expression) / (Expression): Arithmetic Operator", null);

		// Arithmetic Operators children List
		List<LogicTextViewTreeNode> arithmeticOperatorsChildren = new ArrayList<LogicTextViewTreeNode>();
		arithmeticOperatorsChildren.add(add);
		arithmeticOperatorsChildren.add(minus);
		arithmeticOperatorsChildren.add(mul);
		arithmeticOperatorsChildren.add(divide);
		arithmeticOperators.setChildren(arithmeticOperatorsChildren);
	}

	private void makeAndAddStringNodes(LogicTextViewTreeNode stringOperators) {
		//String Operators
		LogicTextViewTreeNode andop = new LogicTextViewTreeNode(TreeItemId.ANDOP, "&", stringOperators, " & ", 0, "(Expression) && (Expression): String Operator", null);
		
		List<LogicTextViewTreeNode> stringOperatorsChildren = new ArrayList<LogicTextViewTreeNode>();
		stringOperatorsChildren.add(andop);
		
		stringOperators.setChildren(stringOperatorsChildren);
	}

	private void makeAndAddLogicalNodes(LogicTextViewTreeNode logicalOperators) {
		// Logical Operators
		LogicTextViewTreeNode and = new LogicTextViewTreeNode(TreeItemId.AND, "AND", logicalOperators, " AND ", 5, " Expression AND Expression: Logical Operator", null);
		LogicTextViewTreeNode not = new LogicTextViewTreeNode(TreeItemId.NOT, "NOT", logicalOperators, " NOT ", 5, " NOT(Expression): Logical Operator", null);
		LogicTextViewTreeNode or = new LogicTextViewTreeNode(TreeItemId.OR, "OR", logicalOperators, " OR ", 4, " Expression OR Expression: Logical Operator", null);

		// Logical Operators Children List
		List<LogicTextViewTreeNode> logicalOperatorsChildren = new ArrayList<LogicTextViewTreeNode>();
		logicalOperatorsChildren.add(and);
		logicalOperatorsChildren.add(not);
		logicalOperatorsChildren.add(or);
		logicalOperators.setChildren(logicalOperatorsChildren);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	public void dispose() {

	}

}

class FieldChildren implements Comparator<LogicTextViewTreeNode> {
	  
    // override the compare() method
    public int compare(LogicTextViewTreeNode s1, LogicTextViewTreeNode s2)
    {
        if (s1.getTitleText().compareTo(s2.getTitleText())==0)
            return 0;
        else if (s1.getTitleText().compareTo(s2.getTitleText())>0)
            return 1;
        else
            return -1;
    }
}

class LookupPathFields implements Comparator<LogicTextViewTreeNode> {
	  
    // override the compare() method
    public int compare(LogicTextViewTreeNode s1, LogicTextViewTreeNode s2)
    {
        if (s1.getTitleText().compareTo(s2.getTitleText())==0)
            return 0;
        else if (s1.getTitleText().compareTo(s2.getTitleText())>0)
            return 1;
        else
            return -1;
    }
}
