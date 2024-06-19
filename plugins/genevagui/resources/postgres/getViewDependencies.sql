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
CREATE OR REPLACE FUNCTION :schemaV.getViewDependencies (IN P_ENVIRONID INT,
                                 IN P_ENTITYID INT,
                                 IN P_GROUPID INT) RETURNS refcursor
AS $$

-- THIS SP EXTRACTS ALL DEPENDANT AND RELATED COMPONENTS OF VIEW.

-- P_FUNCTCODE = '1': CALLING FROM EXPORT
--     EXTRACT EVERYTHING EXCEPT 'Fields'
-- P_FUNCTCODE = '2': CALLING FROM DEPENDENCY CHECKER
--     EXTRACT ALL DEPS, DIRECT AND INDIRECT
-- P_FUNCTCODE = '3': CALLING FROM DEPENDENCY CHECKER
--     EXTRACT DIRECT DEPS ONLY
  DECLARE DEPVW_CUR CURSOR 
   FOR SELECT  DISTINCT
       ENTITYTYPE,
       CHILDTYPE,
       ENTITYID,
       ENTITYNAME,
       ENVIRONID,
       ENVIRON,
       DEFAULTFLG,
       DEPENDFLG
 FROM TMPDEPVW;

BEGIN


 CREATE TEMP TABLE TMPDEPVW
    (ENTITYTYPE VARCHAR(30),
     CHILDTYPE CHAR(30),
     ENTITYID INT,
     ENTITYNAME VARCHAR(254),
     ENVIRONID INT,
     ENVIRON VARCHAR(50),
     DEFAULTFLG SMALLINT,
     DEPENDFLG CHAR(3))
 ON COMMIT DROP ;

CREATE TEMP TABLE TMPSEC
	(ENTITYTYPE VARCHAR(30),
     ENVIRONID INT,
     ENTITYID INT,
     SECRIGHTS INT)
 ON COMMIT DROP;

 CREATE TEMP TABLE TMPDEPRT
      (ENTITYTYPE VARCHAR(30),
       CHILDTYPE char(30),
       ENTITYID INT,
       ENTITYNAME VARCHAR(254),
       ENVIRONID INT,
       ENVIRON VARCHAR(50),
       DEFAULTFLG SMALLINT,
       DEPENDFLG CHAR(3),
       SECRIGHTS INT)
 ON COMMIT DROP;

 
-- ------------------------------------------------
-- Sql in this first section is common to Export and DP checker
-- ------------------------------------------------

-- SQL TO EXTRACT VIEW
  INSERT INTO TMPDEPVW
   VALUES ('Views' ,'None', 0, ' ',0,' ',0,'N');

  INSERT INTO TMPDEPVW
   SELECT 'Views','None',B.VIEWID,B.NAME,
   A.ENVIRONID,A.NAME,0,'N'
   FROM ENVIRON A
   INNER JOIN VIEW B
    ON A.ENVIRONID=B.ENVIRONID
   WHERE B.VIEWID=P_ENTITYID
   AND B.ENVIRONID=B.ENVIRONID;

-- SQL TO EXTRACT LOOKUPS USED IN VIEW COL SRC

  INSERT INTO TMPDEPVW
    VALUES ('Joins' ,'Source', 0, ' ',0,' ',0,'N') ;

  INSERT INTO TMPDEPVW
   SELECT 'Joins','Source',B.LOOKUPID,B.NAME,
   A.ENVIRONID,A.NAME,0,'N'
   FROM ENVIRON A
   INNER JOIN LOOKUP B
    ON A.ENVIRONID=B.ENVIRONID
   WHERE B.LOOKUPID IN (SELECT DISTINCT C.LOOKUPID
                      FROM VIEWCOLUMNSOURCE C
                      WHERE C.ENVIRONID = B.ENVIRONID
                      AND C.ENVIRONID=P_ENVIRONID
                      AND C.VIEWID=P_ENTITYID
                      AND C.LOOKUPID > 0);


-- SQL TO EXTRACT LOOKUPS USED IN LOGIC TEXT
-- FOR PARENTTYPECD = 1 AND 2 (ERF and ECA)

 INSERT INTO TMPDEPVW
  VALUES ('Joins' ,'Logic Text', 0, ' ',0,' ',0,'N') ;

 INSERT INTO TMPDEPVW
  SELECT 'Joins','Logic Text',B.LOOKUPID,B.NAME,
  A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
            INNER JOIN LOOKUP B
                ON A.ENVIRONID=B.ENVIRONID
            WHERE B.LOOKUPID IN ( SELECT DISTINCT C.LOOKUPID
                                  FROM VIEWLOGICDEPEND C
                                    WHERE C.ENVIRONID = B.ENVIRONID
                                    AND C.ENVIRONID=P_ENVIRONID
                                    AND C.VIEWID=P_ENTITYID
                          AND (C.LOGICTYPECD=01 OR C.LOGICTYPECD=02 OR C.LOGICTYPECD=05)
                                    AND C.LOOKUPID > 0);

 INSERT INTO TMPDEPVW
  SELECT 'Joins','Logic Text',B.LOOKUPID,B.NAME,
  A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A
    INNER JOIN LOOKUP B
               ON A.ENVIRONID =B.ENVIRONID
               WHERE B.LOOKUPID
			   IN ( 
			   SELECT DISTINCT C.LOOKUPID
               FROM VIEWLOGICDEPEND C
               INNER JOIN VIEWCOLUMNSOURCE D
                 ON C.ENVIRONID=D.ENVIRONID
                 AND C.VIEWID=D.VIEWID
                 AND C.PARENTID=D.VIEWCOLUMNSOURCEID
                 AND D.SOURCETYPEID=04
               WHERE C.ENVIRONID=B.ENVIRONID
               AND C.ENVIRONID=P_ENVIRONID
               AND C.VIEWID=P_ENTITYID
               AND C.LOGICTYPECD=02
               AND C.LOOKUPID > 0);

