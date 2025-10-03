#!/bin/bash
# Script to directory contents to MVS pds(e)
# Invoked like: ./Copy2pds.sh . DDL "//'GEBT.RTC23321.DDL'"

main() {

# Variables chosen by user
export GERS_DBUSER=SAFRBLD;
export GERS_DBNAME=SADBNEIL;
export GERS_DBSG=SASGNEIL;
export GERS_DBSCH=SAFRNEIL;
export GERS_DBSUB=DM13;
export GERS_TO_PDS_HLQ=GEBT;
export GERS_TO_PDS_MLQ=RTC23321;
export GERS_JOB_CLASS=A;
export GERS_JOB_MSG_CLASS=H;
export GERS_DB2_PROCLIB=DSN.V13R1M0.PROCLIB;
export GERS_DB2_PLAN=DSNTEP13;

GERS_TO_PDS=$GERS_TO_PDS_HLQ'.'$GERS_TO_PDS_MLQ;
echo "GERS_TO_PDS stem: $GERS_TO_PDS";

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

# ./Copy2pds.sh . DDL "//'GEBT.RTC23321.DDL'";
./Copy2pds.sh . DDL $TO_PDSDDL 1;
./Copy2pds.sh . JCL $TO_PDSJCL 0;
./Copy2pds.sh StorProc SQL $TO_PDSSQL 0;
./Copy2pds.sh StorProc JCL $TO_PDSJCL 0;

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"