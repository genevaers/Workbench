******************************************************************
--
-- (C) COPYRIGHT IBM CORPORATION 2011, 2023.
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
--
--  Insert rows into tables for the SAFR metadata
--
-- Replace tags in this file with system specific values.
-- Full stop is part of the tag name.
--
-- e.g. using ISPF editor 'CHANGE ALL &$DBSCH. SAFRTEST'
--
-- 1) Replace user id with DBA access user id &$DBUSER.
-- 2) Replace occurrences of schema &$DBSCH.
--
SET CURRENT SQLID='&$DBUSER.';

INSERT INTO &$DBSCH..C_ENVIRONTBL (environ,
                          sequencenum,editableind,
                          effdate,termdate,
                          comments,
                          createdtimestamp,createduserid,
                          lastmodtimestamp,lastmoduserid)
VALUES ('Development',1,1,
        CURRENT TIMESTAMP,NULL,
        'Created automatically during install',
        CURRENT TIMESTAMP,'INSTALL',
        CURRENT TIMESTAMP,'INSTALL');

INSERT INTO &$DBSCH..C_USERTBL (
         USERID,PASSWORD,DBUSERID,DBPASSWORD,
         DBIDIND,DBSCHEMAIND,DBSCHEMA,DBCONNSTR,
         FIRSTNAME,MIDDLEINIT,LASTNAME,
         EMAIL,DEFENVIRONID,DEFFOLDERID,DEFGROUPID,
         EFFDATE,TERMDATE,ADMININD,ADMINSECIND,
         SECFLAGS,EXTRACTMAXRECCNT,LOGIN1STTIMESTAMP,
         LOGINLASTTIMESTAMP,
         COMMENTS,
         ARCHIVEPATH,LOGFILEPATH,PWDCHNGFLG,EXPIRYDATE,
         CREATEDTIMESTAMP,CREATEDUSERID,
         LASTMODTIMESTAMP,LASTMODUSERID)
VALUES ('ADMIN',NULL,NULL,NULL,
         NULL,NULL,NULL,NULL,
         'The',NULL,'SAFR Administrator',
         NULL,NULL,1,NULL,
         CURRENT TIMESTAMP,NULL,1,1,
         1,NULL,NULL,
         NULL,
         'Created automatically during install',
         NULL,NULL,NULL,NULL,
         CURRENT TIMESTAMP,'INSTALL',
         CURRENT TIMESTAMP,'INSTALL');

INSERT INTO &$DBSCH..c_controlrec (environid,clientname,
                          effdate,termdate,
                          fiscalmonth,fiscalyear,
                          currentsysdatetime,
                          firstmonth,
                          lowvalue,highvalue,
                          maxpartseqnbr,
                          comments,
                          createdtimestamp,createduserid,
                          lastmodtimestamp,lastmoduserid)
VALUES (1,'SAFR Installation',
        CURRENT TIMESTAMP,NULL,
        NULL,NULL,
        NULL,
        1,
        1,12,
        4,
        'Created automatically during install',
        CURRENT TIMESTAMP,'INSTALL',
        CURRENT TIMESTAMP,'INSTALL');

INSERT INTO &$DBSCH..c_secgroups (
                         groupdesc,
                         comments,
                         createdtimestamp,createduserid,
                         lastmodtimestamp,lastmoduserid)
VALUES ('Default User Group',
        'Created automatically during install',
        CURRENT TIMESTAMP,'INSTALL',
        CURRENT TIMESTAMP,'INSTALL');

INSERT INTO &$DBSCH..E_PARTITIONTBL (environid,partitionid,
               partitionname,
               filetypecd,serverid,effdate,termdate,comments,
               createdtimestamp,createduserid,
               lastmodtimestamp,lastmoduserid
)
VALUES
( 1,0,'NON_EXISTENT_PHYSICAL_FILE',
'DISK',NULL,CURRENT TIMESTAMP,CURRENT TIMESTAMP,
 'This physical file is never used',
CURRENT TIMESTAMP,'ADMIN',CURRENT TIMESTAMP,'ADMIN' ) ;

INSERT INTO &$DBSCH..E_FILETBL (environid,fileid,filename,effdate,
        termdate,comments,createdtimestamp,createduserid,
        lastmodtimestamp,lastmoduserid)
VALUES (1,0,'NON_EXISTENT_LOGICAL_FILE',CURRENT TIMESTAMP,
        CURRENT TIMESTAMP, 'This logical file is never used',
        CURRENT TIMESTAMP,'ADMIN',CURRENT TIMESTAMP,'ADMIN') ;

INSERT INTO &$DBSCH..X_FILEPARTITIONTBL (ENVIRONID,XFILEPARTITIONID,
        CHILDPARTITIONID,PARTSEQNBR,PARENTFILEID,
        CHILDFILEID,CHILDTYPE,CREATEDTIMESTAMP,CREATEDUSERID,
        LASTMODTIMESTAMP,LASTMODUSERID)
VALUES (1,0,0,0,0,0,0,CURRENT TIMESTAMP, 'ADMIN',
        CURRENT TIMESTAMP, 'ADMIN') ;
        
INSERT INTO &$DBSCH..C_VIEWFOLDERS (ENVIRONID,
        VIEWFOLDERID,DESCRIPTION,CSFILEPATH1,CSFILEPATH2,
        CSFILEPATH3,CSFILEPATH4,COMMENTS,CREATEDTIMESTAMP,
        CREATEDUSERID,LASTMODTIMESTAMP,LASTMODUSERID)
VALUES (1,0,'ALL_VIEWS',NULL,NULL,NULL,NULL,
        'Folder containing all views',
        CURRENT TIMESTAMP, 'ADMIN', CURRENT TIMESTAMP, 'ADMIN')