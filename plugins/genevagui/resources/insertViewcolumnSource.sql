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

CREATE OR REPLACE FUNCTION :schemaV.insertViewColumnSource (  
                                                IN XIN TEXT) RETURNS VOID
AS $$
DECLARE root_xpath TEXT = '/Root/Record/';
DECLARE DOC XML;

BEGIN
 DOC = XMLPARSE (DOCUMENT XIN);

   INSERT INTO VIEWCOLUMNSOURCE (
     ENVIRONID,
     VIEWCOLUMNID,
     VIEWSOURCEID,
     VIEWID,
     SOURCETYPEID,
     CONSTVAL,
     LOOKUPID,
     LRFIELDID,
     EFFDATEVALUE,
     EFFDATETYPE,
     EFFDATELRFIELDID,
     SORTTITLELOOKUPID,
     SORTTITLELRFIELDID,
     EXTRACTCALCLOGIC,
     CREATEDTIMESTAMP,
     CREATEDUSERID,
     LASTMODTIMESTAMP,
     LASTMODUSERID)
   SELECT X.ENVIRONID,X.VIEWCOLUMNID,
     X.VIEWSOURCEID,X.VIEWID,X.SOURCETYPEID,
     X.CONSTVAL,X.LOOKUPID,X.LRFIELDID,
     X.EFFDATEVALUE,X.EFFDATETYPE,X.EFFDATELRFIELDID,
     X.SORTTITLELOOKUPID,X.SORTTITLELRFIELDID,
     X.EXTRACTCALCLOGIC,
     CURRENT_TIMESTAMP,X.CREATEDUSERID,
     CURRENT_TIMESTAMP,X.LASTMODUSERID
   FROM XMLTABLE('//Root/Record' passing DOC
   COLUMNS
     ENVIRONID                 INT PATH 'ENVIRONID',
     VIEWCOLUMNID              INT PATH 'VIEWCOLUMNID',
     VIEWSOURCEID              INT PATH 'VIEWSOURCEID',
     VIEWID                    INT PATH 'VIEWID',
     SOURCETYPEID              INT PATH 'SOURCETYPEID',
     CONSTVAL                  VARCHAR(256) PATH 'CONSTVAL',
     LOOKUPID                  INT PATH 'LOOKUPID',
     LRFIELDID                 INT PATH 'LRFIELDID',
     EFFDATEVALUE              VARCHAR(24) PATH 'EFFDATEVALUE',
     EFFDATETYPE               VARCHAR(5) PATH 'EFFDATETYPE',
     EFFDATELRFIELDID          INT PATH 'EFFDATELRFIELDID',
     SORTTITLELOOKUPID         INT PATH 'SORTTITLELOOKUPID',
     SORTTITLELRFIELDID        INT PATH 'SORTTITLELRFIELDID',
     EXTRACTCALCLOGIC          TEXT PATH 'EXTRACTCALCLOGIC',
     CREATEDUSERID             VARCHAR(8) PATH 'CREATEDUSERID',
     LASTMODUSERID             VARCHAR(8) PATH 'LASTMODUSERID')  as X;

 --    RETURN;
     -- Creation from Normal Create 

END
$$
LANGUAGE plpgsql;