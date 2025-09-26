#!/bin/sh
# prepare_ddl.sh - Prepare data management statements for user preferences
########################################################

main() {

# &$DBUSER.   DATABASE USER
# &$DBNAME.   DATABASE NAME
# &$DBSG.     DATABASE STORAGE GROUP
# &$DBSCH.    DATABASE SCHEMA
# &$DBSUB.    DATABASE SUBSYSTEM

# Check if member name is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <name of file to prepare";
  echo "Example: $0 GVBQDRAL.DDL";
  exit 1;
fi

MEMBER="$1";
# member = "GVBQDRAL.DDL";

# never mind the symbolics
sed 's/&$DBUSER./${GERS_DBUSER}/g'   $MEMBER > prep/tmp1;
sed 's/&$DBNAME./${GERS_DBNAME}/g' prep/tmp1 > prep/tmp2;
sed 's/&$DBSG./${GERS_DBSG}/g'     prep/tmp2 > prep/tmp3;
sed 's/&$DBSUB./${GERS_DBSUB}/g'   prep/tmp3 > prep/tmp4;
sed 's/&$DBSCH./${GERS_DBSCH}/g'   prep/tmp4 > prep/$MEMBER;

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"