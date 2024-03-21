package com.ibm.safr.we.ui.editors.view;

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


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.menus.IMenuService;

import com.ibm.safr.we.constants.Codes;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.EditRights;
import com.ibm.safr.we.constants.LogicTextType;
import com.ibm.safr.we.constants.OutputPhase;
import com.ibm.safr.we.constants.Permissions;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.exceptions.SAFRViewActivationException;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.SAFRAssociationList;
import com.ibm.safr.we.model.associations.FileAssociation;
import com.ibm.safr.we.model.associations.SAFRAssociationFactory;
import com.ibm.safr.we.model.base.SAFRComponent;
import com.ibm.safr.we.model.base.SAFREnvironmentalComponent;
import com.ibm.safr.we.model.query.ControlRecordQueryBean;
import com.ibm.safr.we.model.query.EnvironmentalQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.UserExitRoutineQueryBean;
import com.ibm.safr.we.model.view.View;
import com.ibm.safr.we.model.view.View.Property;
import com.ibm.safr.we.model.view.ViewColumn;
import com.ibm.safr.we.model.view.ViewColumnSource;
import com.ibm.safr.we.model.view.ViewSortKey;
import com.ibm.safr.we.model.view.ViewSource;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.commands.SourceProvider;
import com.ibm.safr.we.ui.editors.SAFREditorPart;
import com.ibm.safr.we.ui.editors.batchview.ActivateViewsEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditor;
import com.ibm.safr.we.ui.editors.logic.LogicTextEditorInput;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditor;
import com.ibm.safr.we.ui.editors.lr.LogicalRecordEditorInput;
import com.ibm.safr.we.ui.editors.view.ViewColumnEditor.PropertyViewType;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy.SAFRGUIContext;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.ui.views.metadatatable.MetadataView;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem;
import com.ibm.safr.we.ui.views.navigatortree.MainTreeItem.TreeItemId;
import com.ibm.safr.we.ui.views.vieweditor.ActivationLogViewNew;
import com.ibm.safr.we.ui.views.vieweditor.ColumnSourceView;
import com.ibm.safr.we.ui.views.vieweditor.DataSourceView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyTitleView;
import com.ibm.safr.we.ui.views.vieweditor.SortKeyView;
import com.ibm.safr.we.utilities.SAFRLogger;

/**
 * 
 */
public class ViewEditor extends SAFREditorPart implements IPartListener2 {

    static final Logger logger = Logger
    .getLogger("com.ibm.safr.we.ui.editors.view.ViewEditor");
    
	public static String ID = "SAFRWE.ViewEditor";

	private SAFRViewActivationException activateException;

	private  ScrolledForm form;
    private FormToolkit toolkit;
	private ToolBar toolBar;
	
	private CTabFolder tabFolder;
	private CTabItem tabCurrentView;
	private CTabItem tabViewProperties;
	private Composite compositeViewProperties;

	private CTabFolder tabFolderViewProperties;

	static final int MAX_USER_EXIT_PARAM = 32;
	static final int MAX_RECORDS = 9;

	private static String COMPONENTID = "ID";

	private Map<ComponentType, List<EnvironmentalQueryBean>> viewPropertiesMap = null;
	private SAFRAssociationList<FileAssociation> physicalFileAssociations = new SAFRAssociationList<FileAssociation>();

	private View view = null;
	private ViewEditorInput viewInput = null;
	private SAFRGUIToolkit safrToolkit;

	private IToolBarManager formToolbar;

	private ViewEditor viewEditor;

    private ViewMediator mediator = new ViewMediator();
    private IContextActivation activation;

