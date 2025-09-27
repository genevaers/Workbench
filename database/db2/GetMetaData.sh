#!/bin/bash
# Script to directory contents to MVS pds(e)
# Invoked like: ./Copy2pds.sh . DDL "//'GEBT.RTC23321.DDL'"

main() {

# Variables chosen by user
export GERS_DBUSER=SAFRBLD
export GERS_DBNAME=SADBNEIL
export GERS_DBSG=SASGNEIL
export GERS_DBSCH=SAFRNEIL
export GERS_DBSUB=DM13
export GERS_TO_PDS=GEBT.RTC23321; ## "//'GEBT.RTC23321'";

TO_PDSDDL="//'$GERS_TO_PDS.DDL'";
TO_PDSDDL="$TO_PDSDDL";
echo "TO_PDSDDL: $TO_PDSDDL";

TO_PDSJCL="//'$GERS_TO_PDS.JCL'";
TO_PDSJCL="$TO_PDSJCL";
echo "TO_PDSJCL: $TO_PDSJCL";

echo "Preparing metadatafiles for Workbench and exporting to: $TO_PDSDDL"

# ./Copy2pds.sh . DDL "//'GEBT.RTC23321.DDL'";
./Copy2pds.sh . DDL $TO_PDSDDL 1;
./Copy2pds.sh . JCL $TO_PDSJCL.JCL 0;
# ./Copy2pds.sh StorProc/ SQL $TO_PDS.SQL 0;

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"