-- SQL TO EXTRACT LOOKUPS ASSOCIATED WITH SORT KEY TITLE OF
-- VIEW=P_ENTITYID

 INSERT INTO TMPDEPVW
  VALUES('Joins','Sort Key Title',0,' ',0,' ',0,'N');

 INSERT INTO TMPDEPVW
  SELECT 'Joins','Sort Key Title',B.LOOKUPID,B.NAME,
  A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
            INNER JOIN LOOKUP B
                ON A.ENVIRONID=B.ENVIRONID
            WHERE B.LOOKUPID IN (SELECT DISTINCT C.SORTTITLELOOKUPID
                               FROM VIEWCOLUMNSOURCE C
                               WHERE C.ENVIRONID = B.ENVIRONID
                               AND C.ENVIRONID=P_ENVIRONID
                               AND C.VIEWID=P_ENTITYID
                               AND C.SORTTITLELOOKUPID > 0);


-- SQL TO EXTRACT VWSRC LRS ASSOCIATED WITH A VIEW

 INSERT INTO TMPDEPVW
  VALUES ('Logical Records' ,'Source', 0, ' ',0,' ',0,'N');

 INSERT INTO TMPDEPVW
  SELECT DISTINCT 'Logical Records','Source',B.LOGRECID,
  B.NAME,A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
            INNER JOIN LOGREC B
                ON A.ENVIRONID=B.ENVIRONID
            INNER JOIN LRLFASSOC C
                ON B.ENVIRONID=C.ENVIRONID
                AND B.LOGRECID=C.LOGRECID
            WHERE C.LRLFASSOCID IN (SELECT DISTINCT INLRLFASSOCID
                                   FROM VIEWSOURCE
                                   WHERE ENVIRONID = C.ENVIRONID
                                   AND ENVIRONID=P_ENVIRONID
                                   AND VIEWID=P_ENTITYID
                                   AND INLRLFASSOCID > 0);


-- SQL TO EXTRACT EFFDATE FLD LRS

 INSERT INTO TMPDEPVW
  SELECT DISTINCT 'Logical Records','Source',B.LOGRECID,
  B.NAME,A.ENVIRONID,A.NAME,0,'N'
        FROM ENVIRON A
            INNER JOIN LOGREC B
                ON A.ENVIRONID=B.ENVIRONID
            WHERE B.LOGRECID IN ( SELECT DISTINCT C.LOGRECID
                              FROM LRFIELD C
                              INNER JOIN VIEWCOLUMNSOURCE D
                                ON  C.ENVIRONID= D.ENVIRONID
                                AND C.LRFIELDID=D.EFFDATELRFIELDID
                              WHERE D.ENVIRONID=B.ENVIRONID
                              AND D.ENVIRONID=P_ENVIRONID
                              AND D.VIEWID=P_ENTITYID
                              AND D.EFFDATELRFIELDID > 0);


-- SQL TO EXTRACT VWCOLSRC FLD LRS

 INSERT INTO TMPDEPVW
SELECT DISTINCT 
    'Logical Records',
    'Source',
    B.LOGRECID,
    B.NAME,
    A.ENVIRONID,
    A.NAME,
    0,
    'N'
FROM ENVIRON A,
     LOGREC B,
     LRFIELD C,
     VIEWCOLUMNSOURCE D
WHERE A.ENVIRONID=B.ENVIRONID
AND B.ENVIRONID=C.ENVIRONID
AND B.LOGRECID=C.LOGRECID 
AND C.ENVIRONID=D.ENVIRONID
AND C.LRFIELDID=D.LRFIELDID
AND D.ENVIRONID=P_ENVIRONID
AND D.VIEWID=P_ENTITYID
AND D.LRFIELDID>0;


