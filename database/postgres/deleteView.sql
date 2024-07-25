--****************************************************************
--
-- (c) Copyright IBM Corporation 2004,2017.  
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

CREATE OR REPLACE FUNCTION :schemaV.deleteView (
                                IN  P_VIEWID INT,
                                IN  P_ENVIRONID INT)
RETURNS VOID
AS $$
BEGIN

--**************************************************************
--DELETES THE VIEW LOGIC DEPENDENCY ASSOCIATIONS
-- RELATED TO THE ENVIRONMENT AND VIEW
--**************************************************************

   DELETE FROM VIEWLOGICDEPEND WHERE ENVIRONID = P_ENVIRONID 
                                 AND VIEWID = P_VIEWID;

--**************************************************************
--DELETES THE VIEWHEADERFOOTER RELATED TO THE ENVIRONMENT AND VIEW
--**************************************************************

    DELETE FROM VIEWHEADERFOOTER WHERE ENVIRONID = P_ENVIRONID
                                   AND VIEWID = P_VIEWID;

--**************************************************************
--DELETES THE VIEWSORTKEY RELATED TO THE ENVIRONMENT AND VIEW
--**************************************************************

    DELETE FROM VIEWSORTKEY WHERE ENVIRONID = P_ENVIRONID
                              AND VIEWID = P_VIEWID;

--**************************************************************
--DELETES THE VIEWCOLUMNSOURCE RELATED TO THE ENVIRONMENT AND VIEW
--**************************************************************

    DELETE FROM VIEWCOLUMNSOURCE WHERE ENVIRONID = P_ENVIRONID
                                   AND VIEWID = P_VIEWID;

--**************************************************************
--DELETES THE VIEWCOLUMN RELATED TO THE ENVIRONMENT AND VIEW
--**************************************************************

    DELETE FROM VIEWCOLUMN WHERE ENVIRONID = P_ENVIRONID
                             AND VIEWID = P_VIEWID;

--**************************************************************
--DELETES THE VIEWSOURCE 
-- RELATED TO THE ENVIRONMENT AND VIEW
--**************************************************************

   DELETE FROM VIEWSOURCE WHERE ENVIRONID = P_ENVIRONID 
                            AND VIEWID = P_VIEWID;

--**************************************************************
--DELETES THE SECVIEW 
-- RELATED TO THE ENVIRONMENT AND VIEW
--**************************************************************

   DELETE FROM SECVIEW WHERE ENVIRONID = P_ENVIRONID 
                         AND VIEWID = P_VIEWID;

--**************************************************************
--DELETES THE VFVASSOC ASSOCIATIONS
-- RELATED TO THE ENVIRONMENT AND VIEW
--**************************************************************

   DELETE FROM VFVASSOC WHERE ENVIRONID = P_ENVIRONID 
                                 AND VIEWID = P_VIEWID;

--**************************************************************
--DELETES THE VIEW 
-- RELATED TO THE ENVIRONMENT AND VIEW
--**************************************************************

   DELETE FROM VIEW WHERE ENVIRONID = P_ENVIRONID 
                                 AND VIEWID = P_VIEWID;


END;
	$$
	LANGUAGE plpgsql;
