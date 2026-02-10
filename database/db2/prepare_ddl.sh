#!/bin/sh
# prepare_ddl.sh - Prepare data definition statements for user preferences
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
mycmdstr1='s/&$DBUSER.'/${GERS_DB2_DBUSER}/'g';
mycmdstr2='s/&$DBNAME.'/${GERS_DB2_DBNAME}/'g';
mycmdstr3='s/&$DBSG.'/${GERS_DB2_STGGRP}/'g';
mycmdstr4='s/&$DBSCH.'/${GERS_DB2_DBSCH}/'g';
mycmdstr5='s/&$DBSUB.'/${GERS_DB2_SUBSYSTEM}/'g';

# perform substitutions which unfortunately still converts to ACII with -W filecodeset=IBM-1047 
sed $mycmdstr1   $MEMBER > prep/tmp1;
sed $mycmdstr2 prep/tmp1 > prep/tmp2;
sed $mycmdstr3 prep/tmp2 > prep/tmp3;
sed $mycmdstr4 prep/tmp3 > prep/tmp4;
sed $mycmdstr5 prep/tmp4 > prep/tmp5;

#convert output back to EBCDIC again
iconv -f ISO8859-1 -t IBM-1047 prep/tmp5 > prep/$MEMBER;
chtag -r prep/$MEMBER;

}

exitIfError() {

if [ $? != 0 ]
then
    echo "$(date) ${BASH_SOURCE##*/} *** Process terminated: see error message above";
    exit 1;
fi

}

main "$@"