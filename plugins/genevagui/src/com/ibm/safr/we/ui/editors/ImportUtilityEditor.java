package com.ibm.safr.we.ui.editors;

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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.nebula.widgets.tablecombo.TableCombo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.progress.IProgressService;

import com.ibm.safr.we.SAFRUtilities;
import com.ibm.safr.we.constants.ActivityResult;
import com.ibm.safr.we.constants.ComponentType;
import com.ibm.safr.we.constants.SAFRValidationType;
import com.ibm.safr.we.constants.SortType;
import com.ibm.safr.we.constants.UserPreferencesNodes;
import com.ibm.safr.we.data.DAOException;
import com.ibm.safr.we.exceptions.SAFRException;
import com.ibm.safr.we.exceptions.SAFRFatalException;
import com.ibm.safr.we.exceptions.SAFRValidationException;
import com.ibm.safr.we.model.LogicalFile;
import com.ibm.safr.we.model.LogicalRecord;
import com.ibm.safr.we.model.base.SAFRPersistentObject;
import com.ibm.safr.we.model.query.EnvironmentQueryBean;
import com.ibm.safr.we.model.query.LogicalFileQueryBean;
import com.ibm.safr.we.model.query.LogicalRecordQueryBean;
import com.ibm.safr.we.model.query.LookupQueryBean;
import com.ibm.safr.we.model.query.PhysicalFileQueryBean;
import com.ibm.safr.we.model.query.SAFRQuery;
import com.ibm.safr.we.model.query.ViewFolderQueryBean;
import com.ibm.safr.we.model.query.ViewQueryBean;
import com.ibm.safr.we.model.utilities.importer.ImportFile;
import com.ibm.safr.we.model.utilities.importer.ImportUtility;
import com.ibm.safr.we.preferences.SAFRPreferences;
import com.ibm.safr.we.preferences.SortOrderPrefs;
import com.ibm.safr.we.preferences.SortOrderPrefs.Order;
import com.ibm.safr.we.ui.ApplicationMediator;
import com.ibm.safr.we.ui.utilities.EditorOpener;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy;
import com.ibm.safr.we.ui.utilities.SAFRGUIConfirmWarningStrategy.SAFRGUIContext;
import com.ibm.safr.we.ui.utilities.SAFRGUIToolkit;
import com.ibm.safr.we.ui.utilities.UIUtilities;
import com.ibm.safr.we.utilities.ProfileLocation;
import com.ibm.safr.we.utilities.SAFRLogger;

public class ImportUtilityEditor extends SAFREditorPart {
    static transient Logger logger = Logger.getLogger("com.ibm.safr.we.ui.editors.ImportUtilityEditor");

    public static String ID = "SAFRWE.ImportMetadataComponents";

    private static final String SORT_CATEGORY = "Import";
    private static final String SORT_TABLE = "Files";

    private TableCombo comboEnvironment;
    private Combo comboComponentType;

    private Composite compositeComponents;

    private ScrolledForm form;
    private FormToolkit toolkit;
    private SAFRGUIToolkit safrGuiToolkit;

    private Section sectionComponents;

    private Button buttonImport;
    private Button buttonRefresh;

    private ComponentType componentType;
    private EnvironmentQueryBean targetEnvironment;
    private int selectedEnvironment = -1;
    private Label labelArchiveFolder;
    Label labelXmlFiles;
    private ArrayList<ImportFile> files = new ArrayList<ImportFile>();
    private File importFromDir;

    private Table tableXmlFiles;
    private CheckboxTableViewer tableViewerXmlFiles;
    public static String[] componentColumnHeaders = { "Select", "Result", "File Name" };
    public static int[] componentColumnWidths = { 75, 80, 310 };
    private TableComboViewer comboEnvironmentViewer;
    private List<EnvironmentQueryBean> envList;
    private Button buttonLocation;
    protected DirectoryDialog dialogLocation;
    private Text textPath;

    private ImportUtility importUtility;
    private Section sectionErrors;
    private Composite compositeErrors;
    private TableViewer tableViewerErrors;
    private Table tableErrors;
    EnvironmentQueryBean environmentQueryBean;
    private MenuItem envOpenEditorItem = null;

    private class ImportProcess implements IRunnableWithProgress {

        private IProgressMonitor mon;
        private SAFRException ie = null;

        public ImportProcess() {
            mon = ApplicationMediator.getAppMediator().getStatusLineManager().getProgressMonitor();
            mon.beginTask("Importing", IProgressMonitor.UNKNOWN);
        }

        public SAFRException getSAFRException() {
            return ie;
        }

        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            try {
                importUtility.importMetadata();
            } catch (SAFRException se) {
                ie = se;
            }
        }