-- SQL TO EXTRACT VWSRC LFS (SOURCE LR/LF)

 INSERT INTO TMPDEPVW
  VALUES ( 'Logical Files' ,'Source', 0,'  ',0,' ',0,'N') ;

 INSERT INTO TMPDEPVW
  SELECT 'Logical Files','Source',B.LOGFILEID,B.NAME,
  A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
                INNER JOIN LOGFILE B
                    ON A.ENVIRONID=B.ENVIRONID
                WHERE B.LOGFILEID IN (SELECT DISTINCT C.LOGFILEID
                                FROM LRLFASSOC C
                                INNER JOIN VIEWSOURCE D
                                    ON C.ENVIRONID=D.ENVIRONID
                                    AND C.LRLFASSOCID=D.INLRLFASSOCID
                                WHERE D.ENVIRONID = B.ENVIRONID
                                AND D.ENVIRONID=P_ENVIRONID
                                AND D.VIEWID=P_ENTITYID
                                AND D.INLRLFASSOCID > 0);


-- SQL TO EXTRACT OUTPUT LFS (OUTPUT LF/PF)

 INSERT INTO TMPDEPVW
  VALUES ( 'Logical Files' ,'Output', 0,'  ',0,' ',0,'N') ;

 INSERT INTO TMPDEPVW
   SELECT 'Logical Files','Output',B.LOGFILEID,B.NAME,
   A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
                INNER JOIN LOGFILE B
                    ON A.ENVIRONID=B.ENVIRONID
                WHERE B.LOGFILEID
				IN ( SELECT DISTINCT C.LOGFILEID
                    FROM LFPFASSOC C
                    INNER JOIN VIEW D
                    ON C.ENVIRONID = D.ENVIRONID
                    AND C.LFPFASSOCID=D.LFPFASSOCID
                    WHERE D.ENVIRONID = B.ENVIRONID
                    AND D.ENVIRONID=P_ENVIRONID
                    AND D.VIEWID=P_ENTITYID
                    AND D.LFPFASSOCID>0);


-- SQL TO EXTRACT OUTPUT LFS USED IN IN LOGIC TEXT (ECA LF/PF)

 INSERT INTO TMPDEPVW
   VALUES ( 'Logical Files' ,'Logic Text', 0,'  ',0,' ',0,'N') ;

 INSERT INTO TMPDEPVW
  SELECT 'Logical Files','Logic Text',B.LOGFILEID,B.NAME,
  A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A
   INNER JOIN LOGFILE B
    ON A.ENVIRONID=B.ENVIRONID
   WHERE B.LOGFILEID IN ( SELECT DISTINCT C.LOGFILEID
                       FROM LFPFASSOC C
                       INNER JOIN VIEWLOGICDEPEND D
                       ON C.ENVIRONID=D.ENVIRONID
                       AND C.LFPFASSOCID=D.LFPFASSOCID
                       WHERE D.ENVIRONID=B.ENVIRONID
                       AND D.ENVIRONID=P_ENVIRONID
                       AND D.VIEWID=P_ENTITYID
                       AND D.LFPFASSOCID > 0);


-- SQL TO EXTRACT OUTPUT PFS (OUTPUT LF/PF)

 INSERT INTO TMPDEPVW
  VALUES ( 'Physical Files' ,'Output', 0,'  ',0,' ',0,'N') ;

 INSERT INTO TMPDEPVW
  SELECT 'Physical Files','Output',B.PHYFILEID,B.NAME,
  A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A
  INNER JOIN PHYFILE B
   ON A.ENVIRONID=B.ENVIRONID                                                 
  WHERE B.PHYFILEID IN (SELECT DISTINCT C.PHYFILEID
                          FROM LFPFASSOC C
                          WHERE C.ENVIRONID=B.ENVIRONID
                          AND C.LFPFASSOCID
                            IN (SELECT LFPFASSOCID FROM VIEW D
                                WHERE D.ENVIRONID=C.ENVIRONID 
                                AND D.ENVIRONID=P_ENVIRONID
                                AND D.VIEWID=P_ENTITYID
                                AND D.LFPFASSOCID>0));


 -- SQL TO EXTRACT PFS USED IN LOGIC TEXT (ECA LF/PF)

 INSERT INTO TMPDEPVW
   VALUES ( 'Physical Files' ,'Logic Text', 0,'  ',0,' ',0,'N') ;

 INSERT INTO TMPDEPVW
  SELECT 'Physical Files','Logic Text',B.PHYFILEID,B.NAME,
  A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A
   INNER JOIN PHYFILE B
    ON A.ENVIRONID=B.ENVIRONID
   WHERE B.PHYFILEID IN ( SELECT DISTINCT C.PHYFILEID
                       FROM LFPFASSOC C
                       INNER JOIN VIEWLOGICDEPEND D
                       ON C.ENVIRONID=D.ENVIRONID
                       AND C.LFPFASSOCID=D.LFPFASSOCID
                       WHERE D.ENVIRONID=B.ENVIRONID
                       AND D.ENVIRONID=P_ENVIRONID
                       AND D.VIEWID=P_ENTITYID
                       AND D.LFPFASSOCID > 0);



