package com.ibm.safr.we.ui.commands;

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
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

public class SourceProvider extends AbstractSourceProvider {
	public final static String ALLOW_DELETION = "com.ibm.safr.we.ui.commands.SourceProvider.allowDeletion";
	public final static String ALLOW_MOVE_SOURCE_FIELD_UP = "com.ibm.safr.we.ui.commands.SourceProvider.allowMoveSourceFieldUp";
	/**
	 *This source provider variable used for enabling or disabling LR field
	 * Move up command handler.
	 */
	public final static String ALLOW_MOVEUP_LRFIELD = "com.ibm.safr.we.ui.commands.SourceProvider.allowMoveUp";
	/**
	 *This source provider variable used for enabling or disabling LR field
	 * Move Down command handler.
	 */
	public final static String ALLOW_MOVEDOWN_LRFIELD = "com.ibm.safr.we.ui.commands.SourceProvider.allowMoveDown";
	/**
	 *This source provider variable used for enabling or disabling LR field
	 * paste AND Paste Below command handler.
	 */
	public final static String ALLOW_INSERT_LRFIELD = "com.ibm.safr.we.ui.commands.SourceProvider.allowInsertLRField";	
    /**
     *This source provider variable used for enabling or disabling LR field
     * paste and Paste Below command handler.
     */
    public final static String ALLOW_PASTE_LRFIELD = "com.ibm.safr.we.ui.commands.SourceProvider.allowPasteLRField";

    public final static String ALLOW_RECALC_FROM = "com.ibm.safr.we.ui.commands.SourceProvider.allowRecalcFrom";
    public final static String ALLOW_RECALC_ONLY = "com.ibm.safr.we.ui.commands.SourceProvider.allowRecalcOnly"; 
    public final static String ALLOW_RECALC_WITHIN = "com.ibm.safr.we.ui.commands.SourceProvider.allowRecalcWithin";
    /**
     * This source provider variable used for enabling or disabling viewMoveLeft
     * command handler.
     */
    public final static String ALLOW_VIEW_ACTIVATION = "com.ibm.safr.we.ui.commands.SourceProvider.activation";	
	/**
	 * This source provider variable used for enabling or disabling viewMoveLeft
	 * command handler.
	 */
	public final static String ALLOW_MOVE_VIEW_COLUMN_LEFT = "com.ibm.safr.we.ui.commands.SourceProvider.allowMoveViewColumnLeft";
	/**
	 * This source provider variable used for enabling or disabling
	 * viewMoveRight command handler.
	 */
	public final static String ALLOW_MOVE_VIEW_COLUMN_RIGHT = "com.ibm.safr.we.ui.commands.SourceProvider.allowMoveViewColumnRight";

    /**
     * This source provider variable used for enabling or disabling View Column Insert command handler.
     */
    public final static String ALLOW_INSERT_VIEWCOLUMN = "com.ibm.safr.we.ui.commands.SourceProvider.allowInsertViewColumn";
	
	/**
	 * This source provider variable used for enabling or disabling View Paste
	 * left and View Paste right command handler.
	 */
	public final static String ALLOW_PASTE_VIEWCOLUMN = "com.ibm.safr.we.ui.commands.SourceProvider.allowPasteViewColumn";

	/**
	 * This source provider variable used for enabling or disabling view
	 * commands depending upon the whether the focus is on view column or not.
	 */
	public final static String IS_FOCUS_ON_VIEW_COLUMN = "com.ibm.safr.we.ui.commands.SourceProvider.isFocusOnViewColumn";
	/**
	 * This source provider variable used for enabling or disabling view column
	 * delete command depending upon the whether the focus is on view column or
	 * on sort key or datasource.
	 */
	public final static String ALLOW_DELETE_ON_VIEW = "com.ibm.safr.we.ui.commands.SourceProvider.allowDeleteOnView";

    /**
     * This source provider variable used for enabling or disabling view column
     * delete command depending upon the whether the focus is on view column or
     * on sort key or datasource.
     */
    public final static String ALLOW_COPY_VIEW_SOURCE = "com.ibm.safr.we.ui.commands.SourceProvider.allowCopyViewSource";
	
