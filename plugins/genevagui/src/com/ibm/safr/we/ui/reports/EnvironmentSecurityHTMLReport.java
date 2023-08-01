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
import com.ibm.safr.we.model.query.EnvironmentSecurityReportBean;

import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.specialized.DivTag;

public class EnvironmentSecurityHTMLReport extends GenevaHTMLReport{

	private List<EnvironmentSecurityReportBean> securityentires;
	
	private Map<String, Map<String, List<EnvironmentSecurityReportBean>>> environments = new TreeMap<>();

	public void setSecurityentires(List<EnvironmentSecurityReportBean> securityentires) {
		this.securityentires = securityentires;
		preSort();
	}

	public boolean hasData() {
		return securityentires.size() > 0 ;
	}

	public void preSort() {
		String envId = "";
		String groupId = "";
		Map<String, List<EnvironmentSecurityReportBean>> currentGroup = null;
		List<EnvironmentSecurityReportBean> currentUserList = null;
		for( EnvironmentSecurityReportBean secEntry : securityentires) {
			if( !envId.equals(secEntry.getEnvironid())) {
				currentGroup = new TreeMap<>();
				environments.put(secEntry.getEnvironid(), currentGroup);
				envId = secEntry.getEnvironid();
			}
			if(!groupId.equals(secEntry.getGroupid())) {
				currentUserList = new ArrayList<>();
				currentGroup.put(secEntry.getGroupid(), currentUserList);
				groupId = secEntry.getGroupid();
			}
			currentUserList.add(secEntry);
		}
	}

	@Override
	protected ContainerTag<DivTag> bodyContent() {
		return div(
				h2("Environment Security Report"),
				p("Who has access to an environment, via which group, and at what level"),
				getEnvironmentDivs()
			);
	}

	private DomContent getEnvironmentDivs() {
		return div(
				each(environments.values(), e -> addAddEnvironmentDiv(e))
				).withClass("w3-container  w3-twothird");
	}

	private DomContent addAddEnvironmentDiv(Map<String, List<EnvironmentSecurityReportBean>> e) {
		EnvironmentSecurityReportBean env = e.values().iterator().next().get(0);
		return div(
				h2(env.getEnvname() + "[" + env.getEnvironid() + "]"),
				getUserGroupsTable(e)
				).withClass("w3-table-all w3-striped w3-border");
	}

	private DomContent getUserGroupsTable(Map<String, List<EnvironmentSecurityReportBean>> e) {
		return div(
				h3("Groups with access"),
				table(
						tbody(
							getTableHeader(),
							each(e.values(), grp -> getGroupDetails(grp))
						)
					).withClass("w3-table-all w3-striped w3-border")
				   ).withClass("w3-container  w3-twothird");
	}

	private DomContent getGroupDetails(List<EnvironmentSecurityReportBean> grp) {
		EnvironmentSecurityReportBean g = grp.get(0);
		return tr(
					td(g.getGroupid()).withClass("w3-border"),
					td(g.getGrpname()).withClass("w3-border"),
					td(getUsersTable(grp))
				).withClass("w3-border");
	}

	private DomContent getUsersTable(List<EnvironmentSecurityReportBean> grp) {
		return div(
				table(
						tbody(
							getUserTableHeader(),
							each(grp, u -> getUserDetails(u))
						)
					).withClass("w3-table-all w3-striped w3-border")
				   ).withClass("w3-container  w3-twothird");
	}

	private DomContent getUserTableHeader() {
		return tr(
				th("ID").withClass("w3-border"),
				th("Name").withClass("w3-border"),
				th("Level").withClass("w3-border")
				);
	}
	
	private DomContent getUserDetails(EnvironmentSecurityReportBean u) {
		return tr(
				td(u.getUserid()).withClass("w3-border"),
				td(u.getFirstname() + " " + u.getLastname()).withClass("w3-border"),
				td(u.getEnvrole()).withClass("w3-border")
				).withClass("w3-border");
	}

	private DomContent getTableHeader() {
		return tr(
				th("Group ID").withClass("w3-border"),
				th("Name").withClass("w3-border"),
				th("Users").withClass("w3-border")
				);
	}


	public void setFileName(Path path, String baseName) {
		Path htmlPath = makeHtmlDirIfNeeded(path);
		String outputFile = baseName + SAFRApplication.getUserSession().getEnvironment().getId() +  ".html";
		reportPath = htmlPath.resolve(outputFile);
	}
}
