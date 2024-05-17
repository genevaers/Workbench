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
CREATE OR REPLACE FUNCTION :schemaV.updateViewColumn (  
                                                IN XIN TEXT) RETURNS VOID
AS $$
DECLARE root_xpath TEXT = '/Root/Record/';
DECLARE DOC XML;
DECLARE OP RECORD;

BEGIN
 DOC = XMLPARSE (DOCUMENT XIN);

-- If normal create
  FOR OP IN SELECT X.*
   FROM XMLTABLE ('/Root/Record' passing DOC 
   COLUMNS 
      ENVIRONID                 INT PATH 'ENVIRONID',
      VIEWID                    INT PATH 'VIEWID',
      VIEWCOLUMNID              INT PATH 'VIEWCOLUMNID',
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
      DEFAULTVAL                VARCHAR(5) PATH 'DEFAULTVAL',
      VISIBLE                   INT PATH 'VISIBLE',
      SUBTOTALTYPECD            VARCHAR(5) PATH 'SUBTOTALTYPECD',
      SPACESBEFORECOLUMN        INT PATH 'SPACESBEFORECOLUMN',
      EXTRACTAREACD             VARCHAR(5) PATH 'EXTRACTAREACD',
      EXTRAREAPOSITION          INT PATH 'EXTRAREAPOSITION',
      SUBTLABEL                 VARCHAR(5) PATH 'SUBTLABEL',
      RPTMASK                   VARCHAR(5) PATH 'RPTMASK',
      HDRJUSTIFYCD              VARCHAR(5) PATH 'HDRJUSTIFYCD',
      HDRLINE1                  VARCHAR(256) PATH 'HDRLINE1',
      HDRLINE2                  VARCHAR(256) PATH 'HDRLINE2',
      HDRLINE3                  VARCHAR(256) PATH 'HDRLINE3',
      FORMATCALCLOGIC           TEXT PATH 'FORMATCALCLOGIC',
	   CREATEDTIMESTAMP          TIMESTAMP PATH 'CREATEDTIMESTAMP',
      CREATEDUSERID             VARCHAR(8) PATH 'CREATEDUSERID',
	   LASTMODTIMESTAMP          TIMESTAMP PATH 'LASTMODTIMESTAMP',
      LASTMODUSERID             VARCHAR(8) PATH 'LASTMODUSERID')  as X
  LOOP    
    UPDATE VIEWCOLUMN SET
     COLUMNNUMBER=OP.COLUMNNUMBER,
     FLDFMTCD=OP.FLDFMTCD,
     SIGNEDIND=OP.SIGNEDIND,
     STARTPOSITION=OP.STARTPOSITION,
     MAXLEN=OP.MAXLEN,
     ORDINALPOSITION=OP.ORDINALPOSITION,
     DECIMALCNT=OP.DECIMALCNT,
     ROUNDING=OP.ROUNDING,
     FLDCONTENTCD=OP.FLDCONTENTCD,
     JUSTIFYCD=OP.JUSTIFYCD,
     DEFAULTVAL=OP.DEFAULTVAL,
     VISIBLE=OP.VISIBLE,
     SUBTOTALTYPECD=OP.SUBTOTALTYPECD,
     SPACESBEFORECOLUMN=OP.SPACESBEFORECOLUMN,
     EXTRACTAREACD=OP.EXTRACTAREACD,
     EXTRAREAPOSITION=OP.EXTRAREAPOSITION,
     SUBTLABEL=OP.SUBTLABEL,
     RPTMASK=OP.RPTMASK,
     HDRJUSTIFYCD=OP.HDRJUSTIFYCD,
     HDRLINE1=OP.HDRLINE1,
     HDRLINE2=OP.HDRLINE2,
     HDRLINE3=OP.HDRLINE3,
     FORMATCALCLOGIC=OP.FORMATCALCLOGIC,
     LASTMODUSERID=OP.LASTMODUSERID,
     LASTMODTIMESTAMP=CURRENT_TIMESTAMP
    WHERE ENVIRONID=OP.ENVIRONID
    AND VIEWID=OP.VIEWID
    AND VIEWCOLUMNID=OP.VIEWCOLUMNID;
 END LOOP;
END
$$
LANGUAGE plpgsql;