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
CREATE PROCEDURE INSSECGRPRGHT(
    IN P_ENTTYPE VARCHAR(10),
    IN P_SECGROUP INT,
    IN P_USERID CHAR(8),
    IN P_DOC XML)

RESULT SETS 0
LANGUAGE SQL
DISABLE DEBUG MODE 

BEGIN

  FOR OP AS SELECT X.OPTYPE OPTYPE,X.ENVID ENVID,
    X.ENTID ENTID,X.RIGHTS
    FROM XMLTABLE ('$doc/Root/Operation' passing P_DOC as "doc" 
    COLUMNS
     "OPTYPE"            CHAR(2) PATH 'OPTYPE',
     "ENVID"             INT PATH 'ENVID', 
     "ENTID"             INT PATH 'ENTID',
     "RIGHTS"            INT PATH 'RIGHTS') AS X
  DO    
    CASE UPPER(P_ENTTYPE)

    WHEN 'LR' THEN

      CASE OP.OPTYPE

      WHEN 'DE' THEN

        DELETE FROM SECLOGREC
        WHERE ENVIRONID=OP.ENVID
        AND GROUPID=P_SECGROUP
        AND LOGRECID=OP.ENTID ;

      WHEN 'IN' THEN

        INSERT INTO SECLOGREC (
           ENVIRONID,
           GROUPID,
           LOGRECID,
           RIGHTS,
           CREATEDTIMESTAMP,
           CREATEDUSERID,
           LASTMODTIMESTAMP,
           LASTMODUSERID )
        VALUES (
           OP.ENVID,
           P_SECGROUP,
           OP.ENTID,
           OP.RIGHTS,
           CURRENT TIMESTAMP,
           P_USERID,
           CURRENT TIMESTAMP,
           P_USERID);
           
        WHEN 'UP' THEN

        UPDATE SECLOGREC SET
          RIGHTS = OP.RIGHTS,
          LASTMODUSERID = P_USERID,
          LASTMODTIMESTAMP = CURRENT TIMESTAMP
        WHERE GROUPID = P_SECGROUP
          AND LOGRECID = OP.ENTID
          AND ENVIRONID = OP.ENVID;

        END CASE ;

    WHEN 'LF' THEN

      CASE OP.OPTYPE

      WHEN 'DE' THEN

        DELETE FROM SECLOGFILE
        WHERE  ENVIRONID = OP.ENVID
        AND GROUPID = P_SECGROUP
        AND LOGFILEID = OP.ENTID ;

      WHEN 'IN' THEN
      
        INSERT INTO SECLOGFILE (
          ENVIRONID,
          GROUPID,
          LOGFILEID,
          RIGHTS,
          CREATEDTIMESTAMP,
          CREATEDUSERID,
          LASTMODTIMESTAMP,
          LASTMODUSERID )
        VALUES (
          OP.ENVID,
          P_SECGROUP,
          OP.ENTID,
          OP.RIGHTS,
          CURRENT TIMESTAMP,
          P_USERID,
          CURRENT TIMESTAMP,
          P_USERID);
            
      WHEN 'UP' THEN
    
        UPDATE SECLOGFILE SET
          RIGHTS = OP.RIGHTS,
          LASTMODUSERID = P_USERID,
          LASTMODTIMESTAMP = CURRENT TIMESTAMP
        WHERE GROUPID = P_SECGROUP
          AND LOGFILEID = OP.ENTID
          AND ENVIRONID  = OP.ENVID;

      END CASE;

    WHEN 'PF' THEN

      CASE OP.OPTYPE

      WHEN 'DE' THEN

        DELETE FROM SECPHYFILE
        WHERE  ENVIRONID = OP.ENVID
        AND GROUPID = P_SECGROUP
        AND PHYFILEID = OP.ENTID ;

      WHEN 'IN' THEN
        INSERT INTO SECPHYFILE (
          ENVIRONID,
          GROUPID,
          PHYFILEID,
          RIGHTS,
          CREATEDTIMESTAMP,
          CREATEDUSERID,
          LASTMODTIMESTAMP,
          LASTMODUSERID )
        VALUES (
          OP.ENVID,
          P_SECGROUP,
          OP.ENTID,
          OP.RIGHTS,
          CURRENT TIMESTAMP,
          P_USERID,
          CURRENT TIMESTAMP,
          P_USERID  ) ;
                    
      WHEN 'UP' THEN
      
        UPDATE SECPHYFILE SET
          RIGHTS = OP.RIGHTS,
          LASTMODUSERID = P_USERID,
          LASTMODTIMESTAMP = CURRENT TIMESTAMP
        WHERE GROUPID = P_SECGROUP
          AND PHYFILEID = OP.ENTID
          AND ENVIRONID = OP.ENVID;
          
      END CASE;

    WHEN 'EXIT' THEN

      CASE OP.OPTYPE

      WHEN 'DE' THEN

         DELETE FROM SECEXIT
         WHERE  ENVIRONID = OP.ENVID
         AND GROUPID = P_SECGROUP
         AND EXITID  = OP.ENTID ;

      WHEN 'IN' THEN

        INSERT INTO SECEXIT (
          ENVIRONID,
          GROUPID,
          EXITID,
          RIGHTS,
          CREATEDTIMESTAMP,
          CREATEDUSERID,
          LASTMODTIMESTAMP,
          LASTMODUSERID )
        VALUES (
          OP.ENVID,
          P_SECGROUP,
          OP.ENTID,
          OP.RIGHTS,
          CURRENT TIMESTAMP,
          P_USERID,
          CURRENT TIMESTAMP,
          P_USERID);
                                
      WHEN 'UP' THEN
      
        UPDATE SECEXIT SET
          RIGHTS = OP.RIGHTS,
          LASTMODUSERID = P_USERID,
          LASTMODTIMESTAMP = CURRENT TIMESTAMP
        WHERE GROUPID = P_SECGROUP
          AND EXITID = OP.ENTID
          AND ENVIRONID  = OP.ENVID;

      END CASE;

    WHEN 'LP' THEN

      CASE OP.OPTYPE

      WHEN 'DE' THEN

        DELETE FROM SECLOOKUP
        WHERE ENVIRONID = OP.ENVID
        AND GROUPID = P_SECGROUP
        AND LOOKUPID = OP.ENTID ;

      WHEN 'IN' THEN

        INSERT INTO SECLOOKUP (
          ENVIRONID,
          GROUPID,
          LOOKUPID,
          RIGHTS,
          CREATEDTIMESTAMP,
          CREATEDUSERID,
          LASTMODTIMESTAMP,
          LASTMODUSERID )
        VALUES (
          OP.ENVID,
          P_SECGROUP,
          OP.ENTID,
          OP.RIGHTS,
          CURRENT TIMESTAMP,
          P_USERID,
          CURRENT TIMESTAMP,
          P_USERID  );
          
      WHEN 'UP' THEN

        UPDATE SECLOOKUP SET
          RIGHTS = OP.RIGHTS,
          LASTMODUSERID = P_USERID,
          LASTMODTIMESTAMP = CURRENT TIMESTAMP
        WHERE GROUPID = P_SECGROUP
          AND LOOKUPID = OP.ENTID
          AND ENVIRONID = OP.ENVID  ;

        END CASE ;

    WHEN 'VIEW' THEN

      CASE OP.OPTYPE
 
      WHEN 'DE' THEN

        DELETE FROM SECVIEW
        WHERE ENVIRONID = OP.ENVID
        AND GROUPID = P_SECGROUP
        AND VIEWID = OP.ENTID ;

      WHEN 'IN' THEN

        INSERT INTO SECVIEW (
          ENVIRONID,
          GROUPID,
          VIEWID,
          RIGHTS,
          CREATEDTIMESTAMP,
          CREATEDUSERID,
          LASTMODTIMESTAMP,
          LASTMODUSERID)
        VALUES (OP.ENVID,
          P_SECGROUP,
          OP.ENTID,
          OP.RIGHTS,
          CURRENT TIMESTAMP,
          P_USERID,
          CURRENT TIMESTAMP,
          P_USERID);
               
      WHEN 'UP' THEN

        UPDATE SECVIEW SET
          RIGHTS = OP.RIGHTS,
          LASTMODUSERID = P_USERID,
          LASTMODTIMESTAMP = CURRENT TIMESTAMP
        WHERE GROUPID = P_SECGROUP
          AND VIEWID = OP.ENTID
          AND ENVIRONID = OP.ENVID;

      END CASE;

    WHEN 'VF' THEN

      CASE OP.OPTYPE
      
      WHEN 'DE' THEN

        DELETE FROM SECVIEWFOLDER
        WHERE ENVIRONID = OP.ENVID
        AND GROUPID = P_SECGROUP
        AND VIEWFOLDERID = OP.ENTID;

      WHEN 'IN' THEN

        INSERT INTO SECVIEWFOLDER (
          ENVIRONID,
          GROUPID,
          VIEWFOLDERID,
          RIGHTS,
          CREATEDTIMESTAMP,
          CREATEDUSERID,
          LASTMODTIMESTAMP,
          LASTMODUSERID )
        VALUES (
          OP.ENVID,
          P_SECGROUP,
          OP.ENTID,
          OP.RIGHTS,
          CURRENT TIMESTAMP,
          P_USERID,
          CURRENT TIMESTAMP,
          P_USERID  );
          
      WHEN 'UP' THEN

        UPDATE SECVIEWFOLDER SET
          RIGHTS = OP.RIGHTS,
          LASTMODUSERID = P_USERID,
          LASTMODTIMESTAMP = CURRENT TIMESTAMP
        WHERE GROUPID = P_SECGROUP
          AND VIEWFOLDERID = OP.ENTID
          AND ENVIRONID  = OP.ENVID;

      END CASE;
    END CASE;
  END FOR;

END