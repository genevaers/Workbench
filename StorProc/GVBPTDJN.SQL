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

CREATE PROCEDURE GETDEPSLP(IN P_ENVIRONID INT,
                                IN P_ENTITYID INT,
                                IN P_FUNCTCODE CHAR(1),
                                IN P_GROUPID INT,
                                IN P_DESTENVTID INT)

-- P_FUNCTCODE = '1': CALLING FROM MIGRATION RELATED COMPONENTS
-- P_FUNCTCODE = '2': CALLING FROM DEPENDENCY CHECKER
--This stored procedure will be called from the GUI which inturn
--will call respective generic SP for each GEneva component.

Language SQL
Result sets 1
DISABLE DEBUG MODE 

BEGIN

 DECLARE SQLSTATE CHAR(5) DEFAULT '00000';

 DECLARE V_SQLSTATE CHAR(5) DEFAULT '00000';
 DECLARE V_ENTITYTYPE VARCHAR(30);
 DECLARE V_CHILDTYPE CHAR(30);
 DECLARE V_ENTITYID INT;
 DECLARE V_ENTITYNAME VARCHAR(254);
 DECLARE V_ENVIRONID INT;
 DECLARE V_ENVIRON VARCHAR(50);
 DECLARE V_DEFAULTFLG SMALLINT;
 DECLARE V_DEPENDFLG CHAR(3);
 DECLARE V_SECRIGHTS INT;
 DECLARE LOC_CUR RESULT_SET_LOCATOR VARYING;

 DECLARE RET_CUR CURSOR WITH RETURN FOR
   SELECT * FROM SESSION.TMPDEPSJOIN
   ORDER BY UPPER(ENTITYTYPE),
            UPPER(CHILDTYPE),
            ENTITYID,ENVIRONID;

 DECLARE GLOBAL TEMPORARY TABLE
   SESSION.TMPDEPSJOIN
     (ENTITYTYPE VARCHAR(30),CHILDTYPE CHAR(30),
      ENTITYID INT,ENTITYNAME VARCHAR(254),
      ENVIRONID INT,ENVIRON VARCHAR(50),DEFAULTFLG SMALLINT,
      DEPENDFLG CHAR(3),SECRIGHTS INT)
   ON COMMIT DROP TABLE;

 CALL GENLPDEPS(P_ENVIRONID,
                     P_ENTITYID, P_FUNCTCODE,
                     P_GROUPID,P_DESTENVTID);

 ASSOCIATE RESULT SET LOCATORS(LOC_CUR)
 WITH PROCEDURE GENLPDEPS;
 ALLOCATE DEPJOIN_CUR CURSOR FOR RESULT SET LOC_CUR;

 WHILE (V_SQLSTATE ='00000') DO

   FETCH FROM DEPJOIN_CUR INTO V_ENTITYTYPE,V_CHILDTYPE,
                               V_ENTITYID,V_ENTITYNAME,
                               V_ENVIRONID,V_ENVIRON,
                               V_DEFAULTFLG,V_DEPENDFLG,
			       V_SECRIGHTS;

   SET V_SQLSTATE= SQLSTATE;

   IF V_SQLSTATE ='00000' THEN

     INSERT INTO SESSION.TMPDEPSJOIN
       VALUES(V_ENTITYTYPE,V_CHILDTYPE,V_ENTITYID,
              V_ENTITYNAME,V_ENVIRONID,V_ENVIRON,
              V_DEFAULTFLG,V_DEPENDFLG,V_SECRIGHTS);

   END IF;

 END WHILE;

 IF P_FUNCTCODE='1' THEN  ---FOR MIGRATION

   DELETE FROM SESSION.TMPDEPSJOIN
   WHERE ENVIRONID =0;

   UPDATE SESSION.TMPDEPSJOIN
     SET DEFAULTFLG = -1
   WHERE ENVIRONID = P_ENVIRONID ;

 END IF;

 IF P_FUNCTCODE='2' THEN  ---FOR DEPENDANCY CHECKER

--- ENVIRONMENT 0 IS FOR DISPLAY PURPOSES
   DELETE FROM SESSION.TMPDEPSJOIN
   WHERE ENVIRONID NOT IN (P_ENVIRONID,0);

 END IF;

 OPEN RET_CUR;

END