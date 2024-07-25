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
--  Insert rows into tables for the GENEVAERS metadata
--
-- Replace tags in this file with system specific values.
-- Full stop is part of the tag name.
--
-- e.g. using ISPF editor 'CHANGE ALL :schemaV SAFRTEST'
--      for PostgreSQL, :schemaV is a passed in variable when executed. 
--
-- 1) Replace user id with DBA access user id &$DBUSER.
-- 2) Replace occurrences of schema :schemaV
--
INSERT INTO :schemaV.ENVIRON (NAME,
                          COMMENTS,
                          CREATEDTIMESTAMP,CREATEDUSERID,
                          LASTMODTIMESTAMP,LASTMODUSERID)
VALUES ('Development',
        'Created automatically during install',
        CURRENT_TIMESTAMP,'INSTALL',
        CURRENT_TIMESTAMP,'INSTALL');

INSERT INTO :schemaV.VIEWFOLDER (ENVIRONID,
        VIEWFOLDERID,NAME,COMMENTS,CREATEDTIMESTAMP,
        CREATEDUSERID,LASTMODTIMESTAMP,LASTMODUSERID)
VALUES (1,0,'ALL_VIEWS', 'Folder containing all views',
        CURRENT_TIMESTAMP, 'INSTALL', CURRENT_TIMESTAMP, 'INSTALL');
        
INSERT INTO :schemaV.USER (
         USERID,PASSWORD,
         FIRSTNAME,MIDDLEINIT,LASTNAME,
         EMAIL,DEFENVIRONID,DEFFOLDERID,DEFGROUPID,
         SYSADMIN,MAXCOMPILEERRORS,
         COMMENTS,
         CREATEDTIMESTAMP,CREATEDUSERID,
         LASTMODTIMESTAMP,LASTMODUSERID)
VALUES (user,NULL,
         'The',NULL,'SAFR Administrator',
         NULL,NULL,1,NULL,
         1,NULL,
         'Created automatically during install',
         CURRENT_TIMESTAMP,'INSTALL',
         CURRENT_TIMESTAMP,'INSTALL');

INSERT INTO :schemaV.CONTROLREC (ENVIRONID,NAME,
                          FIRSTMONTH,LOWVALUE,HIGHVALUE,
                          COMMENTS,
                          CREATEDTIMESTAMP,CREATEDUSERID,
                          LASTMODTIMESTAMP,LASTMODUSERID)
VALUES (1,'SAFR_Installation',
        1,1,12,
        'Created automatically during install',
        CURRENT_TIMESTAMP,'INSTALL',
        CURRENT_TIMESTAMP,'INSTALL');

INSERT INTO :schemaV.GROUP (
                         NAME,
                         COMMENTS,
                         CREATEDTIMESTAMP,CREATEDUSERID,
                         LASTMODTIMESTAMP,LASTMODUSERID)
VALUES ('Default User Group',
        'Created automatically during install',
        CURRENT_TIMESTAMP,'INSTALL',
        CURRENT_TIMESTAMP,'INSTALL');        