	/**
	 * This source provider variable used for enabling or disabling command move
	 * LookUp field up handler.
	 */
	public final static String ALLOW_MOVE_LKUP_SOURCE_UP = "com.ibm.safr.we.ui.commands.SourceProvider.allowMoveLKUPSourceUp";

	/**
	 * This source provider variable used for enabling or disabling command move
	 * LookUp field down handler.
	 */
	public final static String ALLOW_MOVE_LKUP_SOURCE_DOWN = "com.ibm.safr.we.ui.commands.SourceProvider.allowMoveLKUPSourceDown";
	/**
	 * This source provider variable used for enabling or disabling editors
	 * command handler depending upon user's edit rights .
	 */
	public final static String ALLOW_EDIT = "com.ibm.safr.we.ui.commands.SourceProvider.allowEdit";
	/**
	 * This source provider variable used for enabling or disabling refresh
	 * command depending on element selected in navigator view. Enable only if
	 * view folder is selected else disabled.
	 */
	public final static String REFRESH_VIEWFOLDERLIST = "com.ibm.safr.we.ui.commands.SourceProvider.refreshViewFolderList";
	/**
	 * This source provider variable used for enabling or disabling
	 * Administration menu as per logged in user. Enable for system admin, env Admin,
	 * disabled for general user.
	 */
	public final static String ADMINISTRATION_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.administrationMenu";
	/**
	 * This source provider variable used for enabling or disabling environment
	 * menu as per logged in user.
	 */
	public final static String ENVIRONMENT_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.environment";
	/**
	 * This source provider variable used for enabling or disabling user exit
	 * routine menu as per logged in user.
	 */
	public final static String USEREXITROUTINE_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.userExitRoutine";
	/**
	 * This source provider variable used for enabling or disabling control
	 * record menu as per logged in user.
	 */
	public final static String CONTROLRECORD_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.controlRecord";
	/**
	 * This source provider variable used for enabling or disabling physical
	 * file menu as per logged in user.
	 */
	public final static String PHYSICALFILE_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.physicalFile";
	/**
	 * This source provider variable used for enabling or disabling logical file
	 * menu as per logged in user.
	 */
	public final static String LOGICALFILE_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.logicalFile";
	/**
	 * This source provider variable used for enabling or disabling logical
	 * record menu as per logged in user.
	 */
	public final static String LOGICALRECORD_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.logicalRecord";
	/**
	 * This source provider variable used for enabling or disabling lookup path
	 * menu as per logged in user.
	 */
	public final static String LOOKUPPATH_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.lookupPath";
	/**
	 * This source provider variable used for enabling or disabling view menu as
	 * per logged in user.
	 */
	public final static String VIEW_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.view";
	/**
	 * This source provider variable used for enabling or disabling view folder
	 * menu as per logged in user.
	 */
	public final static String VIEWFOLDER_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.viewFolder";
	/**
	 * This source provider variable used for enabling or disabling user menu as
	 * per logged in user.
	 */
	public final static String USER_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.user";
	/**
	 * This source provider variable used for enabling or disabling group menu
	 * as per logged in user.
	 */
	public final static String GROUP_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.group";
	/**
	 * This source provider variable used for enabling or disabling batch
	 * activate lookup menu as per logged in user.
	 */
	public final static String BATCHLOOKUP_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.BatchLookup";
	/**
	 * This source provider variable used for enabling or disabling batch
	 * activate view menu as per logged in user.
	 */
	public final static String BATCHVIEW_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.BatchViews";
    /**
     * This source provider variable used for enabling or disabling 
     * migrate menu as per logged in user.
     */	
	public final static String MIGRATE_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.Migrate";
    /**
     * This source provider variable used for enabling or disabling 
     * import menu as per logged in user.
     */ 
    public final static String IMPORT_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.Import";
	/**
	 * This source provider variable used for enabling or disabling group
	 * permission menu as per logged in user.
	 */
	public final static String GROUPPERMISSION_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.groupPermission";
	/**
	 * This source provider variable used for enabling or disabling group
	 * permission menu as per logged in user.
	 */
	public final static String ENVPERMISSION_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.environmentPermission";
	/**
	 * This source provider variable used for enabling or disabling group
	 * membership menu as per logged in user.
	 */
	public final static String GROUPMEMBERSHIP_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.groupMembership";
	/**
	 * This source provider variable used for enabling or disabling clear
	 * Environment menu as per logged in user.
	 */
	public final static String CLEAR_ENVIRONMENT_MENU = "com.ibm.safr.we.ui.commands.SourceProvider.clearEnvironment";
	/**
	 * This source provider variable used for enabling or disabling report menu
	 * as per selection in metadata view or focus on editor.
	 */
	public final static String VIEWLIST_SELECTED_METADATAVIEW = "com.ibm.safr.we.ui.commands.SourceProvider.ViewListMetadataView";
	/**
	 * 
	 * This source provider variable used for enabling or disabling report menu
	 * as per selection in metadata view or focus on editor.
	 */
	public final static String LOGICALRECORDLIST_SELECTED_METADATAVIEW = "com.ibm.safr.we.ui.commands.SourceProvider.LogicalRecordListMetadataView";
	/**
	 * This source provider variable used for enabling or disabling report menu
	 * as per selection in metadata view or focus on editor.
	 */
	public final static String LOOKUPPATHLIST_SELECTED_METADATAVIEW = "com.ibm.safr.we.ui.commands.SourceProvider.LookupPathListMetadataView";
	/**
	 * This source provider variable used for enabling or disabling report menu
	 * as per selection in metadata view or focus on editor.
	 */
	public final static String REPORTS_MENU_METADATASELECTION = "com.ibm.safr.we.ui.commands.SourceProvider.enableReportMenu";
    /**
     * This source provider variable used for enabling or disabling report menu
     * as per selection in metadata view or focus on editor.
     */
    public final static String DEPCHECK_MENU_METADATASELECTION = "com.ibm.safr.we.ui.commands.SourceProvider.DepCheckMetadataView";
	/**
	 * This source provider variable used for enabling or disabling report menu
	 * as per focus on editor.
	 */
	public final static String EDITORFOCUSLR = "com.ibm.safr.we.ui.commands.SourceProvider.editorFocusLogicalRecord";
	/**
	 * This source provider variable used for enabling or disabling report menu
	 * as per focus on editor.
	 */
	public final static String EDITORFOCUSVIEW = "com.ibm.safr.we.ui.commands.SourceProvider.editorFocusView";
	/**
	 * This source provider variable used for enabling or disabling report menu
	 * as per focus on editor.
	 */
	public final static String EDITORFOCUSLKPATH = "com.ibm.safr.we.ui.commands.SourceProvider.editorFocusLookupPath";
	/**
	 * This source provider variable used for enabling or disabling report menu
	 * as per focus on editor.
	 */
	public final static String ENVIRONMENTLIST_SELECTED_METADATAVIEW = "com.ibm.safr.we.ui.commands.SourceProvider.EnvironmentListMetadataView";
	/**
	 * This source provider variable used for enabling or disabling report menu
	 * as per focus on editor.
	 */
	public final static String EDITORFOCUSENVIRONMENT = "com.ibm.safr.we.ui.commands.SourceProvider.editorFocusEnvironment";

