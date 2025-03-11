package com.ibm.safr.we.internal.data;

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


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This class generates all kind of SQL statements like <code>Select, SelectAll,
 * Update, Delete, Call</code> which are used to access tables or stored
 * procedures in database.
 * 
 */
public class PGSQLGenerator {

    /**
     * This function generates an <code>Insert</code> statement
     * 
     * @param schema
     *            : The schema to be used.
     * @param table
     *            : The table to be used.
     * @param names
     *            : The list of columns in the table for which value is to be
     *            inserted.
     * @return A string equivalent to the SQL <code>Insert</code> statement with
     *         place holders in place of values to be set later.
     */
    public String getInsertStatementNoIdentifier(String schema, String table,
            List<String> names) {
        String statement;
        if (schema == null) {
            statement = "Insert Into " + table + " (";
        } else {
            statement = "Insert Into " + schema + "." + table + " (";
        }
        String nameStr = "";
        String valueStr = "";
        for (String name : names) {
            nameStr += name + ",";
            if (name.equals("CREATEDTIMESTAMP") || 
                name.equals("LASTMODTIMESTAMP") || 
                name.equals("LASTACTTIMESTAMP")) {
                valueStr += "CURRENT_TIMESTAMP,";
            } else {
                valueStr += "?,";
            }
        }
        nameStr = nameStr.substring(0, nameStr.length() - 1);
        valueStr = valueStr.substring(0, valueStr.length() - 1);

        statement += nameStr + ") Values (" + valueStr + ")";

        return statement;
    }
    
	/**
	 * This function generates an <code>Insert</code> statement
	 * 
	 * @param schema
	 *            : The schema to be used.
	 * @param table
	 *            : The table to be used.
	 * @param names
	 *            : The list of columns in the table for which value is to be
	 *            inserted.
	 * @return A string equivalent to the SQL <code>Insert</code> statement with
	 *         place holders in place of values to be set later.
	 */
	public String getInsertStatement(String schema, String table, String id,
			List<String> names, boolean currentTimestampOnly) {
		String result = id;
		String nameStr = "";
		String valueStr = "";
		for (String name : names) {
			nameStr += name + ",";
			if (currentTimestampOnly && 
			    (name.equals("CREATEDTIMESTAMP") || 
			     name.equals("LASTMODTIMESTAMP") || 
			     name.equals("LASTACTTIMESTAMP"))) {
				valueStr += "CURRENT_TIMESTAMP,";
				result += "," + name;
			} else {
				valueStr += "?,";
			}
		}
		nameStr = nameStr.substring(0, nameStr.length() - 1);
		valueStr = valueStr.substring(0, valueStr.length() - 1);

        String statement;       
        if (schema == null) {
            statement = "INSERT INTO " + table + " (";
        } else {
            statement = "INSERT INTO " + schema + "." + table + " (";
        }		
		statement += nameStr + ") VALUES (" + valueStr + ")  RETURNING " + result;

		return statement;
	}

    public String getInsertStatementNoIdentifier(String schema, String table, List<String> names, boolean currentTimestampOnly) {
        String statement;
        if (schema == null) {
            statement = "INSERT INTO " + table + " (";
        } else {
            statement = "INSERT INTO " + schema + "." + table + " (";
        }
        String nameStr = "";
        String valueStr = "";
        for (String name : names) {
            nameStr += name + ",";
            if (currentTimestampOnly && 
                (name.equals("CREATEDTIMESTAMP") || 
                 name.equals("LASTMODTIMESTAMP") || 
                 name.equals("LASTACTTIMESTAMP"))) {
                valueStr += "CURRENT_TIMESTAMP,";
            } else {
                valueStr += "?,";
            }
        }
        nameStr = nameStr.substring(0, nameStr.length() - 1);
        valueStr = valueStr.substring(0, valueStr.length() - 1);

        statement += nameStr + ") VALUES (" + valueStr + ")";

        return statement;
    }
	
