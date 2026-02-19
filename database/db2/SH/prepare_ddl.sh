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
  echo "Usage: $0 <name of file to prepare> <name of from directory";
  echo "Example: $0 GVBQDRAL.DDL";
  exit 1;
fi

MEMBER="$1";
FROM_DIR="$2"

# member = "GVBQDRAL.DDL";

# never mind the symbolics
mycmdstr1='s/&$DBUSER.'/${GERS_DB2_DBUSER}/'g';
mycmdstr2='s/&$DBNAME.'/${GERS_DB2_DBNAME}/'g';
mycmdstr3='s/&$DBSG.'/${GERS_DB2_STGGRP}/'g';
mycmdstr4='s/&$DBSCH.'/${GERS_DB2_DBSCH}/'g';
mycmdstr5='s/&$DBSUB.'/${GERS_DB2_SUBSYSTEM}/'g';


echo "sed $mycmdstr1   ../$FROM_DIR/$MEMBER > ../"$FROM_DIR"/prep/tmp1;"

# perform substitutions which unfortunately still converts to ACII with -W filecodeset=IBM-1047 
sed $mycmdstr1 ../"$FROM_DIR"/"$MEMBER" > ../"$FROM_DIR"/prep/tmp1;
sed $mycmdstr2 ../"$FROM_DIR"/prep/tmp1 > ../"$FROM_DIR"/prep/tmp2;
sed $mycmdstr3 ../"$FROM_DIR"/prep/tmp2 > ../"$FROM_DIR"/prep/tmp3;
sed $mycmdstr4 ../"$FROM_DIR"/prep/tmp3 > ../"$FROM_DIR"/prep/tmp4;
sed $mycmdstr5 ../"$FROM_DIR"/prep/tmp4 > ../"$FROM_DIR"/prep/tmp5;

#convert output back to EBCDIC again
iconv -f ISO8859-1 -t IBM-1047 prep/tmp5 > ../"$FROM_DIR"/prep/$MEMBER;
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