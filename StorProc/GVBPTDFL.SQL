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

CREATE PROCEDURE GETDEPSFIELD(IN P_ENVIRONID INT,
                                  IN P_ENTITYID INT)

DYNAMIC RESULT SETS 1
LANGUAGE SQL
DISABLE DEBUG MODE 

--THIS SP WILL BE CALLED TO GET LR FIELD DEPENDENCIES

BEGIN

 DECLARE SQLSTATE CHAR(5) DEFAULT '00000' ;

 DECLARE DEPFLD_CUR CURSOR WITH RETURN
  FOR SELECT  * FROM SESSION.TMPDEPFLD
  ORDER BY UPPER(ENTITYTYPE),
  UPPER(CHILDTYPE),
  UPPER(ENTITYNAME);

 DECLARE GLOBAL TEMPORARY TABLE
 SESSION.TMPDEPFLD
 (ENTITYTYPE VARCHAR(30),CHILDTYPE CHAR(30),
 ENTITYID INT,ENTITYNAME VARCHAR(254),
 ENVIRONID INT,ENVIRON VARCHAR(50),DEFAULTFLG SMALLINT,
 DEPENDFLG CHAR(3))
 ON COMMIT DROP TABLE;

 INSERT INTO SESSION.TMPDEPFLD
  VALUES ('Logical Records' ,'NONE', 0, '  ',0,' ',0,'N');

 INSERT INTO SESSION.TMPDEPFLD
  SELECT 'Logical Records','NONE',B.LOGRECID,B.NAME,
   A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A,
   LOGREC B,
   LRFIELD C
  WHERE A.ENVIRONID=B.ENVIRONID
  AND   B.ENVIRONID=C.ENVIRONID
  AND   B.LOGRECID=C.LOGRECID
  AND   C.ENVIRONID=P_ENVIRONID
  AND   C.LRFIELDID=P_ENTITYID;

 INSERT INTO SESSION.TMPDEPFLD
 VALUES ( 'Joins' ,'Source', 0, ' ',0,' ',0,'N');

 INSERT INTO SESSION.TMPDEPFLD
  SELECT DISTINCT 'Joins','Source',B.LOOKUPID,B.NAME,
   A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A,
   LOOKUP B,
   LOOKUPSRCKEY C,
   LRFIELD D
  WHERE A.ENVIRONID=B.ENVIRONID
  AND   B.ENVIRONID=C.ENVIRONID
  AND   B.LOOKUPID=C.LOOKUPID
  AND   C.ENVIRONID=D.ENVIRONID
  AND   C.LRFIELDID=D.LRFIELDID
  AND   D.ENVIRONID=P_ENVIRONID
  AND   D.LRFIELDID=P_ENTITYID;

 INSERT INTO SESSION.TMPDEPFLD
  VALUES ( 'Joins' ,'Target', 0, ' ',0,' ',0,'N')  ;

 INSERT INTO SESSION.TMPDEPFLD
  SELECT DISTINCT 'Joins','Target', B.LOOKUPID, B.NAME,
   A.ENVIRONID, A.NAME,0,'N'
  FROM ENVIRON A,
   LOOKUP B,
   LOOKUPSTEP C,
   LRLFASSOC D,
   LRFIELD E,
   LRINDEXFLD F
  WHERE A.ENVIRONID=B.ENVIRONID
  AND   B.ENVIRONID=C.ENVIRONID
  AND   B.LOOKUPID=C.LOOKUPID
  AND   C.ENVIRONID=D.ENVIRONID
  AND   C.LRLFASSOCID=D.LRLFASSOCID
  AND   D.ENVIRONID=E.ENVIRONID
  AND   D.LOGRECID=E.LOGRECID
  AND   E.ENVIRONID=F.ENVIRONID
  AND   E.LRFIELDID=F.LRFIELDID
  AND   F.ENVIRONID=P_ENVIRONID
  AND   F.LRFIELDID=P_ENTITYID;

 INSERT INTO SESSION.TMPDEPFLD
  VALUES ('Views','Source', 0, ' ',0,' ',0,'N') ;

 INSERT INTO SESSION.TMPDEPFLD
  SELECT DISTINCT 'Views','Source',B.VIEWID,B.NAME,
   A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A,
   VIEW B,
   VIEWCOLUMNSOURCE C,
   LRFIELD D
  WHERE A.ENVIRONID=B.ENVIRONID
  AND   B.ENVIRONID=C.ENVIRONID
  AND   B.VIEWID=C.VIEWID
  AND   C.ENVIRONID=D.ENVIRONID
  AND   C.LRFIELDID=D.LRFIELDID
  AND   D.ENVIRONID=P_ENVIRONID
  AND   D.LRFIELDID=P_ENTITYID;

 INSERT INTO SESSION.TMPDEPFLD
  VALUES ('Views','Effective Date', 0, ' ',0,' ',0,'N') ;

 INSERT INTO SESSION.TMPDEPFLD
  SELECT DISTINCT 'Views','Effective Date',B.VIEWID,B.NAME,
   A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A,
   VIEW B,
   VIEWCOLUMNSOURCE C,
   LRFIELD D
  WHERE A.ENVIRONID=B.ENVIRONID
  AND   B.ENVIRONID=C.ENVIRONID
  AND   B.VIEWID=C.VIEWID
  AND   C.ENVIRONID=D.ENVIRONID
  AND   C.EFFDATELRFIELDID=D.LRFIELDID
  AND   D.ENVIRONID=P_ENVIRONID
  AND   D.LRFIELDID=P_ENTITYID;

 INSERT INTO SESSION.TMPDEPFLD
  VALUES ('Views','Logic Text', 0, ' ',0,' ',0,'N') ;

 INSERT INTO SESSION.TMPDEPFLD
  SELECT 'Views','Logic Text',B.VIEWID,B.NAME,
   A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A,
   VIEW B,
   VIEWLOGICDEPEND C,
   LRFIELD D
  WHERE A.ENVIRONID=B.ENVIRONID
  AND   B.ENVIRONID=C.ENVIRONID
  AND   B.VIEWID=C.VIEWID
  AND   C.ENVIRONID=D.ENVIRONID
  AND   C.LRFIELDID=D.LRFIELDID
  AND  (C.LOGICTYPECD=01 OR C.LOGICTYPECD=05)
  AND   D.ENVIRONID=P_ENVIRONID
  AND   D.LRFIELDID=P_ENTITYID;

 INSERT INTO SESSION.TMPDEPFLD
  SELECT DISTINCT 'Views','Logic Text',B.VIEWID,B.NAME,
   A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A,
   VIEW B,
   VIEWLOGICDEPEND C,
   VIEWCOLUMNSOURCE D
  WHERE A.ENVIRONID=B.ENVIRONID
  AND   B.ENVIRONID=C.ENVIRONID
  AND   B.VIEWID=C.VIEWID
  AND   C.ENVIRONID=D.ENVIRONID
  AND   C.PARENTID=D.VIEWCOLUMNSOURCEID 
  AND   C.LRFIELDID=P_ENTITYID
  AND   C.LOGICTYPECD=02
  AND   D.SOURCETYPEID=04
  AND   D.ENVIRONID=P_ENVIRONID;

 INSERT INTO SESSION.TMPDEPFLD
  VALUES ('View Folders' ,'None', 0, ' ',0,' ',0,'N') ;
   
 INSERT INTO SESSION.TMPDEPFLD
  SELECT DISTINCT 'View Folders' ,'None',
    B.VIEWFOLDERID,
    B.NAME,
    A.ENVIRONID,
    A.NAME,
    0,
    'N'
  FROM ENVIRON A,
       VIEWFOLDER B,
       VFVASSOC C,
       SESSION.TMPDEPFLD D
  WHERE A.ENVIRONID=B.ENVIRONID
  AND B.ENVIRONID=C.ENVIRONID
  AND B.VIEWFOLDERID=C.VIEWFOLDERID
  AND C.ENVIRONID=D.ENVIRONID
  AND C.VIEWID=D.ENTITYID
  AND UPPER(D.ENTITYTYPE)='VIEWS'
  AND D.ENTITYID > 0;
  
 DELETE FROM SESSION.TMPDEPFLD
 WHERE ENTITYID = 0
 AND ENVIRONID > 0;

 OPEN DEPFLD_CUR ;

 END