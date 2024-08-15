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

CREATE PROCEDURE ENVINITVALUES (IN P_ENVTNAME CHARACTER(48),
                                IN P_ENVCOMMENTS VARCHAR(255),
                                IN P_USERID CHAR(8),
                                IN P_CONTROLNAME VARCHAR(64),
                                OUT P_ENVTID INT)

RESULT SETS 0
LANGUAGE SQL
DISABLE DEBUG MODE 

BEGIN

DECLARE SQLSTATE CHAR(5) DEFAULT '00000';

DECLARE V_SRCNVT INT ;

--BELOW SQL POPULATES C_ENVIRONTBL WITH ENVIRONID =V_SRCNVT
--WHICH IS NEW ENVIRONMENT ID.

SELECT environid INTO V_SRCNVT
FROM FINAL TABLE (
  INSERT INTO ENVIRON
 (name,comments,
  createdtimestamp,createduserid,
  lastmodtimestamp,lastmoduserid)
  VALUES (
  P_ENVTNAME,P_ENVCOMMENTS,
  CURRENT TIMESTAMP,P_USERID,
  CURRENT TIMESTAMP,P_USERID)
);

--CODE HAS BEEN REMOVED TO EXTRACT CONTROL RECORD ID FROM
--C_NEXTKEYID TABLE
--SINCE WHILE CREATING ENVIRONMENT CONTROL RECORD SHOULD BE
--INITIALISED TO ONE.
--BELOW SQL POPULATED C_CONTROLREC TABLE WITH CONTROLID SET TO '1'.
INSERT INTO controlrec (environid,name,
  firstmonth,lowvalue,highvalue,comments,
  createdtimestamp,createduserid,
  lastmodtimestamp,lastmoduserid)
VALUES (V_SRCNVT,P_CONTROLNAME,
  1,1,12,'Created automatically during initialization',
  CURRENT TIMESTAMP,P_USERID,
  CURRENT TIMESTAMP,P_USERID);

--- BELOW SQL INSERTS CREATED DUMMY LOGICAL AND PHYSICAL FILES WHICH
--- ARE REQUIRED BY THE VIEW
---- DURING MIGRATION. DUMMY METADATA OBJECTS ARE CREATED WITH ID AS 0.

INSERT INTO PHYFILE (environid,phyfileid,name,
   filetypecd,diskfiletypecd,accessmethodcd,
   readexitid,readexitstartup,
   ddnameinput,dsn,minreclen,maxreclen,
   ddnameoutput,recfm,lrecl,
   dbmssubsys,dbmssql,dbmstable,
   dbmsrowfmtcd,dbmsinclnullsind,comments,
   createdtimestamp,createduserid,
   lastmodtimestamp,lastmoduserid
   )
VALUES (V_SRCNVT,0,'NON_EXISTENT_PHYSICAL_FILE',
            'DISK',NULL,NULL,
            0,NULL,
            NULL,NULL,0,0,
            NULL,NULL,0,
            NULL,NULL,NULL,
            NULL,0,NULL,
            CURRENT TIMESTAMP,P_USERID,
            CURRENT TIMESTAMP,P_USERID ) ;

 INSERT INTO LOGFILE (environid,logfileid,name,
   comments,createdtimestamp,createduserid,
   lastmodtimestamp,lastmoduserid)
 VALUES (V_SRCNVT,0,'NON_EXISTENT_LOGICAL_FILE',
   NULL,CURRENT TIMESTAMP,P_USERID,
   CURRENT TIMESTAMP,P_USERID) ;

INSERT INTO LFPFASSOC (ENVIRONID,LFPFASSOCID,
    PHYFILEID,PARTSEQNBR,LOGFILEID,
    CREATEDTIMESTAMP,CREATEDUSERID,
    LASTMODTIMESTAMP,LASTMODUSERID)
VALUES (V_SRCNVT,0,
        0,0,0,
        CURRENT TIMESTAMP, P_USERID,
        CURRENT TIMESTAMP, P_USERID) ;

INSERT INTO VIEWFOLDER (ENVIRONID,
        VIEWFOLDERID,NAME,COMMENTS,CREATEDTIMESTAMP,
        CREATEDUSERID,LASTMODTIMESTAMP,LASTMODUSERID)
VALUES (V_SRCNVT,0,'ALL_VIEWS','Folder containing all views',
        CURRENT TIMESTAMP, 'ADMIN', CURRENT TIMESTAMP, 'ADMIN');
        
SET P_ENVTID=V_SRCNVT;
END