-- SQL TO EXTRACT CONTROL RECORDS ASSOCIATED WITH VIEW=P_ENTITYID
 INSERT INTO TMPDEPVW
  VALUES ('Control Records' ,'None', 0, ' ',0,' ',0,'N') ;

 INSERT INTO TMPDEPVW
  SELECT 'Control Records','None',B.CONTROLRECID,
  B.NAME,A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
            INNER JOIN CONTROLREC B
                ON A.ENVIRONID=B.ENVIRONID
            WHERE B.CONTROLRECID
			IN (SELECT DISTINCT C.CONTROLRECID
                FROM VIEW C
                WHERE C.ENVIRONID=B.ENVIRONID
                AND C.ENVIRONID=P_ENVIRONID
                AND C.VIEWID=P_ENTITYID
                AND C.CONTROLRECID > 0);


-- SQL TO EXTRACT FORMAT EXITS USED BY THE VIEW
-- (ie 'FORMAT EXIT' SPECIFIED IN FORMAT PHASE)

 INSERT INTO TMPDEPVW
  VALUES ('Procedures' ,'Format', 0, ' ',0,' ',0,'N') ;

 INSERT INTO TMPDEPVW
  SELECT DISTINCT 'Procedures','Format',B.EXITID,
  B.NAME,A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
                INNER JOIN EXIT B
                    ON A.ENVIRONID=B.ENVIRONID
                INNER JOIN VIEW C
                    ON B.ENVIRONID =C.ENVIRONID
                    AND B.EXITID=C.FORMATEXITID
                WHERE C.ENVIRONID=P_ENVIRONID
                AND C.VIEWID=P_ENTITYID
                AND B.EXITID > 0;


-- SQL TO EXTRACT WRITE EXITS USED BY THE VIEW
-- (ie 'OUTPUT EXIT' SPECIFIED IN EXTRACT PHASE)

 INSERT INTO TMPDEPVW
  VALUES ('Procedures' ,'Write', 0, ' ',0,' ',0,'N') ;

 INSERT INTO TMPDEPVW
  SELECT DISTINCT 'Procedures','Write',B.EXITID,
  B.NAME,A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
                INNER JOIN EXIT B
                    ON A.ENVIRONID=B.ENVIRONID
                INNER JOIN VIEW C
                    ON B.ENVIRONID=C.ENVIRONID
                    AND B.EXITID=C.WRITEEXITID
                WHERE C.ENVIRONID=P_ENVIRONID
                AND C.VIEWID=P_ENTITYID
                AND B.EXITID > 0;


-- SQL TO EXTRACT WRITE EXITS USED LOGIC TEXT
-- (ie WRITE EXITS USED IN EXTR COL ASSGMNT)

 INSERT INTO TMPDEPVW
  SELECT DISTINCT 'Procedures','Write',B.EXITID,
  B.NAME,A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
            INNER JOIN EXIT B
                ON A.ENVIRONID=B.ENVIRONID
            INNER JOIN VIEWLOGICDEPEND C
                ON B.ENVIRONID=C.ENVIRONID
                AND B.EXITID=C.EXITID
                WHERE C.ENVIRONID=P_ENVIRONID
                    AND C.VIEWID=P_ENTITYID
                    AND B.EXITID > 0;

-- Sql in this branch is for Export and 'All Dependencies' DP checker
-- but not for 'Direct Dependencies' DP checker

-- SQL TO EXTRACT LRS FROM TO JOINS USED IN VWCOLSRC OR SORT TITLES 
-- This is to extract LRs associated with join where join is
-- part of view.
-- LRs may not be directly related to the view but they are within
-- the 'all dependencies' scope and will be required for export.

  INSERT INTO  TMPDEPVW
   SELECT DISTINCT
    'Logical Records',
    'Source',
    B.LOGRECID,
    B.NAME,                   
    A.ENVIRONID,
    A.NAME,
    0,
    'N'                                      
  FROM ENVIRON A,                                           
     LOGREC B,
     LOOKUPSTEP C,
      TMPDEPVW D                                  
  WHERE A.ENVIRONID = B.ENVIRONID 
  AND B.ENVIRONID = C.ENVIRONID
  AND B.LOGRECID = C.SRCLRID 
  AND C.ENVIRONID = D.ENVIRONID
  AND C.LOOKUPID = D.ENTITYID                              
  AND D.ENVIRONID = P_ENVIRONID         
  AND UPPER(D.ENTITYTYPE)='JOINS'                     
  AND UPPER(D.CHILDTYPE) IN ('SOURCE','SORT KEY TITLE');    

 INSERT INTO  TMPDEPVW
  SELECT DISTINCT
    'Logical Records',
    'Source',
    B.LOGRECID,
    B.NAME,                 
    A.ENVIRONID,
    A.NAME,
    0,
    'N'                                    
  FROM ENVIRON A,                                            
     LOGREC B,                                         
     LRLFASSOC C,
     LOOKUPSTEP D,
      TMPDEPVW E                                    
  WHERE A.ENVIRONID=B.ENVIRONID
  AND B.ENVIRONID=C.ENVIRONID
  AND B.LOGRECID=C.LOGRECID
  AND C.ENVIRONID = D.ENVIRONID
  AND C.LRLFASSOCID = D.LRLFASSOCID
  AND D.ENVIRONID=E.ENVIRONID
  AND D.LOOKUPID=E.ENTITYID
  AND E.ENVIRONID=P_ENVIRONID
  AND UPPER(E.ENTITYTYPE)='JOINS'                 
  AND UPPER(E.CHILDTYPE) IN ('SOURCE','SORT KEY TITLE');                