	/**
	 * This source provider variable used for enabling or disabling toolbar icon
	 * and context menu items on view editor grid.
	 */
	public final static String VIEWEDITORMENU = "com.ibm.safr.we.ui.commands.SourceProvider.viewHasColumns";
	/**
	 * This source provider variable used for enabling or disabling "delete" on
	 * view editor grid for data source selection.
	 */
	public final static String ALLOWDELETEFORDATASOURCE = "com.ibm.safr.we.ui.commands.SourceProvider.enableDeleteForDataSource";

	public final static String ALLOWDELETESORTKEYTITLEVIEW = "com.ibm.safr.we.ui.commands.SourceProvider.enableDeleteSortKeyTitleView";

	public final static String SORTKEYTITLEFIELDEXIST = "com.ibm.safr.we.ui.commands.SourceProvider.TitleFieldExist";

	public final static String METADATAVISIBLE = "com.ibm.safr.we.ui.commands.SourceProvider.MetadataVisible";
	
	
	public final static String TRUE_VALUE = "TRUE";
	public final static String FALSE_VALUE = "FALSE";

	public void dispose() {
	}

	public String[] getProvidedSourceNames() {
		return new String[] { ALLOW_DELETION, ALLOW_MOVE_SOURCE_FIELD_UP, ALLOW_MOVEUP_LRFIELD, 
		        ALLOW_MOVEDOWN_LRFIELD, ALLOW_INSERT_LRFIELD, ALLOW_PASTE_LRFIELD, 
				ALLOW_RECALC_FROM, ALLOW_RECALC_ONLY, ALLOW_RECALC_WITHIN, 
				ALLOW_VIEW_ACTIVATION, ALLOW_MOVE_VIEW_COLUMN_LEFT,
				ALLOW_MOVE_VIEW_COLUMN_RIGHT, ALLOW_INSERT_VIEWCOLUMN, ALLOW_PASTE_VIEWCOLUMN, 
				IS_FOCUS_ON_VIEW_COLUMN, ALLOW_DELETE_ON_VIEW, ALLOW_COPY_VIEW_SOURCE,
				ALLOW_MOVE_LKUP_SOURCE_UP, ALLOW_MOVE_LKUP_SOURCE_DOWN,
				REFRESH_VIEWFOLDERLIST, ADMINISTRATION_MENU, ENVIRONMENT_MENU,
				USEREXITROUTINE_MENU, CONTROLRECORD_MENU,
				PHYSICALFILE_MENU, LOGICALFILE_MENU, LOGICALRECORD_MENU,
				LOOKUPPATH_MENU, VIEW_MENU, VIEWFOLDER_MENU, USER_MENU,
				GROUP_MENU, BATCHLOOKUP_MENU, BATCHVIEW_MENU, MIGRATE_MENU, IMPORT_MENU,
				GROUPPERMISSION_MENU, ENVPERMISSION_MENU, GROUPMEMBERSHIP_MENU, CLEAR_ENVIRONMENT_MENU,
				VIEWLIST_SELECTED_METADATAVIEW,
				LOGICALRECORDLIST_SELECTED_METADATAVIEW,
				LOOKUPPATHLIST_SELECTED_METADATAVIEW,
				REPORTS_MENU_METADATASELECTION, DEPCHECK_MENU_METADATASELECTION, 
				EDITORFOCUSLR, EDITORFOCUSLKPATH, EDITORFOCUSVIEW,
				ENVIRONMENTLIST_SELECTED_METADATAVIEW, EDITORFOCUSENVIRONMENT,
				VIEWEDITORMENU, ALLOWDELETEFORDATASOURCE,
				ALLOWDELETESORTKEYTITLEVIEW, ALLOW_EDIT, SORTKEYTITLEFIELDEXIST,
				METADATAVISIBLE};
	}

