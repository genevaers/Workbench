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

CREATE OR REPLACE FUNCTION :schemaV.getMaxIds () RETURNS refcursor
AS $$
DECLARE M_CONTROLREC INT;
DECLARE M_ENVIRON INT;
DECLARE M_EXIT INT;
DECLARE M_LFPFASSOC INT;
DECLARE M_LOGFILE INT;
DECLARE M_LOGREC INT;
DECLARE M_LOOKUP INT;
DECLARE M_LOOKUPSRCKEY INT;
DECLARE M_LOOKUPSTEP INT;
DECLARE M_LRFIELD INT;
DECLARE M_LRINDEX INT;
DECLARE M_LRINDEXFLD INT;
DECLARE M_LRLFASSOC INT;
DECLARE M_PHYFILE INT;
DECLARE M_SECUSER INT;
DECLARE M_VFVASSOC INT;
DECLARE M_VIEW INT;
DECLARE M_VIEWCOLUMN INT;
DECLARE M_VIEWCOLUMNSOURCE INT;
DECLARE M_VIEWHEADERFOOTER INT;
DECLARE M_VIEWFOLDER INT;
DECLARE M_VIEWSORTKEY INT;
DECLARE M_VIEWSOURCE INT;

  DECLARE MAX_CUR CURSOR 
   FOR SELECT
        CONTROLREC,
        ENVIRON,
        EXIT,
        LFPFASSOC,
        LOGFILE,
        LOGREC,
        LOOKUP,
        LOOKUPSRCKEY,
        LOOKUPSTEP,
        LRFIELD,
        LRINDEX,
        LRINDEXFLD,
        LRLFASSOC,
        PHYFILE,
        SECUSER,
        VFVASSOC,
        VIEW,
        VIEWCOLUMN,
        VIEWCOLUMNSOURCE,
        VIEWHEADERFOOTER,
        VIEWFOLDER,
        VIEWSORTKEY,
        VIEWSOURCE
 FROM TMPNEXTID;

BEGIN

DROP TABLE IF EXISTS TMPNEXTID;
CREATE TEMP TABLE TMPNEXTID
    (   CONTROLREC INT,
        ENVIRON INT,
        EXIT INT,
        LFPFASSOC INT,
        LOGFILE INT,
        LOGREC INT,
        LOOKUP INT,
        LOOKUPSRCKEY INT,
        LOOKUPSTEP INT,
        LRFIELD INT,
        LRINDEX INT,
        LRINDEXFLD INT,
        LRLFASSOC INT,
        PHYFILE INT,
        SECUSER INT,
        VFVASSOC INT,
        VIEW INT,
        VIEWCOLUMN INT,
        VIEWCOLUMNSOURCE INT,
        VIEWHEADERFOOTER INT,
        VIEWFOLDER INT,
        VIEWSORTKEY INT,
        VIEWSOURCE INT)
 ON COMMIT DROP ;
 
 	select max(controlrecid) into M_CONTROLREC from controlrec;
 	select max(environid) into M_ENVIRON from environ;
 	select max(exitid) into M_EXIT from exit;
 	select max(lfpfassocid) into M_LFPFASSOC from lfpfassoc;
 	select max(logfileid) into M_LOGFILE from logfile;
 	select max(logrecid) into M_LOGREC from logrec;
 	select max(lookupid) into M_LOOKUP from lookup;
 	select max(lookupstepid) into M_LOOKUPSRCKEY from lookupsrckey;
 	select max(lookupstepid) into M_LOOKUPSTEP from lookupstep;
 	select max(lrfieldid) into M_LRFIELD from lrfield;
 	select max(lrindexid) into M_LRINDEX from lrindex;
 	select max(lrindexfldid) into M_LRINDEXFLD from lrindexfld;
 	select max(lrlfassocid) into M_LRLFASSOC from lrlfassoc;
 	select max(phyfileid) into M_PHYFILE from phyfile;
 	select max(groupid) into M_SECUSER from secuser;
 	select max(vfvassocid) into M_VFVASSOC from vfvassoc;
 	select max(viewid) into M_VIEW from view;
 	select max(viewcolumnid) into M_VIEWCOLUMN from viewcolumn;
 	select max(viewcolumnsourceid) into M_VIEWCOLUMNSOURCE from viewcolumnsource;
 	select max(headerfooterid) into M_VIEWHEADERFOOTER from viewheaderfooter;
 	select max(viewfolderid) into M_VIEWFOLDER from viewfolder;
 	select max(viewsortkeyid) into M_VIEWSORTKEY from viewsortkey;
 	select max(viewsourceid) into M_VIEWSOURCE  from viewsource;
	
 INSERT INTO TMPNEXTID 
    VALUES ( M_CONTROLREC,         
             M_ENVIRON,
             M_EXIT,
             M_LFPFASSOC,
             M_LOGFILE,
             M_LOGREC,
             M_LOOKUP,
             M_LOOKUPSRCKEY,
             M_LOOKUPSTEP,
             M_LRFIELD,
             M_LRINDEX,
             M_LRINDEXFLD,
             M_LRLFASSOC,
             M_PHYFILE,
             M_SECUSER,
             M_VFVASSOC,
             M_VIEW,
             M_VIEWCOLUMN,
             M_VIEWCOLUMNSOURCE,
             M_VIEWHEADERFOOTER,
             M_VIEWFOLDER,
             M_VIEWSORTKEY,
             M_VIEWSOURCE);

 OPEN MAX_CUR ;
 RETURN MAX_CUR;
END
$$
LANGUAGE plpgsql;