	public String getUpdateStatement(String schema, String table,
			List<String> names, List<String> idNames) {
		String setStr = "";
		String result = "";
		for (String name : names) {
			if (name.equals("LASTMODTIMESTAMP") || 
			    name.equals("LASTACTTIMESTAMP")) {
				setStr += name + "=CURRENT_TIMESTAMP,";
				result += name + ",";
			} else {
				setStr += name + "=?,";
			}
		}
		setStr = setStr.substring(0, setStr.length() - 1);
		
        String statement;
        if (result.isEmpty()) {
            if (schema == null) {
                statement = "Update " + table + " Set ";
            } else {
                statement = "Update " + schema + "." + table + " Set ";
            }            
        } else {
            result = result.substring(0, result.length()-1);
            if (schema == null) {
                statement = "Update " + table + " Set ";
            } else {
                statement = "Update " + schema + "." + table + " Set ";
            }
        }
		statement += setStr + " Where ";
		for (String idName : idNames) {
			statement += idName + "=? AND ";
		}
		statement = statement.substring(0, statement.length() - 4);
        if (!result.isEmpty()) {
            statement += "  RETURNING " + result;
        }
		return statement;
	}

	public String getUpdateStatement(String schema, String table,
			List<String> names, List<String> idNames,
			boolean currentTimestampOnly) {
		String setStr = "";
        String result = "";
		for (String name : names) {
			if (currentTimestampOnly && 
			    (name.equals("LASTMODTIMESTAMP") || 
			     name.equals("LASTACTTIMESTAMP")) ) {
				setStr += name + "=CURRENT_TIMESTAMP,";
                result += name + ",";
			} else {
				setStr += name + "=?,";
			}

		}
        setStr = setStr.substring(0, setStr.length() - 1);
        
        String statement;
        if (result.isEmpty()) {
            if (schema == null) {
                statement = "Update " + table + " Set ";
            } else {
                statement = "Update " + schema + "." + table + " Set ";
            }            
        } else {
            result = result.substring(0, result.length()-1);
            if (schema == null) {
                statement = "Update " + table + " Set ";
            } else {
                statement = "Update " + schema + "." + table + " Set ";
            }
        }        
		statement += setStr + " Where ";
		for (String idName : idNames) {
			statement += idName + "=? AND ";
		}
		statement = statement.substring(0, statement.length() - 4);
        if (!result.isEmpty()) {
            statement += "  RETURNING " + result;
        }
		return statement;
	}

	public String getNoNonesenseUpdateStatement(String schema, String table,
			List<String> names, List<String> whereNames) {
		String setStr = "";
		for (String name : names) {
				setStr += name + "=?,";
		}
        setStr = setStr.substring(0, setStr.length() - 1);  //strip last comma
        
        String statement;
        if (schema == null) { //Do we ever not set schema?
            statement = "Update " + table + " Set ";
        } else {
            statement = "Update " + schema + "." + table + " Set ";
        }            
    
		statement += setStr + " Where ";
		for (String whereName : whereNames) {
			statement += whereName + "=? AND ";
		}
		statement = statement.substring(0, statement.length() - 4); //strip last "AND "
		return statement;
	}

	public String getDeleteStatement(String schema, String table,
			List<String> idNames) {
		String statement;
		if (schema == null) {
			statement = "Delete From " + table;
		} else {
			statement = "Delete From " + schema + "." + table;
		}
		statement += " Where ";
		for (String idName : idNames) {
			statement += idName + "=? AND ";
		}
		statement = statement.substring(0, statement.length() - 4);
		return statement;
	}

	public String getSelectAllStatement(String schema, String table,
			List<String> orderBy) {
		String statement;
		if (schema == null) {
			statement = "Select * From " + table;
		} else {
			statement = "Select * From " + schema + "." + table;
		}
		if (!(orderBy == null)) {
			statement += " Order By ";
			for (String order : orderBy) {
				statement += order + ",";
			}
			statement = statement.substring(0, statement.length() - 1);
		}
		return statement;
	}

	public String getAllMetadataComponent(String schema, String table,
			Integer environmentId, List<String> orderBy, String idField) {
		String statement;
		if (schema == null) {
			statement = "Select * From " + table;
		} else {
			statement = "Select * From " + schema + "." + table;
		}
		statement += " Where " + idField + " >0 AND ENVIRONID ="
				+ environmentId;

		if (!(orderBy == null)) {
			statement += " Order By ";
			for (String order : orderBy) {
				statement += order + ",";
			}
			statement = statement.substring(0, statement.length() - 1);
		}

		return statement;
	}

	public String getSelectStatement(String schema, String table,
			List<String> idNames, List<String> orderBy) {
		String statement;
		if (schema == null) {
			statement = "Select * From " + table;
		} else {
			statement = "Select * From " + schema + "." + table;
		}
		statement += " Where ";
		for (String idname : idNames) {
			statement += idname + "=? AND ";
		}
		statement = statement.substring(0, statement.length() - 4);
		if (!(orderBy == null)) {
			statement += " Order by ";
			for (String order : orderBy) {
				statement += order + ",";
			}
			statement = statement.substring(0, statement.length() - 1);
		}
		return statement;
	}

