-- ******************************************************************
--
-- (C) COPYRIGHT IBM CORPORATION 2010, 2023.
--     Copyright Contributors to the GenevaERS Project.
-- SPDX-License-Identifier: Apache-2.0
--
-- ******************************************************************
--
--   Licensed under the Apache License, Version 2.0 (the "License");
--   you may not use this file except in compliance with the License.
--   You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--   Unless required by applicable law or agreed to in writing, software
--   distributed under the License is distributed on an "AS IS" BASIS,
--   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
--   or implied.
--   See the License for the specific language governing permissions
--   and limitations under the License.
--
-- ****************************************************************
--

CREATE PROCEDURE INSLRFIELD (  
  IN DOC CLOB, 
  IN IMPORT SMALLINT)

RESULT SETS 0
LANGUAGE SQL
DISABLE DEBUG MODE 

BEGIN
 DECLARE PDOC XML;
 SET PDOC = XMLPARSE(DOCUMENT DOC PRESERVE WHITESPACE);

-- If normal create
 IF IMPORT = 0 THEN

  INSERT INTO LRFIELD (
      ENVIRONID,LRFIELDID,LOGRECID,
      NAME,DBMSCOLNAME,FIXEDSTARTPOS,ORDINALPOS,
      ORDINALOFFSET,REDEFINE,COMMENTS,
      CREATEDTIMESTAMP,CREATEDUSERID,
      LASTMODTIMESTAMP,LASTMODUSERID)
   SELECT X.ENVIRONID,X.LRFIELDID,X.LOGRECID,
    X.NAME,X.DBMSCOLNAME,X.FIXEDSTARTPOS,X.ORDINALPOS,
    X.ORDINALOFFSET,X.REDEFINE,X.COMMENTS,
    CURRENT TIMESTAMP,X.CREATEDUSERID,
    CURRENT TIMESTAMP,X.LASTMODUSERID
   FROM XMLTABLE('$doc/Root/Record' passing PDOC as "doc"
   COLUMNS 
     "ENVIRONID"        INT PATH 'ENVIRONID',
     "LRFIELDID"        INT PATH 'LRFIELDID',
     "LOGRECID"         INT PATH 'LOGRECID',
     "NAME"             VARCHAR(48) PATH 'NAME',
     "DBMSCOLNAME"      VARCHAR(48) PATH 'DBMSCOLNAME',
     "FIXEDSTARTPOS"    INT PATH 'FIXEDSTARTPOS',
     "ORDINALPOS"       INT PATH 'ORDINALPOS',
     "ORDINALOFFSET"    INT PATH 'ORDINALOFFSET',
     "REDEFINE"         INT PATH 'REDEFINE',
     "COMMENTS"         VARCHAR(254) PATH 'COMMENTS',
     "CREATEDUSERID"    VARCHAR(8) PATH 'CREATEDUSERID',
     "LASTMODUSERID"    VARCHAR(8) PATH 'LASTMODUSERID') AS X;

  INSERT INTO LRFIELDATTR (
      ENVIRONID,LRFIELDID,FLDFMTCD,SIGNEDIND,
      MAXLEN,DECIMALCNT,ROUNDING,FLDCONTENTCD,
      HDRJUSTIFYCD,HDRLINE1,HDRLINE2,HDRLINE3,
      SUBTLABEL,SORTKEYLABEL,INPUTMASK,
      CREATEDTIMESTAMP,CREATEDUSERID,
      LASTMODTIMESTAMP,LASTMODUSERID)
   SELECT X.ENVIRONID,X.LRFIELDID,X.FLDFMTCD,X.SIGNEDIND,
    X.MAXLEN,X.DECIMALCNT,X.ROUNDING,X.FLDCONTENTCD,
    X.HDRJUSTIFYCD,X.HDRLINE1,X.HDRLINE2,X.HDRLINE3,
    X.SUBTLABEL,X.SORTKEYLABEL,X.INPUTMASK,
    CURRENT TIMESTAMP,X.CREATEDUSERID,
    CURRENT TIMESTAMP,X.LASTMODUSERID
   FROM XMLTABLE('$doc/Root/Record' passing PDOC as "doc"
   COLUMNS 
     "ENVIRONID"        INT PATH 'ENVIRONID',
     "LRFIELDID"        INT PATH 'LRFIELDID',
     "FLDFMTCD"         VARCHAR(5) PATH 'FLDFMTCD',     
     "SIGNEDIND"        SMALLINT PATH 'SIGNEDIND',
     "MAXLEN"           INT PATH 'MAXLEN',
     "DECIMALCNT"       INT PATH 'DECIMALCNT',
     "ROUNDING"         INT PATH 'ROUNDING',
     "FLDCONTENTCD"     VARCHAR(5) PATH 'FLDCONTENTCD',     
     "HDRJUSTIFYCD"     VARCHAR(5) PATH 'HDRJUSTIFYCD',
     "HDRLINE1"         VARCHAR(254) PATH 'HDRLINE1',
     "HDRLINE2"         VARCHAR(254) PATH 'HDRLINE2',
     "HDRLINE3"         VARCHAR(254) PATH 'HDRLINE3',
     "SUBTLABEL"        VARCHAR(48) PATH 'SUBTLABEL',
     "SORTKEYLABEL"     VARCHAR(48) PATH 'SORTKEYLABEL',
     "INPUTMASK"        VARCHAR(48) PATH 'INPUTMASK',
     "CREATEDUSERID"    VARCHAR(8) PATH 'CREATEDUSERID',
     "LASTMODUSERID"    VARCHAR(10) PATH 'LASTMODUSERID') AS X;

 -- Creation from an import or migration
 ELSE

  INSERT INTO LRFIELD (
      ENVIRONID,LRFIELDID,LOGRECID,
      NAME,DBMSCOLNAME,FIXEDSTARTPOS,ORDINALPOS,
      ORDINALOFFSET,REDEFINE,COMMENTS,
      CREATEDTIMESTAMP,CREATEDUSERID,
      LASTMODTIMESTAMP,LASTMODUSERID)
   SELECT X.ENVIRONID,X.LRFIELDID,X.LOGRECID,
    X.NAME,X.DBMSCOLNAME,X.FIXEDSTARTPOS,X.ORDINALPOS,
    X.ORDINALOFFSET,X.REDEFINE,X.COMMENTS,
    X.CREATEDTIMESTAMP,X.CREATEDUSERID,
    X.LASTMODTIMESTAMP,X.LASTMODUSERID
   FROM XMLTABLE('$doc/Root/Record' passing PDOC as "doc"
   COLUMNS 
     "ENVIRONID"        INT PATH 'ENVIRONID',
     "LRFIELDID"        INT PATH 'LRFIELDID',
     "LOGRECID"         INT PATH 'LOGRECID',
     "NAME"             VARCHAR(48) PATH 'NAME',
     "DBMSCOLNAME"      VARCHAR(48) PATH 'DBMSCOLNAME',
     "FIXEDSTARTPOS"    INT PATH 'FIXEDSTARTPOS',
     "ORDINALPOS"       INT PATH 'ORDINALPOS',
     "ORDINALOFFSET"    INT PATH 'ORDINALOFFSET',
     "REDEFINE"         INT PATH 'REDEFINE',
     "COMMENTS"         VARCHAR(254) PATH 'COMMENTS',
     "CREATEDTIMESTAMP" TIMESTAMP PATH 'CREATEDTIMESTAMP',
     "CREATEDUSERID"    VARCHAR(8) PATH 'CREATEDUSERID',
     "LASTMODTIMESTAMP" TIMESTAMP PATH 'LASTMODTIMESTAMP',
     "LASTMODUSERID"    VARCHAR(8) PATH 'LASTMODUSERID') AS X;

  INSERT INTO LRFIELDATTR (
      ENVIRONID,LRFIELDID,FLDFMTCD,SIGNEDIND,
      MAXLEN,DECIMALCNT,ROUNDING,FLDCONTENTCD,
      HDRJUSTIFYCD,HDRLINE1,HDRLINE2,HDRLINE3,
      SUBTLABEL,SORTKEYLABEL,INPUTMASK,
      CREATEDTIMESTAMP,CREATEDUSERID,
      LASTMODTIMESTAMP,LASTMODUSERID)
   SELECT X.ENVIRONID,X.LRFIELDID,X.FLDFMTCD,X.SIGNEDIND,
    X.MAXLEN,X.DECIMALCNT,X.ROUNDING,X.FLDCONTENTCD,
    X.HDRJUSTIFYCD,X.HDRLINE1,X.HDRLINE2,X.HDRLINE3,
    X.SUBTLABEL,X.SORTKEYLABEL,X.INPUTMASK,
    X.CREATEDTIMESTAMP,X.CREATEDUSERID,
    X.LASTMODTIMESTAMP,X.LASTMODUSERID
   FROM XMLTABLE('$doc/Root/Record' passing PDOC as "doc"
   COLUMNS 
     "ENVIRONID"        INT PATH 'ENVIRONID',
     "LRFIELDID"        INT PATH 'LRFIELDID',
     "FLDFMTCD"         VARCHAR(5) PATH 'FLDFMTCD',     
     "SIGNEDIND"        SMALLINT PATH 'SIGNEDIND',
     "MAXLEN"           INT PATH 'MAXLEN',
     "DECIMALCNT"       INT PATH 'DECIMALCNT',
     "ROUNDING"         INT PATH 'ROUNDING',
     "FLDCONTENTCD"     VARCHAR(5) PATH 'FLDCONTENTCD',     
     "HDRJUSTIFYCD"     VARCHAR(5) PATH 'HDRJUSTIFYCD',
     "HDRLINE1"         VARCHAR(254) PATH 'HDRLINE1',
     "HDRLINE2"         VARCHAR(254) PATH 'HDRLINE2',
     "HDRLINE3"         VARCHAR(254) PATH 'HDRLINE3',
     "SUBTLABEL"        VARCHAR(48) PATH 'SUBTLABEL',
     "SORTKEYLABEL"     VARCHAR(48) PATH 'SORTKEYLABEL',
     "INPUTMASK"        VARCHAR(48) PATH 'INPUTMASK',
     "CREATEDTIMESTAMP" TIMESTAMP PATH 'CREATEDTIMESTAMP',
     "CREATEDUSERID"    VARCHAR(8) PATH 'CREATEDUSERID',
     "LASTMODTIMESTAMP" TIMESTAMP PATH 'LASTMODTIMESTAMP',
     "LASTMODUSERID"    VARCHAR(8) PATH 'LASTMODUSERID') AS X;
 
 END IF;
END