-- SQL TO EXTRACT LRS ASSOCIATED WITH FIELDS
-- USED IN LOGIC TEXT FOR PARENTTYPECD = 1 and 2.
-- 1 is extr rec filter. 2 is extr col assgmnt.

 INSERT INTO  TMPDEPVW
  VALUES ('Logical Records' ,'Logic Text', 0, ' ',0,' ',0,'N') ;

 INSERT INTO  TMPDEPVW
  SELECT DISTINCT 
      'Logical Records',
      'Logic Text',
      B.LOGRECID,
      B.NAME,
      A.ENVIRONID,
      A.NAME,
      0,
      'N'
  FROM ENVIRON A,
       LOGREC B,
       LRFIELD C,
       VIEWLOGICDEPEND D
  WHERE A.ENVIRONID=B.ENVIRONID
  AND B.ENVIRONID=C.ENVIRONID
  AND B.LOGRECID=C.LOGRECID
  AND C.ENVIRONID=D.ENVIRONID
  AND C.LRFIELDID=D.LRFIELDID
  AND D.ENVIRONID=P_ENVIRONID
  AND (D.LOGICTYPECD=01 OR D.LOGICTYPECD=02 OR D.LOGICTYPECD=05)
  AND D.VIEWID=P_ENTITYID
  AND D.LRFIELDID > 0;

 INSERT INTO  TMPDEPVW
  SELECT DISTINCT 
      'Logical Records', 
      'Logic Text',
      B.LOGRECID,                          
      B.NAME,                            
      A.ENVIRONID,                     
      A.NAME,                       
      0,                               
      'N'                             
  FROM ENVIRON A,                   
     LOGREC B,                        
     LRFIELD C,                     
     VIEWLOGICDEPEND D,              
     VIEWCOLUMNSOURCE E               
  WHERE A.ENVIRONID = B.ENVIRONID        
  AND B.ENVIRONID = C.ENVIRONID         
  AND B.LOGRECID = C.LOGRECID                  
  AND C.ENVIRONID = D.ENVIRONID        
  AND C.LRFIELDID = D.LRFIELDID          
  AND D.ENVIRONID=P_ENVIRONID    
  AND D.VIEWID=P_ENTITYID            
  AND D.LOGICTYPECD=2               
  AND D.LRFIELDID > 0                   
  AND E.ENVIRONID=D.ENVIRONID        
  AND E.VIEWCOLUMNSOURCEID=D.PARENTID 
  AND E.SOURCETYPEID=4;   

-- EXTRACT SOURCE AND TARGET LRS ASSOCIATED
-- WITH LOOKUPS USED IN LOGIC TEXT

 INSERT INTO  TMPDEPVW
  SELECT DISTINCT 
    'Logical Records',
    'Logic Text',
    B.LOGRECID,
    B.NAME,
    A.ENVIRONID,
    A.NAME,
    0,
    'N'
  FROM ENVIRON A,
     LOGREC B,
     LOOKUPSTEP C,
      TMPDEPVW D
  WHERE A.ENVIRONID=B.ENVIRONID
  AND B.ENVIRONID=C.ENVIRONID
  AND B.LOGRECID=C.SRCLRID
  AND C.ENVIRONID=D.ENVIRONID
  AND C.LOOKUPID=D.ENTITYID
  AND D.ENVIRONID=P_ENVIRONID
  AND UPPER(D.ENTITYTYPE)='JOINS'
  AND UPPER(D.CHILDTYPE)='LOGIC TEXT';    

 INSERT INTO  TMPDEPVW
  SELECT DISTINCT 
    'Logical Records',
    'Logic Text',
    B.LOGRECID,
    B.NAME,
    A.ENVIRONID,
    A.NAME,
    0,
    'N'
  FROM ENVIRON A,
     LOGREC B,
     LRLFASSOC C,
     LOOKUPSTEP D,
      TMPDEPVW E
  WHERE A.ENVIRONID=B.ENVIRONID
  AND B.ENVIRONID=C.ENVIRONID
  AND B.LOGRECID=C.LOGRECID
  AND C.ENVIRONID=D.ENVIRONID
  AND C.LRLFASSOCID=D.LRLFASSOCID
  AND D.ENVIRONID=E.ENVIRONID
  AND D.LOOKUPID=E.ENTITYID
  AND E.ENVIRONID=P_ENVIRONID                 
  AND UPPER(ENTITYTYPE)='JOINS'
  AND UPPER(CHILDTYPE)='LOGIC TEXT';
 
