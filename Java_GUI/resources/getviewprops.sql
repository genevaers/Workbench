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

CREATE OR REPLACE FUNCTION :schemaV.getviewprops ( IN P_ENVTID INT,
                                                 IN  P_ADMININD INT,
                                                 IN  P_GROUPID INT)
  RETURNS refcursor
AS $$


  DECLARE VP_CUR CURSOR FOR SELECT 'VFOLDER',VIEWFOLDERID,NAME,3,''
  FROM VIEWFOLDER
  WHERE  ENVIRONID = P_ENVTID
  AND VIEWFOLDERID > 0

UNION
      
  SELECT 'LR', LOGRECID, NAME,3,''
  FROM LOGREC
  WHERE ENVIRONID = P_ENVTID AND LOGRECID > 0

UNION
   
  SELECT 'CONTROLREC', CONTROLRECID, NAME,3,''
  FROM CONTROLREC
  WHERE ENVIRONID = P_ENVTID AND CONTROLRECID > 0

UNION
   
  SELECT DISTINCT 'LFPIPE', A.LOGFILEID, A.NAME,3,''
  FROM LOGFILE A, PHYFILE B, LFPFASSOC C
   WHERE A.ENVIRONID = B.ENVIRONID
   AND B.ENVIRONID   = C.ENVIRONID
   AND A.LOGFILEID   = C.LOGFILEID
   AND A.LOGFILEID   > 0
   AND B.PHYFILEID   = C.PHYFILEID
   AND A.ENVIRONID   = P_ENVTID AND A.LOGFILEID > 0

UNION
   
  SELECT 'WRITEEXIT', EXITID, NAME,3,MODULEID
   FROM EXIT
   WHERE ENVIRONID = P_ENVTID
   AND EXITTYPECD ='WRITE' AND EXITID > 0 

UNION
   
  SELECT 'FORMATEXIT',EXITID, NAME,3,MODULEID
   FROM EXIT
   WHERE ENVIRONID = P_ENVTID
   AND EXITTYPECD ='FORMT' AND EXITID > 0 ;
  

BEGIN 
  OPEN VP_CUR;
  RETURN VP_CUR;
END;
$$
LANGUAGE plpgsql;