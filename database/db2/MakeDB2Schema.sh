#!/bin/bash
# Script to tailor JCL, DDL and SQL statements needed to define DB2 schema and copy to MVS pds(e)'s
# This script is intended to be run after cloning the Workbench repository to USS file system

main() {

echo "Preparing DB2 metadata artifacts for Workbench and exporting these to MVS PDS/E datasets";

#initialize crucial variable
export GERS_DB2_RUN_LIB="";
export GERS_FROM_PDS_HLQ="";
export GERS_INCLUDE_DB2_RUNLIB="";

#variables chosen by user
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
export GERS_FROM_PDS_MLQ=RTC23321.FROM;
export GERS_FROM_DB2_DBUSER=SAFRBLD;
export GERS_FROM_DB2_DBNAME=SADBNCB2;
export GERS_FROM_DB2_DBSG=SASGNCB2;
export GERS_FROM_DB2_DBSCH=SAFRNCB2;
export GERS_FROM_DB2_DBSUB=DM13;

# variables for running optional smoke test with DB2
export GERS_ENV_HLQ='GEBT.NEILE';
export GERS_DEMO_HLQ=GEBT;
export GERS_DEMO_MLQ=GVBDEMO;
export GERS_JVM_PROC_LIB='AJV.V11R0M0.PROCLIB';
export GERS_JZOS_LOAD_LIB='AJV.V11R0M0.SIEALNKE';
export GERS_DB2_HOST='sp13.pok.stglabs.ibm.com';
export GERS_DB2_PORT='5036';
export GERS_DB2_SAFR_ENV='1';
export GERS_JAVA_HOME="/Java/J17.0_64"
export GERS_RCA_JAR_DIR="/u/nbeesle/git/public/RCA_jar"

# mainline

if [[ -z "$GERS_FROM_PDS_HLQ" ]]; then
  echo "Preparing JCL to define the GenedvaERS schema only";
else
  echo "Preparing JCL to define the GenedvaERS schema and import data from DB2 export files";
fi

if [[ -z "$GERS_DB2_RUN_LIB" ]]; then
  echo "All references to DB2 RUN library in JCL will be suppressed";
  export GERS_INCLUDE_DB2_RUNLIB=N;
fi

GERS_TO_PDS=$GERS_TO_PDS_HLQ'.'$GERS_TO_PDS_MLQ;
echo "GERS_TO_PDS stem: $GERS_TO_PDS";

GERS_FROM_PDS=$GERS_FROM_PDS_HLQ'.'$GERS_FROM_PDS_MLQ;
echo "GERS_FROM_PDS stem: $GERS_FROM_PDS";

TO_PDSDDL="//'$GERS_TO_PDS.DDL'";
TO_PDSDDL="$TO_PDSDDL";
# echo "TO_PDSDDL: $TO_PDSDDL";

TO_PDSJCL="//'$GERS_TO_PDS.JCL'";
TO_PDSJCL="$TO_PDSJCL";
# echo "TO_PDSJCL: $TO_PDSJCL";

TO_PDSSQL="//'$GERS_TO_PDS.SQL'";
TO_PDSSQL="$TO_PDSSQL";
# echo "TO_PDSSQL: $TO_PDSSQL";

# echo "Obtaining DB2 metadata artifacts for Workbench and exporting to: $TO_PDSDDL  $TO_PDSJCL  $TO_PDSSQL"

./Copy2pds.sh . DDL $TO_PDSDDL 1;
./Copy2pds.sh . JCL $TO_PDSJCL 2;
./Copy2pds.sh StorProc SQL $TO_PDSSQL 0;

echo "DB2 metadata artifacts for Workbench prepared successfully and exported to: $TO_PDSDDL  $TO_PDSJCL  $TO_PDSSQL";

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"