-- SQL TO EXTRACT LFS RELATED TO SRC LRS IN TEMP TABLE
 
 INSERT INTO  TMPDEPVW
  SELECT DISTINCT 'Logical Files','Source',B.LOGFILEID,
      B.NAME,A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A
  INNER JOIN LOGFILE B
     ON A.ENVIRONID= B.ENVIRONID
  INNER JOIN LRLFASSOC C
     ON B.ENVIRONID =C.ENVIRONID
        AND B.LOGFILEID=C.LOGFILEID
  WHERE C.LOGRECID IN (SELECT ENTITYID FROM  TMPDEPVW
                    WHERE ENVIRONID=P_ENVIRONID
                    AND ENVIRONID=C.ENVIRONID
                    AND UPPER(ENTITYTYPE)='LOGICAL RECORDS'
                    AND UPPER(CHILDTYPE) ='SOURCE');
 

-- SQL TO EXTRACT LFS RELATED TO LOGIC TEXT LRS IN TEMP TABLE
    
 INSERT INTO  TMPDEPVW
  SELECT DISTINCT 'Logical Files','Logic Text',B.LOGFILEID,
      B.NAME,A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A
  INNER JOIN LOGFILE B
     ON A.ENVIRONID=B.ENVIRONID
  INNER JOIN LRLFASSOC C
     ON B.ENVIRONID=C.ENVIRONID
        AND B.LOGFILEID=C.LOGFILEID
   WHERE C.LOGRECID IN (SELECT ENTITYID FROM  TMPDEPVW
                    WHERE ENVIRONID=P_ENVIRONID
                    AND ENVIRONID=C.ENVIRONID
                    AND UPPER(ENTITYTYPE)='LOGICAL RECORDS'
                    AND UPPER(CHILDTYPE) ='LOGIC TEXT'); 


-- SQL TO EXTRACT LFS USED AS TARGET LRLF IN JOINS USED IN LOGIC TEXT
-- This was under Func 1 in orig WE SP. Not sure if its needed but
-- for now keep it under Funcs 1 and 2. JAK

 INSERT INTO  TMPDEPVW
  SELECT DISTINCT 
    'Logical Files',
    'Logic Text',
    B.LOGFILEID,
    B.NAME,
    A.ENVIRONID,
    A.NAME,
    0,
    'N'
  FROM ENVIRON A,
    LOGFILE B,
    LRLFASSOC C,
    LOOKUPSTEP D,
     TMPDEPVW E
  WHERE A.ENVIRONID= B.ENVIRONID
  AND B.ENVIRONID = C.ENVIRONID
  AND B.LOGFILEID=C.LOGFILEID
  AND C.ENVIRONID=D.ENVIRONID
  AND C.LRLFASSOCID=D.LRLFASSOCID
  AND D.ENVIRONID=P_ENVIRONID
  AND D.ENVIRONID=E.ENVIRONID
  AND D.LOOKUPID=E.ENTITYID
  AND UPPER(E.ENTITYTYPE)='JOINS'
  AND UPPER(E.CHILDTYPE) = 'LOGIC TEXT';

-- SQL TO EXTRACT PFS RELATED TO VIEW SOURCE LFS

 INSERT INTO  TMPDEPVW
  VALUES ( 'Physical Files' ,'Source', 0,'  ',0,' ',0,'N') ;

  INSERT INTO  TMPDEPVW
  SELECT DISTINCT
    'Physical Files',
    'Source',
    B.PHYFILEID,
    B.NAME,
    A.ENVIRONID,
    A.NAME,
    0,
    'N'
  FROM ENVIRON A,
     PHYFILE B,
     LFPFASSOC C,
     LRLFASSOC D,
     VIEWSOURCE E
  WHERE A.ENVIRONID=B.ENVIRONID
  AND B.ENVIRONID=C.ENVIRONID
  AND B.PHYFILEID=C.PHYFILEID
  AND C.ENVIRONID=D.ENVIRONID
  AND C.LOGFILEID=D.LOGFILEID
  AND D.ENVIRONID=E.ENVIRONID
  AND D.LRLFASSOCID=E.INLRLFASSOCID
  AND E.ENVIRONID=P_ENVIRONID
  AND E.VIEWID=P_ENTITYID
  AND E.INLRLFASSOCID > 0;   

