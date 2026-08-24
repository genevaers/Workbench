--
--
-- ******************************************************************
--
-- (C) COPYRIGHT IBM CORPORATION 2023.
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
--  Insert rows into CODE for the GENEVAERS metadata
--
-- Replace tags in this file with system specific values.
-- Full stop is part of the tag name.
--
-- e.g. using ISPF editor 'CHANGE ALL &$DBSCH. SAFRTEST'
--
-- 1) Replace user id with DBA access user id &$DBUSER.
-- 2) Replace occurrences of schema &$DBSCH.
--
SET CURRENT SQLID='&$DBUSER';

INSERT INTO &$DBSCH.ENVIRON (NAME,
                          COMMENTS,
                          CREATEDTIMESTAMP,CREATEDUSERID,
                          LASTMODTIMESTAMP,LASTMODUSERID)
VALUES ('DEVELOPMENT',
        'CREATED AUTOMATICALLY DURING INSTALL',
        CURRENT TIMESTAMP,'INSTALL',
        CURRENT TIMESTAMP,'INSTALL');
INSERT INTO &$DBSCH.VIEWFOLDER (ENVIRONID,
        VIEWFOLDERID,NAME,COMMENTS,CREATEDTIMESTAMP,
        CREATEDUSERID,LASTMODTIMESTAMP,LASTMODUSERID)
VALUES (1,0,'ALL_VIEWS', 'FOLDER CONTAINING ALL VIEWS',
        CURRENT TIMESTAMP, 'ADMIN', CURRENT TIMESTAMP, 'ADMIN');

INSERT INTO &$DBSCH.USER (
         USERID,PASSWORD,
         FIRSTNAME,MIDDLEINIT,LASTNAME,
         EMAIL,DEFENVIRONID,DEFFOLDERID,DEFGROUPID,
         SYSADMIN,MAXCOMPILEERRORS,
         COMMENTS,
         CREATEDTIMESTAMP,CREATEDUSERID,
         LASTMODTIMESTAMP,LASTMODUSERID)
VALUES ('ADMIN',NULL,
         'THE',NULL,'SAFR ADMINISTRATOR',
         NULL,NULL,1,NULL,
         1,NULL,
         'CREATED AUTOMATICALLY DURING INSTALL',
         CURRENT TIMESTAMP,'INSTALL',
         CURRENT TIMESTAMP,'INSTALL');
INSERT INTO &$DBSCH.CONTROLREC (ENVIRONID,NAME,
                          FIRSTMONTH,LOWVALUE,HIGHVALUE,
                          COMMENTS,
                          CREATEDTIMESTAMP,CREATEDUSERID,
                          LASTMODTIMESTAMP,LASTMODUSERID)
VALUES (1,'SAFR INSTALLATION',
        1,1,12,
        'CREATED AUTOMATICALLY DURING INSTALL',
        CURRENT TIMESTAMP,'INSTALL',
        CURRENT TIMESTAMP,'INSTALL');
INSERT INTO &$DBSCH.GROUP (
                         NAME,
                         COMMENTS,
                         CREATEDTIMESTAMP,CREATEDUSERID,
                         LASTMODTIMESTAMP,LASTMODUSERID)
VALUES ('DEFAULT USER GROUP',
        'CREATED AUTOMATICALLY DURING INSTALL',
        CURRENT TIMESTAMP,'INSTALL',
        CURRENT TIMESTAMP,'INSTALL');