    public String handleSpecialChars(String input) {
        String returnStr;
        returnStr = input.replaceAll("&", "&amp;");
        returnStr = returnStr.replaceAll("<", "&lt;");
        returnStr = returnStr.replaceAll(">", "&gt;");
        returnStr = returnStr.replaceAll("\"", "&quot;");
        returnStr = returnStr.replaceAll("'", "&apos;");
        returnStr = returnStr.replaceAll("\\p{C}", "");
        return returnStr;
    }
	
	
	public String getSelectColumnsStatement(List<String> columns,
			String schema, String table, List<String> idNames,
			List<String> orderBy) {
		String statement = "Select ";
		for (String column : columns) {
			statement += column + ",";
		}
		// remove the last comma
		statement = statement.substring(0, statement.length() - 1);

		if (schema == null) {
			statement = statement + " From " + table;
		} else {
			statement = statement + " From " + schema + "." + table;
		}

		statement += " Where ";
		for (String idname : idNames) {
			statement += idname + "=? AND ";
		}
		// remove the last 'AND '
		statement = statement.substring(0, statement.length() - 4);

		if (!(orderBy == null)) {
			statement += " Order by ";
			for (String order : orderBy) {
				statement += order + ",";
			}
			// remove the last comma
			statement = statement.substring(0, statement.length() - 1);
		}

		return statement;
	}

	public String getStoredProcedure(String schema, String storedprocedure,
			int numParam) {
		String statement;
		if (schema == null) {
			statement = "call " + storedprocedure;
		} else {
			statement = "call " + schema + "." + storedprocedure;
		}

		statement += "(";
		for (int i = 1; i < numParam; i++) {
			statement += "?, ";
		}
		statement += "? )";

		return statement;
	}

	public String getFunction(String schema, String function, int numParam) {
		String statement;
		if (schema == null) {
			statement = "{? = call  " + function;
		} else {
			statement = "{? = call  " + schema + "." + function;
		}

		statement += "(";
		if(numParam > 0) {
			for (int i = 1; i < numParam; i++) {
			statement += "?, ";
			}
			statement += "? )}";
		} else {
			statement += ")}";			
		}
		return statement;
	}

	public String getSelectFromFunction(String schema, String function,
			int numParam) {
		String statement;
		if (schema == null) {
			statement = "select " + function;
		} else {
			statement = "select " + schema + "." + function;
		}

		statement += "(";
		for (int i = 1; i < numParam; i++) {
			statement += "?, ";
		}
		statement += "? )";

		return statement;
	}

    public String getReturnStoredProcedure(String schema, String storedprocedure,
            int numParam) {
        String statement;
        if (schema == null) {
            statement = "{? = call " + storedprocedure;
        } else {
            statement = "{? = call " + schema + "." + storedprocedure;
        }

        statement += "(";
        for (int i = 1; i < numParam; i++) {
            statement += "?, ";
        }
        statement += "? )}";

        return statement;
    }
	
	
	public String getDuplicateComponent(String schema, String table,
			String componentName, String componentId) {
		String statement;
		if (schema == null) {
			statement = "Select * From " + table;
		} else {
			statement = "Select * From " + schema + "." + table;
		}
		statement += " Where Upper(" + componentName + ") =? AND "
				+ componentId + " <> ?";
		return statement;
	}

	public String getDuplicateComponent(String schema, String table,
			String environ, String componentName, String componentId) {
		String statement;
		if (schema == null) {
			statement = "Select * From " + table;
		} else {
			statement = "Select * From " + schema + "." + table;
		}
		statement += " Where " + environ + " = ? AND Upper(" + componentName
				+ ")= ? AND " + componentId + " <> ?";
		return statement;
	}

    public String genStrParm(String val) {
        if (val == null) {
            return "null,";
        }
        else {
            return "'" + val + "',";
        }
    }

    public String genTimeParm(Date val) {
        if (val == null) {
            return "null,";
        }
        else {
            String s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(val);
            return s;            
        }
    }

    public String genIntParm(Integer val) {
        if (val == null) {
            return "null,";
        }
        else {
            return val + ",";
        }
    }

	public String getPlaceholders(int size) {
			StringBuilder builder = new StringBuilder();
			if(size != 0) {
				if (size > 1) {
					for (int i = 1; i < size; i++) {
						builder.append("?,");
					}
				}
				builder.append("?");
			}
			return builder.toString();
	}

}