	public Map<String, String> getCurrentState() {
		return new HashMap<String, String>();
	}

	public void setDeleteAllowed(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_DELETION, value);
	}

	public void setMoveSourceFieldUpAllowed(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_MOVE_SOURCE_FIELD_UP, value);
	}

	public void setMoveUpAllowed(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_MOVEUP_LRFIELD, value);
	}

	public void setMoveDownAllowed(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_MOVEDOWN_LRFIELD, value);
	}

    public void setAllowInsertLRField(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, ALLOW_INSERT_LRFIELD, value);
    }
	
	public void setAllowPasteLRField(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_PASTE_LRFIELD, value);
	}

    public void setAllowRecalcFrom(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, ALLOW_RECALC_FROM, value);
    }

    public void setAllowRecalcOnly(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, ALLOW_RECALC_ONLY, value);
    }

    public void setAllowRecalcWithin(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, ALLOW_RECALC_WITHIN, value);
    }
    
    public void setActivationAllowed(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, ALLOW_VIEW_ACTIVATION, value);
    }
	
	public void setMoveViewColumnLeftAllowed(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_MOVE_VIEW_COLUMN_LEFT,
				value);
	}

	public void setMoveViewColumnRightAllowed(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_MOVE_VIEW_COLUMN_RIGHT,
				value);
	}

    public void setInsertViewColumnAllowed(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, ALLOW_INSERT_VIEWCOLUMN, value);
    }
	
	public void setPasteViewColumnAllowed(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_PASTE_VIEWCOLUMN, value);
	}

	public void setFocusOnViewColumn(boolean focus) {
		String value = focus ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, IS_FOCUS_ON_VIEW_COLUMN, value);
	}

	public void setAllowDeleteOnView(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_DELETE_ON_VIEW, value);
	}

    public void setAllowCopyViewSource(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, ALLOW_COPY_VIEW_SOURCE, value);
    }
	
	public void setAllowMoveLookUpSourceUp(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_MOVE_LKUP_SOURCE_UP, value);
	}

	public void setAllowMoveLookUpSourceDown(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_MOVE_LKUP_SOURCE_DOWN,
				value);
	}

	public void setAllowRefreshViewFolder(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, REFRESH_VIEWFOLDERLIST, value);
	}

	public void setAdministrationMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ADMINISTRATION_MENU, value);
	}

	public void setEnvironmentMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ENVIRONMENT_MENU, value);
	}

	public void setUserExitRoutineMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, USEREXITROUTINE_MENU, value);
	}

	public void setControlRecordMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, CONTROLRECORD_MENU, value);
	}

	public void setPhysicalFileMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, PHYSICALFILE_MENU, value);
	}

	public void setLogicalFileMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, LOGICALFILE_MENU, value);
	}

	public void setLogicalRecordMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, LOGICALRECORD_MENU, value);
	}

	public void setLookupPathMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, LOOKUPPATH_MENU, value);
	}

	public void setViewMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, VIEW_MENU, value);
	}

	public void setViewFolderMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, VIEWFOLDER_MENU, value);
	}

	public void setUserMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, USER_MENU, value);
	}

	public void setGroupMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, GROUP_MENU, value);
	}

	public void setBatchLookupMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, BATCHLOOKUP_MENU, value);
	}

	public void setBatchViewMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, BATCHVIEW_MENU, value);
	}

    public void setMigrateMenu(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, MIGRATE_MENU, value);
    }
	
    public void setImportMenu(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, IMPORT_MENU, value);
    }    
    
	public void setGroupPermissionMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, GROUPPERMISSION_MENU, value);
	}

	public void setEnvironmentPermissionMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ENVPERMISSION_MENU, value);
	}

	public void setGroupMembershipMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, GROUPMEMBERSHIP_MENU, value);
	}

	public void setClearEnvironment(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, CLEAR_ENVIRONMENT_MENU, value);
	}

	public void setViewListMetadataView(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, VIEWLIST_SELECTED_METADATAVIEW,
				value);
	}

	public void setLogicalRecordListMetadataview(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH,
				LOGICALRECORDLIST_SELECTED_METADATAVIEW, value);
	}

	public void setLookupPathListMetadataView(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH,
				LOOKUPPATHLIST_SELECTED_METADATAVIEW, value);
	}

	public void setReportsMenuMetadataSelection(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, REPORTS_MENU_METADATASELECTION,
				value);
	}

    public void setDepCheckMenuMetadataSelection(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, DEPCHECK_MENU_METADATASELECTION,value);
    }
	
	public void setEditorFocusLR(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, EDITORFOCUSLR, value);
	}

	public void setEditorFocusLookupPath(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, EDITORFOCUSLKPATH, value);
	}

	public void setEditorFocusView(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, EDITORFOCUSVIEW, value);
	}

	public void setEnvironmentListMetadataView(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH,
				ENVIRONMENTLIST_SELECTED_METADATAVIEW, value);
	}

	public void setEditorFocusEnv(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, EDITORFOCUSENVIRONMENT, value);
	}

	public void setViewGridMenu(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, VIEWEDITORMENU, value);
	}

	public void setAllowDeleteForDataSource(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOWDELETEFORDATASOURCE, value);
	}

	public void setAllowDeleteSortKeyTitleView(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOWDELETESORTKEYTITLEVIEW,
				value);
	}

	public void setAllowEdit(boolean allowed) {
		String value = allowed ? TRUE_VALUE : FALSE_VALUE;
		fireSourceChanged(ISources.WORKBENCH, ALLOW_EDIT, value);
	}

    public void setSortKeyTitleExist(boolean allowed) {
        String value = allowed ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, SORTKEYTITLEFIELDEXIST, value);
    }

    public void setMetadataVisible(boolean visible) {
        String value = visible ? TRUE_VALUE : FALSE_VALUE;
        fireSourceChanged(ISources.WORKBENCH, METADATAVISIBLE, value);
    }
    
}