        public void endProgress() {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    mon.done();
                }
            });
        }

    }

    /**
     * This private class is used for column sorting in the XML Files Table.
     */
    private class ColumnSelectionListenerXmlFilesTable extends SelectionAdapter {
        private int colNumber1;
        private TableViewer tabViewer1;

        ColumnSelectionListenerXmlFilesTable(TableViewer tabViewer, int colNumber) {
            this.colNumber1 = colNumber;
            this.tabViewer1 = tabViewer;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            try {
                getSite().getShell().setCursor(getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
                TableColumn sortColumn = tabViewer1.getTable().getSortColumn();
                TableColumn currentColumn = (TableColumn) e.widget;
                int dir = tabViewer1.getTable().getSortDirection();
                if (sortColumn == currentColumn) {
                    dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
                } else {
                    tabViewer1.getTable().setSortColumn(currentColumn);
                    if (currentColumn == tabViewer1.getTable().getColumn(0)) {
                        dir = SWT.DOWN;
                    } else {
                        dir = SWT.UP;
                    }
                }

                tabViewer1.getTable().setSortDirection(dir);
                tabViewer1.setComparator(new XmlFilesTableSorter(colNumber1, dir));

                Order order = Order.ASCENDING;
                if (dir == SWT.DOWN) {
                    order = Order.DESCENDING;
                }
                SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE, colNumber1, order);
                prefs.store();
            } finally {
                getSite().getShell().setCursor(null);
            }
        }

    }

    /**
     * This private class is used to provide sorting functionality for XML Files
     * table.
     */
    private class XmlFilesTableSorter extends ViewerComparator {
        private int colNumber1;
        private int dir2;

        public XmlFilesTableSorter(int colNumber1, int dir2) {
            super();
            this.colNumber1 = colNumber1;
            this.dir2 = dir2;

        }

        public int compare(Viewer viewer, Object obj1, Object obj2) {
            ImportFile g1 = (ImportFile) obj1;
            ImportFile g2 = (ImportFile) obj2;
            int rc = 0;

            switch (colNumber1) {
            case 0:
                rc = g1.isSelected().compareTo(g2.isSelected());
                break;
            case 1:
                String str1 = g1.getResult() != null ? g1.getResult().getLabel() : null;
                String str2 = g2.getResult() != null ? g2.getResult().getLabel() : null;
                rc = UIUtilities.compareStrings(str1, str2);
                break;
            case 2:
                String str3 = g1.getName();
                String str4 = g2.getName();
                rc = UIUtilities.compareStrings(str3, str4);
                break;
            default:
                rc = 0;
            }

            // If the direction of sorting was descending previously,
            // reverse the direction of sorting
            if (dir2 == SWT.DOWN) {
                rc = -rc;
            }
            return rc;
        }
    }

    public ImportUtilityEditor() {

    }

    @Override
    public void createPartControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        safrGuiToolkit = new SAFRGUIToolkit(toolkit);
        form = toolkit.createScrolledForm(parent);
        form.getBody().setLayout(new FormLayout());
        form.getBody().setLayoutData(new FormData());
        form.setText("Import Utility");

        createSectionForImport(form.getBody());
        createSectionErrors(form.getBody());

        ManagedForm mFrm = new ManagedForm(toolkit, form);
        setMsgManager(mFrm.getMessageManager());
    }

    private void createSectionForImport(Composite body) {
        sectionComponents = safrGuiToolkit.createSection(body, Section.TITLE_BAR, "Files to Import");
        FormData dataSectionComp = new FormData();
        dataSectionComp.top = new FormAttachment(0, 10);
        dataSectionComp.left = new FormAttachment(0, 5);
        dataSectionComp.bottom = new FormAttachment(100, 0);
        sectionComponents.setLayoutData(dataSectionComp);

        compositeComponents = safrGuiToolkit.createComposite(sectionComponents, SWT.NONE);
        compositeComponents.setLayout(new FormLayout());
        compositeComponents.setLayoutData(new FormData());

        // The label is used as a reference for alignment
        Label labelComponentType = addComponentCombo();
        Label labelEnvironment = addEnvironmentCombo(labelComponentType);
        FormData dataButtonLocation = addFolderSelection(labelEnvironment);

        addFilesTable(labelEnvironment);

        addButtons(labelEnvironment, dataButtonLocation);

        sectionComponents.setClient(compositeComponents);
    }

    private void addButtons(Label labelEnvironment, FormData dataButtonLocation) {
        FormData dataImportButton = addImportButton();
        FormData dataRefresh = addRefreshButton(labelEnvironment);
        setButtonWidths(dataButtonLocation, dataImportButton, dataRefresh);
    }

    private void setButtonWidths(FormData dataButtonLocation, FormData dataImportButton, FormData dataRefresh) {
        // Set width of all buttons to the widest button
        List<Button> buttons = new ArrayList<Button>();
        buttons.add(buttonRefresh);
        buttons.add(buttonImport);
        buttons.add(buttonLocation);
        int width = UIUtilities.computePreferredButtonWidth(buttons);
        dataRefresh.width = width;
        dataImportButton.width = width;
        dataButtonLocation.width = width;
    }

    private FormData addRefreshButton(Label labelEnvironment) {
        // CQ10364 Refresh button
        buttonRefresh = safrGuiToolkit.createButton(compositeComponents, SWT.PUSH, "&Refresh");
        buttonRefresh.setData(SAFRLogger.USER, "Refresh");
        FormData dataRefresh = new FormData();
        dataRefresh.left = new FormAttachment(labelEnvironment, 10);
        dataRefresh.top = new FormAttachment(tableXmlFiles, 5);
        dataRefresh.bottom = new FormAttachment(100, -10);
        buttonRefresh.setLayoutData(dataRefresh);
        buttonRefresh.setEnabled(false);

        buttonRefresh.addSelectionListener(makeRefreshListener());
        return dataRefresh;
    }

    private SelectionListener makeRefreshListener() {
        return new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                // no op
            }

            public void widgetSelected(SelectionEvent e) {
                loadComponentFiles();
                tableViewerXmlFiles.refresh();
                tableViewerXmlFiles.setAllChecked(false);
                showSectionErrors(false);
                enableImportButtons(false);
            }

        };
    }

    private void loadComponentFiles() {
        if (componentType == ComponentType.CobolCopyBook) {
            loadFiles(".cpy");
        } else {
            loadFiles(".xml");
        }
    }

    private FormData addImportButton() {
        buttonImport = safrGuiToolkit.createButton(compositeComponents, SWT.PUSH, "&Import");
        buttonImport.setData(SAFRLogger.USER, "Import");
        FormData dataImportButton = new FormData();
        dataImportButton.top = new FormAttachment(tableXmlFiles, 5);
        dataImportButton.bottom = new FormAttachment(100, -10);
        dataImportButton.right = new FormAttachment(tableXmlFiles, 0, SWT.RIGHT);
        buttonImport.setLayoutData(dataImportButton);
        buttonImport.setEnabled(false);

        buttonImport.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (tableViewerXmlFiles.getCheckedElements().length > -1) {

                    Preferences preferences = SAFRPreferences.getSAFRPreferences();
                    preferences.put(UserPreferencesNodes.IMPORT_PATH, textPath.getText());
                    try {
                        preferences.flush();
                    } catch (BackingStoreException e1) {
                        logger.log(Level.SEVERE, "Failed to save preferences", e1);
                        throw new SAFRFatalException("Failed to save preferences " + e1.getMessage());
                    }

                    Object[] checkedElements = tableViewerXmlFiles.getCheckedElements();

                    // !!DO THE IMPORT!!

                    List<ImportFile> selectedFiles = new ArrayList<ImportFile>();
                    for (Object item : checkedElements) {
                        ImportFile f = (ImportFile) item;
                        selectedFiles.add(f);
                    }
                    List<PhysicalFileQueryBean> queryAllPhysicalFiles = SAFRQuery
                            .queryAllPhysicalFiles(environmentQueryBean.getId(), SortType.SORT_BY_ID);
                    List<LogicalRecordQueryBean> queryAllLogicalRecords = SAFRQuery
                            .queryAllLogicalRecords(environmentQueryBean.getId(), SortType.SORT_BY_ID);
                    List<LogicalFileQueryBean> queryAllLogicalFile = SAFRQuery
                            .queryAllLogicalFiles(environmentQueryBean.getId(), SortType.SORT_BY_ID);
                    List<ViewQueryBean> queryAllView = SAFRQuery.queryAllViews(environmentQueryBean.getId(),
                            SortType.SORT_BY_ID);
                    List<ViewFolderQueryBean> queryAllViewfolder = SAFRQuery
                            .queryAllViewFolders(environmentQueryBean.getId(), SortType.SORT_BY_ID);
                    List<LookupQueryBean> queryAllLookups = SAFRQuery.queryAllLookups(environmentQueryBean.getId(),
                            SortType.SORT_BY_ID);
                    if ((!componentType.equals(ComponentType.CobolCopyBook)
                            && (!queryAllView.isEmpty() || !queryAllPhysicalFiles.isEmpty()
                                    || !queryAllLogicalRecords.isEmpty() || !queryAllLogicalFile.isEmpty()
                                    || queryAllViewfolder.size() > 1 || !queryAllLookups.isEmpty()))) {
                        MessageDialog.openError(getSite().getShell(), "Import Error",
                                "Import is only allowed into empty environements. Please create or choose an empty environment to import");

                    } else {
                        importUtility = new ImportUtility(targetEnvironment, componentType, selectedFiles);
                        importUtility
                                .setConfirmWarningStrategy(new SAFRGUIConfirmWarningStrategy(SAFRGUIContext.IMPORT));
                        ImportProcess proc = new ImportProcess();
                        try {

                            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                            progressService.run(true, false, proc);
                            proc.endProgress();

                            SAFRException ex = proc.getSAFRException();
                            if (ex != null) {
                                throw ex;
                            }

                            tableViewerXmlFiles.refresh();
                            getMsgManager().removeAllMessages();
                            buttonRefresh.setEnabled(true);

                            // refresh only if the selected
                            // target environment is the same as the
                            // currently logged in
                            // environment
                            if (targetEnvironment.getId().equals(UIUtilities.getCurrentEnvironmentID())) {
                                Boolean importSuccess = false;
                                for (ImportFile item : selectedFiles) {
                                    if (item.getErrorMsg() == null) {
                                        importSuccess = true;
                                        break;
                                    }
                                }

                                // refresh if any of the selected files were
                                // imported successfully
                                if (importSuccess) {
                                    refreshMetadataView();
                                    if (componentType.equals(ComponentType.ViewFolder)) {
                                        ApplicationMediator.getAppMediator().refreshNavigator();
                                    }
                                    UIUtilities.enableDisableMenuAsPerUserRights();
                                }
                            }
                        } catch (SAFRValidationException e1) {
                            decorateEditor(e1);
                            String ctx = e1.getContextMessage();
                            ctx = (ctx != null ? ctx.replaceAll("&", "&&") : "");
                            String msg = e1.getMessageString().replaceAll("&", "&&");
                            MessageDialog.openError(getSite().getShell(), "Error with the import parameters",
                                    ctx + msg);
                        } catch (SAFRException e2) {
                            logger.log(Level.SEVERE, "System error on import", e2);
                            tableViewerXmlFiles.refresh();
                            String msg = e2.getMessage().replaceAll("&", "&&");
                            MessageDialog.openError(getSite().getShell(), "System error on import", msg);
                        } catch (InvocationTargetException e3) {
                            logger.log(Level.SEVERE, "System error on import", e3);
                            tableViewerXmlFiles.refresh();
                            if ((e3.getCause()) != null) {
                                if (e3.getCause() instanceof RuntimeException) {
                                    throw (RuntimeException) e3.getCause();
                                } else {
                                    throw new RuntimeException(e3.getCause());
                                }
                            } else {
                                throw new RuntimeException(e3);
                            }
                        } catch (InterruptedException e4) {
                            tableViewerXmlFiles.refresh();
                            logger.log(Level.SEVERE, "System error on import", e4);
                            MessageDialog.openError(getSite().getShell(), "System error on import",
                                    "Operation interrupted. See log file for details.");
                        } finally {
                            proc.endProgress();
                        }
                    }
                }
            }
        });
        return dataImportButton;
    }

    private void addFilesTable(Label labelEnvironment) {
        labelXmlFiles = safrGuiToolkit.createLabel(compositeComponents, SWT.NONE, "Fi&le(s):");
        FormData dataLabelComponents = new FormData();
        dataLabelComponents.width = 85;
        dataLabelComponents.top = new FormAttachment(labelArchiveFolder, 18);
        dataLabelComponents.left = new FormAttachment(0, 5);
        labelXmlFiles.setLayoutData(dataLabelComponents);
        labelXmlFiles.setEnabled(true);

        tableXmlFiles = safrGuiToolkit.createTable(compositeComponents,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK, false);
        tableXmlFiles.setData(SAFRLogger.USER, "Files");
        tableViewerXmlFiles = safrGuiToolkit.createCheckboxTableViewer(tableXmlFiles);
        FormData dataTableXmlFiles = new FormData();
        dataTableXmlFiles.left = new FormAttachment(labelEnvironment, 10);
        dataTableXmlFiles.top = new FormAttachment(textPath, 10);
        dataTableXmlFiles.height = 0;
        dataTableXmlFiles.right = new FormAttachment(buttonLocation, 0, SWT.RIGHT);
        dataTableXmlFiles.bottom = new FormAttachment(100, -40);

        tableXmlFiles.setLayoutData(dataTableXmlFiles);
        tableXmlFiles.setHeaderVisible(true);
        tableXmlFiles.setLinesVisible(true);

        int counter = 0;

        int length = componentColumnHeaders.length;

        for (counter = 0; counter < length; counter++) {
            TableViewerColumn column = new TableViewerColumn(tableViewerXmlFiles, SWT.NONE);
            column.getColumn().setText(componentColumnHeaders[counter]);
            column.getColumn().setToolTipText(componentColumnHeaders[counter]);
            column.getColumn().setWidth(componentColumnWidths[counter]);

            column.getColumn().setResizable(true);
            ColumnSelectionListenerXmlFilesTable colListener = new ColumnSelectionListenerXmlFilesTable(
                    tableViewerXmlFiles, counter);
            column.getColumn().addSelectionListener(colListener);
        }

        tableViewerXmlFiles.setContentProvider(new IStructuredContentProvider() {

            public Object[] getElements(Object inputElement) {
                if (files == null) {
                    return new String[0];
                } else
                    return files.toArray();
            }

            public void dispose() {
                // TODO Auto-generated method stub

            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // TODO Auto-generated method stub

            }

        });

        tableViewerXmlFiles.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                ImportFile importFile = (ImportFile) event.getElement();
//                if(importFile.isSelected()) {
//                    importFile.setSelected(false);                                            
//                } else {
                    if (tableViewerXmlFiles.getCheckedElements().length > 1) {
                        tableViewerXmlFiles.setAllChecked(false);
                    }
                    tableViewerXmlFiles.setChecked(importFile, true);
                    importFile.setSelected(true);
//                }
            }
        });

        tableViewerXmlFiles.getTable().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {

                checkEnableImport();
            }


        });

        tableViewerXmlFiles.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                ImportFile currModel = (ImportFile) ((IStructuredSelection) event.getSelection()).getFirstElement();

                if (currModel != null) {
                    if (currModel.getErrorMsg() == null) {
                        showSectionErrors(false);
                    } else {
                        showSectionErrors(true);
                    }
                }
            }
        });

        tableViewerXmlFiles.setLabelProvider(new XmlFilesTableLabelProvider());
        tableViewerXmlFiles.setInput(1);

        SortOrderPrefs prefs = new SortOrderPrefs(SORT_CATEGORY, SORT_TABLE);
        if (prefs.load()) {
            tableViewerXmlFiles.getTable().setSortColumn(tableViewerXmlFiles.getTable().getColumn(prefs.getColumn()));
            if (prefs.getOrder() == Order.ASCENDING) {
                tableViewerXmlFiles.getTable().setSortDirection(SWT.UP);
                tableViewerXmlFiles.setComparator(new XmlFilesTableSorter(prefs.getColumn(), SWT.UP));
            } else {
                tableViewerXmlFiles.getTable().setSortDirection(SWT.DOWN);
                tableViewerXmlFiles.setComparator(new XmlFilesTableSorter(prefs.getColumn(), SWT.DOWN));
            }
        } else {
            tableViewerXmlFiles.getTable().setSortColumn(tableViewerXmlFiles.getTable().getColumn(2));
            tableViewerXmlFiles.getTable().setSortDirection(SWT.UP);
            tableViewerXmlFiles.setComparator(new XmlFilesTableSorter(2, SWT.UP));
        }
    }

    private FormData addFolderSelection(Label labelEnvironment) {
        labelArchiveFolder = safrGuiToolkit.createLabel(compositeComponents, SWT.NONE, "Archive F&older:");
        FormData dataLabel = new FormData();
        dataLabel.width = SWT.DEFAULT;
        dataLabel.top = new FormAttachment(labelEnvironment, 18);
        dataLabel.left = new FormAttachment(0, 5);
        labelArchiveFolder.setLayoutData(dataLabel);
        labelArchiveFolder.setEnabled(true);

        textPath = safrGuiToolkit.createTextBox(compositeComponents, SWT.NONE);
        textPath.setData(SAFRLogger.USER, "Archive Folder");
        FormData dataLocation = new FormData();
        dataLocation.left = new FormAttachment(labelEnvironment, 10);
        dataLocation.top = new FormAttachment(comboEnvironment, 10);
        dataLocation.width = 368;
        textPath.setLayoutData(dataLocation);
        Preferences preferences = SAFRPreferences.getSAFRPreferences();
        String impPath = preferences.get(UserPreferencesNodes.IMPORT_PATH, "");
        if (impPath == null || impPath.equals("")) {
            textPath.setText(ProfileLocation.getProfileLocation().getLocalProfile() + "xml");
        } else {
            textPath.setText(impPath);
        }
        importFromDir = new File(textPath.getText());
        textPath.setEnabled(true);

        FormData dataButtonLocation = makeBrowseButtonAlignedTo(labelEnvironment);
        return dataButtonLocation;
    }

    private FormData makeBrowseButtonAlignedTo(Label labelEnvironment) {
        buttonLocation = safrGuiToolkit.createButton(compositeComponents, SWT.NONE, "&Browse...");
        buttonLocation.setData(SAFRLogger.USER, "Browse");
        FormData dataButtonLocation = new FormData();
        dataButtonLocation.left = new FormAttachment(textPath, 5);
        dataButtonLocation.top = new FormAttachment(labelEnvironment, 16);
        buttonLocation.setLayoutData(dataButtonLocation);
        buttonLocation.setEnabled(true);
        buttonLocation.addSelectionListener(getFileSelectionListener());
        return dataButtonLocation;
    }

    private SelectionListener getFileSelectionListener() {
        return new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {

                // User has selected to open multiple files
                dialogLocation = new DirectoryDialog(getSite().getShell());
                dialogLocation.setFilterPath(textPath.getText());
                String path = dialogLocation.open();
                if (path != null) {
                    textPath.setText(path);
                    importFromDir = new File(path);
                }

                // Dir is wrong
                // And we need to set extension based on component type?
                loadComponentFiles();
                tableViewerXmlFiles.refresh();
                showSectionErrors(false);
                checkEnableImport();
            }
        };
    }

    private Label addComponentCombo() {
        Label note = safrGuiToolkit.createLabel(compositeComponents, SWT.NONE,
                "Note: only one XML file may be imported into an empty environment.");
        Label labelComponentType = safrGuiToolkit.createLabel(compositeComponents, SWT.NONE, "Component &Type:");
        FormData dataLabelComponentType = new FormData();
        dataLabelComponentType.width = SWT.DEFAULT;
        dataLabelComponentType.top = new FormAttachment(note, 20);
        dataLabelComponentType.left = new FormAttachment(0, 5);
        labelComponentType.setLayoutData(dataLabelComponentType);

        comboComponentType = safrGuiToolkit.createComboBox(compositeComponents, SWT.READ_ONLY, "");
        comboComponentType.setData(SAFRLogger.USER, "Component Type");
        FormData dataComboComponentType = new FormData();
        dataComboComponentType.left = new FormAttachment(labelComponentType, 22);
        dataComboComponentType.top = new FormAttachment(note, 10);
        dataComboComponentType.width = 353;
        comboComponentType.setLayoutData(dataComboComponentType);
        int i = 0;
        comboComponentType.add("View");
        comboComponentType.setData(String.valueOf(i++), ComponentType.View);
        comboComponentType.add("View Folder");
        comboComponentType.setData(String.valueOf(i++), ComponentType.ViewFolder);

        comboComponentType.add(ComponentType.CobolCopyBook.getLabel());
        comboComponentType.setData(String.valueOf(i++), ComponentType.CobolCopyBook);

        comboComponentType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                    componentType = (ComponentType) comboComponentType.getData(String.valueOf(comboComponentType.getSelectionIndex()));
                    buttonRefresh.setEnabled(true);
                    checkEnableImport();
                }
         });
        return labelComponentType;
    }

    private Label addEnvironmentCombo(Label labelComponentType) {
        Label labelEnvironment = safrGuiToolkit.createLabel(compositeComponents, SWT.NONE, "Target &Environment:");
        FormData dataLabelEnvironment = new FormData();
        dataLabelEnvironment.width = SWT.DEFAULT;
        dataLabelEnvironment.left = new FormAttachment(0, 5);
        dataLabelEnvironment.top = new FormAttachment(labelComponentType, 10);
        labelEnvironment.setLayoutData(dataLabelEnvironment);

        comboEnvironmentViewer = safrGuiToolkit.createTableComboForComponents(compositeComponents,
                ComponentType.Environment);
        comboEnvironment = comboEnvironmentViewer.getTableCombo();
        comboEnvironment.setData(SAFRLogger.USER, "Target Environment");
        comboEnvironment.setEnabled(true);

        addEnvOpenEditorMenu();

        FormData dataComboEnvironment = new FormData();
        dataComboEnvironment.left = new FormAttachment(labelEnvironment, 10);
        dataComboEnvironment.top = new FormAttachment(comboComponentType, 10);
        dataComboEnvironment.width = 375;
        comboEnvironment.setLayoutData(dataComboEnvironment);
        try {
            envList = SAFRQuery.queryEnvironmentsForLoggedInUser(SortType.SORT_BY_NAME, true);
        } catch (DAOException e1) {
            UIUtilities.handleWEExceptions(e1, "Error occurred while retrieving all environments.",
                    UIUtilities.titleStringDbException);
        }
        Integer count = 0;
        if (envList != null) {
            for (EnvironmentQueryBean enviromentQuerybean : envList) {
                comboEnvironment.setData(Integer.toString(count), enviromentQuerybean);
                count++;
            }
        }

        comboEnvironmentViewer.setInput(envList);

        comboEnvironment.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (comboEnvironment.getSelectionIndex() != selectedEnvironment) {
                    environmentQueryBean = (EnvironmentQueryBean) comboEnvironment.getTable().getSelection()[0]
                            .getData();
                    if (environmentQueryBean != null) {
                        environmentQueryBean.getId();
                        targetEnvironment = environmentQueryBean;
                    }

                    try {
                        // Display cursor in the form of an
                        // hourglass while the View Folders combo is
                        // being populated
                        getSite().getShell().setCursor(getSite().getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
                        tableViewerXmlFiles.refresh();
                        checkEnableImport();
                    } finally {
                        // restore cursor to original one
                        getSite().getShell().setCursor(null);
                    }
                    selectedEnvironment = comboEnvironment.getSelectionIndex();
                }
            }
        });
        return labelEnvironment;
    }

    private void addEnvOpenEditorMenu() {
        Text text = comboEnvironment.getTextControl();
        Menu menu = text.getMenu();
        envOpenEditorItem = new MenuItem(menu, SWT.PUSH);
        envOpenEditorItem.setText("Open Editor");
        envOpenEditorItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                EnvironmentQueryBean bean = (EnvironmentQueryBean) ((StructuredSelection) comboEnvironmentViewer
                        .getSelection()).getFirstElement();
                if (bean != null) {
                    EditorOpener.open(bean.getId(), ComponentType.Environment);
                }
            }
        });

        comboEnvironment.addMouseListener(new MouseListener() {

            public void mouseDown(MouseEvent e) {
                if (e.button == 3) {
                    EnvironmentQueryBean bean = (EnvironmentQueryBean) ((StructuredSelection) comboEnvironmentViewer
                            .getSelection()).getFirstElement();
                    if (bean != null) {
                        envOpenEditorItem.setEnabled(true);
                    } else {
                        envOpenEditorItem.setEnabled(false);
                    }
                }
            }

            public void mouseUp(MouseEvent e) {
            }

            public void mouseDoubleClick(MouseEvent e) {
            }

        });
    }

    private void createSectionErrors(Composite body) {

        sectionErrors = safrGuiToolkit.createSection(body, Section.TITLE_BAR | Section.TWISTIE, "Errors");
        FormData dataErrors = new FormData();
        dataErrors.left = new FormAttachment(sectionComponents, 5);
        dataErrors.top = new FormAttachment(0, 10);
        dataErrors.bottom = new FormAttachment(100, 0);
        dataErrors.right = new FormAttachment(100, -5);
        sectionErrors.setLayoutData(dataErrors);

        compositeErrors = safrGuiToolkit.createComposite(sectionErrors, SWT.NONE);
        compositeErrors.setLayout(new FormLayout());

        tableViewerErrors = safrGuiToolkit.createTableViewer(compositeErrors,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER, false);
        tableErrors = tableViewerErrors.getTable();
        tableErrors.setHeaderVisible(true);
        tableErrors.setLinesVisible(true);
        tableErrors.setLayout(new FormLayout());
        FormData dataTableErrors = new FormData();
        dataTableErrors.left = new FormAttachment(0, 5);
        dataTableErrors.top = new FormAttachment(0, 10);
        dataTableErrors.right = new FormAttachment(100, 0);
        dataTableErrors.bottom = new FormAttachment(100, -40);
        tableErrors.setLayoutData(dataTableErrors);
        int iCounter = 0;
        int[] columnWidthsErrors = { 750 };
        int lengthErrors = columnWidthsErrors.length;

        for (iCounter = 0; iCounter < lengthErrors; iCounter++) {
            TableViewerColumn column = new TableViewerColumn(tableViewerErrors, SWT.NONE);
            String[] columnHeaderErrors = { "Errors" };
            column.getColumn().setText(columnHeaderErrors[iCounter]);
            column.getColumn().setToolTipText(columnHeaderErrors[iCounter]);
            column.getColumn().setWidth(columnWidthsErrors[iCounter]);
            column.getColumn().setResizable(true);

        }

        tableViewerErrors.setContentProvider(new IStructuredContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

            }

            public void dispose() {

            }

            public Object[] getElements(Object inputElement) {
                if (tableViewerXmlFiles.getSelection() == null) {
                    return new String[0];
                } else {
                    IStructuredSelection selection = (IStructuredSelection) tableViewerXmlFiles.getSelection();
                    ImportFile currModel = (ImportFile) selection.getFirstElement();
                    if (currModel == null) {
                        return new String[0];
                    } else if (currModel.getErrorMsg() == null) {
                        return new String[0];
                    } else {
                        if (currModel.getException() != null) {
                            return showDependencyError(currModel);
                        } else {
                            return currModel.getErrorMsg().split("\n");
                        }
                    }
                }
            }

        });
        tableViewerErrors.setLabelProvider(new ColumnLabelProvider());

        tableViewerErrors.setInput(1);

        showSectionErrors(false);
        sectionErrors.setClient(compositeErrors);
    }

    /**
     * This method is used to display the errors section expanded or enabled
     * based upon the value passed
     * 
     * @param visible
     */
    private void showSectionErrors(Boolean visible) {
        sectionErrors.setExpanded(visible);
        sectionErrors.setEnabled(visible);
        if (visible) {
            tableViewerErrors.refresh();
        }
    }

    @Override
    public void doSaveAs() {

    }

    @Override
    public String getModelName() {

        return null;
    }

    @Override
    public boolean isSaveAsAllowed() {

        return false;
    }

    @Override
    public void doRefreshControls() throws SAFRException {

    }

    @Override
    public void refreshModel() {

    }

    @Override
    public void setFocus() {
        comboComponentType.setFocus();

    }

    @Override
    public void storeModel() throws DAOException, SAFRException {

    }

    @Override
    public void validate() throws DAOException, SAFRException {

    }

    public class XmlFilesTableLabelProvider implements ITableLabelProvider, ITableColorProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            ImportFile file = (ImportFile) element;
            String text = null;

            switch (columnIndex) {
            case 0:
                text = "";
                break;
            case 1:
                if (file.getResult() == null) {
                    text = "";
                } else {
                    text = file.getResult().getLabel();
                }
                break;
            case 2:
                text = file.getName();
            }

            return text;
        }

        public void addListener(ILabelProviderListener listener) {

        }

        public void dispose() {

        }

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {

        }

        @Override
        public Color getForeground(Object element, int columnIndex) {
            ImportFile file = (ImportFile) element;
            if (file.getResult() == ActivityResult.CANCEL) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
            } else if (file.getResult() == ActivityResult.FAIL) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
            } else if (file.getResult() == ActivityResult.LOADERRORS) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
            } else {
                return null;
            }
        }

        @Override
        public Color getBackground(Object element, int columnIndex) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    public ComponentType getEditorCompType() {
        return null;
    }

    @Override
    public SAFRPersistentObject getModel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getComponentNameForSaveAs() {
        return null;
    }

    @Override
    public Boolean retrySaveAs(SAFRValidationException sve) {
        return null;
    }

    protected Control getControlFromProperty(Object property) {
        if (property == ImportUtility.Property.COMPONENT_TYPE) {
            return comboComponentType;
        } else if (property == ImportUtility.Property.TARGET_ENVIRONMENT) {
            return comboEnvironment;
        } else if (property == ImportUtility.Property.FILES) {
            return tableXmlFiles;
        }
        return null;
    }

    public void refreshMetadataView() {

        switch (componentType) {
        // refresh the metadata view for the component selected to be imported
        // as well as for all dependent components
        case ViewFolder:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.ViewFolder, null);
        case View:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.View, 0);
        case LogicalRecord:
        case CobolCopyBook:
            ApplicationMediator.getAppMediator().refreshMetadataView(ComponentType.LogicalRecord, null);
        default:
            break;
        }
    }

    private String[] showDependencyError(ImportFile file) {
        SAFRValidationException sve = file.getException();
        SAFRValidationType svt;
        if (sve.getSafrValidationToken() != null) {
            svt = sve.getSafrValidationToken().getValidationFailureType();
        } else {
            svt = sve.getSafrValidationType();
        }
        StringBuffer buffer = new StringBuffer();
        switch (svt) {
        case DEPENDENCY_LF_ASSOCIATION_ERROR:
            buffer.append(sve.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP_IMPORT));
            buffer.append(SAFRUtilities.LINEBREAK);
            buffer.append(sve.getMessageString(LogicalRecord.Property.LF_ASSOCIATION_DEP));
            break;
        case DEPENDENCY_LR_FIELDS_ERROR:
            buffer.append(sve.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP_IMPORT));
            buffer.append(SAFRUtilities.LINEBREAK);
            buffer.append(sve.getMessageString(LogicalRecord.Property.VIEW_LOOKUP_DEP));
            break;
        case DEPENDENCY_PF_ASSOCIATION_WARNING:
            buffer.append(sve.getMessageString(LogicalFile.Property.PF_ASSOCIATION_DEP_IMPORT));
            buffer.append(SAFRUtilities.LINEBREAK);
            buffer.append(sve.getMessageString(LogicalFile.Property.PF_ASSOCIATION_DEP));
            break;
        default:
            break;
        }
        String[] array = buffer.toString().split(SAFRUtilities.LINEBREAK);
        return array;
    }

    private void loadFiles(String ext) {
        if (files == null || importFromDir == null) {
            return;
        }
        files.clear();
        if (importFromDir.listFiles() != null) {
            for (File f : importFromDir.listFiles()) {
                if (f.isFile()) {
                    if (f.getName().toLowerCase().endsWith(ext)) {
                        ImportFile impFile = new ImportFile(f);
                        files.add(impFile);
                    }
                }
            }
        }
    }

    private void enableImportButtons(boolean enabled) {
        buttonImport.setEnabled(enabled);
    }
    
    private void checkEnableImport() {
        if (tableViewerXmlFiles.getCheckedElements().length > 0 && componentType != null
                && targetEnvironment != null) {
            enableImportButtons(true);
        } else {
            enableImportButtons(false);
        }
    }


}
