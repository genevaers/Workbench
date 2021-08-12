--****************************************************************
--
--     Copyright Contributors to the GenevaERS Project.
-- SPDX-License-Identifier: Apache-2.0
--
--***********************************************************************
--*                                                                           
--*   Licensed under the Apache License, Version 2.0 (the "License");         
--*   you may not use this file except in compliance with the License.        
--*   You may obtain a copy of the License at                                 
--*                                                                           
--*     http://www.apache.org/licenses/LICENSE-2.0                            
--*                                                                           
--*   Unless required by applicable law or agreed to in writing, software     
--*   distributed under the License is distributed on an "AS IS" BASIS,       
--*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
--*   or implied.
--*   See the License for the specific language governing permissions and     
--*   limitations under the License.                                          
--***********************************************************************
CREATE OR REPLACE FUNCTION :schemaV.newenvironment (
				IN P_ENVTNAME CHARACTER(48),
                                IN P_ENVCOMMENTS VARCHAR(255),
                                IN P_USERID CHAR(8),
                                IN P_CONTROLNAME VARCHAR(64)) 
RETURNS SETOF INT 
AS $$
DECLARE 
        NEWENV INT;
BEGIN
	        RETURN QUERY INSERT INTO environ(
	                environid, name, comments, createdtimestamp, createduserid, lastmodtimestamp, lastmoduserid)
                        VALUES ( DEFAULT, P_ENVTNAME,P_ENVCOMMENTS,
                                CURRENT_TIMESTAMP,P_USERID,
                        CURRENT_TIMESTAMP,P_USERID)
                        RETURNING environid ;

          select max(environid) into NEWENV from environ;

          INSERT INTO controlrec (environid,name,
                         firstmonth,lowvalue,highvalue,comments,
                         createdtimestamp,createduserid,
                         lastmodtimestamp,lastmoduserid)
          VALUES (NEWENV,P_CONTROLNAME,
                  1,1,12,'Created automatically during initialization',
                  CURRENT_TIMESTAMP,P_USERID,
                  CURRENT_TIMESTAMP,P_USERID);

         INSERT INTO VIEWFOLDER (ENVIRONID,
                VIEWFOLDERID,NAME,COMMENTS,CREATEDTIMESTAMP,
                CREATEDUSERID,LASTMODTIMESTAMP,LASTMODUSERID)
         VALUES (NEWENV,0,'ALL_VIEWS','Folder containing all views',
                CURRENT_TIMESTAMP, P_USERID, CURRENT_TIMESTAMP, P_USERID);
	END;
$$
LANGUAGE plpgsql;
  
  
  
 