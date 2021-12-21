package com.ibm.safr.we;

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




/**
 * This utility class is for the functions which can be used by all the classes
 * irrespective of the layers(UI, model, data layer).
 * 
 * 
 */
public class SAFRUtilities {

    private static String WEVersion = "";
    
    private static final byte[] HEXCHAR = new byte[]
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	/**
	 * Line break string specific to the run time platform. To be used instead
	 * of platform-dependent escaped characters like '\r' and '\n'.
	 */
	public static final String LINEBREAK = System.getProperty("line.separator");
	
	public static final String dumpBytes( byte[] buffer )
	{
	      if ( buffer == null ) {
	          return "";
	      }
	      StringBuffer sb = new StringBuffer();

	      for ( int i = 0; i < buffer.length; i++ ) {
	          sb.append("0x").append((char)(HEXCHAR[(buffer[i] & 0x00F0) >> 4] )).append(
	              (char) (HEXCHAR[buffer[i] & 0x000F])).append(" ");
	      }
	      return sb.toString();
	}
	
    public static String getWEVersion() {
        return WEVersion;
    }

    public static void setWEVersion(String version) {
        SAFRUtilities.WEVersion = version;
    }	
}
