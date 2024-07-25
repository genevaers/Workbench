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

CREATE OR REPLACE FUNCTION :schemaV.insertColumn (  
                                                IN XIN TEXT) RETURNS VOID
AS $$
DECLARE root_xpath TEXT = '/Root/Record/';
DECLARE DOC XML;

BEGIN
 DOC = XMLPARSE (DOCUMENT XIN);

 INSERT INTO VIEWCOLUMN (
      ENVIRONID,
      VIEWID,
      COLUMNNUMBER,
      FLDFMTCD,
      SIGNEDIND,
      STARTPOSITION,
      MAXLEN,
      ORDINALPOSITION,
      DECIMALCNT,ROUNDING,
      FLDCONTENTCD,
      JUSTIFYCD,
      DEFAULTVAL,
      VISIBLE,
      SUBTOTALTYPECD,
      SPACESBEFORECOLUMN,
      EXTRACTAREACD,
      EXTRAREAPOSITION,
      SUBTLABEL,
      RPTMASK,
      HDRJUSTIFYCD,
      HDRLINE1,
      HDRLINE2,
      HDRLINE3,
      FORMATCALCLOGIC,
      CREATEDTIMESTAMP,
      CREATEDUSERID,
      LASTMODTIMESTAMP,
      LASTMODUSERID)
SELECT X.ENVIRONID,X.VIEWID,X.COLUMNNUMBER,
      X.FLDFMTCD,X.SIGNEDIND,X.STARTPOSITION,X.MAXLEN,
      X.ORDINALPOSITION,X.DECIMALCNT,X.ROUNDING,
      X.FLDCONTENTCD,X.JUSTIFYCD,X.DEFAULTVAL,
      X.VISIBLE,X.SUBTOTALTYPECD,X.SPACESBEFORECOLUMN,
      X.EXTRACTAREACD,X.EXTRAREAPOSITION,
      X.SUBTLABEL,X.RPTMASK,
      X.HDRJUSTIFYCD,X.HDRLINE1,X.HDRLINE2,X.HDRLINE3,
      X.FORMATCALCLOGIC,
      CURRENT_TIMESTAMP,X.CREATEDUSERID,
      CURRENT_TIMESTAMP,X.LASTMODUSERID
      FROM XMLTABLE('//Root/Record' passing DOC
   COLUMNS 
      ENVIRONID                 INT PATH 'ENVIRONID',
      VIEWID                    INT PATH 'VIEWID',
      COLUMNNUMBER              INT PATH 'COLUMNNUMBER',
      FLDFMTCD                  VARCHAR(5) PATH 'FLDFMTCD',
      SIGNEDIND                 INT PATH 'SIGNEDIND',
      STARTPOSITION             INT PATH 'STARTPOSITION',
      MAXLEN                    INT PATH 'MAXLEN',
      ORDINALPOSITION           INT PATH 'ORDINALPOSITION',
      DECIMALCNT                INT PATH 'DECIMALCNT',
      ROUNDING                  INT PATH 'ROUNDING',
      FLDCONTENTCD              VARCHAR(5) PATH 'FLDCONTENTCD',
      JUSTIFYCD                 VARCHAR(5) PATH 'JUSTIFYCD',
      DEFAULTVAL                VARCHAR(256) PATH 'DEFAULTVAL',
      VISIBLE                   INT PATH 'VISIBLE',
      SUBTOTALTYPECD            VARCHAR(5) PATH 'SUBTOTALTYPECD',
      SPACESBEFORECOLUMN        INT PATH 'SPACESBEFORECOLUMN',
      EXTRACTAREACD             VARCHAR(5) PATH 'EXTRACTAREACD',
      EXTRAREAPOSITION          INT PATH 'EXTRAREAPOSITION',
      SUBTLABEL                 VARCHAR(48) PATH 'SUBTLABEL',
      RPTMASK                   VARCHAR(48) PATH 'RPTMASK',
      HDRJUSTIFYCD              VARCHAR(5) PATH 'HDRJUSTIFYCD',
      HDRLINE1                  VARCHAR(256) PATH 'HDRLINE1',
      HDRLINE2                  VARCHAR(256) PATH 'HDRLINE2',
      HDRLINE3                  VARCHAR(256) PATH 'HDRLINE3',
      FORMATCALCLOGIC           TEXT PATH 'FORMATCALCLOGIC',
      CREATEDUSERID             VARCHAR(8) PATH 'CREATEDUSERID',
      LASTMODUSERID             VARCHAR(8) PATH 'LASTMODUSERID')  as X;

END
$$
LANGUAGE plpgsql;