    // store the expand state of the related view activation log
    private Object expandsOld[] = null;
    private Object expandsNew[] = null;
    
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		
		viewInput = (ViewEditorInput) getEditorInput();
		view = viewInput.getView();
		viewEditor = this.getEditor();
		if (view.getId() > 0) {
			addToRecentViewsList();
		}
	}

	private void addToRecentViewsList() {
		// save this view in the recent views preference.
		Preferences preference = SAFRPreferences.getSAFRPreferences();
		String rViews = preference.get(UserPreferencesNodes.RECENT_VIEWS, "");
		String viewName = UIUtilities.getComboString(view.getName(), view
				.getId());
		if (!rViews.equals("")) {
			// some views already exist.
			rViews = rViews.replace(viewName, ""); // remove current view
			// from old list
			rViews = viewName + ";" + rViews; // put the current view as the
			// first view
			String newrViews = "";
			int vCount = 1;
			for (String rView : rViews.split(";")) {
				if (vCount > 5) {
					break; // keep only first 5 views.
				}
				if (!rView.equals("")) {
					// add to new list
					newrViews += rView + ";";
					vCount++;
				}
			}
			preference.put(UserPreferencesNodes.RECENT_VIEWS, newrViews);
		} else {
			// this is the first view in recent views folder.
			preference.put(UserPreferencesNodes.RECENT_VIEWS, viewName);
		}
		try {
			preference.flush(); // save preferences
		} catch (BackingStoreException e) {
			UIUtilities.handleWEExceptions(e,
			    "Error occured while storing view information in recent views list.",null);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		boolean retval = false;
		
        //if not dealing with a new component 
		if(view.getId() > 0) {		    
            //if not dealing with a new component 
            //check with parent based on permissions
            return isSaveAsAllowed(Permissions.CreateView);                
		}
		return retval;
	}

	@Override
	public void createPartControl(Composite parent) {
		
	    mediator.setViewEditor(this);
	    List<ControlRecordQueryBean> allrec = SAFRQuery.queryAllControlRecords(view.getEnvironmentId(), SortType.SORT_BY_ID);
        if(allrec.isEmpty()) {
        	MessageDialog.openError(mediator.getSite().getShell(),
                    "No CR is present.","A Control Record is required before creating a View"); 
        	closeEditor();
        }
		toolkit = new FormToolkit(parent.getDisplay());
		safrToolkit = new SAFRGUIToolkit(toolkit);
		safrToolkit.setReadOnly(viewInput.getEditRights() == EditRights.Read);

		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FillLayout());
		FormData dataForm = new FormData();
		dataForm.top = new FormAttachment(0, 0);
		dataForm.bottom = new FormAttachment(100, 0);
		dataForm.left = new FormAttachment(0, 0);
		dataForm.right = new FormAttachment(100, 0);
		form.getBody().setLayoutData(dataForm);

		createTabFolder(form.getBody());
		refreshControls();
		setDirty(false);

		// Code to manage the form toolbar.
		formToolbar = (ToolBarManager) form.getToolBarManager();
		IMenuService ms = (IMenuService) getSite().getService(
				IMenuService.class);
		ms.populateContributionManager((ContributionManager) formToolbar,
				"toolbar:SAFRWE.com.ibm.safr.we.ui.editors.view.ViewEditor");
		formToolbar.update(true);
		toolBar = ((ToolBarManager) formToolbar).getControl();
		toolBar.setData(SAFRLogger.USER, "Toolbar");
		toolBar.addMouseListener(new MouseAdapter() {

			public void mouseDown(MouseEvent e) {
				ToolItem item = toolBar.getItem(new Point(e.x, e.y));
				if (item == null) {
					toolBar.setData(SAFRLogger.USER, "Toolbar");					
				}
				else {
					toolBar.setData(SAFRLogger.USER, "Toolbar " + item.getToolTipText());					
				}
			}
			
		});
		
		ManagedForm mFrm = new ManagedForm(toolkit, form);
		setMsgManager(mFrm.getMessageManager());
		getSite().getPage().addPartListener(this);
	}

	/**
	 * Method to create the main tab folder, containing the tabs 'View
	 * Properties' and 'Current View' (grid)
	 * 
	 * @param body
	 */
	private void createTabFolder(Composite body) {
		tabFolder = new CTabFolder(body, SWT.TOP);
		tabFolder.setLayout(new FormLayout());
		toolkit.adapt(tabFolder, true, true);

		createCompositeViewProperties();
		createCompositeViewColumns();
		if (view.getId() <= 0) {
			tabViewProperties = new CTabItem(tabFolder, SWT.NONE);
			tabViewProperties.setText("View Properties");
			tabViewProperties.setControl(compositeViewProperties);
			tabFolder.setSelection(tabViewProperties);
		} else {
			tabCurrentView = new CTabItem(tabFolder, SWT.NONE);
			tabCurrentView.setText("View Editor");
			tabCurrentView.setControl(mediator.getColumnEditorComposite());
			tabFolder.setSelection(tabCurrentView);
		}
		tabFolder.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
				if (tabFolder.getItem(0).getText().equals("View Properties")
						&& tabFolderViewProperties.getItemCount() > 3) {

					// retain the focus when traversed from one tab to another.
					mediator.setHFFocus();
				}
			}
		});
	}

	public void toggleViewEditorTab() {
		// toggle between view editor and view grid tabs

		// save the previous dirty flag
		boolean dirty = this.isDirty();

		String currTab = tabFolder.getItem(0).getText();
		// remove the current tab
		// we always only have one so can use index 0
		tabFolder.getItem(0).dispose();

		if (currTab.equals("View Properties")) {
            // store the current UI values in model
            refreshModel();                        
            
            tabCurrentView = new CTabItem(tabFolder, SWT.NONE);
            tabCurrentView.setText("View Editor");
            tabCurrentView.setControl(mediator.getColumnEditorComposite());
            tabFolder.setSelection(tabCurrentView);
            mediator.setColumnEditorFocus();
            refreshToolbar();
            refreshEditRights();
            if (mediator.copiedViewColumnsIsEmpty()) {
                UIUtilities.getSourceProvider().setPasteViewColumnAllowed(false);
            } else {
                UIUtilities.getSourceProvider().setPasteViewColumnAllowed(true);
            }
            UIUtilities.getSourceProvider().setEditorFocusView(true);		
            mediator.openFocusedView();
        } 
		else {
			tabViewProperties = new CTabItem(tabFolder, SWT.NONE);
			tabViewProperties.setText("View Properties");
			tabViewProperties.setControl(compositeViewProperties);
			tabFolder.setSelection(tabViewProperties);
            mediator.closePropertyView(PropertyViewType.NONE);
		}
		try {
			doRefreshControls();
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e,
			    "Unexpected error occurred while toggling between the view properties and view grid page.",null);
		}
		// reset dirty flag
		setDirty(dirty);
	}

	protected boolean isViewPropVisible() {
		return tabFolder.getItem(0).getText().equals("View Properties");
	}

	private void createCompositeViewProperties() {
		compositeViewProperties = safrToolkit.createComposite(tabFolder,
				SWT.NONE);
		FormLayout layoutComposite = new FormLayout();
		layoutComposite.marginTop = UIUtilities.TOPMARGIN;
		layoutComposite.marginBottom = UIUtilities.BOTTOMMARGIN;
		layoutComposite.marginLeft = UIUtilities.LEFTMARGIN;
		layoutComposite.marginRight = UIUtilities.RIGHTMARGIN;
		compositeViewProperties.setLayout(layoutComposite);

		FormData dataComposite = new FormData();
		dataComposite.bottom = new FormAttachment(100, 0);
		compositeViewProperties.setLayoutData(dataComposite);

		createViewPropertiesTabFolder(compositeViewProperties);

	}

	/**
	 * Method to create the View Properties tab folder, containing the tabs
	 * 'General' , 'Extract Phase' and 'Format Phase'
	 * 
	 * @param body
	 */
	private void createViewPropertiesTabFolder(Composite body) {
		tabFolderViewProperties = new CTabFolder(body, SWT.TOP);
		tabFolderViewProperties.setLayout(new FormLayout());
		FormData dataTabFolder = new FormData();
		dataTabFolder.left = new FormAttachment(0, 10);
		dataTabFolder.top = new FormAttachment(0, 20);
		tabFolderViewProperties.setLayoutData(dataTabFolder);

		toolkit.adapt(tabFolderViewProperties, true, true);
		ViewGeneralEditor genEditor = new ViewGeneralEditor(mediator, tabFolderViewProperties, view);
        mediator.setViewGeneralEditor(genEditor);
        genEditor.create();

        ViewExtractEditor extEditor = new ViewExtractEditor(mediator, tabFolderViewProperties, view);
        mediator.setViewExtractEditor(extEditor);
        extEditor.create();		
        mediator.enableExtractOutputFile(false);
        
        ViewFormatEditor forEditor = new ViewFormatEditor(mediator, tabFolderViewProperties, view);
        mediator.setViewFormatEditor(forEditor);
        forEditor.create();        

        ViewFormatReportEditor hfEditor = new ViewFormatReportEditor(mediator, tabFolderViewProperties, view);
        mediator.setViewFormatReportEditor(hfEditor);
		
        ViewFormatDelimitedEditor deEditor = new ViewFormatDelimitedEditor(mediator, tabFolderViewProperties, view);
        mediator.setViewFormatDelimitedEditor(deEditor);
        
        tabFolderViewProperties.setSelection(mediator.getGeneralTab());        
		tabFolderViewProperties.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {

			}

			public void widgetSelected(SelectionEvent e) {
			    CTabItem tabHeaderFooter = mediator.getHeaderFooterTab();
				if (tabHeaderFooter != null) {
					if (!tabHeaderFooter.isDisposed()) {
						if (tabFolderViewProperties.getItemCount() > 2) {
							mediator.setHFFocus();
						}
					}
				}
			}
		});
	}


    
	/**
	 * This method populates the various combo-boxes contained in the View
	 * Properties tab with values retrieved from the Map of View Properties
	 * lists.
	 * 
	 * @param componentType
	 *            the Component type, isntances of which are to be populated in
	 *            the combo-box
	 * @param combo
	 *            the combo-box to be populated
	 * @param allowBlank
	 *            a boolean parameter which determines whether the user can
	 *            select a blank value in the combo-box
	 */
	void populateViewCombos(ComponentType componentType,
			TableCombo combo, TableComboViewer comboViewer, boolean allowBlank) {
		Integer counter = 0;
		combo.getTable().removeAll();

		List<EnvironmentalQueryBean> componentList = new ArrayList<EnvironmentalQueryBean>();

		if (viewPropertiesMap != null && viewPropertiesMap.get(componentType) != null) {
			componentList.addAll(viewPropertiesMap.get(componentType));
		}
		if (allowBlank) {

			// Allow user to select a blank value
			EnvironmentalQueryBean blankElement = null;

			if (componentType == ComponentType.WriteUserExitRoutine
					|| componentType == ComponentType.FormatUserExitRoutine) {
				blankElement = new UserExitRoutineQueryBean(null, 0, "", null, null, null,
						EditRights.ReadModifyDelete, null, null, null, null);
			} else if (componentType == ComponentType.LogicalFile) {
				blankElement = new LogicalFileQueryBean(null, 0, "", EditRights.ReadModifyDelete, 
					null, null, null, null);
			}
			componentList.add(0, blankElement);
			combo.setData(Integer.toString(counter++), null);
		}
		comboViewer.setInput(componentList);
		comboViewer.refresh();
		if (componentList != null) {
			for (EnvironmentalQueryBean componentBean : componentList) {
				// combo.add(UIUtilities.getComboString(componentBean.getName(),
				// componentBean.getId()));
				combo.setData(Integer.toString(counter), componentBean);
				// Storing the ID of the component in the combo-box as well
				// in order to retrieve/select the value of the combo-box
				// based on the ID. The key in this case is a string
				// composed of the string literal COMPONENTID and the
				// counter value
				combo.setData(COMPONENTID + Integer.toString(counter),
						componentBean.getId());
				counter++;
			}
		}
	}

	/**
	 * This method populates the Physical File combo-box with all the Physical
	 * Files associated with the selected Logical File
	 * 
	 * @param comboLF
	 *            the Extract Phase or Format Phase Logical File combo-box,
	 *            based on whose selection the Physical File combo-box is to be
	 *            populated.
	 * @param comboPF
	 *            the Extract Phase or Format Phase Physical File combo-box,
	 *            which is to be populated.
	 */
	protected void populatePhysicalFileCombo(TableCombo comboLF,
			TableCombo comboPF, TableComboViewer comboPFViewer) {
		comboPF.getTable().deselectAll();

		try {
			if (comboLF.getTable().getSelectionIndex() >= 0) {
				LogicalFileQueryBean logicalFileBean = null;
				if (comboLF.getTable().getSelection().length > 0) {
					logicalFileBean = (LogicalFileQueryBean) comboLF.getTable()
							.getSelection()[0].getData();
				}
				if (logicalFileBean != null && logicalFileBean.getId() > 0) {
					physicalFileAssociations = SAFRAssociationFactory
							.getLogicalFileToPhysicalFileAssociations(logicalFileBean);
				} else {
					physicalFileAssociations = null;
				}
			} else {
				// if nothing is selected in logical file combo, nothing should
				// be loaded in physical file combo
				physicalFileAssociations = null;
			}
		} catch (DAOException de) {
			UIUtilities.handleWEExceptions(de,
			    "Database error getting Logical File To Physical File Associations.",UIUtilities.titleStringDbException);
		} catch (SAFRException e1) {
			UIUtilities.handleWEExceptions(e1,"Error getting Logical File To Physical File Associations.",null);
		}

		Integer counter = 0;
		comboPF.getTable().removeAll();

		if (physicalFileAssociations != null) {
			comboPFViewer.setInput(physicalFileAssociations);
			comboPFViewer.refresh();
			comboPF.select(-1);
			for (FileAssociation association : physicalFileAssociations) {
				// This method stores the FileAssociation object, as opposed
				// to the QueryBean object stored in other combo-boxes
				comboPF.setData(Integer.toString(counter), association);
				counter++;
			}
		} else {
			// cleare the PF combo
			comboPFViewer.setInput(new Object[0]);
			comboPFViewer.refresh();
			comboPF.select(-1);
		}

	}

	@Override
	public void setFocus() {
		super.setFocus();
		if (isViewPropVisible()) {
	        mediator.setFocus();
		}
	}

	@Override
	public String getModelName() {
		return "View";
	}

	@Override
	public void doRefreshControls() throws SAFRException {
		if (isViewPropVisible()) {
		    getViewPropertiesMap();
            // for general.
            mediator.populateGeneralCombos();
            mediator.refreshControlsGeneral();
            
			// for extract.
            mediator.populateExtractCombos();
			mediator.loadExtractPhase();
			
			// format phase
			mediator.refreshControlsFormat();
			// refresh header text of opened LT editors related to this
			// view.
			refreshRelatedLogicTextEditorsHeaders();
			mediator.refreshControlsExtract();

		}
		statusText();
	}

    /**
	 * Refreshes logic text editors that are related to this view.
	 * 
	 * @throws SAFRException
	 */
	protected void refreshRelatedLogicTextEditorsHeaders() throws SAFRException {
		IEditorReference[] editors = getEditorSite().getPage()
				.getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			if (editors[i].getEditor(false) instanceof LogicTextEditor) {
				// if the editor is of type logic text editor.
				LogicTextEditorInput input = (LogicTextEditorInput) editors[i]
						.getEditor(false).getEditorInput();
				if (input.getView().getId().equals(view.getId())) {
					// if the logic text editor opened is related to
					// current view, refresh its form headers.
					((LogicTextEditor) (editors[i].getEditor(false)))
							.refreshEditorHeaders();
				}
			}
		}

	}

    @Override
    public void refreshModel() {
        mediator.refreshModel();
    }
	
	@Override
	public void storeModel() throws DAOException, SAFRException {
		storeModel(true);
		refreshToolbar();
	}

	@Override
	public void validate() throws DAOException, SAFRException {
		view.validate();
		getMsgManager().removeAllMessages();
	}

	private void createCompositeViewColumns() {
        ViewColumnEditor columnEditor = new ViewColumnEditor(mediator, tabFolder, view);
        mediator.setViewColumnEditor(columnEditor);             	    
        columnEditor.create();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		// close view editor and logic text editors without saving.
		IEditorReference[] editors = getEditorSite().getPage()
				.getEditorReferences();
		List<IEditorReference> lTeditors = new ArrayList<IEditorReference>();
		for (int i = 0; i < editors.length; i++) {
			IEditorReference editorPart = editors[i];
			if (editorPart.getEditor(false) instanceof LogicTextEditor) {
				// if the editor is of type logic text editor.
				if (((LogicTextEditorInput) ((LogicTextEditor) (editorPart
						.getEditor(false))).getEditorInput()).getView().getId() == this.view
						.getId()) {
					// if the logic text editor opened is related to
					// current view.
					lTeditors.add(editorPart);
				}
			}
		}
		getEditorSite().getPage().closeEditors(
				lTeditors.toArray(new IEditorReference[lTeditors.size()]),
				false);
		getSite().getPage().removePartListener(this);
	}

	/**
	 * Inserts column to left of current highlighted column.
	 * 
	 * @param refresh
	 *            pass true if the grid is to be refreshed after inserting
	 *            viewColumn.
	 * @return newly added viewColumn reference.
	 */
	public ViewColumn insertColumnBefore(Boolean refresh) {
	    return mediator.insertColumnBefore(refresh);
	}

    public ViewColumn insertColumnAfter(boolean refresh) {
        return mediator.insertColumnAfter(refresh);        
    }

	
    /**
     * 
     * Closes all logic text editors without saving that are related to the
     * specified component.
     * 
     * @param component
     */
    public void closeRelatedLogicTextEditors(SAFREnvironmentalComponent component) {
        // Close related LT editors without saving.
        IEditorReference[] editors = getSite().getPage().getEditorReferences();
        List<IEditorReference> lTeditorsToClose = new ArrayList<IEditorReference>();
        for (int i = 0; i < editors.length; i++) {
            IEditorReference editorPart = editors[i];
            if (editorPart.getEditor(false) instanceof LogicTextEditor) {
                // if the editor is of type logic text editor.
                LogicTextEditorInput lTEInput = ((LogicTextEditorInput) ((LogicTextEditor) (editorPart
                        .getEditor(false))).getEditorInput());
                if (lTEInput.getView().getId().equals(this.view.getId())) {
                    // if the logic text editor opened is related to
                    // current view.

                    if (lTEInput.getLogicTextType() == LogicTextType.Format_Column_Calculation) {
                        if (lTEInput.getViewColumn().equals(component)) {
                            lTeditorsToClose.add(editorPart);
                        }
                    } else if (lTEInput.getLogicTextType() == LogicTextType.Extract_Column_Assignment) {
                        if ((lTEInput.getViewColumnSource().getViewColumn()
                                .equals(component))
                                || (lTEInput.getViewColumnSource()
                                        .getViewSource().equals(component))
                                || (lTEInput.getViewColumnSource()
                                        .equals(component))) {
                            lTeditorsToClose.add(editorPart);
                        }
                    } else if (lTEInput.getLogicTextType() == LogicTextType.Extract_Record_Filter) {
                        if (lTEInput.getViewSource().equals(component)) {
                            lTeditorsToClose.add(editorPart);
                        }
                    } else if (lTEInput.getLogicTextType() == LogicTextType.Extract_Record_Output) {
                        if (lTEInput.getViewSource().equals(component)) {
                            lTeditorsToClose.add(editorPart);
                        }
                    } else if (lTEInput.getLogicTextType() == LogicTextType.Format_Record_Filter) {
                        if (component instanceof View) {
                            lTeditorsToClose.add(editorPart);
                        }
                    }
                }
            }
        }
        getSite().getPage().closeEditors(
                lTeditorsToClose.toArray(new IEditorReference[lTeditorsToClose
                        .size()]), false);
    }    

	public void addColumn() {
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));

			mediator.addNewColumn(-1, true);
			/*
			 * Jaydeep March 9, 2010 CQ 7615 : Added Source provider
			 * Implementation.
			 */
			refreshToolbar();

		} finally {
			getSite().getShell().setCursor(null);
		}
	}

	public void moveColumnRight() {
        mediator.moveColumnRight();
	}

	public void moveColumnLeft() {
	    mediator.moveColumnLeft();
	}

	public int getCurrentColIndex() {
	    return mediator.getCurrentColIndex();
	}

	public ViewColumn getCurrentColumn() {
		return mediator.getCurrentColumn();
	}

	/**
	 * Checks if the selected cell is a Sort Key.
	 * 
	 * @return true if sort key cell is selected.
	 */
	public boolean isCurrentSortKey() {
	    return mediator.isCurrentSortKey();
	}

	/**
	 * Returns the currently selected sort key.
	 * 
	 * @return current sort key or null if no sort key is selected.
	 */
	public ViewSortKey getCurrentSortKey() {
	    return mediator.getCurrentSortKey();
	}

	/**
	 * Checks if the selected cell is a View Source.
	 * 
	 * @return true if view source cell is selected.
	 */
	public boolean isCurrentViewSource() {
	    return mediator.isCurrentViewSource();
	}

	/**
	 * Returns the currently selected view source.
	 * 
	 * @return current view source or null if no view source is selected.
	 */
	public ViewSource getCurrentViewSource() {
	    return mediator.getCurrentViewSource();
	}

	/**
	 * Checks if the selected cell is a View Column Source.
	 * 
	 * @return true if View Column Source cell is selected.
	 */
	public boolean isCurrentViewColumnSource() {
	    return mediator.isCurrentViewColumnSource();
	}

	/**
	 * Returns the currently selected View Column Source.
	 * 
	 * @return current View Column Source or null if no View Column Source is
	 *         selected.
	 */
	public ViewColumnSource getCurrentViewColumnSource() {
	    return mediator.getCurrentViewColumnSource();
	}

	/**
	 * This method updates formula rows after change in Logic Text of Extract
	 * Column Assignment.
	 */
	public void updateFormulaRows() {
		List<ViewSource> activeItems = this.view.getViewSources()
				.getActiveItems();
		for (Iterator<ViewSource> iterator = activeItems.iterator(); iterator.hasNext();) {
			ViewSource viewSource = (ViewSource) iterator.next();
			mediator.updateColumnSourceAffectedRows(viewSource);
		}
	}

	/**
	 * This method updates Format Phase Calculation row. Should be called by the
	 * logic text editor when the user edits the format phase calculation and
	 * saves the logic text.
	 */
	private ViewEditor getEditor() {
		return this;
	}

    protected ViewEditorInput getViewInput() {
        return viewInput;
    }
	
	/**
	 * This method updates row with given propertyId. It only works on static
	 * rows. This method cannot be used with dynamic rows
	 * 
	 * @param propertyId
	 *            : property ID of the row to be updated.
	 */
	public void updateElement(int propertyId) {
	    mediator.updateColumnEditorElement(propertyId);
	}

	/**
	 * Updates all sort key rows. This includes sort key header and sort key
	 * rows below it.
	 */
	public void updateSortKeyRows() {
	    mediator.updateSortKeyRows();
	}

	/**
	 * Updates the data source row.
	 * 
	 * @param viewSource
	 *            the data source whose row is to be updated.
	 */
	public void updateColumnSourceAffectedRows(ViewSource viewSource) {
	    mediator.updateColumnSourceAffectedRows(viewSource);
	}

	/**
	 * Updates the rows of the View affected by changing the Source LR Field or
	 * the Lookup Field of the Column Source, after these values are copied into
	 * the View Column.
	 */
	public void updateColumnSourceAffectedRows() {
	    mediator.updateColumnSourceAffectedRows();
	}

	/**
	 * Updates editor rows affected by a change made in output format, output
	 * format type and format phase usage.
	 */
	public void updateOutputInformationChangeAffectedRows(
			boolean updatePropertyViews) {
	}

	// To fix RTC18967 
	private ToolBar setCursorWaiting() {
		
		getSite().getShell().setCursor(
				getSite().getShell().getDisplay().getSystemCursor(
						SWT.CURSOR_WAIT));
		
		ToolBar toolBar = null;
		try {
			Field f = formToolbar.getClass().getDeclaredField("toolBar"); 
			f.setAccessible(true);
			toolBar = (ToolBar) f.get(formToolbar);
			toolBar.setCursor(getSite().getShell().getDisplay().getSystemCursor(
					SWT.CURSOR_WAIT));
		} catch (Exception e1) {
            UIUtilities.handleWEExceptions(e1);
		}
		return toolBar;
	}
	
	private void setCursorNormal(ToolBar toolBar) {
		// restore cursor to original one
		getSite().getShell().setCursor(null);
		if (toolBar != null) {
			toolBar.setCursor(getSite().getShell().getDisplay().getSystemCursor(
					SWT.CURSOR_HAND));
		}		
	}
	
	public boolean activateView() {
		ToolBar toolBar = setCursorWaiting();
		try {

			// Determine read only access
			boolean readOnly = false; 
            try {
    			if (view.getId() > 0 && SAFRApplication.getUserSession().isOrdinaryUser()) {
                    EditRights rights = SAFRApplication.getUserSession().getEditRights(ComponentType.View, view.getId());
                    if (rights == EditRights.Read) {
                        readOnly = true;
                    }
    			}
            } catch (DAOException e) {
                UIUtilities.handleWEExceptions(e,
                    "A database error occured while performing this operation.",UIUtilities.titleStringDbException);
            } catch (SAFRException e) {
                UIUtilities.handleWEExceptions(e,
                    "An error occurred while performing this operation.",null);
            }
			
			boolean activated = false;

			if (isActive()) {
				int confirm = 1;
				MessageDialog dialog = new MessageDialog(getSite().getShell(),
						"Inactivate View", null,
						"Are you sure you want to make current view Inactive?",
						MessageDialog.QUESTION,
						new String[] { "&OK", "&Cancel" }, 0);
				confirm = dialog.open();
				if (confirm == 0) {
				    if (readOnly) {
			            this.setPartName("*" + viewInput.getName());
			            view.makeViewInactive();
			            statusText();			            
				    }
				    else {
                        view.makeViewInactive();
	                    setModified(true);				        
				    }
				} else {
					return false;
				}

			} else {
				try {
					// clear previous activation errors first
					activateException = null;
					view.activate(getSite());
					activated = true;
					// view activated without messages so close logView
					closeActivationLog();
					if (readOnly) {
                        this.setPartName("*" + viewInput.getName());
					}
					else {
					    setDirty(true);
					}
				} catch (DAOException e) {
					UIUtilities.handleWEExceptions(e,
					    "A database error occured while performing this operation.",UIUtilities.titleStringDbException);
				} catch (SAFRViewActivationException e) {
					activateException = e;
					openActivationLog();
					if (!e.hasErrorOccured()) {
						activated = true;
						if (readOnly) {
	                        this.setPartName("*" + viewInput.getName());
						}
						else {
						    setDirty(true);
						}						
					}
				} catch (SAFRException e) {
					// show exception error message.
					UIUtilities.handleWEExceptions(e,"An error occurred while activating this view. ",null);
				}
			}
			statusText();
			return activated;
		} finally {
			updateElement(ViewColumnEditor.STARTPOS);
			updateElement(ViewColumnEditor.COLUMN_PROP_HEADER);
			setCursorNormal(toolBar);
		}
	}

	public void openActivationLog() {
		// Activation error. store in local variable so that the
		// View error table can use it. Also open the error RCP
		// view.
		if (viewActivationMessageExistsNew()) {
			try {
				IWorkbenchPage page = getSite().getPage();
				if(page != null) {
					ActivationLogViewNew eView = (ActivationLogViewNew) page.showView(ActivationLogViewNew.ID);
					eView.setViewEditor(true);
					eView.showGridForCurrentEditor(this);
					eView.setExpands(expandsNew);
				}
			} catch (PartInitException e1) {
				UIUtilities.handleWEExceptions(
				    e1,"Unexpected error occurred while opening activation errors view.",null);
			}
		}
	}
	
	public void closeActivationLog() {
		if(getSite().getPage() != null) {
			ActivationLogViewNew logView = (ActivationLogViewNew)getSite().getPage().findView(ActivationLogViewNew.ID);
			if (logView != null && logView.isViewEditor()) {
				if (viewActivationMessageExistsNew()) {
					expandsNew = logView.getExpands();
				}
				else {
					expandsNew = null;
				}
				getSite().getPage().hideView(logView);
			}		
		}
	}
	
	public boolean viewActivationMessageExistsNew() {
		return (activateException != null && !activateException.getActivationLogNew().isEmpty());
	}

	public SAFRViewActivationException getViewActivationException() {
		return activateException;
	}

	@Override
	public int promptToSaveOnClose() {

		IEditorPart[] dirtyEditors = getEditorSite().getPage()
				.getDirtyEditors();
		boolean relatedLTeditorFound = false;// this flag is set if the view's
		// related dirty LT editor is
		// found.
		for (int i = 0; i < dirtyEditors.length; i++) {
			IEditorPart editorPart = dirtyEditors[i];
			if (editorPart instanceof LogicTextEditor) {
				// if the editor is of type logic text editor.
				if (((LogicTextEditorInput) ((LogicTextEditor) editorPart)
						.getEditorInput()).getView().getId() == this.view
						.getId()) {
					// if the logic text editor opened is related to
					// current view then set flag.
					relatedLTeditorFound = true;
					break;
				}
			}
		}
		String msgDialogString = "";
		// create dialog's message according to related LT editor are found or
		// not.
		if (relatedLTeditorFound) {
			msgDialogString = "Do you want to save changes made to "
					+ getPartName()
					+ " and View's related unsaved Logic Text Editors?";

		} else {
			msgDialogString = "Do you want to save changes made to "
					+ getPartName() + "?";
		}

		MessageDialog dialog = new MessageDialog(getSite().getShell(),
				"Save Changes?", null, msgDialogString, MessageDialog.QUESTION,
				new String[] { "&Yes", "&No", "&Cancel" }, 0);

		int returnVal = dialog.open();
		// Display an hour glass till editor is closed.
		Display.getCurrent().getActiveShell().setCursor(
				Display.getCurrent().getActiveShell().getDisplay()
						.getSystemCursor(SWT.CURSOR_WAIT));

		if (returnVal == 0) {
			// 'Yes' pressed, try to validate and save view editors and related
			// unsaved logic text editors.
			try {
				refreshModel();
				validate();
				storeModel(false);
				setDirty(false);
				refreshMetadataView();
				returnVal = ISaveablePart2.NO; // already saved. return no to
				// continue with other editors.
			} catch (SAFRValidationException e) {
				if (!(e.getMessageString().equals(""))) {
					MessageDialog.openError(getSite().getShell(),
							"Error saving " + getModelName() + ".", e
									.getMessageString());
					setDirty(false);
				}
				returnVal = ISaveablePart2.CANCEL; // allow users to modify data
			} catch (SAFRException e) {
				setDirty(false);
                UIUtilities.handleWEExceptions(e);
				returnVal = ISaveablePart2.CANCEL; // allow users to modify data
			}
		} else if (returnVal == 1) {
			// 'No' pressed, just return the code back.
			// close view editor and logic text editors without saving.

			IEditorReference[] editors = getEditorSite().getPage()
					.getEditorReferences();

			List<IEditorReference> lTeditors = new ArrayList<IEditorReference>();

			for (int i = 0; i < editors.length; i++) {
				IEditorReference editorPart = editors[i];
				if (editorPart.getEditor(false) instanceof LogicTextEditor) {
					// if the editor is of type logic text editor.

					if (((LogicTextEditorInput) ((LogicTextEditor) (editorPart
							.getEditor(false))).getEditorInput()).getView()
							.getId() == this.view.getId()) {
						// if the logic text editor opened is related to
						// current view.
						lTeditors.add(editorPart);
					}
				}
			}

			getEditorSite().getPage().closeEditors(
					lTeditors.toArray(new IEditorReference[lTeditors.size()]),
					false);
			returnVal = ISaveablePart2.NO;
		} else {
			// 'Cancel' pressed, just return the code back.
			returnVal = ISaveablePart2.CANCEL;
		}

		// return cursor to normal
		Display.getCurrent().getActiveShell().setCursor(null);

		return returnVal;
	}

	/**
	 * @param confirm
	 *            true if the save confirmation dialog is to be shown for
	 *            unsaved logic text editors on saving of the view editor.
	 * @throws DAOException
	 * @throws SAFRException
	 */
	private void storeModel(boolean confirm) throws SAFRException, DAOException {
		if (confirm) {
			// if confirmation is true then save all dirty logic text editors.
			IEditorPart[] dirtyEditors = getEditorSite().getPage()
					.getDirtyEditors();
			boolean msgFlg = true;
			for (int i = 0; i < dirtyEditors.length; i++) {
				IEditorPart editorPart = dirtyEditors[i];
				if (editorPart instanceof LogicTextEditor) {
					// if the editor is of type logic text editor.
					if (((LogicTextEditorInput) ((LogicTextEditor) editorPart)
							.getEditorInput()).getView().getId() == this.view.getId()) {
						// if the logic text editor opened is related to
						// current view.
						if (msgFlg) {
							// show confirmation dialog for the 1st time an
							// unsaved logic text editor is found.
							msgFlg = false;
							MessageDialog dialog = new MessageDialog(
									getSite().getShell(),
									"Save View",
									null,
									"This View has modified Logic Text which must be saved with the View data. Do you want to continue?",
									MessageDialog.QUESTION, new String[] {
											"&OK", "&Cancel" }, 0);
							int confirmSaveLT = dialog.open();
							if (confirmSaveLT == 1) {
								throw new SAFRValidationException();
							}
						}
						((LogicTextEditor) editorPart).doSave(ApplicationMediator.getAppMediator()
								.getStatusLineManager().getProgressMonitor());
					}
				}
			}
		} else {
			// on closing of view editor, save and close related unsaved Logic
			// text editors.

			IEditorReference[] editors = getEditorSite().getPage().getEditorReferences();

			List<IEditorReference> lTeditors = new ArrayList<IEditorReference>();

			for (int i = 0; i < editors.length; i++) {
				IEditorReference editorPart = editors[i];
				if (editorPart.getEditor(false) instanceof LogicTextEditor) {
					// if the editor is of type logic text editor.

					if (((LogicTextEditorInput) ((LogicTextEditor) (editorPart
							.getEditor(false))).getEditorInput()).getView()
							.getId() == this.view.getId()) {
						// if the logic text editor opened is related to
						// current view.
						if (editorPart.getEditor(false).isDirty()) {
							((LogicTextEditor) editorPart.getEditor(false)).doSave(
							    ApplicationMediator.getAppMediator().getStatusLineManager().getProgressMonitor());
						}

						lTeditors.add(editorPart);
					}
				}
			}
			getEditorSite().getPage().closeEditors(
					lTeditors.toArray(new IEditorReference[lTeditors.size()]),
					true);
		}
		// save SAFR view.
		boolean newView = view.getId() <= 0;
		view.setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(
									SAFRGUIContext.MODEL));
		view.store();
		if (newView) {
			// a new view, when saved, should be added to recent views list.
			addToRecentViewsList();
		}
        // update menus 
        UIUtilities.enableDisableBatchViewMenu();
        
        ApplicationMediator.getAppMediator().refreshMetaBar();                      
	}

	/**
	 * This method is used as command handler of insert new View Source.
	 */
	public void insertDataSource() {
	    mediator.insertDataSource();
	}

    /**
     * This method is used as command handler of copy View Source.
     */
    public void copyDataSource() {
        mediator.copyDataSource();
    }

    /**
     * This method is used as command handler of copy View Source.
     */
    public void columnGenerator() {
        mediator.columnGenerator();
    }
    
	/**
	 * This method deletes an item from view editor grid depending upon where
	 * the mouse is clicked.Used in command handler of Delete popup menu.
	 */
	public void delete() {
	    mediator.deleteColumnEditorItem();
	}

	protected void updateSortKeyViews() {
		SortKeyTitleView sktView = (SortKeyTitleView) getSite().getPage()
				.findView(SortKeyTitleView.ID);
		if (sktView != null) {
			sktView.showGridForCurrentEditor(viewEditor, false);
		}
		SortKeyView skView = (SortKeyView) getSite().getPage().findView(
				SortKeyView.ID);
		if (skView != null) {
			skView.showGridForCurrentEditor(viewEditor, false);
		}

	}

	protected void updateDataSourceViews() {
		DataSourceView dsView = (DataSourceView) getSite().getPage().findView(
				DataSourceView.ID);
		if (dsView != null) {
			dsView.showGridForCurrentEditor(viewEditor);
		}
		ColumnSourceView csView = (ColumnSourceView) getSite().getPage()
				.findView(ColumnSourceView.ID);
		if (csView != null) {
			csView.showGridForCurrentEditor(viewEditor);
		}

	}

	public void addRemoveSortKey() {
		try {
			getSite().getShell().setCursor(
					getSite().getShell().getDisplay().getSystemCursor(
							SWT.CURSOR_WAIT));
			// return if there are no view columns defined yet
			if (view.getViewColumns().getActiveItems().size() <= 0) {
				return;
			}

			if (getCurrentColIndex() < 0) {
				return;
			}
			ViewColumn col = getCurrentColumn();

			if (col.isSortKey()) {
				mediator.removeSortKey();
			} else {
				mediator.addSortKey();
			}
			updateElement(ViewColumnEditor.RECORDAGGREGATIONFUNCTION);
			updateElement(ViewColumnEditor.GROUPAGGREGATIONFUNCTION);
			updateElement(ViewColumnEditor.FORMATPHASECALCULATION);
			updateElement(ViewColumnEditor.VISIBLE);

			updateSortKeyViews();
		} finally {
			getSite().getShell().setCursor(null);
		}
	}

	/**
	 * Refreshes the sort key rows. this will first remove all sort key rows and
	 * will than add again using the latest sort key list from View model. This
	 * function will also update the sort key header row.
	 */
	public void refreshSortKeyRows() {
	    mediator.refreshSortKeyRows();
	}

	/**
	 * Method to set the dirty flag. When the view editor is marked dirty, its
	 * also set to inactive, the toolbar button should reflect this. This method
	 * should be used by all functions who needs to set the editor dirty.
	 * 
	 * @param dirty
	 */
	public void setModified(Boolean dirty) {
		super.setDirty(dirty);
		statusText();
	}

	/**
	 * Returns true if the view shown by this editor is active.
	 * 
	 * @return
	 */
	public boolean isActive() {
		return (view.getStatusCode() != null && view.getStatusCode().getGeneralId() == Codes.ACTIVE);
	}

	/**
	 * Returns the active or inactive state of the view.
	 */
	public void statusText() {
	    
        String status = null;
        if (view.getStatusCode() != null) {
            status = view.getStatusCode().getDescription();
            status = "[" + status + "] ";
        }

        if (view.getName() == null) {
            form.setText(status);           
        } else {
            form.setText(status + view.getName());         
        }       
	}

	public void copyViewColumns() {
	    mediator.copyViewColumns();
	}

	public void pasteViewColumnsLeft() {
	    mediator.pasteViewColumnsLeft();
	}

	public void pasteViewColumnsRight() {
	    mediator.pasteViewColumnsRight();
	}

    public void genLRFromView() {
        ApplicationMediator.getAppMediator().waitCursor();
        String currTab = tabFolder.getItem(0).getText();
        LogicalRecord logicalRecord = SAFRApplication.getSAFRFactory().createLogicalRecord();
        LogicalRecordEditorInput input = new LogicalRecordEditorInput(
            logicalRecord, EditRights.ReadModify);
        List<ViewColumn> columns = new ArrayList<ViewColumn>();
        if (currTab.equals("View Properties")) {
            // gen from whole view
            columns.addAll(view.getViewColumns().getActiveItems());
        } else {
            // gen from selected columns
            List<ViewColumn> sel = mediator.getSelectedColumns();
            if (sel.size() > 1) { 
                columns.addAll(sel);
            } else {
                columns.addAll(view.getViewColumns().getActiveItems());                
            }
        }
        
        // add columns as fields to LR
        for (ViewColumn column : columns) {
            // possibly filter out non visible columns
            if (view.getOutputPhase() == OutputPhase.Format && !column.isVisible()) {
                continue;
            }
            logicalRecord.addViewColumnAsField(column);
        }
        
        ApplicationMediator.getAppMediator().normalCursor();
        
        try {
            LogicalRecordEditor editor = (LogicalRecordEditor)getSite().getPage().openEditor(input, LogicalRecordEditor.ID);
            editor.setDirty(true);
        } catch (PartInitException e) {
            UIUtilities.handleWEExceptions(e,
                "Unexpected error occurred while opening logical record editor.",null);
        }                    
    }
	
	/**
	 * This method is used to get the widget based on the property passed.
	 * 
	 * @param property
	 * @return the widget.
	 */
	protected Control getControlFromProperty(Object property) {
	    
		return mediator.getControlFromProperty(property);
	}

	@Override
	public ComponentType getEditorCompType() {
		return ComponentType.View;
	}

	@Override
	public SAFRComponent getModel() {
		return view;
	}

	@Override
	public void refreshMetadataView() {
		MainTreeItem navTreeSelection = ApplicationMediator.getAppMediator()
				.getCurrentNavItem();
		// if view folder is not selected in Navigator View then no need to refresh.
		if ((!navTreeSelection.getId().equals(TreeItemId.VIEWFOLDERCHILD))) {
			return;
		}

		MetadataView metadataview = (MetadataView) (PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.findView(MetadataView.ID));
		try {
			if (metadataview != null) {
				metadataview.refreshView();
			}
		} catch (SAFRException e) {
			UIUtilities.handleWEExceptions(e, "Error occurred while refreshing the metadata list.", null);
		}
	}

	/* Jaydeep March 9, 2010 CQ 7615 : Added Source provider Implementation. */
	protected void refreshToolbar() {
		int index = getCurrentColIndex();
		SourceProvider service = UIUtilities.getSourceProvider();
		if (index <= 1) {
			service.setMoveViewColumnLeftAllowed(false);
		} else {
			service.setMoveViewColumnLeftAllowed(true);
		}
		if ((index < 0) || ((index) >= mediator.getColumnCount())) {
			service.setMoveViewColumnRightAllowed(false);
		} else {
			service.setMoveViewColumnRightAllowed(true);
		}

		// check if no column is added related to CQ8401
		if (mediator.getColumnCount() > 0) {
			service.setViewGridMenu(true);
		} else {
			service.setViewGridMenu(false);
		}
				
        EditRights rights = UIUtilities.getEditRights(view.getId(), ComponentType.View);
        if (rights.equals(EditRights.ReadModify) || rights.equals(EditRights.ReadModifyDelete)) {
            service.setActivationAllowed(true);
        }
        else {
            service.setActivationAllowed(false);            
        }
        
        if (mediator.getColumnCount() == 0 ||
            index != -1) {
            service.setInsertViewColumnAllowed(true);
        } else {
            service.setInsertViewColumnAllowed(false);
        }
        
        		
	}

	protected void refreshEditRights() {
		SourceProvider sourceProvider = UIUtilities.getSourceProvider();
		if (viewInput.getEditRights() == EditRights.Read) {
			sourceProvider.setAllowEdit(false);
		} else {
			sourceProvider.setAllowEdit(true);
		}
	}

	public void partActivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false).equals(this)) {
			/*
			 * Jaydeep April 13, 2010 CQ 7615 : Added Source provider
			 * Implementation.
			 */
			if (!isViewPropVisible()) {
				mediator.setColumnEditorFocus();
				refreshToolbar();
				if (mediator.copiedViewColumnsIsEmpty()) {
					UIUtilities.getSourceProvider().setPasteViewColumnAllowed(false);
				} else {
					UIUtilities.getSourceProvider().setPasteViewColumnAllowed(true);
				}
			}
			refreshEditRights();

			SourceProvider sourceProvider = UIUtilities.getSourceProvider();
			sourceProvider.setEditorFocusLR(false);
			sourceProvider.setEditorFocusLookupPath(false);
			sourceProvider.setEditorFocusView(true);
			sourceProvider.setEditorFocusEnv(false);
			
			ApplicationMediator.getAppMediator().updateStatusContribution(
			    ApplicationMediator.STATUSBARVIEW, "View Length: " + Integer.toString(view.getViewLength()), true);
			
			IContextService contextService = (IContextService)PlatformUI.getWorkbench()
			    .getService(IContextService.class);
			activation = contextService.activateContext("vieweditor.context");
		}
	}

	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	public void partClosed(IWorkbenchPartReference partRef) {		
        closeActivationLog();
        mediator.closePropertyView(PropertyViewType.NONE);
}

	public void partDeactivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false).equals(this)) {
			SourceProvider sourceProvider = UIUtilities.getSourceProvider();
			sourceProvider.setEditorFocusView(false);
	        if (getSite().getWorkbenchWindow().getActivePage().getEditorReferences() != null) {
	            
	            IEditorReference[] openEditors = getSite().getWorkbenchWindow().getActivePage().getEditorReferences();
				if (openEditors.length == 0) {
					sourceProvider.setEditorFocusLR(false);
					sourceProvider.setEditorFocusLookupPath(false);
					sourceProvider.setEditorFocusEnv(false);
				}
	        }	 
            ApplicationMediator.getAppMediator().updateStatusContribution(ApplicationMediator.STATUSBARVIEW, "", false);
            activation.getContextService().deactivateContext(activation);
		}			
	}

	public void partHidden(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) { 
        	Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
		            mediator.closePropertyView(PropertyViewType.NONE);
		            closeActivationLog();
				}
        	});        	    		
        }      
	}

	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	public void partOpened(IWorkbenchPartReference partRef) {
	}

	public void partVisible(IWorkbenchPartReference partRef) {
        if (partRef.getPart(false).equals(this)) {		
        	Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
				    if (!tabFolder.getItem(0).isDisposed() && tabFolder.getItem(0).getText().equals("View Editor")) {
				        mediator.openFocusedView();
				    }
	                initActivationState();
	                openActivationLog();
				}
        	});
        }
	}
	
	private void initActivationState() {
		if (activateException == null) {
			// check for activation state in batch activator
			IWorkbenchPage page = getSite().getPage();
			if(page != null) {
				for (IEditorReference ref : page.getEditorReferences()) {
					IEditorPart editor = ref.getEditor(false);
					if (editor instanceof ActivateViewsEditor) {
						ActivateViewsEditor vEditor = (ActivateViewsEditor)editor;
						activateException = vEditor.getViewActivationState(view.getId());
						break;
					}
				}
			}
		}		
	}
	
	@Override
	public String getComponentNameForSaveAs() {
		return view.getName();
	}

	@Override
	public Boolean retrySaveAs(SAFRValidationException sve) {
		if (sve.getErrorMessageMap().containsKey(Property.NAME)) {
			return true;
		}
		return false;
	}

	public void showColumn(final Integer colNum) {
	    mediator.showColumn(colNum);
	}
		
    public View getView() {
        return view;
    }

    public SAFRGUIToolkit getGUIToolKit() {
        return safrToolkit;
    }

    public void updateFRFButtonState() {
        mediator.updateFRFButtonState();
    }

    public ScrolledForm getForm() {
        return form;
    }

    public Map<ComponentType, List<EnvironmentalQueryBean>> getViewPropertiesMap() {
        if (viewPropertiesMap == null) {
            viewPropertiesMap = SAFRQuery.queryViewPropertiesLists(UIUtilities.getCurrentEnvironmentID());
        }
        return viewPropertiesMap;
    }
    
    public void makeViewInactive() {
        view.makeViewInactive();        
    }

}
