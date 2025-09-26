#!/bin/bash
# Script to directory contents to MVS pds(e)
# Invoked like: ./Copy2pds.sh . DDL "//'GEBT.RTC23321.DDL'"

main() {

# Check if a directory and pattern are provided
if [ -z "$1" ] || [ -z "$2" ] || [ -z "$3" ]; then
  echo "Usage: $0 <directory> <suffix> <pds>";
  echo "Example: $0 /u/usr1/sample  jcl  //'SMPL.JCL'";
  exit 1;
fi

FROM_DIR="$1";
FROM_SUF="$2";
TO_PDS="$3";

# Variables chosen by user
export GERS_DBUSER=SAFRBLD
export GERS_DBNAME=SADBNEIL
export GERS_DBSG=SASGNEIL
export GERS_DBSCH=SAFRNEIL
export GERS_DBSUB=DM13
export GERS_TO_PDS="//'GEBT.RTC23321.DDL'";

TO_PDS="$GERS_TO_PDS";

echo "Preparing metadatafiles for Workbench and exporting to: $TO_PDS"

# ./Copy2pds.sh . DDL "//'GEBT.RTC23321.DDL'";
./Copy2pds.sh . DDL $TO_PDS Y;

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"