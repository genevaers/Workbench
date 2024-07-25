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
CREATE OR REPLACE FUNCTION :schemaV.newcrforenv (
                                IN P_ENVTNAME TEXT,
                                IN P_ENVCOMMENTS TEXT,
                                IN P_USERID TEXT,
                                IN P_CONTROLNAME TEXT) 
RETURNS INT 
AS $$
DECLARE EID INT;
      BEGIN
            select newenvironment(P_ENVTNAME,P_ENVCOMMENTS,P_USERID) INTO EID;
            INSERT INTO controlrec (environid,name,
                        firstmonth,lowvalue,highvalue,comments,
                        createdtimestamp,createduserid,
                        lastmodtimestamp,lastmoduserid)
            VALUES (EID,
                    P_CONTROLNAME,
                    1,1,12,'Created automatically during initialization',
                  CURRENT_TIMESTAMP,P_USERID,
                  CURRENT_TIMESTAMP,P_USERID);
		    RETURN EID;
      END;
$$
LANGUAGE plpgsql;
  
  
  
 