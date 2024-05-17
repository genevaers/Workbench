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

CREATE OR REPLACE FUNCTION :schemaV.insertlookupsrcfield (  
  IN XIN TEXT)
  RETURNS VOID

AS $$ 
--DECLARE PDOC XML;
DECLARE root_xpath TEXT = '/Root/Record/';
DECLARE DOC XML;
BEGIN
 DOC = XMLPARSE (DOCUMENT XIN);

  INSERT INTO LOOKUPSRCKEY (
    ENVIRONID,
    LOOKUPSTEPID,
    KEYSEQNBR,
    FLDTYPE,
    LRFIELDID,
    LRLFASSOCID,
    LOOKUPID,
    VALUEFMTCD,
    SIGNED,
    VALUELEN,
    DECIMALCNT,
    FLDCONTENTCD,
    ROUNDING,
    JUSTIFYCD,
    MASK,
    SYMBOLICNAME,
    VALUE,
    CREATEDTIMESTAMP,
    CREATEDUSERID,
    LASTMODTIMESTAMP,
    LASTMODUSERID)
   SELECT   X.ENVIRONID,
            X.LOOKUPSTEPID,
            X.KEYSEQNBR,
            X.FLDTYPE,
            X.LRFIELDID,
            X.LRLFASSOCID,
            X.LOOKUPID,
            X.VALUEFMTCD,
            X.SIGNED,
            X.VALUELEN,
            X.DECIMALCNT,
            X.FLDCONTENTCD,
            X.ROUNDING,
            X.JUSTIFYCD,
            X.MASK,
            X.SYMBOLICNAME,
            X.VALUE,
            CURRENT_TIMESTAMP,
            X.CREATEDUSERID,
            CURRENT_TIMESTAMP,
            X.LASTMODUSERID
   FROM XMLTABLE('//Root/Record' passing DOC
   COLUMNS 
     ENVIRONID        INT PATH 'ENVIRONID',
     LOOKUPSTEPID     INT PATH 'LOOKUPSTEPID',
     KEYSEQNBR        INT PATH 'KEYSEQNBR',
     FLDTYPE          INT PATH 'FLDTYPE',
     LRFIELDID        INT PATH 'LRFIELDID',
     LRLFASSOCID      INT PATH 'LRLFASSOCID',
     LOOKUPID         INT PATH 'LOOKUPID',
     VALUEFMTCD       VARCHAR(5) PATH 'VALUEFMTCD',
     SIGNED           SMALLINT PATH 'SIGNED',
     VALUELEN         INT PATH 'VALUELEN',
     DECIMALCNT       INT PATH 'DECIMALCNT',
     FLDCONTENTCD     VARCHAR(5) PATH 'FLDCONTENTCD',
     ROUNDING         INT PATH 'ROUNDING',
     JUSTIFYCD        VARCHAR(5) PATH 'JUSTIFYCD',
     MASK             VARCHAR(48) PATH 'MASK',
     SYMBOLICNAME     VARCHAR(254) PATH 'SYMBOLICNAME',
     VALUE            VARCHAR(254) PATH 'VALUE',
     CREATEDUSERID    VARCHAR(8) PATH 'CREATEDUSERID',
     LASTMODUSERID    VARCHAR(8) PATH 'LASTMODUSERID')  as X;
     
 END
 $$
LANGUAGE plpgsql;