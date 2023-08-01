package com.ibm.safr.we.model;

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



public class Views {
	private Folder folder;

	  private int viewId;

	  private String viewName;

	  public Views() {
	    this(0, "");
	  }

	  public Views(int viewId, String viewName) {
	    this.viewId = viewId;
	    this.viewName=viewName;
	  }
	  
	  public Views(Folder folder,int viewId, String viewName) {
		  	this.folder=folder;
		    this.viewId = viewId;
		    this.viewName=viewName;
	  }
	  
	  public void setFolder(Folder folder) {
	    this.folder = folder;
	  }

	  public Folder getFolder() {
		  return folder;
	  }
	  
	  public int getViewId() {
		  return viewId;
	  }

	  public void setViewId(int viewId) {
		  this.viewId = viewId;
	  }
		  
	  public String getViewtName() {
	    return viewName;
	  }

	  public void setViewName(String viewName) {
	    this.viewName = viewName;
	  }
}
