package com.ibm.safr.we.data;

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


public class ConnectionParameters {

	private DBType _type;
	private String _url;
	private String _schema;
	private String _database;
	private String _port;
	private String _server;
	private String _userName;
	private String _passWord;

	public ConnectionParameters() {
		super();
	}

	public DBType getType() {
		return _type;
	}

	public void setType(DBType type) {
		_type = type;
	}

	public String getUrl() {
		return _url;
	}

	public void setUrl(String url) {
		_url = url;
	}

	public String getSchema() {
		return _schema;
	}

	public void setSchema(String schema) {
		_schema = schema;
	}

	public String getUserName() {
		return _userName;
	}

	public void setUserName(String userName) {
		_userName = userName;
	}

	public String getPassWord() {
		return _passWord;
	}

	public void setPassWord(String passWord) {
		_passWord = passWord;
	}

	public String getDatabase() {
		return _database;
	}

	public void setDatabase(String database) {
		this._database = database;
	}

	public String getPort() {
		return _port;
	}

	public void setPort(String port) {
		this._port = port;
	}

	public String getServer() {
		return _server;
	}

	public void setServer(String server) {
		this._server = server;
	}
}