-- SQL TO EXTRACT PFS RELATED TO SOURCE LFS IN TEMP TABLE
-- (ie VWSRC LFS, LFS RELATED TO VWSRC FLD LRS, EFFDAT LRS, JOIN LRS)

 INSERT INTO  TMPDEPVW
  SELECT 'Physical Files','Source',B.PHYFILEID,
  B.NAME,A.ENVIRONID,A.NAME,0,'N'								
            FROM ENVIRON A
                INNER JOIN PHYFILE B
                    ON A.ENVIRONID=B.ENVIRONID
                INNER JOIN LFPFASSOC C
                    ON B.ENVIRONID=C.ENVIRONID
                    AND B.PHYFILEID=C.PHYFILEID
                WHERE C.LOGFILEID
				IN ( SELECT DISTINCT ENTITYID
                     FROM  TMPDEPVW
                     WHERE ENVIRONID=P_ENVIRONID
                     AND ENVIRONID=C.ENVIRONID
                     AND UPPER(ENTITYTYPE)='LOGICAL FILES'
                     AND UPPER(CHILDTYPE)='SOURCE');                 


-- SQL TO SELECT PFS RELATED TO OUTPUT LFS IN TEMP TABLE
-- (probably redundant sql, but leave in for now)
 
 INSERT INTO  TMPDEPVW
  SELECT 'Physical Files','Output',B.PHYFILEID,B.NAME,
  A.ENVIRONID,A.NAME,0,'N'
  FROM ENVIRON A
    INNER JOIN PHYFILE B
      ON A.ENVIRONID=B.ENVIRONID
    INNER JOIN LFPFASSOC C
      ON B.ENVIRONID =C.ENVIRONID
      AND B.PHYFILEID=C.PHYFILEID
    WHERE C.LOGFILEID
	IN ( SELECT DISTINCT ENTITYID
         FROM  TMPDEPVW
         WHERE ENVIRONID=P_ENVIRONID
         AND ENVIRONID=C.ENVIRONID
         AND UPPER(ENTITYTYPE)='LOGICAL FILES'
         AND UPPER(CHILDTYPE)='OUTPUT');

-- SQL TO EXTRACT PFS RELATED TO LFS USED IN LOGIC TEXT
-- ie - LFs used in ECA, LFs related to LRs with fields in ERF/ECA 
-- or LFs related to joinstep src/trg LRs 

 INSERT INTO  TMPDEPVW
  SELECT 'Physical Files','Logic Text',B.PHYFILEID,
  B.NAME,A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
                INNER JOIN PHYFILE B
                    ON A.ENVIRONID=B.ENVIRONID
                INNER JOIN LFPFASSOC C
                    ON B.ENVIRONID =C.ENVIRONID
                    AND B.PHYFILEID=C.PHYFILEID
                WHERE C.LOGFILEID
				IN ( SELECT DISTINCT ENTITYID
                     FROM  TMPDEPVW
                     WHERE ENVIRONID=P_ENVIRONID
                     AND ENVIRONID=C.ENVIRONID
                     AND UPPER(ENTITYTYPE)='LOGICAL FILES'
                     AND UPPER(CHILDTYPE)='LOGIC TEXT');


-- SQL TO EXTRACT READ EXITS ON ANY PFS RELATED TO THE VIEW

 INSERT INTO  TMPDEPVW
  VALUES('Procedures','Read',0,' ',0,' ',0,'N');

 INSERT INTO  TMPDEPVW
 SELECT DISTINCT 'Procedures','Read',B.EXITID,
 B.NAME,A.ENVIRONID,A.NAME,0,'N'
            FROM ENVIRON A
            INNER JOIN EXIT B
                ON A.ENVIRONID=B.ENVIRONID
            INNER JOIN PHYFILE C
                ON B.ENVIRONID=C.ENVIRONID
                AND B.EXITID=C.READEXITID
            WHERE C.PHYFILEID
			IN (SELECT ENTITYID FROM  TMPDEPVW
            WHERE ENVIRONID=P_ENVIRONID
             AND ENVIRONID=C.ENVIRONID
             AND UPPER(ENTITYTYPE)=UPPER('Physical Files'))
            AND B.EXITID > 0;

-- SQL TO EXTRACT LOOKUP EXITS ON ANY LRS RELATED TO THE VIEW

 INSERT INTO  TMPDEPVW
   VALUES('Procedures','Lookup',0,' ',0,' ',0,'N');

 INSERT INTO  TMPDEPVW
   SELECT DISTINCT 'Procedures','Lookup',B.EXITID,
   B.NAME,A.ENVIRONID,A.NAME,0,'N'
   FROM ENVIRON A
    INNER JOIN EXIT B
      ON A.ENVIRONID=B.ENVIRONID
    INNER JOIN LOGREC C
      ON B.ENVIRONID =C.ENVIRONID
      AND B.EXITID=C.LOOKUPEXITID
    WHERE C.LOGRECID
	IN(SELECT ENTITYID FROM  TMPDEPVW
    WHERE ENVIRONID =P_ENVIRONID
    AND ENVIRONID = C.ENVIRONID
    AND UPPER(ENTITYTYPE)=UPPER('Logical Records'))
    AND B.EXITID > 0;

-- SQL in this branch is for Export only, not for DP checker

 IF P_GROUPID <> 0 THEN

