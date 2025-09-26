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
mycmdstr1='s/&$DBUSER.'/$GERS_DBUSER/'g';
mycmdstr2='s/&$DBNAME.'/${GERS_DBNAME}/'g'
mycmdstr3='s/&$DBSG.'/${GERS_DBSG}/'g'
mycmdstr4='s/&$DBSUB.'/${GERS_DBSUB}/'g'
mycmdstr5='s/&$DBSCH.'/${GERS_DBSCH}/'g'

# perform substitutions
sed -W filecodeset=IBM-1047 $mycmdstr1   $MEMBER > prep/tmp1;
sed -W filecodeset=IBM-1047 $mycmdstr2 prep/tmp1 > prep/tmp2;
sed -W filecodeset=IBM-1047 $mycmdstr3 prep/tmp2 > prep/tmp3;
sed -W filecodeset=IBM-1047 $mycmdstr4 prep/tmp3 > prep/tmp4;
sed -W filecodeset=IBM-1047 $mycmdstr5 prep/tmp4 > prep/$MEMBER;

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"