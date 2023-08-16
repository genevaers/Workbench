package com.ibm.safr.we.ui.reports;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
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


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.safr.we.model.SAFRApplication;
import com.ibm.safr.we.model.query.SystemAdministorsBean;
import com.ibm.safr.we.model.query.UserGroupsReportBean;

import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

public class UserGroupsHTMLReport extends GenevaHTMLReport{

	private Map<String, List<UserGroupsReportBean>> groupsByUser = new TreeMap<>();
	private List<SystemAdministorsBean> sysAdmins;

	public void setUserGroupEntries(List<UserGroupsReportBean> ugs) {
		String userId = "";
		List<UserGroupsReportBean> groups;
		for(UserGroupsReportBean ug : ugs) {
			if(!ug.getUserid().trim().equals(userId)) {
				userId = ug.getUserid().trim();
				groups = new ArrayList<>();
				groups.add(ug);
				groupsByUser.put(userId, groups);
			} else {
				groupsByUser.get(userId).add(ug);
			}
		}		
	}

	public void setSystemAdminEntries(List<SystemAdministorsBean> sas) {
		sysAdmins = sas;		
	}

	public void setFileName(Path path, String baseName) {
		Path htmlPath = makeHtmlDirIfNeeded(path);
		String outputFile = baseName + "_Env" + SAFRApplication.getUserSession().getEnvironment().getId() +  ".html";
		reportPath = htmlPath.resolve(outputFile);
	}

	@Override
	protected ContainerTag<DivTag> bodyContent() {
		return div(
					h2("Users Security Report"),
					getSystemAdministratorsTable(),
					getUserGroupsTable()
				);
	}

	private DomContent getUserGroupsTable() {
		return div(h3("User Groups"),
				table(
						tbody(
							getTableHeader(),
							each(groupsByUser.values(), ug -> getUserGroups(ug))
						)
					).withClass("w3-table-all w3-striped w3-border")
				   ).withClass("w3-container  w3-twothird");
	}

	private DomContent getUserGroups(List<UserGroupsReportBean> ug) {
		return tr(
				td(ug.get(0).getUserid()).withClass("w3-border"),
				td(ug.get(0).getFirstname() + " " + ug.get(0).getLastname()).withClass("w3-border"),
				td(getUserPasswordSet(ug.get(0))),
				td(getGroups(ug))
				).withClass("w3-border");
	}

	private DomContent getUserPasswordSet(UserGroupsReportBean ugb) {
		if(ugb.getPassword().length() > 0) {
			return i().withClass("fa fa-check w3-green");
		} else {
			return i().withClass("fa fa-times w3-red");
		}
	}

	private DomContent  getGroups(List<UserGroupsReportBean> ug) {
		return table(
				tbody(
					getGroupTableHeader(),
					each(ug, g -> getGroups(g))
				)
			).withClass("w3-table-all w3-striped w3-border");
	}

	private DomContent getGroups(UserGroupsReportBean g) {
		return tr(
				td(g.getGroupid()).withClass("w3-border"),
				td(g.getGrpname()).withClass("w3-border")
				).withClass("w3-border");
	}

	private DomContent getGroupTableHeader() {
		return tr(
				th("Group ID").withClass("w3-border"),
				th("Name").withClass("w3-border")
				);
	}

	private DomContent getTableHeader() {
		return tr(
				th("User ID").withClass("w3-border"),
				th("Name").withClass("w3-border"),
				th("Password").withClass("w3-border"),
				th("Groups").withClass("w3-border")
				);
	}

	private ContainerTag<DivTag> getSystemAdministratorsTable() {
		return div(
				h3("System Administrators"),
				getSysAdminsTable()	
				).withClass("w3-container  w3-half");
	}

	private DomContent getSysAdminsTable() {
		return table(
				tbody(
					getSysAdminsTableHeader(),
					each(sysAdmins, sa -> getSysAdminRow(sa))
				)
			).withClass("w3-table-all w3-striped w3-border");
	}

	private DomContent getSysAdminRow(SystemAdministorsBean sa) {
		return tr(
				td(sa.getUserid()).withClass("w3-border"),
				td(sa.getFirstname() + " " + sa.getLastname()).withClass("w3-border"),
				td(getPasswordTestResult(sa))
				).withClass("w3-border");
	}

	private DomContent getPasswordTestResult(SystemAdministorsBean sa) {
		if(sa.getPassword().length() > 0) {
			return i().withClass("fa fa-check w3-green");
		} else {
			return i().withClass("fa fa-times w3-red");
		}
	}

	private DomContent getSysAdminsTableHeader() {
		return tr(
				th("User ID").withClass("w3-border"),
				th("Name").withClass("w3-border"),
				th("Password").withClass("w3-border")
				);
	}
}
