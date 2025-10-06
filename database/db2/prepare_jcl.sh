#!/bin/sh
# prepare_ddl.sh - Prepare data management statements for user preferences
########################################################

main() {

# &$DBUSER.   DATABASE USER
# &$DBNAME.   DATABASE NAME
# &$DBSG.     DATABASE STORAGE GROUP
# &$DBSCH.    DATABASE SCHEMA
# &$DBSUB.    DATABASE SUBSYSTEM

# &$JBCLASS.  JOB CLASS   -- GERS_JOB_CLASS=A;
# &$MSGCLASS. MSG CLASS   -- GERS_JOB_MSG_CLASS=H;
# &$DB2PLIB.  DB2 PROCLIB -- GERS_DB2_PROCLIB=DSN.V13R1M0.PROCLIB;
# &$DB2RLIB.  DB2 RUNLIB  -- GERS_DB2_RUNLIB=DSN131.RUNLIB.LOAD;
# &$DB2PLAN.  DB2 PLAN    -- GERS_DB2_PLAN=DSNTEP13;
# &$PDSHLQ.   HLQ         --
# &$PDSMLQ.   MLQ         --

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

mycmdstr6='s/&$JBCLASS.'/${GERS_JOB_CLASS}/'g'
mycmdstr7='s/&$MSGCLASS.'/${GERS_JOB_MSG_CLASS}/'g'
mycmdstr8='s/&$DB2PLIB.'/${GERS_DB2_PROCLIB}/'g'
mycmdstr9='s/&$DB2RLIB.'/${GERS_DB2_RUNLIB}/'g'
mycmdstr10='s/&$DB2PLAN.'/${GERS_DB2_PLAN}/'g'
mycmdstr11='s/&$PDSHLQ.'/${GERS_TO_PDS_HLQ}/'g'
mycmdstr12='s/&$PDSMLQ.'/${GERS_TO_PDS_MLQ}/'g'

# perform substitutions which unfortunately still converts to ACII with -W filecodeset=IBM-1047 
sed $mycmdstr1   $MEMBER > prep/tmp1;
sed $mycmdstr2 prep/tmp1 > prep/tmp2;
sed $mycmdstr3 prep/tmp2 > prep/tmp3;
sed $mycmdstr4 prep/tmp3 > prep/tmp4;
sed $mycmdstr5 prep/tmp4 > prep/tmp5;
sed $mycmdstr6 prep/tmp5 > prep/tmp6;
sed $mycmdstr7 prep/tmp6 > prep/tmp7;
sed $mycmdstr8 prep/tmp7 > prep/tmp8;
sed $mycmdstr9 prep/tmp8 > prep/tmp9;
sed $mycmdstr10 prep/tmp9 > prep/tmp10;
sed $mycmdstr11 prep/tmp10 > prep/tmp11;
sed $mycmdstr12 prep/tmp11 > prep/tmp12;

#convert output back to EBCDIC again
iconv -f ISO8859-1 -t IBM-1047 prep/tmp12 > prep/$MEMBER;
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