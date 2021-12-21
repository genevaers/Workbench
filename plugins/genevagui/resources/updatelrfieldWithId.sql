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
CREATE OR REPLACE FUNCTION :schemaV.updatelrfieldWithId (  
  IN XIN TEXT)
  RETURNS VOID

AS $$ 
DECLARE root_xpath TEXT = '/Root/Record/';
DECLARE DOC XML;
DECLARE OP RECORD;

BEGIN
 DOC = XMLPARSE (DOCUMENT XIN);

FOR OP IN SELECT X.*                                                 
   FROM XMLTABLE ('/Root/Record' passing DOC             
    COLUMNS                                                            
     ENVIRONID        INT PATH 'ENVIRONID',                          
     LRFIELDID        INT PATH 'LRFIELDID',                          
     LOGRECID         INT PATH 'LOGRECID',                           
     NAME             VARCHAR(48) PATH 'NAME',                       
     DBMSCOLNAME      VARCHAR(48) PATH 'DBMSCOLNAME',                
     FIXEDSTARTPOS    INT PATH 'FIXEDSTARTPOS',                      
     ORDINALPOS       INT PATH 'ORDINALPOS',                         
     ORDINALOFFSET    INT PATH 'ORDINALOFFSET',                      
     REDEFINE         INT PATH 'REDEFINE',                           
     COMMENTS         VARCHAR(254) PATH 'COMMENTS',                  
     FLDFMTCD         VARCHAR(5) PATH 'FLDFMTCD',                    
     SIGNEDIND        SMALLINT PATH 'SIGNEDIND',                     
     MAXLEN           INT PATH 'MAXLEN',                             
     DECIMALCNT       INT PATH 'DECIMALCNT',                         
     ROUNDING         INT PATH 'ROUNDING',                           
     FLDCONTENTCD     VARCHAR(5) PATH 'FLDCONTENTCD',                
     HDRJUSTIFYCD     VARCHAR(5) PATH 'HDRJUSTIFYCD',                
     HDRLINE1         VARCHAR(254) PATH 'HDRLINE1',                  
     HDRLINE2         VARCHAR(254) PATH 'HDRLINE2',                  
     HDRLINE3         VARCHAR(254) PATH 'HDRLINE3',                  
     SUBTLABEL        VARCHAR(48) PATH 'SUBTLABEL',                  
     SORTKEYLABEL     VARCHAR(48) PATH 'SORTKEYLABEL',               
     INPUTMASK        VARCHAR(48) PATH 'INPUTMASK',                  
     CREATEDTIMESTAMP TIMESTAMP PATH 'CREATEDTIMESTAMP',             
     CREATEDUSERID    VARCHAR(8) PATH 'CREATEDUSERID',               
     LASTMODTIMESTAMP TIMESTAMP PATH 'LASTMODTIMESTAMP',             
     LASTMODUSERID    VARCHAR(8) PATH 'LASTMODUSERID') AS X          
  LOOP                                                                   
    UPDATE LRFIELD SET                                                 
     NAME=OP.NAME, 
     DBMSCOLNAME=OP.DBMSCOLNAME,FIXEDSTARTPOS=OP.FIXEDSTARTPOS,        
     ORDINALPOS=OP.ORDINALPOS,ORDINALOFFSET=OP.ORDINALOFFSET,          
     REDEFINE=OP.REDEFINE,COMMENTS=OP.COMMENTS,                        
     CREATEDTIMESTAMP=OP.CREATEDTIMESTAMP,                             
     CREATEDUSERID=OP.CREATEDUSERID,                                   
     LASTMODTIMESTAMP=OP.LASTMODTIMESTAMP,                             
     LASTMODUSERID=OP.LASTMODUSERID                                    
    WHERE ENVIRONID=OP.ENVIRONID
    AND LOGRECID=OP.LOGRECID                                       
    AND LRFIELDID=OP.LRFIELDID;                                        
                                 
    UPDATE LRFIELDATTR SET                                      
     FLDFMTCD=OP.FLDFMTCD,SIGNEDIND=OP.SIGNEDIND,               
     MAXLEN=OP.MAXLEN,DECIMALCNT=OP.DECIMALCNT,                 
     ROUNDING=OP.ROUNDING,FLDCONTENTCD=OP.FLDCONTENTCD,         
     HDRJUSTIFYCD=OP.HDRJUSTIFYCD,                              
     HDRLINE1=OP.HDRLINE1,HDRLINE2=OP.HDRLINE2,                 
     HDRLINE3=OP.HDRLINE3,SUBTLABEL=OP.SUBTLABEL,               
     SORTKEYLABEL=OP.SORTKEYLABEL,INPUTMASK=OP.INPUTMASK,       
     CREATEDTIMESTAMP=OP.CREATEDTIMESTAMP,                      
     CREATEDUSERID=OP.CREATEDUSERID,                            
     LASTMODTIMESTAMP=OP.LASTMODTIMESTAMP,                      
     LASTMODUSERID=OP.LASTMODUSERID                             
    WHERE ENVIRONID=OP.ENVIRONID                                
    AND LRFIELDID=OP.LRFIELDID;                                 
                                                                
  END LOOP;                  
 
END
$$
LANGUAGE plpgsql;
