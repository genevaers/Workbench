#!/bin/bash
# Script to directory contents to MVS pds(e)

main() {

# Variables chosen by user
export GERS_DB2_DBUSER=SAFRBLD;
export GERS_DB2_DBNAME=SADBNEIL;
export GERS_DB2_STGGRP=SASGNEIL;
export GERS_DB2_DBSCH=SAFRNEIL;
export GERS_DB2_SUBSYSTEM=DM13;
export GERS_DB2_PLAN=DSNTEP13;
export GERS_DB2_PROCLIB=DSN.V13R1M0.PROCLIB;
export GERS_DB2_RUN_LIB=DSN131.RUNLIB.LOAD;
export GERS_DB2_LOAD_LIB=DSN.V13R1M0.SDSNLOAD;
export GERS_DB2_EXIT_LIB=DSN.V13R1M0.SDSNEXIT;


export GERS_JOB_CLASS=A;
export GERS_JOB_MSG_CLASS=H;
export GERS_LE_RUN_LIB=CEE.SCEERUN;
export GERS_LE_RUN_LIB2=CEE.SCEERUN2;
export GERS_SCBCDLL=CBC.SCLBDLL;
export GERS_TO_PDS_HLQ=GEBT;
export GERS_TO_PDS_MLQ=RTC23321;

#optional variables when replicating GenevaERS metadata from another DB2 schema
export GERS_FROM_PDS_HLQ=GEBT;
export GERS_FROM_PDS_MLQ=RTC23321;
export GERS_FROM_DB2_DBUSER=SAFRBLD;
export GERS_FROM_DB2_DBNAME=SADBNEIL;
export GERS_FROM_DB2_DBSG=SASGNEIL;
export GERS_FROM_DB2_DBSCH=SAFRNEIL;
export GERS_FROM_DB2_DBSUB=DM13;

# mainline
GERS_TO_PDS=$GERS_TO_PDS_HLQ'.'$GERS_TO_PDS_MLQ;
echo "GERS_TO_PDS stem: $GERS_TO_PDS";

GERS_FROM_PDS=$GERS_FROM_PDS_HLQ'.'$GERS_FROM_PDS_MLQ;
echo "GERS_FROM_PDS stem: $GERS_FROM_PDS";

TO_PDSDDL="//'$GERS_TO_PDS.DDL'";
TO_PDSDDL="$TO_PDSDDL";
echo "TO_PDSDDL: $TO_PDSDDL";

TO_PDSJCL="//'$GERS_TO_PDS.JCL'";
TO_PDSJCL="$TO_PDSJCL";
echo "TO_PDSJCL: $TO_PDSJCL";

TO_PDSSQL="//'$GERS_TO_PDS.SQL'";
TO_PDSSQL="$TO_PDSSQL";
echo "TO_PDSSQL: $TO_PDSSQL";

echo "Obtaining DB2 metadata artifacts for Workbench and exporting to: $TO_PDSDDL  $TO_PDSJCL  $TO_PDSSQL"

./Copy2pds.sh . DDL $TO_PDSDDL 1;
./Copy2pds.sh . JCL $TO_PDSJCL 2;
./Copy2pds.sh StorProc SQL $TO_PDSSQL 0;

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"