--  EXECUTE SECURITY CHECKS ONLY WHEN USER IS NOT ADMINISTRATOR.
      INSERT INTO  TMPSEC
        SELECT 'View Folders',A.ENVIRONID,
                        A.VIEWFOLDERID,COALESCE(B.RIGHTS,-1)
        FROM VIEWFOLDER A
        LEFT JOIN SECVIEWFOLDER B
            ON A.VIEWFOLDERID=B.VIEWFOLDERID
            AND A.ENVIRONID=B.ENVIRONID
            AND B.RIGHTS > 0
            AND B.GROUPID = P_GROUPID
        WHERE A.ENVIRONID IN (P_DESTENVTID,P_ENVIRONID);
        
     INSERT INTO  TMPSEC
        SELECT 'Views',A.ENVIRONID,
                        A.VIEWID,COALESCE(B.RIGHTS,-1)
        FROM VIEW A
        LEFT JOIN SECVIEW B
            ON A.VIEWID=B.VIEWID
            AND A.ENVIRONID=B.ENVIRONID
            AND B.RIGHTS > 0
            AND B.GROUPID = P_GROUPID
        WHERE A.ENVIRONID IN (P_DESTENVTID,P_ENVIRONID);

     INSERT INTO  TMPSEC
        SELECT 'Joins',A.ENVIRONID,
                        A.LOOKUPID,COALESCE(B.RIGHTS,-1)
        FROM LOOKUP A
        LEFT JOIN SECLOOKUP B
            ON A.LOOKUPID=B.LOOKUPID
            AND A.ENVIRONID=B.ENVIRONID
            AND B.RIGHTS > 0
            AND B.GROUPID = P_GROUPID
        WHERE A.ENVIRONID IN (P_DESTENVTID,P_ENVIRONID);

     INSERT INTO  TMPSEC
        SELECT 'Logical Records',A.ENVIRONID,
                        A.LOGRECID,COALESCE(B.RIGHTS,-1)
        FROM LOGREC A
        LEFT JOIN SECLOGREC B
            ON A.LOGRECID=B.LOGRECID
            AND A.ENVIRONID=B.ENVIRONID
            AND B.RIGHTS > 0
            AND B.GROUPID = P_GROUPID
        WHERE A.ENVIRONID IN (P_DESTENVTID,P_ENVIRONID);

     INSERT INTO  TMPSEC
        SELECT 'Logical Files',A.ENVIRONID,
                        A.LOGFILEID,COALESCE(B.RIGHTS,-1)
        FROM LOGFILE A
        LEFT JOIN SECLOGFILE B
	        ON A.LOGFILEID=B.LOGFILEID
	        AND A.ENVIRONID=B.ENVIRONID
	        AND B.RIGHTS > 0
	        AND B.GROUPID=P_GROUPID
	       WHERE A.ENVIRONID IN (P_DESTENVTID,P_ENVIRONID);

     INSERT INTO  TMPSEC
        SELECT 'Physical Files',A.ENVIRONID,
                        A.PHYFILEID,COALESCE(B.RIGHTS,-1)
        FROM PHYFILE A
        LEFT JOIN SECPHYFILE B
            ON A.PHYFILEID=B.PHYFILEID
            AND A.ENVIRONID=B.ENVIRONID
            AND B.RIGHTS > 0
            AND B.GROUPID=P_GROUPID
        WHERE A.ENVIRONID IN (P_DESTENVTID,P_ENVIRONID);

     INSERT INTO  TMPSEC
      SELECT DISTINCT 'Procedures',A.ENVIRONID,
                          A.EXITID,COALESCE(B.RIGHTS,-1)
      FROM EXIT A
         LEFT JOIN SECEXIT B
            ON A.EXITID = B.EXITID
            AND A.ENVIRONID=B.ENVIRONID
            AND B.RIGHTS > 0
            AND B.GROUPID = P_GROUPID
        WHERE A.ENVIRONID IN (P_DESTENVTID,P_ENVIRONID);

END IF;

 UPDATE  TMPDEPVW
  SET CHILDTYPE ='NONE';

   DELETE FROM  TMPDEPVW
 WHERE ENTITYID= 0 AND ENVIRONID > 0;

 ---  CREATE TMPDEPRT WITH SECURITY RIGHTS

 INSERT INTO   TMPDEPRT
  SELECT   TMPDEPVW.ENTITYTYPE,
          TMPDEPVW.CHILDTYPE,
          TMPDEPVW.ENTITYID,
          TMPDEPVW.ENTITYNAME,
          TMPDEPVW.ENVIRONID,
          TMPDEPVW.ENVIRON,
          TMPDEPVW.DEFAULTFLG,
          TMPDEPVW.DEPENDFLG,
          TMPSEC.SECRIGHTS
  FROM   TMPDEPVW
  LEFT JOIN   TMPSEC
    ON   TMPDEPVW.ENTITYTYPE =   TMPSEC.ENTITYTYPE
    AND   TMPDEPVW.ENTITYID =   TMPSEC.ENTITYID;


 OPEN DEPVW_CUR ;
 RETURN DEPVW_CUR;

END;

	$$
	LANGUAGE plpgsql;